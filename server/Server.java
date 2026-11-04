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
                clientSocket.setSoTimeout(10000);

                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();

                //Đọc dữ liệu gửi đến từ Client và ghi nó vào trong RAM của ByteArrayOutPutStream
                byte[] buffer = new byte[8];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int byteRead;
                int totalByte = 0;
                
                while ((byteRead = in.read(buffer))!=-1) {
                    baos.write(buffer,0,byteRead);
                    totalByte += byteRead;
                    System.out.println("Read: " + byteRead + " byte | TotalRead: " + totalByte + " byte");
                    if (totalByte > 1024*1024) {
                        throw new Exception("Message is too large");
                    }
                }
                //Đọc dữ liệu từ RAM của ByteArrayOutputStream rồi đưa nó về dạng mảng byte thông thường để xử lý.
                byte[] data = baos.toByteArray();
                System.out.println("Size baos: " + baos.size());
                String message = new String(data, StandardCharsets.UTF_8);
                System.out.println("Received successful");
                System.out.println("Payload: " + message);

                //Gửi phản hồi về cho Client
                String response = "Received " + data.length + " byte";
                out.write(response.getBytes(StandardCharsets.UTF_8));
                out.flush();
                System.out.println("Responsed to Client");

            } catch (Exception e) {
                System.out.println(e);
            }
            serverSocket.close();
            System.out.println("Server is closed");

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e);
        }
    }
}