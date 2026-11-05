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

                String username = null;

                while (true) {
                    String request = readFrame(in);
                    if (request == null) {
                        System.out.println("Client closed connection");
                        break;
                    }
                    System.out.println("Request: " + request);

                    // Split command
                    int index = request.indexOf("|");
                    String command;
                    String body;

                    if (index == -1) {
                        command = request;
                        body = "";
                    } else {
                        command = request.substring(0, index);
                        body = request.substring(index + 1);
                    }

                    switch (command) {
                        case "HELLO":
                            if (username != null) {
                                sendFrame(out, "409|Already registed");
                            } else if (body.isBlank() || body.length() > 20 || body.contains("|")) {
                                sendFrame(out, "400|Invalid username");
                            } else {
                                username = body;
                                sendFrame(out, "200|OK: Welcome " + username);
                            }
                            break;

                        case "SEND_MESSAGE":
                            if (username == null) {
                                sendFrame(out, "400|Empty message");
                            } else {
                                System.out.println(username + ": " + body);
                                sendFrame(out, "200|OK received from " + username + body);
                            }
                            break;

                        case "QUIT":
                            if (!body.isEmpty()) {
                                sendFrame(out, "400|QUIT takes no payload");
                                break;
                            }
                            sendFrame(out, "200|GOOD BYE");
                            System.out.println("Client left...");
                            break;

                        // bonus
                        case "PING":
                            sendFrame(out, "200|PONG");
                            break;

                        case "WHOAMI":
                            if (username == null) {
                                sendFrame(out, "401|Not found");

                            } else {
                                sendFrame(out, "200|OK username: " + username);
                            }

                            break;

                        default:
                            sendFrame(out, "400|Unknow command");
                            break;
                    }
                }

            } catch (Exception e) {
                System.out.println(e);
            }

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e);
        }
    }

    private static String readFrame(DataInputStream dis) throws Exception {
        int length = dis.readInt();
        if (length <= 0 || length > MAX_MESSAGE_BYTES) {
            throw new Exception("Invalid frame length: " + length);
        }
        byte[] payload = new byte[length];
        dis.readFully(payload);
        return new String(payload, StandardCharsets.UTF_8);
    }

    private static void sendFrame(DataOutputStream dos, String message) throws Exception {
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        int length = payload.length;
        dos.writeInt(length);
        dos.write(payload);
        dos.flush();
    }
}