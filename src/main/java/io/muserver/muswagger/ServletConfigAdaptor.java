package io.muserver.muswagger;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

import java.util.Collections;
import java.util.Map;

class ServletConfigAdaptor implements ServletConfig {

    private final Map<String, String> initialConfig;
    private final ServletContextAdaptor servletContextAdaptor;

    ServletConfigAdaptor(Map<String, String> initialConfig, ServletContextAdaptor servletContextAdaptor) {
        this.initialConfig = initialConfig;
        this.servletContextAdaptor = servletContextAdaptor;
    }

    @Override
    public String getServletName() {
        return "MuOpenApiResource";
    }

    @Override
    public ServletContext getServletContext() {
        return servletContextAdaptor;
    }

    @Override
    public String getInitParameter(String name) {
        return initialConfig.get(name);
    }

    @Override
    public java.util.Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initialConfig.keySet());
    }
}
