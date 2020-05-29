package com.example.ar_fps_run_mobilenet;

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MessageSender extends AsyncTask<String, Void, Void> {
    Socket s;
    DataOutputStream dos;
    PrintWriter pw;

    @Override
    protected Void doInBackground(String... voids) {
        String message = voids[0];
        System.out.println("message " + message);

        String ipAddress = "192.168.24.1";
        InetAddress geek = null;
        try {
            geek = InetAddress.getByName(ipAddress);
            System.out.println("Sending Ping Request to " + ipAddress);
            if (geek.isReachable(5000))
                System.out.println("Host is reachable");
            else
                System.out.println("Sorry ! We can't reach to this host");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            s = new Socket("192.168.24.1", 7800);
            System.out.println("set up socket");
            pw = new PrintWriter(s.getOutputStream());
            System.out.println("set up print writer");
            pw.write(message);
            System.out.println("wrote message");
            pw.flush();
            pw.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
