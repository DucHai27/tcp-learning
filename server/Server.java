package server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.*;

public class Server {
    private static final int PORT = 5000;
    private static final int MAX_FRAME_BYTES = 65536;
    private static final int MAX_CLIENTS = 8;

    private static final ConcurrentHashMap<String, ClientHandler> online = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);
        Semaphore slots = new Semaphore(MAX_CLIENTS);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port: " + PORT);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    if (!slots.tryAcquire()) {
                        System.out.println("Server is full; rejecting a connection");
                        socket.close();
                        continue;
                    }
                    try {
                        socket.setSoTimeout(120000);
                        ClientHandler handler = new ClientHandler(socket);
                        pool.execute(()->{
                            try {
                                handler.run();
                            } finally {
                                slots.release();
                            }
                        });
                    } catch (Exception e) {
                        System.out.println(e);
                        slots.release();
                        socket.close();
                    }


                } catch (Exception e) {
                    System.out.println(e);
                }
            }


        } catch (Exception e) {
            System.out.println(e);
        } finally {
            pool.shutdownNow();
        }
    }

    public static class ClientHandler implements Runnable {
        private final Socket socket;
        private final DataInputStream in;
        private final DataOutputStream out;

        private final Object sendLock = new Object();
        private String username;


        ClientHandler(Socket socket) throws Exception {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        }

        @Override 
        public void run() {
            try (socket) {
                System.out.println("Connected: " + socket.getRemoteSocketAddress());
                while (true) {
                    String request = readFrame(in);
                    int index = request.indexOf("|");
                    String command = (index == -1) ? request : request.substring(0,index);
                    String body = (index == - 1) ? "" : request.substring(index+1);
                    switch (command) {
                        case "HELLO":
                            if (username != null) {
                                send("409|Already registed");
                            } else if (body.isBlank() || body.length() > 20 || body.contains("|")) {
                                send("400|Invalid username");
                            } else if (online.putIfAbsent(body, this) != null) {
                                send("409|Username already in use");
                            } else {
                                username = body;
                                send("200|OK: WELCOME " + username);
                                System.out.println(username + " JOINED");
                            }
                            break;
                        
                        case "WHOAMI":
                            if (username == null) {
                                send("401|Please HELLO first");
                            } else {
                                send("200|" + username);
                            }
                            break;
                        
                        case "SEND_MESSAGE":
                            if (username == null) {
                                send("401|Please HELLO first");
                            } else if (body.isBlank()) {
                                send("400|Empty message");
                            } else {
                                send("200|OK Message accepted");
                                broadcast("EVENT" + username + "|" + body);
                            }
                        case "LIST_USERS":
                            if (username == null) {
                                send("401|Please HELLO first");
                            } else {
                                ArrayList<String> users = new ArrayList<>(online.keySet());
                                Collections.sort(users);
                                send("200|OK online: " + String.join(", ", users));
                            }

                            break;
                        
                        case "QUIT":
                            if (!body.isEmpty()) {
                                send("400|QUIT take no payload");
                                break;
                            }
                            send("200|OK: GOODBYE");
                            return;

                        default:
                            send("400|Unknow command");
                            break;
                    } 
                }
            } catch (Exception e) {
               System.out.println(e);
            } finally {
                if (username != null) {
                    online.remove(username,this);
                    System.out.println(username + " left...");
                }
            }
        }

        public void send(String message) throws Exception {
            synchronized (sendLock) {
                sendFrame(out, message);
            }
        }

        public void broadcast(String event) {
            for (ClientHandler receiver : online.values()) {
                if (receiver == this) continue;
                try {
                    receiver.send(event);
                } catch (Exception e) {
                    // TODO: handle exception
                    System.out.println("Broadcast failed: " + e.getMessage());
                    try {
                        receiver.socket.close();
                    } catch (Exception ignored) {
                        // TODO: handle exception
                    }
                }
            }
        }

        public String readFrame(DataInputStream in) throws Exception{
            int length = in.readInt();
            if (length <= 0 || length > MAX_FRAME_BYTES) {
                throw new Exception("Invalid frame length: " + length);
            }
            byte[] payload = new byte[length];
            in.readFully(payload);
            return new String(payload, StandardCharsets.UTF_8);
        }

        public void sendFrame(DataOutputStream out, String message) throws Exception {
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
}