package com.example.lenovo.smartcamera;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Lenovo on 2016-04-07.
 */
public class SendInfo {

    Socket sendSocket;
    final String IP = "10.2.0.159";
   // final String IP = "192.168.1.17";
    final int PORT = 8080;
    PrintWriter pw;
    public SendInfo(){

        new Thread() {
            public void run() {
                try {
                    InetAddress serverAddres = Inet4Address.getByName(IP);
                    sendSocket = new Socket();
                    sendSocket.connect(new InetSocketAddress(serverAddres, PORT), PORT);

                } catch (UnknownHostException e) {

                } catch (IOException e) {

                }
            }
        }.start();
    }

    public void sendMessage(String msg){
        if(sendSocket==null)
            return;
        try {
            PrintWriter pw = new PrintWriter(sendSocket.getOutputStream(), true);
            pw.println(msg);
        }
        catch(IOException e){};

    }
}
