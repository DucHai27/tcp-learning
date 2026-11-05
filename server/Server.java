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
                
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

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

    private static String readMessage(DataInputStream dis) throws Exception{
        int length = dis.readInt();
        if (length <= 0 || length > MAX_MESSAGE_BYTES) {
            throw new Exception("Invalid frame length: " + length);
        }
        byte[] payload = new byte[length];
        dis.readFully(payload);
        return new String(payload, StandardCharsets.UTF_8);
    }

    private static void sendMessage(DataOutputStream dos, String message) throws Exception{
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        int length = payload.length;
        dos.writeInt(length);
        dos.write(payload);
        dos.flush();
    }
}