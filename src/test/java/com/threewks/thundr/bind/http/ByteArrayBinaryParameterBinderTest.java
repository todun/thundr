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
package com.threewks.thundr.bind.http;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.threewks.thundr.bind.parameter.ByteArrayBinaryParameterBinder;
import com.threewks.thundr.http.MultipartFile;
import com.threewks.thundr.introspection.ParameterDescription;

public class ByteArrayBinaryParameterBinderTest {
	private ByteArrayBinaryParameterBinder binder = new ByteArrayBinaryParameterBinder();

	@Test
	public void shouldReturnTrueForWillBindOnByteArrays() {
		assertThat(binder.willBind(new ParameterDescription("data", byte[].class)), is(true));
		assertThat(binder.willBind(new ParameterDescription("data", int[].class)), is(false));
		assertThat(binder.willBind(new ParameterDescription("data", byte.class)), is(false));
	}

	@Test
	public void shouldBindByteArrayByReturningByteArray() {
		assertThat(binder.bind(new ParameterDescription("data", byte[].class), new MultipartFile("test", new byte[] { 1, 2, 3 }, "none/none")), is(new byte[] { 1, 2, 3 }));
	}
}
