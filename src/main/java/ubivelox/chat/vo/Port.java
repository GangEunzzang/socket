package ubivelox.chat.vo;


import lombok.Builder;

public record Port(int value) {

    @Builder
    public Port {
        validated(value);
    }

    public static Port of(int value) {
        return Port.builder()
                .value(value)
                .build();
    }

    private void validated(int port) {
        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("포트는 1024 ~ 65535 사이여야 합니다.");
        }
    }
}
