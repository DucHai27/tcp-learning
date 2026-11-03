package server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {

            System.out.println("Server is running on port 5000");

            try (Socket clientSocket = serverSocket.accept()) {
                System.out.println("Client is connected");

                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();

                byte[] buffer = new byte[1024];
                int len = in.read(buffer);
                if (len != -1) {
                    String message = new String(buffer, 0, len, StandardCharsets.UTF_8);
                    System.out.println("Client send: " + message);

                    String response = "Server received: " + message;
                    byte[] responseByte = response.getBytes(StandardCharsets.UTF_8);

                    out.write(responseByte);
                    out.flush();
                }
            }

            serverSocket.close();
            System.out.println("Server is closed");

        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}