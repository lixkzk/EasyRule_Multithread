package com.xkzk.multithreadeasyrule.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@Slf4j
public class ThreadPoolConfig {

    private static int availableProcessors = Runtime.getRuntime().availableProcessors();

    @Bean(name = "ruleWorkThreadPool")
    public ThreadPoolTaskExecutor ruleWorkThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //默认最小为4核
        if(availableProcessors < 4){
            availableProcessors = 4;
        }
        //核心线程池大小
        executor.setCorePoolSize(availableProcessors * 2);
        //最大线程数
        executor.setMaxPoolSize(availableProcessors * 4);
        //队列容量
        executor.setQueueCapacity(100);
        //活跃时间
        executor.setKeepAliveSeconds(30 * 60);

        executor.setRejectedExecutionHandler(new CustomRejectedExecutionHandler());

        executor.initialize();

        return executor;
    }

    private static class CustomRejectedExecutionHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
            try {
                executor.getQueue().put(runnable);
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("CustomRejectedExecutionHandler error !",e);
            }
        }
    }

}
