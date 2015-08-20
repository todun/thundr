/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://3wks.github.io/thundr/
 * Copyright (C) 2014 3wks, <thundr@3wks.com.au>
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
package com.threewks.thundr.bind;

import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.bind.parameter.ParameterBindingModule;
import com.threewks.thundr.injection.BaseModule;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.module.DependencyRegistry;
import com.threewks.thundr.transformer.TransformerManager;
import com.threewks.thundr.transformer.TransformerModule;

public class BinderModule extends BaseModule {
	@Override
	public void requires(DependencyRegistry dependencyRegistry) {
		super.requires(dependencyRegistry);
		dependencyRegistry.addDependency(ParameterBindingModule.class);
		dependencyRegistry.addDependency(TransformerModule.class);
	}

	@Override
	public void initialise(UpdatableInjectionContext injectionContext) {
		super.initialise(injectionContext);
		BinderRegistry binderRegistry = new BinderRegistry();
		injectionContext.inject(binderRegistry).as(BinderRegistry.class);
	}

	@Override
	public void configure(UpdatableInjectionContext injectionContext) {
		super.configure(injectionContext);

		TransformerManager transformerManager = injectionContext.get(TransformerManager.class);
		ParameterBinderRegistry parameterBinderRegistry = injectionContext.get(ParameterBinderRegistry.class);
		BinderRegistry binderRegistry = injectionContext.get(BinderRegistry.class);

		BinderRegistry.registerDefaultBinders(binderRegistry, parameterBinderRegistry, transformerManager);

	}
}
