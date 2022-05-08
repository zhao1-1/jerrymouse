package cn.zhaobin.jerrymouse;

import cn.zhaobin.jerrymouse.classloader.CommonClassLoader;

public class Bootstrap {

    public static void main(String[] args) throws Exception {
        CommonClassLoader commonClassLoader = new CommonClassLoader();

        Thread.currentThread().setContextClassLoader(commonClassLoader);

        Class<?> serverClazz = commonClassLoader.loadClass("cn.zhaobin.jerrymouse.catalina.Server");
        serverClazz.getMethod("start").invoke(serverClazz.newInstance());

        System.out.println(serverClazz.getClassLoader());

        // 不能关闭，否则后续就不能使用啦
        // commonClassLoader.close();
    }

}