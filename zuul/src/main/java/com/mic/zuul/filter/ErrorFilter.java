package com.mic.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Log4j2
public class ErrorFilter extends ZuulFilter {

    private static final String FILTER_TYPE = "error";
    private static final String THROWABLE_KEY = "throwable";
    private static final int FILTER_ORDER = -1;

    @Override
    public String filterType() {
        return FILTER_TYPE;
    }

    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        final RequestContext context = RequestContext.getCurrentContext();
        final Object throwable = context.get(THROWABLE_KEY);

        if (throwable instanceof ZuulException) {
            final ZuulException zuulException = (ZuulException) throwable;
            log.error("Zuul failure detected: " + zuulException.getMessage() + ": " + zuulException.getCause().getMessage());

            context.remove(THROWABLE_KEY);
            ErrorDetails errorDetails = new ErrorDetails(new Date(), zuulException.getMessage(), zuulException.getCause().getMessage());

            context.setResponseBody(errorDetails.toString());
            context.getResponse().setContentType("application/json");
            context.setResponseStatusCode(403);
        }
        return null;
    }
}