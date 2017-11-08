(function($, _, W) {
	'use strict'
	
	function ajax(method, url, data)
	{		
		return new Promise(function(resolve, reject)
		{
			var xhr = new XMLHttpRequest()
						
			xhr.open(method, 'rest/' + url, true)
			
			xhr.onload = function()
			{
				var body = this.responseText
				
				if(this.status >= 400)
				{
					reject(body ? JSON.parse(body) : undefined)
				}
				else
				{
					resolve(body ? JSON.parse(body) : undefined, this)
				}
				return
			}
			xhr.onerror = function()
			{
				reject(this)
			}
			
			xhr.setRequestHeader("Accept", "application/json")
			xhr.setRequestHeader("Content-Type", "application/json")
			xhr.send(data ? JSON.stringify(data) : null)
		});
	}
	
	function getJSON(url)
	{
		return new Promise(function(resolve, reject)
		{
			var xhr = new XMLHttpRequest()

			xhr.open('GET', url)
			xhr.onreadystatechange = handler
			xhr.responseType = 'json'
			xhr.setRequestHeader('Accept', 'application/json')
			xhr.send()

			function handler()
			{
				if (this.readyState === this.DONE)
				{
					if (this.status === 200)
					{
						resolve(this.response);
					}
					else
					{
						reject(new Error('getJSON: `' + url + '` failed with status: [' + this.status + ']'))
					}
				}
            }
		})
	}
	
	function validate($form, handler)
	{
    	var
    	opts = {
    		rules: {},
			messages: {},
			submitHandler: function(element, event) {
				event.preventDefault()
				handler && handler(event, element)
			}
		},
		$fields = $form.find('[data-validate]')
		
		_.each($fields, function(field)
		{	
			var
			$field = $(field),
			name = _.uniqueId('field-'),
			validators = ($field.data('validate') || '').split(';')
			
			$field.attr('name', name)
			
			_.each(validators, function(validator)
			{	
				var rule, params, message

				if(typeof opts['rules'][name] == 'undefined')
				{
					opts['rules'][name] = {}
					opts['messages'][name] = {}
				}
				
				var vm = validator.match(/(\w+)(\[(.*?)\])?/i)
							
				if(vm && vm.length > 0)
				{	
					rule = vm[1]
					
					params = !vm[3] ? true : _.indexOf(vm[3], ',') == -1 ? vm[3] : vm[3].split(',')
				
					if(rule !== 'remote')
					{
						opts['rules'][name][rule] = params
					}
					else
					{
						opts['rules'][name][rule] = 'rest/' + params
					}
					
					message = $field.data(_.camelCase('message-' + rule.toLowerCase()))
					
					if(message)
					{
						opts['messages'][name][rule] = message
					}
				}
			})
		})
		
		$form.validate(opts)
	}
	
	function watch(obj, prop, handler)
	{
		var val = obj[prop]
		
		Object.__defineGetter__.call(obj, prop, function()
		{
			return val
		})
		
		Object.__defineSetter__.call(obj, prop, function(_val)
		{
			if(val === _val) return
			
			val = _val
			
			handler.call(obj, val)
		})
    	
    	if(_.isArray(val))
		{    		
    		_.each(['push', 'pop', 'shift', 'unshift', 'splice', 'sort', 'reverse'], function(method)
    		{
    			var _method = Array.prototype[method]
    			
    			val[method] = function()
    			{
    				_method.apply(this, Array.prototype.slice.apply(arguments))
					handler.call(obj, val)
    			}
    		})
		}
    	else if(_.isObject(val))
		{			
			_.forOwn(val, function(value, key)
			{
				watch(val, key, function()
				{
					handler.call(obj, val)
				})
			})
		}
	}
	
    function unwatch(obj, prop) {
		var val = obj[prop]
		   delete obj[prop]
		obj[prop] = val
    }
    
    function toJSON(str) {
    	return _.isString(str) && ((_.startsWith(str, '{') && _.endsWith(str, '}')) || (_.startsWith(str, '[') && _.endsWith(str, ']'))) ? 
    			(new Function('return ' + str))() : str
    }
	
	_.assign(W.__proto__, {
		ajax: ajax,
		post: function(url, data) { return this.ajax('POST', url, data) },
		get:  function(url      ) { return this.ajax('GET', url       ) },
		put:  function(url, data) { return this.ajax('PUT', url, data ) },
		del:  function(url, data) { return this.ajax('DELETE', url, data)},
		download: function(resource) { window.location.href = 'download/' + resource; },
		getJSON: getJSON,
		validate: validate,
		watch: watch,
		unwatch: unwatch,
		toJSON: toJSON
	})
	
})($, _, W)