package cn.zhaobin.jerrymouse.test.tcp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class SendFunc extends Thread {

    private Socket s;

    public SendFunc(Socket s) {
        this.s = s;
    }

    public void run() {
        try {
            OutputStream os = s.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);

            while (true) {
                Scanner sc = new Scanner(System.in);
                String msg = sc.next();
                dos.writeUTF(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
