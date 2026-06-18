package ${basePackageName};


import java.util.Deque;
import java.util.concurrent.*;

/**
 * 速率限制器。
 * 限制每分钟的Request数量以适配API的要求，匀速请求防止出现Http 429问题。
 */
public class RateDispatcher {
    
    private ConcurrentLinkedDeque<CountDownLatch> latches = new ConcurrentLinkedDeque<>();
    
    private ScheduledFuture dispatchFuture;
    
    private long rate = -1;
    
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(1, (runnable) -> {
        
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName("Deamon Client rate dispatcher");
        return thread;
        
    });
    
    public void requestPerMinute(int requestCountPerSec) {
        if (requestCountPerSec < 0) {
            return;
        }
        
        long rate = (1000 * 60) / requestCountPerSec;
        if (rate != this.rate || dispatchFuture == null) {
            this.rate = rate;
            if (dispatchFuture != null) {
                dispatchFuture.cancel(true);
            }
            this.dispatchFuture = service.scheduleAtFixedRate(() -> {

                if (latches.isEmpty()) {
                    return;
                }
                latches.pop().countDown();

            }, 0, rate, TimeUnit.MILLISECONDS);
        }
        
        try {
            CountDownLatch latch = new CountDownLatch(1);
            this.latches.push(latch);
            latch.await();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
}
