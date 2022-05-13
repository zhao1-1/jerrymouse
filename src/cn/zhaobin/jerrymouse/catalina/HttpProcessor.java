package cn.zhaobin.jerrymouse.catalina;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.zhaobin.jerrymouse.http.Request;
import cn.zhaobin.jerrymouse.http.Response;
import cn.zhaobin.jerrymouse.servlet.DefaultServlet;
import cn.zhaobin.jerrymouse.servlet.InvokeServlet;
import cn.zhaobin.jerrymouse.util.CommonUtils;
import cn.zhaobin.jerrymouse.util.Constant;
import cn.zhaobin.jerrymouse.util.SessionManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HttpProcessor {

    private final Socket socket;
    private final Request request;
    private final Response response;

    public HttpProcessor(Socket socket, Request request, Response response) {
        this.socket = socket;
        this.request = request;
        this.response = response;
    }

    public void execute() {
        try {
            prepareSession(this.request, this.response);
            executeServlet();
            handle();
        } catch (Exception e) {
            LogFactory.get().error(e);
            handle500(e);
        }
    }

    private void executeServlet() throws Exception{
        if (request.getContext().servletClassValid(request.getUri()))
            InvokeServlet.getInstance().service(this.request, this.response);
        else
            DefaultServlet.getInstance().service(this.request, this.response);
    }

    public void prepareSession(Request request, Response response) {
        request.setSession(SessionManager.getSession(request.getJSessionIdFromCookie(), request, response));
    }

    private void handle() throws Exception {
        switch (this.response.getStatus()) {
        case Constant.CODE_200:
            handle200();
            break;
        case Constant.CODE_404:
            handle404();
            break;
        case 302:
            handle302();
            break;
        default:
            throw new RuntimeException("undefined status code!");
        }
    }

    private void handle200() throws Exception {
        this.socket.getOutputStream().write(response.getOutputBytes());
    }

    private void handle404() throws IOException {
        String uri = this.request.getUri();

        byte[] responseHead = Constant.RESPONSE_HEAD_404.getBytes(StandardCharsets.UTF_8);
        byte[] responseBody = StrUtil.format(Constant.TEXT_FORMAT_404, uri, uri).getBytes(StandardCharsets.UTF_8);

        OutputStream os = this.socket.getOutputStream();
        os.write(CommonUtils.glue2bytes(responseHead, responseBody));
    }

    private void handle500(Throwable e) {
        try {
            byte[] responseHead = Constant.RESPONSE_HEAD_500.getBytes(StandardCharsets.UTF_8);
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

    private void handle302() throws IOException {
        this.socket.getOutputStream().write(StrUtil.format(Constant.RESPONSE_HEAD_302, this.response.getRedirectPath()).getBytes(StandardCharsets.UTF_8));
    }

}
