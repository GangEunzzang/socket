package ubivelox.chat.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ubivelox.chat.vo.Port;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.Mockito.*;

public class ChatServerTest {

    private ChatServer chatServer;
    private ThreadPoolExecutor threadPool;
    private Set<ClientHandler> clientHandlers;

    @BeforeEach
    void setUp() {
        clientHandlers = Collections.synchronizedSet(new HashSet<>());
        threadPool = mock(ThreadPoolExecutor.class);
        chatServer = new ChatServer(clientHandlers, threadPool);
    }

    @Test
    void testStart() throws IOException {
        ServerSocket serverSocket = mock(ServerSocket.class);
        Socket clientSocket = mock(Socket.class);

        when(serverSocket.accept()).thenReturn(clientSocket).thenThrow(IOException.class);

        chatServer.start(Port.of(8080));

        verify(serverSocket, times(1)).accept();
        verify(threadPool, times(1)).submit(any(Runnable.class));
    }

    @Test
    void testHandleClientSocket() {
        Socket clientSocket = mock(Socket.class);
        ClientHandler handler = new ClientHandler(clientSocket, clientHandlers);

        when(threadPool.getActiveCount()).thenReturn(0);
        chatServer.handleClientSocket(clientSocket);

        verify(threadPool, times(1)).submit(handler::handleClient);
    }

    @Test
    void testSendRejectMessage() throws IOException {
        Socket clientSocket = mock(Socket.class);
        PrintWriter out = mock(PrintWriter.class);

        when(clientSocket.getOutputStream()).thenReturn(mock(OutputStream.class));
        chatServer.sendRejectMessage(clientSocket);

        verify(out, times(1)).println("서버의 가용 가능한 스레드가 없습니다. 나중에 다시 시도해주세요");
        verify(clientSocket, times(1)).close();
    }
}
