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
            socket.setSoTimeout(10000);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            String message = "Đây là chuỗi tiếng việt nè" + " " + "".repeat(10);
            byte[] data = message.getBytes(StandardCharsets.UTF_8);

            out.write(data);
            out.flush();

            System.out.println("Client sent " + data.length + " byte");
            socket.shutdownOutput(); //Đóng chiều gửi, vẫn có thể nhận

            //Đọc phản hồi từ Server
            byte[] buffer = new byte[8];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int byteRead;
            while ((byteRead = in.read(buffer)) != -1) {
                baos.write(buffer, 0, byteRead);
            }
            String response = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            System.out.println("Server responsed: " + response);

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
