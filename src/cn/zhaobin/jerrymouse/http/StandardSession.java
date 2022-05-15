package cn.zhaobin.jerrymouse.http;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class StandardSession implements HttpSession {
    private final Map<String, Object> attributesMap;

    private final String id;
    private final long creationTime;
    private long lastAccessedTime;
    private final ServletContext servletContext;
    private int maxInactiveInterval;

    public StandardSession(String jsessionid, ServletContext servletContext) {
        this.attributesMap = new HashMap<>();
        this.id = jsessionid;
        this.creationTime = System.currentTimeMillis();
        this.servletContext = servletContext;
    }

    public void removeAttribute(String name) { attributesMap.remove(name); }

    public void setAttribute(String name, Object value) { attributesMap.put(name, value); }

    public Object getAttribute(String name) { return attributesMap.get(name); }

    public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributesMap.keySet()); }

    public long getCreationTime() { return this.creationTime; }

    public String getId() { return id; }

    public long getLastAccessedTime() { return lastAccessedTime; }

    public void setLastAccessedTime(long lastAccessedTime) { this.lastAccessedTime = lastAccessedTime; }

    public int getMaxInactiveInterval() { return this.maxInactiveInterval; }

    public void setMaxInactiveInterval(int maxInactiveInterval) { this.maxInactiveInterval = maxInactiveInterval; }

    public ServletContext getServletContext() { return servletContext; }

    @Deprecated
    public HttpSessionContext getSessionContext() { return null; }

    public Object getValue(String arg0) { return null; }

    public String[] getValueNames() { return null; }

    public void invalidate() { attributesMap.clear(); }

    public boolean isNew() { return creationTime == lastAccessedTime; }

    public void putValue(String arg0, Object arg1) { }

    public void removeValue(String arg0) { }

}
