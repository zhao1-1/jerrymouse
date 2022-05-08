package cn.zhaobin.jerrymouse.http;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.zhaobin.jerrymouse.catalina.Context;
import cn.zhaobin.jerrymouse.catalina.Host;
import cn.zhaobin.jerrymouse.catalina.Service;
import cn.zhaobin.jerrymouse.util.CommonUtils;
import cn.zhaobin.jerrymouse.util.Constant;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

public class Request extends BaseRequest {

    private final Socket socket;
    private final Service service;
    private String requestString;
    private String method;
    private String uri;
    private Context context;

    private String queryString;
    private Map<String, String> headerMap;
    private Map<String, String[]> parameterMap;
    private Cookie[] cookies;

    public Request(Socket socket, Service service) throws IOException {
        this.socket = socket;
        this.service = service;
        this.headerMap = new HashMap<>();
        this.parameterMap = new HashMap<>();
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
        parseHeaders();
        parseParameters();
        parseCookies();
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

    public void parseHeaders() {
        StringReader stringReader = new StringReader(this.requestString);
        List<String> lines = new ArrayList<>();
        IoUtil.readLines(stringReader, lines);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (0 == line.length())
                return;
            String[] segments = line.split(":");
            headerMap.put(segments[0].toLowerCase(), segments[1]);
        }
    }

    private void parseParameters() {
        if ("GET".equals(this.getMethod())) {
            String url = StrUtil.subBetween(requestString, " ", " ");
            if (StrUtil.contains(url, '?')) {
                queryString = StrUtil.subAfter(url, '?', false);
            }
        }
        if ("POST".equals(this.getMethod())) {
            queryString = StrUtil.subAfter(requestString, "\n\n", false);
        }
        if (null == queryString)
            return;
        queryString = URLUtil.decode(queryString);
        String[] parameterValues = queryString.split("&");
        if (null != parameterValues) {
            for (String parameterValue : parameterValues) {
                String[] nameValues = parameterValue.split("=");
                String name = nameValues[0];
                String value = nameValues[1];
                String values[] = parameterMap.get(name);
                if (null == values) {
                    values = new String[] { value };
                    parameterMap.put(name, values);
                } else {
                    values = ArrayUtil.append(values, value);
                    parameterMap.put(name, values);
                }
            }
        }
    }

    private void parseCookies() {
        String cookies = headerMap.get("cookie");
        if (StrUtil.isEmpty(cookies))
            return;
        List<Cookie> cookieList = new ArrayList<>();
        for (String pair : StrUtil.split(cookies, ";")) {
            if (StrUtil.isBlank(pair))
                continue;
            String[] segments = StrUtil.split(pair, "=");
            cookieList.add(new Cookie(segments[0].trim(), segments[1].trim()));
        }
        this.cookies = ArrayUtil.toArray(cookieList, Cookie.class);
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

    @Override
    public String getParameter(String name) {
        String values[] = parameterMap.get(name);
        if (null != values && 0 != values.length)
            return values[0];
        return null;
    }

    @Override
    public Map getParameterMap() { return parameterMap; }

    @Override
    public Enumeration getParameterNames() { return Collections.enumeration(parameterMap.keySet()); }

    @Override
    public String[] getParameterValues(String name) { return parameterMap.get(name); }

    @Override
    public Cookie[] getCookies() { return this.cookies; }

    @Override
    public String getHeader(String name) {
        if(null == name) return null;
        return headerMap.get(name.toLowerCase());
    }

    @Override
    public Enumeration getHeaderNames() { return Collections.enumeration(this.headerMap.keySet()); }

    @Override
    public int getIntHeader(String name) { return Convert.toInt(this.headerMap.get(name), 0); }

    @Override
    public String getLocalAddr() { return this.socket.getLocalAddress().getHostAddress(); }

    @Override
    public String getLocalName() { return socket.getLocalAddress().getHostName(); }

    @Override
    public int getLocalPort() { return socket.getLocalPort(); }

    @Override
    public String getProtocol() { return "HTTP:/1.1"; }

    @Override
    public String getRemoteAddr() {
        InetSocketAddress isa = (InetSocketAddress) this.socket.getRemoteSocketAddress();
        return StrUtil.subAfter(isa.getAddress().toString(), "/", false);
    }

    @Override
    public String getRemoteHost() {
        InetSocketAddress isa = (InetSocketAddress) this.socket.getRemoteSocketAddress();
        return isa.getHostName();
    }

    @Override
    public int getRemotePort() { return this.socket.getPort(); }

    @Override
    public String getScheme() { return "http"; }

    @Override
    public String getServerName() { return getHeader("host").trim(); }

    @Override
    public int getServerPort() { return getLocalPort(); }

    @Override
    public String getContextPath() { return this.context.getPath(); }

    @Override
    public String getRequestURI() { return uri; }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer();
        String scheme = getScheme();
        int port = getServerPort();
        if (port < 0) {
            port = 80; // Work around java.net.URL bug
        }
        url.append(scheme);
        url.append("://");
        url.append(getServerName());
        if ((scheme.equals("http") && (port != 80)) || (scheme.equals("https") && (port != 443))) {
            url.append(':');
            url.append(port);
        }
        url.append(getRequestURI());
        return url;
    }

    @Override
    public String getServletPath() { return uri; }

}
