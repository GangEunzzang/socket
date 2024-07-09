package ubivelox.chat.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CommandType {

    LIST("#list", "조회"),
    QUIT("#quit", "종료");

    private final String command;
    private final String desc;

    public static CommandType of(String command) {
        for (CommandType type : values()) {
            if (type.command.equals(command.toLowerCase())) {
                return type;
            }
        }

        throw new IllegalArgumentException("지원하지 않는 명령어입니다.");
    }

    public static boolean isCommand(String message) {
        return Arrays.stream(CommandType.values())
                .map(CommandType::getCommand)
                .anyMatch(cmd -> cmd.equals(message));
    }

}
