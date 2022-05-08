package cn.zhaobin.jerrymouse.test.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class QQClientA {

    public static void main(String[] args) {

        try {
            ServerSocket ss = new ServerSocket(8848);
            Socket s = ss.accept();

            new SendFunc(s).start();
            new ReceiveFunc(s).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
