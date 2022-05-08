package cn.zhaobin.jerrymouse.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import cn.zhaobin.jerrymouse.util.parsexml.ServerXMLUtils;

import java.util.ArrayList;
import java.util.List;

public class Service {

    private Server server;
    private String name;
    private Engine engine;
    private List<Connector> connectors;

    public Service(Server server) {
        this.server = server;
        this.name = ServerXMLUtils.parseServiceName();
        this.engine = new Engine(this);
        loadConnectors();
    }

    public Engine getEngine() { return this.engine; }

    public void start() {
        init();
    }

    private void init() {
        TimeInterval timeInterval = DateUtil.timer();

        this.connectors.forEach(Connector::init);
        LogFactory.get().info("Initialization processed in {} ms",timeInterval.intervalMs());

        this.connectors.forEach(Connector::start);
        LogFactory.get().info("connectors startup in {} ms",timeInterval.intervalMs());
    }

    private void loadConnectors() {
        List<Connector> result = new ArrayList<>();
        ServerXMLUtils.parseConnectorsPort().forEach(ele -> result.add(new Connector(ele, this)));
        this.connectors = result;
    }

}
