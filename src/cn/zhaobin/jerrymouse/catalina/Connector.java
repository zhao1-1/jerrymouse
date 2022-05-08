package cn.zhaobin.jerrymouse.catalina;

import cn.hutool.log.LogFactory;
import cn.zhaobin.jerrymouse.http.Request;
import cn.zhaobin.jerrymouse.http.Response;
import cn.zhaobin.jerrymouse.util.ThreadPoolUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connector implements Runnable{
    private final int port;
    private final Service service;
    private ServerSocket serverSocket;

    public Connector(int port, Service service) {
        this.port = port;
        this.service = service;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Socket s =  this.serverSocket.accept();
                ThreadPoolUtils.run(() -> {
                    try {
                        Request request = new Request(s, this.service);
                        Response response = new Response();
                        HttpProcessor httpProcessor = new HttpProcessor(s, request, response);
                        httpProcessor.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (!s.isClosed()) s.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                LogFactory.get().error(e);
                e.printStackTrace();
            }
        }
    }

    public void init() {
        LogFactory.get().info("Initializing ProtocolHandler [http-bio-{}]", this.port);
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            LogFactory.get().error(e);
            e.printStackTrace();
        }
    }

    public void start() {
        LogFactory.get().info("Starting ProtocolHandler [http-bio-{}]", this.port);
        new Thread(this).start();
    }

}
