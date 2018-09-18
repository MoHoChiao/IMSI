//package redis.services;
//
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import akka.actor.ActorRef;
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.JedisPubSub;
//import redis.config.AppConfig;
//import redis.messages.RedisActorProtocol;
//
///**
// * This class is a Redis subscriber that also holds an ActorRef.
// * Each message received from Redis channel will be forwarded to
// * the corresponding Actor.
// *
// * Created by mintik on 4/20/16.
// */
//public class RedisListener extends JedisPubSub {
//    public static final Logger logger = LoggerFactory.getLogger( RedisListener.class );
//    private ActorRef subscriberActor = ActorRef.noSender();
//    private Jedis jedis;
//
//    private AppConfig config;
//
//    public RedisListener(AppConfig config) {
//        this.config = config;
//        initFields();
//    }
//
//    private void initFields() {
//        jedis = new Jedis(config.REDIS_HOST);
//    }
//
//    public void setSubscriberActor(ActorRef subscriberActor, ExecutorService exec) {
//        this.subscriberActor = subscriberActor;
//        /**
//         * Start listening for the messages from channel on separate thread pool
//         */
//        CompletableFuture.runAsync(() -> {
//                    jedis.subscribe(this, config.REDIS_CHANNEL);
//                    jedis.quit();
//                },
//                exec);
//    }
//
//    @Override
//    public void onMessage(String channel, String message) {
//        logger.debug("RedisListener onMessage: channel = {}, message = {}", channel, message);
//        subscriberActor.tell(new RedisActorProtocol.SubscribedMessage(message, channel), ActorRef.noSender());
//    }
//
//    @Override
//    public void onPMessage(String pattern, String channel, String message) {
//        logger.debug("RedisListener onPMessage: pattern = {}, channel = {}, message = {}", pattern, channel, message);
//        subscriberActor.tell(new RedisActorProtocol.SubscribedMessage(message, channel, pattern), ActorRef.noSender());
//    }
//
//    @Override
//    public void onSubscribe(String channel, int subscribedChannels) {
//        logger.debug("RedisListener onSubscribe: channel = {}, subscribedChannels = {}", channel, subscribedChannels);
//    }
//
//    @Override
//    public void onUnsubscribe(String channel, int subscribedChannels) {
//        logger.debug("RedisListener onUnsubscribe: channel = {}, subscribedChannels = {}", channel, subscribedChannels);
//    }
//
//    @Override
//    public void onPUnsubscribe(String pattern, int subscribedChannels) {
//        logger.debug("RedisListener onPUnsubscribe: pattern = {}, subscribedChannels = {}", pattern, subscribedChannels);
//    }
//
//    @Override
//    public void onPSubscribe(String pattern, int subscribedChannels) {
//        logger.debug("RedisListener onPSubscribe: pattern = {}, subscribedChannels = {}", pattern, subscribedChannels);
//    }
//}