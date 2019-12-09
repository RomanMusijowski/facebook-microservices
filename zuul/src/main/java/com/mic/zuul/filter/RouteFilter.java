package com.mic.zuul.filter;

import com.mic.zuul.client.AuthClient;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.stereotype.Component;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SIMPLE_HOST_ROUTING_FILTER_ORDER;

@Component
@Log4j2
@AllArgsConstructor
class RouteFilter extends ZuulFilter {

    private final AuthClient authClient;

    @Override
    public String filterType() {
        return ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return SIMPLE_HOST_ROUTING_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        String requestURI = context.getRequest().getRequestURI();
        if ("/auth/api/auth/signup".equals(requestURI) || "/auth/api/auth/signin".equals(requestURI) || requestURI.startsWith("/auth/api/auth/activation/")) {
            return null;
        }

        if (!authClient.isAuthenticated()) {
            ZuulException zuulException = new ZuulException("User not Authenticated", 403, "User not Authenticated");
            throw new ZuulRuntimeException(zuulException);
        }
        return null;
    }
}

