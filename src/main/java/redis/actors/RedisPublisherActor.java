//package redis.actors;
//
//import akka.actor.UntypedActor;
//import akka.event.Logging;
//import akka.event.LoggingAdapter;
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.JedisPool;
//import redis.config.AppConfig;
//import redis.messages.RedisActorProtocol;
//
///**
// * Created by mintik on 4/19/16.
// */
//public class RedisPublisherActor extends UntypedActor {
//
//    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
//    private JedisPool jedisPool;
//    private Jedis jedis;
//    private AppConfig config;
//
//    public RedisPublisherActor(AppConfig config, JedisPool jedisPool) {
//        this.config = config;
//        this.jedisPool = jedisPool;
//    }
//
//    @Override
//    public void onReceive(Object message) throws Exception {
//        log.debug("RedisPublisherActor: {} received message: {}", self(), message);
//
//        if (message instanceof RedisActorProtocol.PublishMessage) {
//            String publishMessage = ((RedisActorProtocol.PublishMessage) message).publishMessage;
//            jedis = jedisPool.getResource();
//            jedis.publish(config.REDIS_CHANNEL, publishMessage);
//            jedis.close();
////            sender().tell(RedisActorProtocol.PublishAcknowledged.INSTANCE, self());
//        }
//    }
//}