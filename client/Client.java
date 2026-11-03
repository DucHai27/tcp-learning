package client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;
        try (Socket socket = new Socket(host,port)) {    
            System.out.println("Connected to Server");

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            String message = "Đây là chuỗi tiếng việt nè";
            byte[] data = message.getBytes(StandardCharsets.UTF_8);

            out.write(data);
            out.flush();

            byte[] buffer = new byte[1024];
            int len = in.read(buffer);
            if (len != -1) {
                String response = new String(buffer,0,len,StandardCharsets.UTF_8);
                System.out.println("Server responsed: " + response);
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
