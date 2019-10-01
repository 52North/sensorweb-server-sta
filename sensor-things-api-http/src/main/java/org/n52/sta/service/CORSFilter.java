package org.n52.sta.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CORSFilter implements Filter {

    @Value("${http.cors.allowOrigin:*}")
    private String CORSOrigin;

    @Value("${http.cors.allowMethods:POST, PUT, GET, OPTIONS, DELETE, PATCH}")
    private String CORSMethods;

    @Value("${http.cors.maxAge:3600}")
    private String CORSMaxAge;

    @Value("${http.cors.allowHeaders:Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With}")
    private String CORSHeaders;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;

        res.setHeader("Access-Control-Allow-Origin", CORSOrigin);
        res.setHeader("Access-Control-Allow-Methods", CORSMethods);
        res.setHeader("Access-Control-Max-Age", CORSMaxAge);
        res.setHeader("Access-Control-Allow-Headers", CORSHeaders);

        chain.doFilter(request, res);
    }

    @Override
    public void destroy() {

    }
}
