package client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;
    private static final int MAX_FRAME_BYTES = 65536;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT)) {
            Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            Thread receiver = new Thread(() -> {
                while (true) {
                    try {
                        String message = readFrame(in);
                        System.out.println("\nServer> " + message + "\nClient");
                    } catch (Exception e) {
                        System.out.println(e);
                        break;
                    }
                }
            }, "receiver");

            receiver.setDaemon(true);
            receiver.start();

            while (true) {
                System.out.println("Client> ");
                if (!sc.hasNextLine()) {
                    break;
                }
                String command = sc.nextLine();
                if (command.isBlank()) continue;
                sendFrame(out, command);
                if (command.equals("QUIT")) {
                    receiver.join(2000);
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static String readFrame(DataInputStream in) throws Exception {
        int length = in.readInt();
        if (length <= 0 || length > MAX_FRAME_BYTES) {
            throw new Exception("Invalid frame length: " + length);
        }
        byte[] payload = new byte[length];
        in.readFully(payload);
        return new String(payload, StandardCharsets.UTF_8);
    }

    private static void sendFrame(DataOutputStream out, String message) throws Exception {
        byte[] payload = message.getBytes();
        int length = payload.length;
        if (length <= 0 || length > MAX_FRAME_BYTES) {
            throw new Exception("Invalid message length: " + length);
        }
        out.writeInt(length);
        out.write(payload);
        out.flush();
    }

}
