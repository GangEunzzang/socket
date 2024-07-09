package ubivelox.chat.server;

import lombok.Getter;
import ubivelox.chat.common.CommandType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

public class ClientHandler {

    @Getter
    private final Socket clientSocket;
    private final Set<ClientHandler> clientHandlers;
    private final ChatServer chatServer;
    private PrintWriter out;
    private String clientName;

    public ClientHandler(Socket clientSocket, Set<ClientHandler> clientHandlers, ChatServer chatServer) {
        this.clientSocket = clientSocket;
        this.clientHandlers = clientHandlers;
        this.chatServer = chatServer;
    }

    public void handleClient() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            promptForUniqueNickname(in);
            out.println(clientName + "님 반갑습니다.");
            broadcast("[서버] " + clientName + "님이 접속하였습니다.");
            clientHandlers.add(this);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("#") && CommandType.isCommand(message)){
                    processCommand(message);
                } else if (message.startsWith("@")) {
                    sendPrivateMessage(message);
                } else {
                    broadcast("[" + clientName + "] " + message);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            disconnect();
        }
    }

    private void promptForUniqueNickname(BufferedReader in) throws IOException {
        while (true) {
            out.println("대화명을 입력해주세요:");
            String tmpClientName = in.readLine();

            boolean isUniqueNickName = clientHandlers.stream()
                    .noneMatch(handler -> tmpClientName.equals(handler.clientName));

            if (isUniqueNickName) {
                clientName = tmpClientName;
                break;
            }

            out.println("중복된 대화명이 존재합니다. 대화명을 다시 입력해주세요:");
        }
    }

    private void broadcast(String message) {
        System.out.println(message);
        clientHandlers.forEach(handler -> handler.out.println(message));
    }

    private void processCommand(String command) {
        switch (CommandType.of(command)) {
            case LIST -> listUsers();
            case QUIT -> disconnect();

            default -> out.println("유효한 명령어가 아닙니다.");
        }
    }

    private void listUsers() {
        out.println("현재 접속 중인 사용자:");
        clientHandlers.forEach(handler -> out.println("@" + handler.clientName));
    }

    private void sendPrivateMessage(String message) {
        int spaceIndex = message.indexOf(' ');
        if (spaceIndex == -1) {
            out.println("메세지 내용을 입력하세요.");
            return;
        }

        String targetName = message.substring(1, spaceIndex);
        String privateMessage = message.substring(spaceIndex + 1);

        clientHandlers.stream()
                .filter(handler -> handler.clientName.equals(targetName))
                .findFirst()
                .ifPresentOrElse(
                        handler -> handler.out.println("[" + clientName + "] (귓속말) " + privateMessage),
                        () -> out.println("존재하지 않는 대화명입니다.")
                );
    }

    private void disconnect() {
        try {
            out.println("연결이 종료되었습니다.");
            clientHandlers.remove(this);
            broadcast("[서버] " + clientName + "님이 접속 종료하였습니다.");
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("클라이언트 소켓 닫기 중 오류 발생: " + e.getMessage());
        }
    }

    public void sendServerShutdownMessage() {
        out.println("서버가 종료되었습니다.");
    }

    public static class ClientHandlerRunnable implements Runnable {
        private final ClientHandler handler;

        public ClientHandlerRunnable(ClientHandler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.handleClient();
        }

        public Socket getClientSocket() {
            return handler.getClientSocket();
        }
    }
}
