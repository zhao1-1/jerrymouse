package cn.zhaobin.jerrymouse.http;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.zhaobin.jerrymouse.util.CommonUtils;
import cn.zhaobin.jerrymouse.util.Constant;
import cn.zhaobin.jerrymouse.util.StatusCodeEnum;
import cn.zhaobin.jerrymouse.util.parsexml.WebXMLUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class Response extends BaseResponse{

    private StringWriter stringWriter;
    private PrintWriter writer;
    private File sourceFile;
    private byte[] body;
    private String contentType;
    private int status;

    public Response(){
        this.stringWriter = new StringWriter();
        this.writer = new PrintWriter(stringWriter);
        this.contentType = "text/html";
    }

    @Override
    public String getContentType() { return this.contentType; }

    public byte[] getOutputBytes() throws Exception {
        return CommonUtils.glue2bytes(getHead(), getBody());
    }

    private byte[] getHead() throws Exception{
        return StrUtil.format(StatusCodeEnum.valueOf(this.status).getHead(), this.contentType).getBytes();
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
