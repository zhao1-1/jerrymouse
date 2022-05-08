package cn.zhaobin.jerrymouse.test.tcp;

import java.io.IOException;
import java.net.Socket;

public class QQClientB {

    public static void main(String[] args) {

        try {
            Socket s = new Socket("127.0.0.1", 8848);

            new SendFunc(s).start();
            new ReceiveFunc(s).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
