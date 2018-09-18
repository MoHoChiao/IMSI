package redis.services;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCounter implements Counter {

    private final AtomicInteger atomicCounter = new AtomicInteger();

    @Override
    public int nextCount() {
       return atomicCounter.getAndIncrement();
    }

}