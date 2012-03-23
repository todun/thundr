package com.atomicleopard.webFramework.routes;

import static com.atomicleopard.expressive.Expressive.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomicleopard.webFramework.configuration.JsonProperties;
import com.atomicleopard.webFramework.exception.BaseException;
import com.atomicleopard.webFramework.logger.Logger;

public class Routes {
	private static final String Static = "static";

	private Map<String, Route> getRoutes = new LinkedHashMap<String, Route>();
	private Map<String, Route> postRoutes = new LinkedHashMap<String, Route>();
	private Map<String, Route> putRoutes = new LinkedHashMap<String, Route>();
	private Map<String, Route> deleteRoutes = new LinkedHashMap<String, Route>();
	private Map<Route, Action> actionsForRoutes = new HashMap<Route, Action>();
	private Map<RouteType, Map<String, Route>> routes = mapKeys(RouteType.GET, RouteType.POST, RouteType.PUT, RouteType.DELETE).to(getRoutes, postRoutes, putRoutes, deleteRoutes);

	private Map<Class<? extends Action>, ActionResolver<?>> actionResolvers = new LinkedHashMap<Class<? extends Action>, ActionResolver<?>>();

	private boolean debug = true;

	public Routes(ServletContext servletContext) {
		actionResolvers.put(StaticResourceAction.class, new StaticResourceActionResolver(servletContext));
		actionResolvers.put(ActionMethod.class, new ActionMethodResolver());
	}

	public void addRoutes(Collection<Route> routes) {
		for (Route route : routes) {
			String path = route.getRouteMatchRegex();
			String actionName = route.getActionName();
			RouteType routeType = route.getRouteType();
			Action action = createAction(actionName);
			this.routes.get(routeType).put(path, route);
			this.actionsForRoutes.put(route, action);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Action> Object invoke(String routePath, RouteType routeType, HttpServletRequest req, HttpServletResponse resp) {
		Logger.debug("Requesting '%s'", routePath);
		Map<String, Route> routesForRouteType = routes.get(routeType);
		for (Route route : routesForRouteType.values()) {
			if (route.matches(routePath)) {
				Map<String, String> pathVars = route.getPathVars(routePath);
				T action = (T) actionsForRoutes.get(route);
				ActionResolver<T> actionResolver = (ActionResolver<T>) actionResolvers.get(action.getClass());
				return actionResolver.resolve(action, req, resp, pathVars);
			}
		}
		String debugString = debug ? listRoutes() : "";
		throw new RouteException("No route matching the request %s %s\n%s", routeType, routePath, debugString);
	}

	private static final String routeDisplayFormat = "%s\n";

	@SuppressWarnings("unchecked")
	private String listRoutes() {
		List<String> allRoutes = flatten(getRoutes.keySet(), postRoutes.keySet(), putRoutes.keySet(), deleteRoutes.keySet());
		allRoutes = list(new HashSet<String>(allRoutes));

		Collections.sort(allRoutes);
		StringBuilder sb = new StringBuilder();
		for (String route : allRoutes) {
			if (getRoutes.containsKey(route)) {
				sb.append(String.format(routeDisplayFormat, getRoutes.get(route)));
			}
			if (postRoutes.containsKey(route)) {
				sb.append(String.format(routeDisplayFormat, postRoutes.get(route)));
			}
			if (putRoutes.containsKey(route)) {
				sb.append(String.format(routeDisplayFormat, putRoutes.get(route)));
			}
			if (deleteRoutes.containsKey(route)) {
				sb.append(String.format(routeDisplayFormat, deleteRoutes.get(route)));
			}
		}
		return sb.toString();
	}

	public Action createAction(String actionName) {
		try {
			if (Static.equalsIgnoreCase(actionName)) {
				return new StaticResourceAction();
			}
			return new ActionMethod(actionName);
		} catch (Exception e) {
			throw new BaseException(e, "Failed to create an action for the route %s: %s", actionName, e.getMessage());
		}

	}

	public static List<Route> parseJsonRoutes(String source) {
		try {
			List<Route> routes = new ArrayList<Route>();
			JsonProperties properties = new JsonProperties(source);
			for (String route : properties.getKeys()) {
				String actionName = null;
				if (properties.is(route, String.class)) {
					// simple route
					actionName = properties.getString(route);
					routes.add(new Route(route, actionName, RouteType.GET));
				} else if (properties.is(route, List.class)) {
					// complex route
					List<String> list = properties.getList(route);
					actionName = list.get(0);
					for (int i = 1; i < list.size(); i++) {
						RouteType routeType = RouteType.from(list.get(i));
						routes.add(new Route(route, actionName, routeType));
					}
				}
			}

			return routes;
		} catch (Exception e) {
			throw new RouteException(e, "Failed to parse routes: %s", e.getMessage());
		}
	}
}
