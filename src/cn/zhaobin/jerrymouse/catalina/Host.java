package cn.zhaobin.jerrymouse.catalina;

import cn.zhaobin.jerrymouse.util.Constant;
import cn.zhaobin.jerrymouse.util.parsexml.ServerXMLUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Host {

    private Engine engine;
    private String name;
    private Map<String, Context> contextMap;

    public Host(Engine engine, String name) {
        this.engine = engine;
        this.name = name;
        this.contextMap = new HashMap<>();
        loadContextsFromWebAppsFolder();
        loadContextsFromServerXML();
    }

    public String getName() { return name; }

    private void loadContextsFromWebAppsFolder() {
        scanWebAppsFolderAndLoadContexts();
    }

    private void scanWebAppsFolderAndLoadContexts() {
        File[] folders = Constant.WEBAPPS_FOLDER.listFiles();
        for (File folder : folders) {
            if (!folder.isDirectory())
                continue;
            loadContextFromFolder(folder);
        }
    }

    private void loadContextFromFolder(File folder) {
        String path = folder.getName();
        if ("ROOT".equals(path))
            path = "/";
        else
            path = "/" + path;

        String docBase = folder.getAbsolutePath();
        Context context = new Context(this, path, docBase);

        contextMap.put(context.getPath(), context);
    }

    private void loadContextsFromServerXML() {
        ServerXMLUtils.parseAndLoadContexts(this).stream().forEach(
                (context) -> contextMap.put(context.getPath(), context));
    }

    public Context getContext(String path) { return this.contextMap.get(path); }

}
