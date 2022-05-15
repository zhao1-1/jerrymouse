package cn.zhaobin.jerrymouse.catalina;

import cn.zhaobin.jerrymouse.util.parsexml.ServerXMLUtils;

import java.util.ArrayList;
import java.util.List;

public class Engine {

    private final Service service;
    private List<Host> hosts;
    private Host defaultHost;

    public Engine(Service service) {
        this.service = service;
        loadHosts();
        loadDefaultHost();
        checkDefault();
    }

    private void loadHosts() {
        List<Host> result = new ArrayList<>();
        ServerXMLUtils.parseEngineHostsName().forEach(ele -> result.add(new Host(this, ele)));
        this.hosts = result;
    }

    private void loadDefaultHost() {
        String defaultHostName = ServerXMLUtils.parseEngineDefaultHostName();
        for (Host host : this.hosts) {
            if (host.getName().equals(defaultHostName)) {
                this.defaultHost = host;
                return;
            }
        }
    }

    private void checkDefault() {
        if (null == this.defaultHost) {
            throw new RuntimeException("the defaultHost does not exist!");
        }
    }

    public Host getDefaultHost() { return this.defaultHost; }

}
