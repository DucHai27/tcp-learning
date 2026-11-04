package client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;
    private static final int MAX_MESSAGE_BYTES = 65546;
    public static void main(String[] args) {
        try (Socket socket  = new Socket(HOST,PORT)) {
            System.out.println("Client connected to Server");

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);
            while (true) {
                System.out.println("Enter the message: ");
                if (!sc.hasNextLine()) {
                    break;
                }
                String message = sc.nextLine();
                if (message.isEmpty()) {
                    System.out.println("Not send none message");
                    continue;
                }
                sendMessage(out, message);
                
                String response = readMessage(in);
                if (response == null) {
                    System.out.println("Server closed connection");
                    break;
                }
                System.out.println("Server: " + response);
                if (message.equalsIgnoreCase("QUIT")) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        
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
        header[3] = (byte) length;

        out.write(header);
        out.write(payload);
        out.flush();
    }

    private static String readMessage(InputStream in) throws Exception {
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
        return new String(payload,StandardCharsets.UTF_8);
    }

    private static void readExactly(InputStream in, byte[] buffer, int offset, int length) throws Exception {
        int total = 0;
        while (total < length) {
            int n = in.read(buffer,offset+total,length-total);
            if (n == -1) {
                throw new Exception("Connection is closed in the middle of a frame");
            }
            total += n;
        }
    }

}
