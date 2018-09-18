//package redis.actors;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//
//import akka.actor.UntypedActor;
//import akka.event.Logging;
//import akka.event.LoggingAdapter;
//import redis.config.AppConfig;
//import redis.messages.RedisActorProtocol;
//import redis.services.RedisListener;
//
///**
// * Created by mintik on 4/19/16.
// */
//public class RedisSubscriberActor extends UntypedActor {
//
//    private final ExecutorService exec;
//    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
//    final private List<String> receivedMessages = new ArrayList<>();
//    private RedisListener redisListener;
//    private AppConfig config;
//
//    public RedisSubscriberActor(AppConfig config, ExecutorService exec) {
//        this.config = config;
//        this.exec = exec;
//        initListener();
//    }
//
//    @Override
//    public void preStart() throws Exception {
//        super.preStart();
//    }
//
//    private void initListener() {
//        redisListener = new RedisListener(config);
//        /**
//         * This will start the listener on another thread.
//         * The listener will forward messages received from Redis to
//         * this actor instance.
//         */
//        redisListener.setSubscriberActor(self(), exec);
//    }
//
//    @Override
//    public void onReceive(Object message) throws Exception {
//        log.debug("RedisSubscriberActor: {} received message: {}", self(), message);
//
//        if (message instanceof RedisActorProtocol.DisplayMessages) {
//            sender().tell(new RedisActorProtocol.ReceivedMessages("Messages seen so far: " +
//                            String.join(":", receivedMessages.toArray(new String[receivedMessages.size()]))),
//                    self());
//        } else if (message instanceof RedisActorProtocol.SubscribedMessage) {
//            receivedMessages.add(((RedisActorProtocol.SubscribedMessage) message).subscribedMessage);
//        }
//    }
//}