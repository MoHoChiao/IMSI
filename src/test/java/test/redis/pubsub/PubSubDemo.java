package test.redis.pubsub;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import tw.moze.imsi.redis.RedisUtil;
import tw.moze.util.dev.XXX;
import tw.moze.util.redis.RedisSubscriber;


public class PubSubDemo
{
    public static void main( String[] args ) throws InterruptedException
    {
    	RedisUtil.initPool();
        System.out.println(String.format("redis pool is starting, redis ip %s, redis port %d",
        		RedisUtil.getHost(), RedisUtil.getPort()));

        RedisSubscriber subThread = new RedisSubscriber("mychannel", new JedisPubSub(){
        	  public void onMessage(String channel, String message) {
        		  XXX.out("onMessage channel = " + channel + ", message = " + message);
        	  }
        });
        subThread.start();
		Jedis jedis = RedisUtil.getResource();
        jedis.publish("mychannel", "" + System.currentTimeMillis());
        Thread.sleep(1000);
        jedis.close();
    }
}
