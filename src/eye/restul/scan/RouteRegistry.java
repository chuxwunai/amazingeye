package eye.restul.scan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * 路由注册表。提供路由注册和查找功能。此类是单例的。
 */
public class RouteRegistry {

	private static class InstanceHolder {
		private static RouteRegistry instance = new RouteRegistry();
	}

	private static final Logger logger = Logger.getLogger(RouteRegistry.class.getName());

	private final List<Route> routes = new ArrayList<Route>();

	private boolean routesSortEnabled;

	private List<Map<String, BitSet>> segmentBitsetMaps;

	private List<BitSet> segmentPatternBitsets;

	/**
	 * 添加一条路由。由于添加路由后，内部用于匹配的路由位组需要被重新计算，因此这个操作相对耗时。尽量使用该方法的批量版本：
	 * {@link #addAll(Collection)}
	 * 
	 * @param route
	 *            路由
	 */
	public synchronized void add(Route route) {
		logger.info(route.toString());
		routes.add(route);
		sortRoutes();
		initSegmentBitset();
	}

	/**
	 * 添加多条路由。添加路由后，内部用于匹配的路由位组会被重新计算
	 * 
	 * @param routes
	 *            路由列表
	 */
	public synchronized void addAll(Collection<Route> routes) {
		for (Route route : routes) {
			logger.info(route.toString());
			this.routes.add(route);
		}
		sortRoutes();
		initSegmentBitset();
	}

	/**
	 * 查找与请求方法和请求路径匹配的路由。在有多条路由匹配的情况下，返回最先匹配到的那条路由
	 * 
	 * @param method
	 *            请求方法，如 "GET", "POST"...
	 * @param path
	 *            请求路径，如 "aaa/bbb/ccc"。请求路径开头的 "/" 会被忽略
	 * @return 匹配的路由
	 */
	public Route find(String method, String path) {
		List<String> segmentSources = Route.convertToSegmentValues(method, path);
		int length = segmentSources.size();

		// 超出最大路由的分段数，返回null
		if (length > segmentBitsetMaps.size())
			return null;

		// 构建位组，初始状态为全true
		BitSet bitset = new BitSet(routes.size());
		bitset.set(0, bitset.size());

		for (int j = 0; j < length; j++) {
			Map<String, BitSet> segmentBitsetMap = segmentBitsetMaps.get(j);
			BitSet segmentBitset = segmentBitsetMap.get(segmentSources.get(j));
			if (segmentBitset == null) {
				segmentBitset = segmentPatternBitsets.get(j);
			}

			bitset.and(segmentBitset);
		}

		outer: for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
			Route route = routes.get(i);
			Segment[] segments = route.getSegments();

			if (length != segments.length)
				continue;

			for (int j = 0; j < length; j++) {
				Segment segment = segments[j];
				if (segment.getPattern() == null)
					continue;
				Matcher matcher = segment.getPattern().matcher(segmentSources.get(j));
				if (!matcher.matches()) {
					continue outer;
				}
			}

			return route;
		}

