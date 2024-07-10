package ubivelox.chat.server;

import com.sun.jdi.connect.spi.Connection;
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
    private PrintWriter out;
    private String clientName;

    public ClientHandler(Socket clientSocket, Set<ClientHandler> clientHandlers) {
        this.clientSocket = clientSocket;
        this.clientHandlers = clientHandlers;
    }

    public void handleClient() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            requestAndSetUniqueNickname(in);
            welcomeUser();
            listenForMessages(in);
        } catch (IOException e) {
            if (!e.getMessage().equals("Connection closed")) {
                System.out.println("클라이언트와의 통신 중 오류 발생: " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }

    private void requestAndSetUniqueNickname(BufferedReader in) throws IOException {
        while (true) {
            sendMessage("대화명을 입력해주세요:");
            String name = in.readLine();
            if (validateAndSetNickname(name)) break;
        }
    }

    private boolean validateAndSetNickname(String name) {
        if (name.isBlank()) {
            sendMessage("대화명은 공백일 수 없습니다.");
            return false;
        }

        if (!isNicknameUnique(name)) {
            sendMessage("중복된 대화명이 존재합니다. 대화명을 다시 입력해주세요:");
            return false;
        }

        clientName = name;
        return true;
    }

    private boolean isNicknameUnique(String name) {
        return clientHandlers.stream().noneMatch(handler -> handler.clientName.equals(name));
    }

    private void listenForMessages(BufferedReader in) throws IOException {
        String message;
        while ((message = in.readLine()) != null) {
            handleMessages(message);
        }
    }

    private void handleMessages(String message) {
        if (message.startsWith("#") && CommandType.isCommand(message)) {
            processCommand(message);
        } else if (message.startsWith("@")) {
            sendPrivateMessage(message);
        } else {
            broadcast("[" + clientName + "] " + message);
        }
    }

    private void welcomeUser() {
        sendMessage(clientName + "님 반갑습니다.");
        broadcast("[서버] " + clientName + "님이 접속하였습니다.");
        clientHandlers.add(this);
    }

    private void broadcast(String message) {
        System.out.println(message);
        clientHandlers.forEach(handler -> handler.sendMessage(message));
    }

    private void processCommand(String command) {
        switch (CommandType.of(command)) {
            case LIST -> listUsers();
            case QUIT -> disconnect();

            default -> sendMessage("유효한 명령어가 아닙니다.");
        }
    }

    private void listUsers() {
        sendMessage("현재 접속 중인 사용자:");
        clientHandlers.forEach(handler ->
                sendMessage(handler.clientName.equals(clientName) ?
                        "@" + handler.clientName + "[본인]" :
                        "@" + handler.clientName
                )
        );
    }

    private void sendPrivateMessage(String message) {
        int spaceIndex = message.indexOf(' ');
        if (spaceIndex == -1) {
            sendMessage("메세지 내용을 입력하세요.");
            return;
        }

        String targetName = message.substring(1, spaceIndex);
        String privateMessage = message.substring(spaceIndex + 1);

        boolean targetExists = clientHandlers.stream()
                .anyMatch(handler -> findByName(handler, targetName));

        if (!targetExists) {
            sendMessage("존재하지 않는 대화명입니다.");
            return;
        }

        if (targetName.equals(clientName)) {
            sendMessage("자신에게 귓속말을 보낼 수 없습니다.");
            return;
        }

        clientHandlers.stream()
                .filter(handler -> handler.clientName.equals(targetName) || handler.clientName.equals(this.clientName))
                .forEach(handler -> handler.sendMessage("[" + clientName + "] (귓속말) " + privateMessage)
                );
    }

    private boolean findByName(ClientHandler handler, String targetName) {
        return handler.clientName.equals(targetName);
    }

    public void disconnect() {
        try {
            sendMessage("연결이 종료되었습니다.");
            broadcast("[서버] " + clientName + "님이 접속 종료하였습니다.");
            clientHandlers.remove(this);
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("클라이언트 소켓 닫기 중 오류 발생: " + e.getMessage());
        }
    }

    private void sendMessage(String message) {
        out.println(message);
    }
}
