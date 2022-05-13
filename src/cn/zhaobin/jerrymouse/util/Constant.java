package cn.zhaobin.jerrymouse.util;

import cn.hutool.system.SystemUtil;

import java.io.File;

public class Constant {
    // test
    public static final int CONNECTOR_1_PORT = 8848;
    public static final String LOCAL_IP = "127.0.0.1";


    public static final int BUFFER_SIZE = 1024;

    public static final String EMPTY_REQUEST_LINE = "GET / HTTP/1.1";

    public static final String NO_INDEX_WELCOME_CONTENT = "hi, welcome jerryMouse by zhaoBin, and your context don't have index.html";

    public static final String TEXT_FORMAT_404 =
            "<html><head><title>DIY JerryMouse/1.0.1 - Error report</title><style>" +
            "<!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} " +
            "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} " +
            "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} " +
            "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} " +
            "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} " +
            "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}" +
            "A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> " +
            "</head><body><h1>HTTP Status 404 - {}</h1>" +
            "<HR size='1' noshade='noshade'><p><b>type</b> Status report</p><p><b>message</b> <u>{}</u></p><p><b>description</b> " +
            "<u>The requested resource is not available.</u></p><HR size='1' noshade='noshade'><h3>DiyJerryMouse 1.0.1</h3>" +
            "</body></html>";
    public static final String TEXT_FORMAT_500 =
            "<html><head><title>DIY Tomcat/1.0.1 - Error report</title><style>"
            + "<!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} "
            + "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} "
            + "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} "
            + "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} "
            + "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} "
            + "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}"
            + "A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> "
            + "</head><body><h1>HTTP Status 500 - An exception occurred processing {}</h1>"
            + "<HR size='1' noshade='noshade'><p><b>type</b> Exception report</p><p><b>message</b> <u>An exception occurred processing {}</u></p><p><b>description</b> "
            + "<u>The server encountered an internal error that prevented it from fulfilling this request.</u></p>"
            + "<p>Stacktrace:</p>" + "<pre>{}</pre>" + "<HR size='1' noshade='noshade'><h3>DiyTocmat 1.0.1</h3>"
            + "</body></html>";

    public static final File WEBAPPS_FOLDER = new File(SystemUtil.get("user.dir"), "webapps");

    public static final File CONF_FOLDER = new File(SystemUtil.get("user.dir"), "conf");
    public static final File SERVER_XML_FILE = new File(CONF_FOLDER, "server.xml");
    public static final File WEB_XML_FILE = new File(CONF_FOLDER, "web.xml");
    public static final File CONTEXT_XML_FILE = new File(CONF_FOLDER, "context.xml");
    public static final String DEFAULT_WATCHED_RESOURCE = "WEB-INF/web.xml";

}
