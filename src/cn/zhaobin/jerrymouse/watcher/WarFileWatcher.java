package cn.zhaobin.jerrymouse.watcher;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import cn.zhaobin.jerrymouse.catalina.Host;
import cn.zhaobin.jerrymouse.util.Constant;

public class WarFileWatcher {
    private WatchMonitor monitor;
    public WarFileWatcher(Host host) {
        this.monitor = WatchUtil.createAll(Constant.WEBAPPS_FOLDER, 1, new Watcher() {
            private void dealWith(WatchEvent<?> event, Path currentPath) {
                synchronized (WarFileWatcher.class) {
                    String fileName = event.context().toString();
                    if(fileName.toLowerCase().endsWith(".war")  && ENTRY_CREATE.equals(event.kind())) {
                        File warFile = FileUtil.file(Constant.WEBAPPS_FOLDER, fileName);
                        host.loadWarDynamic(warFile);
                    }
                }
            }
            @Override
            public void onCreate(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);
            }

            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);

            }
            @Override
            public void onDelete(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);
            }
            @Override
            public void onOverflow(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);
            }

        });
    }

    public void start() {
        monitor.start();
    }

    public void stop() {
        monitor.interrupt();
    }

}