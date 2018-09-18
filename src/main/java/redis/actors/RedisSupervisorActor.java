//package redis.actors;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.stream.IntStream;
//
//import akka.actor.ActorRef;
//import akka.actor.Props;
//import akka.actor.Terminated;
//import akka.actor.UntypedActor;
//import akka.event.Logging;
//import akka.event.LoggingAdapter;
//import akka.routing.ActorRefRoutee;
//import akka.routing.RoundRobinRoutingLogic;
//import akka.routing.Routee;
//import akka.routing.Router;
//import redis.clients.jedis.JedisPool;
//import redis.config.AppConfig;
//import redis.messages.RedisActorProtocol;
//
///**
// * Created by mintik on 4/27/16.
// */
//public class RedisSupervisorActor extends UntypedActor {
//    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
//    private final AppConfig config;
//    private final int NUMBER_OF_ACTORS = 10;
//    /**
//     * Separate thread pool for RedisSubscriberActors listeners
//     */
//    private final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_ACTORS);
//    private final JedisPool jedisPool;
//    private Router publisherRouter;
//    private Router subscriberRouter;
//
//    private void initRoutees() {
//        List<Routee> subscriberRoutees = new ArrayList<>();
//        List<Routee> publisherRoutees = new ArrayList<>();
//        IntStream.range(0, NUMBER_OF_ACTORS).forEach(value -> {
//            ActorRef subscriberActor = getContext().actorOf(Props.create(RedisSubscriberActor.class, config,
//                            executorService),
//                    "RedisSubscriberActor-" + value);
//            getContext().watch(subscriberActor);
//            subscriberRoutees.add(new ActorRefRoutee(subscriberActor));
//            ActorRef publisherActor = getContext().actorOf(Props.create(RedisPublisherActor.class, config,
//                            jedisPool),
//                    "RedisPublisherActor-" + value);
//            getContext().watch(publisherActor);
//            publisherRoutees.add(new ActorRefRoutee(publisherActor));
//        });
//        publisherRouter = new Router(new RoundRobinRoutingLogic(), publisherRoutees);
//        subscriberRouter = new Router(new RoundRobinRoutingLogic(), subscriberRoutees);
//    }
//
//    public RedisSupervisorActor(AppConfig config, JedisPool jedisPool) {
//        this.config = config;
//        this.jedisPool = jedisPool;
//        initRoutees();
//    }
//
//    @Override
//    public void onReceive(Object message) throws Exception {
//        log.debug("RedisSubscriberActor: {} received message: {}", self(), message);
//
//        if (message instanceof RedisActorProtocol.DisplayMessages) {
//            subscriberRouter.route(message, getSender());
//        } else if (message instanceof RedisActorProtocol.PublishMessage) {
//            publisherRouter.route(message, getSender());
//        } else if (message instanceof Terminated) {
//            Terminated terminated = (Terminated) message;
//            ActorRef terminatedActor = terminated.actor();
//            if (terminatedActor.getClass().equals(RedisPublisherActor.class)) {
//                publisherRouter = publisherRouter.removeRoutee(terminatedActor);
//                ActorRef newPublisherActor = getContext().actorOf(Props.create(RedisPublisherActor.class, config,
//                        jedisPool), terminatedActor.path().name());
//                getContext().watch(newPublisherActor);
//                publisherRouter = publisherRouter.addRoutee(new ActorRefRoutee(newPublisherActor));
//            } else {
//                subscriberRouter = subscriberRouter.removeRoutee(terminatedActor);
//                ActorRef newSubscriberActor = getContext().actorOf(Props.create(RedisSubscriberActor.class, config,
//                        executorService), terminatedActor.path().name());
//                getContext().watch(newSubscriberActor);
//                subscriberRouter = subscriberRouter.addRoutee(new ActorRefRoutee(newSubscriberActor));
//            }
//        }
//    }
//}
