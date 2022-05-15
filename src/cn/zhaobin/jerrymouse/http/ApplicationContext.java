package cn.zhaobin.jerrymouse.http;

import cn.zhaobin.jerrymouse.catalina.Context;

import java.io.File;
import java.util.*;

public class ApplicationContext extends BaseServletContext{

    private final Map<String, Object> attributesMap;
    private final Context context;

    public ApplicationContext(Context context) {
        this.attributesMap = new HashMap<>();
        this.context = context;
    }

    @Override
    public void setAttribute(String name, Object value) { attributesMap.put(name, value); }

    @Override
    public Object getAttribute(String name) { return attributesMap.get(name); }

    @Override
    public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributesMap.keySet()); }

    @Override
    public void removeAttribute(String name) { attributesMap.remove(name); }

    @Override
    public String getRealPath(String path) { return new File(context.getDocBase(), path).getAbsolutePath(); }
}
