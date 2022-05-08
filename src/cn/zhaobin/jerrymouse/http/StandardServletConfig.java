package cn.zhaobin.jerrymouse.http;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class StandardServletConfig implements ServletConfig {

    private ServletContext servletContext;
    private Map<String, String> initParameters;
    private String servletName;

    public StandardServletConfig(ServletContext servletContext, String servletName, Map<String, String> initParameters) {
        this.servletContext = servletContext;
        this.servletName = servletName;
        this.initParameters = initParameters;
        if (null == this.initParameters)
            this.initParameters = new HashMap<>();
    }

    @Override
    public String getServletName() { return servletName; }

    @Override
    public ServletContext getServletContext() { return servletContext; }

    @Override
    public String getInitParameter(String key) { return initParameters.get(key); }

    @Override
    public Enumeration<String> getInitParameterNames() { return Collections.enumeration(initParameters.keySet()); }
}
