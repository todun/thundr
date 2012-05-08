package com.atomicleopard.webFramework.view;

import com.atomicleopard.webFramework.http.exception.HttpStatusException;
import com.atomicleopard.webFramework.injection.InjectionConfiguration;
import com.atomicleopard.webFramework.injection.UpdatableInjectionContext;
import com.atomicleopard.webFramework.route.RouteNotFoundException;
import com.atomicleopard.webFramework.view.exception.ExceptionViewResolver;
import com.atomicleopard.webFramework.view.exception.HttpStatusExceptionViewResolver;
import com.atomicleopard.webFramework.view.exception.RouteNotFoundViewResolver;
import com.atomicleopard.webFramework.view.json.JsonView;
import com.atomicleopard.webFramework.view.json.JsonViewResolver;
import com.atomicleopard.webFramework.view.jsp.JspView;
import com.atomicleopard.webFramework.view.jsp.JspViewResolver;
import com.atomicleopard.webFramework.view.redirect.RedirectView;
import com.atomicleopard.webFramework.view.redirect.RedirectViewResolver;
import com.atomicleopard.webFramework.view.string.StringView;
import com.atomicleopard.webFramework.view.string.StringViewResolver;

public class ViewResolverInjectionConfiguration implements InjectionConfiguration {

	@Override
	public void configure(UpdatableInjectionContext injectionContext) {
		ViewResolverRegistry viewResolverRegistry = new ViewResolverRegistry();
		injectionContext.inject(ViewResolverRegistry.class).as(viewResolverRegistry);
		addViewResolvers(viewResolverRegistry, injectionContext);
	}

	protected void addViewResolvers(ViewResolverRegistry viewResolverRegistry, UpdatableInjectionContext injectionContext) {
		ExceptionViewResolver exceptionViewResolver = new ExceptionViewResolver();
		HttpStatusExceptionViewResolver statusViewResolver = new HttpStatusExceptionViewResolver();
		
		injectionContext.inject(ExceptionViewResolver.class).as(exceptionViewResolver);
		injectionContext.inject(HttpStatusExceptionViewResolver.class).as(statusViewResolver);
		
		viewResolverRegistry.addResolver(Throwable.class, exceptionViewResolver);
		viewResolverRegistry.addResolver(HttpStatusException.class, statusViewResolver);
		viewResolverRegistry.addResolver(RouteNotFoundException.class, new RouteNotFoundViewResolver());
		viewResolverRegistry.addResolver(RedirectView.class, new RedirectViewResolver());
		viewResolverRegistry.addResolver(JsonView.class, new JsonViewResolver());
		viewResolverRegistry.addResolver(JspView.class, new JspViewResolver());
		viewResolverRegistry.addResolver(StringView.class, new StringViewResolver());
	}
}
