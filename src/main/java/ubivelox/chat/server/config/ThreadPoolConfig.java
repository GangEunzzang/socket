package ubivelox.chat.server.config;

import java.util.concurrent.*;

public class ThreadPoolConfig {

    public static final int CORE_POOL_SIZE = 2;
    public static final int MAXIMUM_POOL_SIZE = 4;
    public static final long KEEP_ALIVE_TIME = 10L;
    public static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    public static final int CAPACITY = 2;


    public static ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TIME_UNIT,
                new LinkedBlockingQueue<>(CAPACITY)
        );
    }
}
