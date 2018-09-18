package test.redis.pubsub;

import java.util.Timer;
import java.util.TimerTask;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import tw.moze.imsi.redis.RedisUtil;
import tw.moze.util.dev.XXX;
import tw.moze.util.redis.RedisSubscriber;

public class PubSubAndTimer {
	private static volatile boolean isRunning = false;
	private static volatile long prevEventTime = 0;
	private static void doMyJob(String src) {
		long now = System.currentTimeMillis();

		// if the event comes too close, just ignore it
		synchronized (PubSubAndTimer.class) {
			if (isRunning || now - prevEventTime < 0.2 * 1000L)
				return;
			isRunning = true;			
		}

		XXX.out("Running Job from " + src);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		isRunning = false;
		prevEventTime = System.currentTimeMillis();
		
	}
	
    public static void main( String[] args ) throws InterruptedException
    {
    	runTimer();
    	RedisUtil.initPool();
        System.out.println(String.format("redis pool is starting, redis ip %s, redis port %d",
        		RedisUtil.getHost(), RedisUtil.getPort()));

        RedisSubscriber subThread = new RedisSubscriber("mychannel", new JedisPubSub(){
        	  public void onMessage(String channel, String message) {
        		  doMyJob("onMessage channel = " + channel + ", message = " + message);
        	  }
        });
        subThread.start();
		Jedis jedis = RedisUtil.getResource();
		for(int i = 1; i < 20; i++) {
			jedis.publish("mychannel", "" + System.currentTimeMillis());
			Thread.sleep(1000);
		}
        Thread.sleep(1000 * 10);
        jedis.close();
    }
    
	
	public static void runTimer() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				doMyJob("Timer");
			}

		}, 2 * 1000, 1000);
	}
}
