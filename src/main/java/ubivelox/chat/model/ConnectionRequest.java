package ubivelox.chat.model;

import lombok.Builder;

import java.util.Arrays;

public record ConnectionRequest(String ip, Port port) {

    @Builder
    public ConnectionRequest {
        validated(ip);
    }


    public static ConnectionRequest of(String ip, Port port) {
        return ConnectionRequest.builder()
                .ip(ip)
                .port(port)
                .build();
    }

    private void validated(String ip) {
        if (!ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            throw new IllegalArgumentException("유효한 IP 주소가 아닙니다.");
        }

        String[] octets = ip.split("\\.");
        Arrays.stream(octets)
                .mapToInt(Integer::parseInt)
                .filter(value -> value < 0 || value > 255)
                .forEachOrdered(value -> {
                    throw new IllegalArgumentException("유효한 IP 주소가 아닙니다.");
                });
    }
}
