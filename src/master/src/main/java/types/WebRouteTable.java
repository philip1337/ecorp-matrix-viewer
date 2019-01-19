package types;

import java.util.ArrayList;
import io.netty.handler.codec.http.HttpMethod;

/**
 * The RouteTable class contains all URL routes in the WebServer.
 */
public class WebRouteTable {
    /**
     * Route
     */
    private final ArrayList<WebRoute> routes;

    /**
     * Web route table
     */
    public WebRouteTable() {
        this.routes = new ArrayList<WebRoute>();
    }

    /**
     * Add route
     * @param route to register
     */
    public void AddRoute(final WebRoute route) {
        this.routes.add(route);
    }

    /**
     * Find matching route
     * @param method request method
     * @param path uri
     * @return route or null
     */
    public WebRoute FindRoute(final HttpMethod method, final String path) {
        for (final WebRoute route : routes) {
            if (route.Matches(method, path)) {
                return route;
            }
        }

        return null;
    }
}