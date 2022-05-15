package cn.zhaobin.jerrymouse.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Server {

    private final Service service;

    public Server() {
        this.service = new Service(this);
    }

    public void start() {
        TimeInterval timeInterval = DateUtil.timer();
        logJVM();
        init();
        LogFactory.get().info("<<<<<<<<<<<<====  SERVER  SUCCESS  STARTUP  ====>>>>>>>>>>>>> start use: " + timeInterval.intervalMs());
    }

    private void init() {
        this.service.start();
    }

    private static void logJVM() {
        Map<String,String> infos = new LinkedHashMap<>();
        infos.put("Server version", "cpp jerryMouse/1.0.1");
        infos.put("Server built", "2022-04-04 19:20:22");
        infos.put("Server number", "1.0.1");
        infos.put("OS Name", SystemUtil.get("os.name"));
        infos.put("OS Version", SystemUtil.get("os.version"));
        infos.put("Architecture", SystemUtil.get("os.arch"));
        infos.put("Java Home", SystemUtil.get("java.home"));
        infos.put("JVM Version", SystemUtil.get("java.runtime.version"));
        infos.put("JVM Vendor", SystemUtil.get("java.vm.specification.vendor"));
        infos.put("Service Dir", SystemUtil.get("user.dir"));
        Set<String> keys = infos.keySet();
        for (String key : keys) {
            LogFactory.get().info(key + ":\t" + infos.get(key));
        }
        LogFactory.get().info("-------系---------统--------信-------息--------线------");
    }
}
