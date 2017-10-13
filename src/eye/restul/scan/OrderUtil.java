package eye.restul.scan;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderUtil {

	static class Group<T> {
		List<GroupItem<T>> items = new ArrayList<GroupItem<T>>();
		Group<T>[] subs;
	}

	static class GroupItem<T> {
		String[] after;
		String[] before;
		T data;
		String name;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Annotation> List<String> order(Collection<String> classNames, Class<T> annotationClass)
			throws Exception {
		Order order = annotationClass.getAnnotation(Order.class);
		if (order == null) {
			throw new IllegalArgumentException("标注类[" + annotationClass.getName() + "]上不存在元标注" + Order.class.getName());
		}

		Map<String, Method> methods = new HashMap<String, Method>();
		for (String methodName : new String[] { "after", "before", "group", "index", "name" }) {
			try {
				Method method = annotationClass.getMethod(methodName, (Class<?>[]) null);
				methods.put(methodName, method);
			} catch (NoSuchMethodException ignore) {
			}
		}

		String[] groupNames = order.groups();

		Map<String, Group<String>> groups = new LinkedHashMap<String, Group<String>>();
		for (int i = 0; i < groupNames.length; i++) {
			Group<String> group = new Group<String>();
			group.subs = new Group[] { new Group<String>(), // before =
															// Order.OTHERS 组
					new Group<String>(), // 默认组
					new Group<String>() // after = Order.OTHERS 组
			};
			groups.put(groupNames[i], group);
		}

		for (String className : classNames) {
			Class<?> clazz = Class.forName(className);
			T annotation = clazz.getAnnotation(annotationClass);
			if (annotation == null) {
				GroupItem<String> item = new GroupItem<String>();
				item.data = className;
				groups.get(Order.DEFAULT).subs[1].items.add(item);
			} else {
				String group = getAnnotationValue(annotation, methods.get("group"), Order.DEFAULT);
				GroupItem<String> item = new GroupItem<String>();
				item.data = className;
				item.name = getAnnotationValue(annotation, methods.get("name"), null);
				item.before = getAnnotationValue(annotation, methods.get("before"), null);
				item.after = getAnnotationValue(annotation, methods.get("after"), null);

				int index = 1;
				if (item.before != null) {
					for (String target : item.before) {
						if (Order.OTHERS.equals(target)) {
							index = 0;
							break;
						}
					}
				}
				if (item.after != null) {
					for (String target : item.after) {
						if (Order.OTHERS.equals(target)) {
							if (index == 0) {
								throw new IllegalArgumentException("类[" + className + "]上的标注["
										+ annotationClass.getName() + "]同时定义了before=Order.OTHERS和after=Order.OTHERS");
							}
							index = 2;
							break;
						}
					}
				}

				groups.get(group).subs[index].items.add(item);
			}
		}

		List<String> result = new ArrayList<String>();
		for (Group<String> group : groups.values()) {
			order(result, group);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T> T getAnnotationValue(Annotation annotation, Method method, T defaultValue) throws Exception {
		if (method == null)
			return defaultValue;
		return (T) method.invoke(annotation, (Object[]) null);
	}

	private static <T> void order(List<T> result, Group<T> group) {
		if (group.subs != null) {
			for (Group<T> sub : group.subs) {
				order(result, sub);
			}
		} else {
			order(result, group.items);
		}
	}

	private static <T> void order(List<T> result, List<GroupItem<T>> items) {
		int length = items.size();

		if (length == 1) {
			result.add(items.get(0).data);
			return;
		}

		int[] V = new int[length];
		boolean[][] E = new boolean[length][length];

		Map<String, Integer> indexes = new HashMap<String, Integer>();
		for (int i = 0; i < length; i++) {
			GroupItem<T> item = items.get(i);
			if (item.name == null) {
				continue;
			}
			indexes.put(item.name, i);
		}

		for (int i = 0; i < length; i++) {
			GroupItem<T> item = items.get(i);
			if (item.name == null)
				continue;

			if (item.before != null) {
				for (String target : item.before) {
					if (Order.OTHERS.equals(target))
						continue;
					Integer j = indexes.get(target);
					if (j != null && E[i][j] == false) {
						E[i][j] = true;
						V[j]++;
					}
				}
			}

			if (item.after != null) {
				for (String target : item.after) {
					if (Order.OTHERS.equals(target))
						continue;
					Integer j = indexes.get(target);
					if (j != null && E[j][i] == false) {
						E[j][i] = true;
						V[i]++;
					}
				}
			}
		}

		boolean change, done;
		do {
			change = false;
			done = true;
			for (int i = 0; i < length; i++) {
				if (V[i] == 0) {
					change = true;
					for (int j = 0; j < length; j++) {
						if (E[i][j]) {
							E[i][j] = false;
							V[j]--;
						}
					}
					result.add(items.get(i).data);
					V[i] = -1;
				} else if (V[i] > 0) {
					done = false;
				}
			}

			if (!change && !done) {
				List<T> datas = new ArrayList<T>();
				for (int i = 0; i < length; i++) {
					if (V[i] > 0) {
						datas.add(items.get(i).data);
					}
				}
				throw new IllegalArgumentException("检测到循环，剩余未决节点[" + datas + "]");
			}
		} while (!done);
	}

}
