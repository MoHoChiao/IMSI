package tw.moze.util.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import tw.moze.imsi.redis.RedisUtil;
import tw.moze.util.dev.XXX;


public class RedisSubscriber extends Thread {
    private final JedisPubSub handler;

    private final String channel;

    public RedisSubscriber(String channel, JedisPubSub handler) {
        super("RedisSubscriber-Thread");
        this.channel = channel;
        this.handler = handler;
    }

    @Override
    public void run() {
        XXX.out(String.format("subscribe redis, channel %s, thread will be blocked", channel));
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getResource();
            jedis.subscribe(handler, channel);
        } catch (Exception e) {
        	XXX.out(String.format("subsrcibe channel error, %s", e));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
