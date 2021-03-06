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
package com.threewks.thundr.route;

import com.threewks.thundr.bind.BinderModule;
import com.threewks.thundr.bind.BinderRegistry;
import com.threewks.thundr.injection.BaseModule;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.module.DependencyRegistry;
import com.threewks.thundr.route.controller.Controller;
import com.threewks.thundr.route.controller.ControllerRouteResolver;
import com.threewks.thundr.route.controller.FilterRegistry;
import com.threewks.thundr.route.controller.FilterRegistryImpl;
import com.threewks.thundr.route.controller.InterceptorRegistry;
import com.threewks.thundr.route.redirect.Redirect;
import com.threewks.thundr.route.redirect.RedirectRouteResolver;
import com.threewks.thundr.route.xsrf.Xsrf;
import com.threewks.thundr.route.xsrf.XsrfInterceptor;
import com.threewks.thundr.view.ViewModule;

public class RouterModule extends BaseModule {
	@Override
	public void requires(DependencyRegistry dependencyRegistry) {
		dependencyRegistry.addDependency(ViewModule.class);
		dependencyRegistry.addDependency(BinderModule.class);
	}

	@Override
	public void initialise(UpdatableInjectionContext injectionContext) {
		super.initialise(injectionContext);
		injectionContext.inject(new Router()).as(Router.class);
		injectionContext.inject(new FilterRegistryImpl()).as(FilterRegistry.class);
	}

	@Override
	public void configure(UpdatableInjectionContext injectionContext) {
		Router router = injectionContext.get(Router.class);
		FilterRegistry filters = injectionContext.get(FilterRegistry.class);
		BinderRegistry binderRegistry = injectionContext.get(BinderRegistry.class);

		ControllerRouteResolver controllerRouteResolver = new ControllerRouteResolver(injectionContext, filters, binderRegistry);
		injectionContext.inject(controllerRouteResolver).as(ControllerRouteResolver.class);
		injectionContext.inject(controllerRouteResolver).as(InterceptorRegistry.class);

		router.addResolver(Redirect.class, new RedirectRouteResolver());
		router.addResolver(Controller.class, controllerRouteResolver);

		controllerRouteResolver.registerInterceptor(Xsrf.class, new XsrfInterceptor());

	}
}
