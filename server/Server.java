package server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Server {

    private static int PORT = 5000;
    private static int MAX_MESSAGE_BYTES = 65536;
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Server is listening on port " + PORT);
            try (Socket socket = serverSocket.accept()) {
                System.out.println("Client connected to Server successful");
                
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                while (true) {
                    String message = readMessage(in);
                    if (message == null) {
                        System.out.println("Client closed connection");
                        break;
                    }
                    System.out.println("Client: " + message);

                    if (message.equalsIgnoreCase("QUIT")) {
                        sendMessage(out,"BYE BYE");
                        break;
                    }
                    sendMessage(out, "ACK: " + message);
                }

            } catch (Exception e) {
                System.out.println(e);
            }
            
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e);
        }
    }

    private static void readExactly(InputStream in, byte[] buffer, int offset, int length) throws Exception {
        int total = 0;
        while (total < length) {
            int n = in.read(buffer, offset+total, length-total);
            if (n == -1) throw new Exception("Connection is closed in the middle of a frame");
            total += n; 
        }
    }

    private static String readMessage(InputStream in) throws Exception{
        byte[] header = new byte[4];
        int n = in.read(header);
        if (n == -1) return null;
        readExactly(in, header, n, 4-n);
        int length = ((header[0] & 0xFF) << 24) | ((header[1] & 0xFF) << 16) | ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
        if (length <= 0 || length > MAX_MESSAGE_BYTES) {
            throw new Exception("Invalid frame length: " + length);
        }
        byte[] payload = new byte[length];
        readExactly(in, payload, 0, length);
        return new String(payload, StandardCharsets.UTF_8);
    }

    private static void sendMessage(OutputStream out, String message) throws Exception{
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        int length = payload.length;
        if (length == 0 || length > MAX_MESSAGE_BYTES) {
            throw new Exception("Invalid message length");
        }
        byte[] header = new byte[4];
        header[0] = (byte) (length >> 24);
        header[1] = (byte) (length >> 16);
        header[2] = (byte) (length >> 8);
        header[3] = (byte) (length);

        out.write(header);
        out.write(payload);
        out.flush();   
    }
}