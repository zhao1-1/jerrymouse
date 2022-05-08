package cn.zhaobin.jerrymouse.test;

import cn.zhaobin.jerrymouse.util.CommonUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MiniBrowserOld {

    /**
     * @return 返回二进制的 http 响应
     */
    public static byte[] getHttpBytes(String url, boolean gzip) {
        byte[] result;
        try {
            // 1. 连接指定url的socket
            URL u = new URL(url);
            Socket client = new Socket();
            int port = u.getPort();
            if(-1==port)
                port = 80;
            InetSocketAddress inetSocketAddress = new InetSocketAddress(u.getHost(), port);
            client.connect(inetSocketAddress, 1000);

            // 2.1(1) 组装请求行
            String path = u.getPath();
            if(path.length()==0)
                path = "/";
            String firstLine = "GET " + path + " HTTP/1.1\n";

            // 2.1(2) 组装请求头
            Map<String,String> requestHeaders = new HashMap<>();
            requestHeaders.put("Host", u.getHost()+":"+port);
            requestHeaders.put("Accept", "text/html");
            requestHeaders.put("Connection", "close");
            requestHeaders.put("User-Agent", "ZhaoBin mini Browser / java1.8");
            if(gzip)
                requestHeaders.put("Accept-Encoding", "gzip");

            // 2.2(1) 生成sb
            StringBuffer httpRequestString = new StringBuffer();

            // 2.2(2) 请求行封进sb
            httpRequestString.append(firstLine);

            // 2.2(3) 请求头封进sb
            Set<String> headers = requestHeaders.keySet();
            for (String header : headers) {
                String headerLine = header + ":" + requestHeaders.get(header)+"\n";
                httpRequestString.append(headerLine);
            }

            // 2.3 将封装了请求行 + 请求头的sb发送给服务器
            PrintWriter pWriter = new PrintWriter(client.getOutputStream(), true);
            pWriter.println(httpRequestString);


            // 3. 获得服务器给返回的response体
            InputStream is = client.getInputStream();
            result = CommonUtils.readBytes(is, true);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
            result = e.toString().getBytes(StandardCharsets.UTF_8);
        }

        return result;

    }

    /**
     * @return 返回字符串的 http 响应
     */
    public static String getHttpString(String url, boolean gzip) {
        byte[] result = getHttpBytes(url,gzip);
        return CommonUtils.byte2String(result, "utf-8");
    }

    public static String getHttpString(String url) { return getHttpString(url,false); }



    /**
     * @param gzip 是否获取压缩后的数据
     * @return 返回二进制的 http 响应内容 （可简单理解为去掉头的 html 部分）
     */
    public static byte[] getContentBytes(String url, boolean gzip, String lineBreak) {
        byte[] response = getHttpBytes(url,gzip);
        byte[] doubleReturn = lineBreak.getBytes();

        int pos = -1;
        for (int i = 0; i < response.length - doubleReturn.length; i++) {
            byte[] temp = Arrays.copyOfRange(response, i, i + doubleReturn.length);

            if(Arrays.equals(temp, doubleReturn)) {
                pos = i;
                break;
            }
        }
        if(-1==pos)
            return null;

        pos += doubleReturn.length;

        return Arrays.copyOfRange(response, pos, response.length);
    }

    /**
     * @param gzip 是否获取压缩后的数据
     * @return 返回字符串的 http 响应内容 （可简单理解为去掉头的 html 部分）
     */
    public static String getContentString(String url, boolean gzip, String lineBreak) {
        byte[] result = getContentBytes(url, gzip, lineBreak);
        return CommonUtils.byte2String(result, "utf-8");
    }

    public static String getContentString(String url) { return getContentString(url, false, "\n\n"); }

    public static byte[] getContentBytes(String url) { return getContentBytes(url, false, "\n\n"); }

}