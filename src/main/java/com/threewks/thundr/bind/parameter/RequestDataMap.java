/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://3wks.github.io/thundr/
 * Copyright (C) 2015 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.threewks.thundr.bind.parameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Represents post parameters or query parameters in a structured hierarchical way so that
 * they can be meaningfully parsed to java objects.
 * 
 * Given a request map, alters the key to represent the path as components.
 * For example object[0].name should be a key of object, [0], name
 * 
 * This allows us to pop keys as we walk a path.
 * For the above example, when dealing with object, the path should then be [0], name
 */
public class RequestDataMap {

	private Map<List<String>, List<String>> delegate = new LinkedHashMap<>();

	private RequestDataMap() {

	}

	public RequestDataMap(Map<String, List<String>> input) {
		this();
		if (input != null) {
			for (Map.Entry<String, List<String>> entry : input.entrySet()) {
				String key = entry.getKey();
				String removeDashes = key.replaceAll("-", "");
				String expandedKey = removeDashes.replaceAll("\\.", "\r");
				expandedKey = expandedKey.replaceAll("\\[([^\\]])", "\r[$1");
				List<String> path = Collections.unmodifiableList(Arrays.asList(expandedKey.split("\r")));
				delegate.put(path, entry.getValue());
			}
		}
	}

	public RequestDataMap pathMapFor(String key) {
		RequestDataMap pathMap = new RequestDataMap();
		for (Map.Entry<List<String>, List<String>> entry : delegate.entrySet()) {
			List<String> path = entry.getKey();
			if (path.size() > 1 && path.get(0).equals(key)) {
				List<String> newPath = path.subList(1, path.size());
				pathMap.delegate.put(newPath, entry.getValue());
			}
		}
		return pathMap;
	}

	public Set<String> uniqueChildren() {
		Set<String> results = new HashSet<String>();
		for (Map.Entry<List<String>, List<String>> entry : delegate.entrySet()) {
			List<String> path = entry.getKey();
			String child = path.get(0);
			results.add(child);
		}
		return results;
	}

	public List<String> get(List<String> arg0) {
		return delegate.get(arg0);
	}

	public List<String> get(String arg0) {
		return delegate.get(Collections.singletonList(arg0));
	}

	public int size() {
		return delegate.size();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	/**
	 * Creates a string map for all entries using the given map - assumes that the entity we're binding to is named.
	 * I.e.
	 * 
	 * Object {
	 * public Object a;
	 * public Object b;
	 * }
	 * 
	 * would look like this:
	 * object.a -&gt; value A
	 * object.b -&gt; value B
	 * 
	 * The returned map is a map of String -&gt; String, or if multiple values exist, String -&gt; List&lt;String&gt;
	 */
	public Map<String, Object> toStringMap(String pathElement) {
		Map<String, Object> stringMap = new HashMap<String, Object>();
		for (Entry<List<String>, List<String>> entry : delegate.entrySet()) {
			List<String> key = entry.getKey();

			if (pathElement.equals(key.get(0))) {
				StringBuilder joinedKey = new StringBuilder();
				boolean first = true;
				for (int i = 1; i < key.size(); i++) {
					String string = key.get(i);
					if (!first && !string.startsWith("[")) {
						joinedKey.append(".");
					}
					joinedKey.append(string);
					first = false;
				}
				String stringKey = joinedKey.toString();
				if (stringKey.length() > 0) {
					List<String> value = entry.getValue();
					stringMap.put(stringKey, value == null || value.size() > 1 ? value : value.get(0));
				}
			}
		}
		return stringMap;
	}

	/**
	 * Creates a string map for all entries using the given map - assumes that the entity we're binding to is not named.
	 * I.e.
	 * 
	 * Object {
	 * public Object a;
	 * public Object b;
	 * }
	 * 
	 * would look like this:
	 * a -&gt; value A
	 * b -&gt; value B
	 */
	public Map<String, List<String>> toStringMap() {
		Map<String, List<String>> stringMap = new HashMap<String, List<String>>();
		for (Entry<List<String>, List<String>> entry : delegate.entrySet()) {
			List<String> key = entry.getKey();

			StringBuilder joinedKey = new StringBuilder();
			boolean first = true;
			for (int i = 0; i < key.size(); i++) {
				String string = key.get(i);
				if (!first && !string.startsWith("[")) {
					joinedKey.append(".");
				}
				joinedKey.append(string);
				first = false;
			}
			String stringKey = joinedKey.toString();
			if (stringKey.length() > 0) {
				stringMap.put(stringKey, entry.getValue());
			}
		}
		return stringMap;
	}
}