		return null;
	}

	/**
	 * 指定请求路径，根据已注册的路由列表，查找所有允许的请求方法
	 * 
	 * @param path
	 *            请求路径
	 * @return 允许的请求方法集合
	 */
	public Set<String> findMethods(String path) {
		Set<String> methods = new HashSet<String>();

		List<String> segmentSources = Route.convertToSegmentValues(null, path);
		int length = segmentSources.size();

		// 超出最大路由的分段数，返回null
		if (length > segmentBitsetMaps.size())
			return methods;

		// 构建位组，初始状态为全true
		BitSet bitset = new BitSet(routes.size());
		bitset.set(0, bitset.size());

		for (int j = 1 /* 从1开始，忽略请求方法 */; j < length; j++) {
			Map<String, BitSet> segmentBitsetMap = segmentBitsetMaps.get(j);
			BitSet segmentBitset = segmentBitsetMap.get(segmentSources.get(j));
			if (segmentBitset == null) {
				segmentBitset = segmentPatternBitsets.get(j);
			}

			bitset.and(segmentBitset);
		}

		outer: for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
			Route route = routes.get(i);
			Segment[] segments = route.getSegments();

			for (int j = 0; j < length; j++) {
				Segment segment = segments[j];
				if (segment.getPattern() == null)
					continue;
				Matcher matcher = segment.getPattern().matcher(segmentSources.get(j));
				if (!matcher.matches()) {
					continue outer;
				}
			}

			methods.add(segments[0].getSource());
		}

		return methods;
	}

	public List<Route> getAll() {
		return routes;
	}

	private void initSegmentBitset() {
		segmentBitsetMaps = new ArrayList<Map<String, BitSet>>();

		/**
		 * 分两次遍历来构建路径段到位组的映射信息。 第一次遍历处理所有的普通路径段。 第二次遍历处理所有的通配路径段。
		 * 
		 * 例如以下三条路由： 0: a/{x}/b 1: a/{y}/c 2: b/c/{z}
		 * 
		 * 第一次遍历后映射信息： [ { a: [0, 1], b: [2] }, { c: [2] }, { b: [0], c: [1] } ]
		 * 
		 * 第二次遍历后映射信息： [ { a: [0, 1], b: [2] }, { c: [0, 1, 2] }, { b: [0, 2],
		 * c: [1, 2] } ]
		 */
		// 第一次遍历
		for (int i = 0; i < routes.size(); i++) {
			Route route = routes.get(i);
			Segment[] segments = route.getSegments();

			while (segmentBitsetMaps.size() < segments.length) {
				segmentBitsetMaps.add(new HashMap<String, BitSet>());
			}

			for (int j = 0; j < segments.length; j++) {
				Map<String, BitSet> segmentBitsetMap = segmentBitsetMaps.get(j);
				Segment segment = segments[j];
				if (segment.getPattern() == null) {
					// 普通路径段
					BitSet bitset = segmentBitsetMap.get(segment.getSource());
					if (bitset == null) {
						bitset = new BitSet(routes.size());
						segmentBitsetMap.put(segment.getSource(), bitset);
					}
					bitset.set(i);
				}
			}
		}

		segmentPatternBitsets = new ArrayList<BitSet>();
		for (int i = 0; i < segmentBitsetMaps.size(); i++) {
			segmentPatternBitsets.add(new BitSet(routes.size()));
		}

		// 第二次遍历
		for (int i = 0; i < routes.size(); i++) {
			Route route = routes.get(i);
			Segment[] segments = route.getSegments();

			for (int j = 0; j < segments.length; j++) {
				Map<String, BitSet> segmentBitsetMap = segmentBitsetMaps.get(j);
				Segment segment = segments[j];
				if (segment.getPattern() != null) {
					// 通配路径段
					for (BitSet bitset : segmentBitsetMap.values()) {
						bitset.set(i);
					}

					segmentPatternBitsets.get(j).set(i);
				}
			}
		}
	}

	/**
	 * 排序路由列表，根据以下规则： 1. 分段数小的路由在先 2. 分段数相等时，从前到后比较各分段： a. 特定分段相较于通配分段在先 b.
	 * 都是特定分段时，字符串值较小者在先 c. 都是通配分段时，匹配模式较长者在先
	 */
	private void sortRoutes() {
		if (!routesSortEnabled)
			return;
		Collections.sort(routes, new Comparator<Route>() {
			public int compare(Route o1, Route o2) {
				Segment[] a1 = o1.getSegments();
				Segment[] a2 = o2.getSegments();

				if (a1.length != a2.length) {
					return a1.length - a2.length;
				}

				for (int i = 0; i < a1.length; i++) {
					Segment s1 = a1[i], s2 = a2[i];
					int c;
					if (s1.getPattern() == null) {
						if (s2.getPattern() == null) {
							c = s1.getSource().compareTo(s2.getSource());
						} else {
							c = -1;
						}
					} else {
						if (s2.getPattern() == null) {
							c = 1;
						} else {
							c = s2.getPattern().pattern().length() - s1.getPattern().pattern().length();
						}
					}
					if (c != 0)
						return c;
				}

				return 0;
			}
		});
	}

	/**
	 * 获得路由注册表的实例
	 * 
	 * @return 路由注册表
	 */
	public static RouteRegistry instance() {
		return InstanceHolder.instance;
	}

}
