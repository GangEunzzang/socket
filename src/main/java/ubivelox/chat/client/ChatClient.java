package ubivelox.chat.client;

import ubivelox.chat.vo.ConnectionRequest;
import ubivelox.chat.vo.Port;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {

    private PrintWriter out;
    private BufferedReader server;
    private final BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
    private Socket socket;

    public void start(ConnectionRequest connectionRequest) {
        try {
            initializeConnection(connectionRequest.ip(), connectionRequest.port().value());
            handleServerMessages();
            handleUserInput();
        } catch (IOException e) {
            System.out.println("서버에 연결 중 오류 발생: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("소켓 종료 중 오류 발생: " + e.getMessage());
                }
            }
        }
    }

    private void initializeConnection(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        server = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    private void handleServerMessages() {
        new Thread(() -> {
            try {
                String response;
                while ((response = server.readLine()) != null) {
                    System.out.println(response);
                    checkShutdownCmd(response);
                }
            } catch (IOException e) {
                checkConnectionShutdownMessage(e);
                System.out.println("서버와의 통신 중 오류 발생: " + e.getMessage());
            }
        }).start();
    }

    private void checkShutdownCmd(String response) throws IOException {
        if ("연결이 종료되었습니다.".equals(response) || "서버의 가용 가능한 스레드가 없습니다. 나중에 다시 시도해주세요".equals(response)) {
            socket.close();
            System.exit(0);
        }

    }

    private void checkConnectionShutdownMessage(IOException e) {
        if (e.getMessage().equals("Connection reset")) {
            System.out.println("서버가 종료되었습니다.");
            System.exit(0);
        }
    }

    private void handleUserInput() throws IOException {
        String message;
        while ((message = keyboard.readLine()) != null) {
            out.println(message);
        }
    }

    public static void main(String[] args) {
        if (args.length != 1 || !args[0].contains(":")) {
            System.out.println("사용법 : 서버 IP:포트 ");
            return;
        }
        String[] address = args[0].split(":");

        String ip = address[0];
        Port port = Port.of(Integer.parseInt(address[1]));

        new ChatClient().start(ConnectionRequest.of(ip, port));
    }
}
