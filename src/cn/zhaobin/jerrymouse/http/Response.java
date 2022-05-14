package cn.zhaobin.jerrymouse.http;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.zhaobin.jerrymouse.util.CommonUtils;
import cn.zhaobin.jerrymouse.util.Constant;
import cn.zhaobin.jerrymouse.util.StatusCodeEnum;
import cn.zhaobin.jerrymouse.util.parsexml.WebXMLUtils;

import javax.servlet.http.Cookie;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static cn.zhaobin.jerrymouse.util.StatusCodeEnum.*;

public class Response extends BaseResponse{

    private Socket socket;

    private StringWriter stringWriter;
    private PrintWriter writer;
    private File sourceFile;
    private byte[] body;
    private String contentType;
    private int status;

    private List<Cookie> cookies;
    private String redirectPath;

    public Response(Socket socket){
        this.socket = socket;
        this.stringWriter = new StringWriter();
        this.writer = new PrintWriter(stringWriter);
        this.contentType = "text/html";
        this.cookies = new ArrayList<>();
    }

    @Override
    public String getContentType() { return this.contentType; }

    private String getContentTypeHeader() {
        if (null == this.contentType) return "";
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        sb.append("Content-Type: ");
        sb.append(getContentType());
        return sb.toString();
    }

    @Override
    public void addCookie(Cookie cookie) { this.cookies.add(cookie); }

    @Override
    public void sendRedirect(String redirect) throws IOException { this.redirectPath = redirect; }

    private List<Cookie> getCookies() { return this.cookies; }

    private String getCookiesHeader() {
        if (null == this.cookies)
            return "";
        StringBuffer sb = new StringBuffer();
        for (Cookie cookie : getCookies()) {
            sb.append("\n");
            sb.append("Set-Cookie: ");
            sb.append(cookie.getName() + "=" + cookie.getValue() + "; ");
            if (-1 != cookie.getMaxAge()) { //-1 mean forever
                sb.append("Expires=");
                sb.append(parseCookieExpires(cookie.getMaxAge()));
                sb.append("; ");
            }
            if (null != cookie.getPath()) {
                sb.append("Path=" + cookie.getPath());
            }
        }
        return sb.toString();
    }

    private String parseCookieExpires(int maxAge) {
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)
                .format(DateUtil.offset(new Date(), DateField.MINUTE, maxAge));
    }

    private byte[] getOutputBytes() throws Exception {
        return CommonUtils.glue2bytes(getHead(), getBody());
    }

    private byte[] getHead() throws Exception{
        return StrUtil.format(StatusCodeEnum.valueOf(this.status).getHead(), getContentTypeHeader(), getCookiesHeader()).getBytes();
    }

    @Override
    public PrintWriter getWriter() { return this.writer; }

    private byte[] getBody() {
        if (null == this.body) {
            String content = stringWriter.toString();
            return content.getBytes(StandardCharsets.UTF_8);
        }
        return this.body;
    }

    @Override
    public void setStatus(int status) { this.status = status; }

    private void setStatus(StatusCodeEnum statue) {
        for (StatusCodeEnum type : StatusCodeEnum.values()) {
            if (statue.equals(type)) {
                this.status = type.getCode();
            }
        }
    }

    @Override
    public int getStatus() { return status; }

    public void servletHandle() {
        if (null != this.redirectPath)
            setStatus(STATUS_CODE_302);
        else
            setStatus(STATUS_CODE_200);
    }

    public void handleResourceFile(String realPath) {
        this.sourceFile = FileUtil.file(realPath);
        if (this.sourceFile.exists()) {
            assembleResponse();
            setStatus(STATUS_CODE_200);
        } else {
            setStatus(STATUS_CODE_404);
        }
    }

    private void assembleResponse() {
        assembleResponseHead();
        assembleResponseBody();
    }

    private void assembleResponseHead() {
        this.contentType = WebXMLUtils.getMimeType(FileUtil.extName(this.sourceFile));
    }

    private void assembleResponseBody() {
        this.body = FileUtil.readBytes(this.sourceFile);
    }

    public void handle() throws Exception {
        switch (StatusCodeEnum.valueOf(this.getStatus())) {
            case STATUS_CODE_200:
                handle200();
                break;
            case STATUS_CODE_404:
                handle404();
                break;
            case STATUS_CODE_302:
                handle302();
                break;
            default:
                throw new RuntimeException("undefined status code!");
        }
    }

    private void handle200() throws Exception {
        this.socket.getOutputStream().write(this.getOutputBytes());
    }

    private void handle404() throws IOException {
        byte[] responseHead = STATUS_CODE_404.getHead().getBytes(StandardCharsets.UTF_8);
        byte[] responseBody = StrUtil.format(STATUS_CODE_404.getContent()).getBytes(StandardCharsets.UTF_8);

        OutputStream os = this.socket.getOutputStream();
        os.write(CommonUtils.glue2bytes(responseHead, responseBody));
    }

    private void handle302() throws IOException {
        this.socket.getOutputStream().write(StrUtil.format(STATUS_CODE_302.getHead(), this.redirectPath).getBytes(StandardCharsets.UTF_8));
    }

    public void handle500(Throwable e) {
        try {
            byte[] responseHead = STATUS_CODE_500.getHead().getBytes(StandardCharsets.UTF_8);
            byte[] responseBody = StrUtil.format(Constant.TEXT_FORMAT_500,
                    CommonUtils.convertExceptionMsg(e),
                    e.toString(),
                    CommonUtils.convertStackTraceMsg(e))
                    .getBytes(StandardCharsets.UTF_8);

            OutputStream os = this.socket.getOutputStream();
            os.write(CommonUtils.glue2bytes(responseHead, responseBody));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
