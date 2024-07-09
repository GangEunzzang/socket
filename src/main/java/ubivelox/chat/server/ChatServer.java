package ubivelox.chat.server;

import java.util.concurrent.RejectedExecutionException;
import ubivelox.chat.model.Port;
import ubivelox.chat.server.config.ThreadPoolConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class ChatServer {
    private final Set<ClientHandler> clientHandlers;
    private final ThreadPoolExecutor threadPool;

    public ChatServer() {
        clientHandlers = Collections.synchronizedSet(new HashSet<>());
        threadPool = ThreadPoolConfig.threadPoolExecutor();
    }

    public void start(Port port) {
        try (ServerSocket serverSocket = new ServerSocket(port.value())) {
            System.out.println("채팅 서버가 시작되었습니다. (포트: " + port.value() + ")");
            System.out.println("접속 가능 유저 수 : " + ThreadPoolConfig.CORE_POOL_SIZE + "명");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClientSocket(clientSocket);
            }
        } catch (IOException e) {
            System.out.println("서버 시작 중 오류 발생: " + e.getMessage());
        } finally {
            shutdownServer();
        }
    }

    private void handleClientSocket(Socket clientSocket) {
        ClientHandler handler = new ClientHandler(clientSocket, clientHandlers, this);
        try {
            threadPool.execute(handler::handleClient);
        } catch (RejectedExecutionException e) {
            System.out.println("자리없음");
            sendRejectMessage(clientSocket);
        }
    }

    private void sendRejectMessage(Socket clientSocket) {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            out.println("서버의 가용 가능한 스레드가 없습니다. 대기 중입니다.");
        } catch (IOException ioException) {
            System.out.println("클라이언트 거절 메시지 전송 중 오류 발생: " + ioException.getMessage());
        }
    }

    private void shutdownServer() {
        clientHandlers.forEach(ClientHandler::sendServerShutdownMessage);
        threadPool.shutdown();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("포트 번호를 입력해주세요");
        }

        Port port = Port.of(Integer.parseInt(args[0]));
        new ChatServer().start(port);
    }

    private class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (r instanceof ClientHandler.ClientHandlerRunnable) {
                Socket clientSocket = ((ClientHandler.ClientHandlerRunnable) r).getClientSocket();
                sendRejectMessage(clientSocket);
            }
        }
    }
}
