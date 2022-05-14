package cn.zhaobin.jerrymouse.classloader;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class WebAppClassLoader extends URLClassLoader {
    public WebAppClassLoader(String docBase, ClassLoader commonClassLoader) {
        super(new URL[] {}, commonClassLoader);
        try {
            File WEBINFFolder = new File(docBase, "WEB-INF");

            this.addURL(new URL("file:" + new File(WEBINFFolder, "classes").getAbsolutePath() + "/"));

            List<File> jarFiles = FileUtil.loopFiles(new File(WEBINFFolder, "lib"));
            for (File jarFile : jarFiles) {
                this.addURL(new URL("file:" + jarFile.getAbsolutePath()));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
