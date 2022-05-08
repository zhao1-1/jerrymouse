package cn.zhaobin.jerrymouse.util.parsexml;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.zhaobin.jerrymouse.catalina.Connector;
import cn.zhaobin.jerrymouse.catalina.Context;
import cn.zhaobin.jerrymouse.catalina.Host;
import cn.zhaobin.jerrymouse.util.Constant;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

public class ServerXMLUtils {

    public static List<Integer> parseConnectorsPort() {
        List<Integer> result = new ArrayList<>();
        parseServerXml2Document()
                .select("Connector")
                .forEach(ele -> result.add(Convert.toInt(ele.attr("port"))));
        return result;
    }

    public static String parseServiceName() {
        return parseServerXml2Document()
                .select("Service").first()
                .attr("name");
    }

    public static String parseEngineDefaultHostName() {
        return parseServerXml2Document()
                .select("Engine").first()
                .attr("defaultHost");
    }

    public static List<String> parseEngineHostsName() {
        List<String> result = new ArrayList<>();
        parseServerXml2Document()
                .select("Host")
                .forEach(ele -> result.add(ele.attr("name")));
        return result;
    }

    public static List<Context> parseAndLoadContexts(Host host) {
        List<Context> result = new ArrayList<>();
        parseServerXml2Document()
                .select("Context")
                .forEach(ele -> result.add(new Context(host, ele.attr("path"), ele.attr("docBase"))));
        return result;
    }

    private static Document parseServerXml2Document() {
        return Jsoup.parse(FileUtil.readUtf8String(Constant.SERVER_XML_FILE));
    }

}
