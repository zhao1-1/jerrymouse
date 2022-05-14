package cn.zhaobin.jerrymouse.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.zhaobin.jerrymouse.util.Constant;
import cn.zhaobin.jerrymouse.util.parsexml.ServerXMLUtils;
import cn.zhaobin.jerrymouse.watcher.WarFileWatcher;

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
        scanWebAppsFolderAndLoadContexts();
        loadContextsFromServerXML();

        // 开启war动态部署监听器
        new WarFileWatcher(this).start();
    }

    public String getName() { return name; }

    private void scanWebAppsFolderAndLoadContexts() {
        File[] folders = Constant.WEBAPPS_FOLDER.listFiles();
        for (File f : folders) {
            if (f.isDirectory())
                loadContextFromFolder(f);
            if (!f.isDirectory() && f.getName().toLowerCase().endsWith(".war"))
                loadContextFromWar(f);
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

    private void loadContextFromWar(File warFile) {
        String fileName = warFile.getName();
        String folderName = StrUtil.subBefore(fileName,".",true);

        //看看是否已经有对应的 Context了
        if(null != getContext("/" + folderName))
            return;

        //先看是否已经有对应的文件夹
        if(new File(Constant.WEBAPPS_FOLDER, folderName).exists())
            return;

        //移动war文件，因为jar 命令只支持解压到当前目录下
        File tempWarFile = FileUtil.file(Constant.WEBAPPS_FOLDER, folderName, fileName);
        File contextFolder = tempWarFile.getParentFile();
        contextFolder.mkdir();
        FileUtil.copyFile(warFile, tempWarFile);

        //解压
        String command = "jar xvf " + fileName;
        Process p = RuntimeUtil.exec(null, contextFolder, command);
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //解压之后删除临时war
        tempWarFile.delete();

        //然后创建新的 Context
        loadContextFromFolder(contextFolder);
    }

    private void loadContextsFromServerXML() {
        ServerXMLUtils.parseAndLoadContexts(this).stream().forEach(
                (context) -> contextMap.put(context.getPath(), context));
    }

    public Context getContext(String path) { return this.contextMap.get(path); }

    public void loadWarDynamic(File warFile) {
        loadContextFromWar(warFile);
    }
}
