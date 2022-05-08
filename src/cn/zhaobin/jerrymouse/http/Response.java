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
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Response extends BaseResponse{

    private StringWriter stringWriter;
    private PrintWriter writer;
    private File sourceFile;
    private byte[] body;
    private String contentType;
    private int status;

    private List<Cookie> cookies;

    public Response(){
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

    public byte[] getOutputBytes() throws Exception {
        return CommonUtils.glue2bytes(getHead(), getBody());
    }

    private byte[] getHead() throws Exception{
        return StrUtil.format(StatusCodeEnum.valueOf(this.status).getHead(), getContentTypeHeader(), getCookiesHeader()).getBytes();
    }

    @Override
    public PrintWriter getWriter() { return this.writer; }

    public byte[] getBody() {
        if (null == this.body) {
            String content = stringWriter.toString();
            return content.getBytes(StandardCharsets.UTF_8);
        }
        return this.body;
    }

    private void setBody(byte[] body) { this.body = body; }

    @Override
    public void setStatus(int status) { this.status = status; }

    @Override
    public int getStatus() { return status; }

    public void handleResourceFile(String realPath) {
        this.sourceFile = FileUtil.file(realPath);
        if (this.sourceFile.exists()) {
            assembleResponse();
            setStatus(Constant.CODE_200);
        } else {
            setStatus(Constant.CODE_404);
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
        this.setBody(FileUtil.readBytes(this.sourceFile));
    }

}
