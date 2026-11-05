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

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

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

    private static void sendMessage(DataOutputStream dos, String message) throws Exception{
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        int length = payload.length;
        if (length <= 0 || length > MAX_MESSAGE_BYTES) {
            throw new Exception("Invalid message length: " + length);
        }
        dos.writeInt(length);
        dos.write(payload);
        dos.flush();
    }

    private static String readMessage(DataInputStream dis) throws Exception {
        int length = dis.readInt();
        if (length <= 0 || length > MAX_MESSAGE_BYTES) {
            throw new Exception("Invalid frame length: " + length);
        }
        byte[] payload = new byte[length];
        dis.readFully(payload);
        return new String(payload, StandardCharsets.UTF_8);
    }

}
