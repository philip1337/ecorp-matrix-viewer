package types;

import java.util.ArrayList;
import io.netty.handler.codec.http.HttpMethod;
import veloxio.Provider;

/**
 * The RouteTable class contains all URL routes in the WebServer.
 */
public class WebRouteTable {
    /**
     * Route
     */
    private final ArrayList<WebRoute> routes;

    /**
     * Provider
     */
    private Provider provider_ = null;

    /**
     * Web route table
     */
    public WebRouteTable(Provider p) {
        this.routes = new ArrayList<>();
        provider_ = p;
    }

    /**
     * Add route
     * @param route to register
     */
    public void AddRoute(final WebRoute route) {
        if (route.Initialize(provider_)) {
            this.routes.add(route);
        } else {
            // TODO: Log
        }
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