package ubivelox.chat.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    CONNECT("연결 요청"),
    DISCONNECT("연결 종료"),
    CHAT("채팅"),
    COMMAND("명령");

    private final String desc;
}
