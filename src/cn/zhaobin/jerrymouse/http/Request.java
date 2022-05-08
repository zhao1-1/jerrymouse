package cn.zhaobin.jerrymouse.http;

import cn.hutool.core.util.StrUtil;
import cn.zhaobin.jerrymouse.catalina.Context;
import cn.zhaobin.jerrymouse.catalina.Host;
import cn.zhaobin.jerrymouse.catalina.Service;
import cn.zhaobin.jerrymouse.util.CommonUtils;
import cn.zhaobin.jerrymouse.util.Constant;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Objects;

public class Request extends BaseRequest {

    private final Socket socket;
    private final Service service;
    private String requestString;
    private String method;
    private String uri;
    private Context context;

    public Request(Socket socket, Service service) throws IOException {
        this.socket = socket;
        this.service = service;
        parseHttpRequest();
        if (StrUtil.isEmpty(this.requestString)) this.requestString = Constant.EMPTY_REQUEST_LINE;
        parseMethod();
        parseUri();
        parseContext();
        if (!"/".equals(this.context.getPath())) {
            this.uri = StrUtil.removePrefix(uri, context.getPath());
        }
        if (StrUtil.isEmpty(this.uri)) uri = "/";
        System.out.println("浏览器的输入信息： \n" + this.getRequestString());
        System.out.println("uri:" + this.uri);
    }

    private void parseHttpRequest() throws IOException {
        InputStream is = this.socket.getInputStream();
        byte[] bytes = CommonUtils.readBytes(is, false);
        this.requestString = CommonUtils.byte2String(bytes, "utf-8");
    }

    private void parseMethod() {
        this.method = StrUtil.subBefore(this.requestString, " ", false);
    }

    private void parseUri() {
        String temp;
        temp = StrUtil.subBetween(requestString, " ", " ");
        if (!StrUtil.contains(temp, '?')) {
            this.uri = temp;
            return;
        }
        temp = StrUtil.subBefore(temp, '?', false);
        this.uri = temp;
    }

    private void parseContext() {
        // 直接先通过uri先获取Context
        this.context = getDefaultHost().getContext(this.uri);
        if (Objects.nonNull(context))
            return;

        // 如果通过uri获取不到，再解析path，通过path获取Context
        String path = StrUtil.subBetween(this.uri, "/", "/");
        if (null == path)
            path = "/";
        else
            path = "/" + path;
        this.context = getDefaultHost().getContext(path);
        if (Objects.isNull(context))
            this.context = getDefaultHost().getContext("/");
    }

    private Host getDefaultHost() {
        return this.service.getEngine().getDefaultHost();
    }



    public String getRequestString(){ return this.requestString; }

    @Override
    public String getMethod() { return this.method; }

    public String getUri() { return this.uri; }

    public Context getContext() { return context; }

    @Override
    public ServletContext getServletContext() { return context.getServletContext(); }

    @Override
    public String getRealPath(String path) { return getServletContext().getRealPath(path); }

}
