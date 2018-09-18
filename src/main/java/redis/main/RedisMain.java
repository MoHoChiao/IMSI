//package redis.main;
//
//import static akka.pattern.Patterns.ask;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.concurrent.CompletionStage;
//import java.util.concurrent.TimeUnit;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.actor.Props;
//import redis.actors.RedisSupervisorActor;
//import redis.clients.jedis.JedisPool;
//import redis.clients.jedis.JedisPoolConfig;
//import redis.config.AppConfig;
//import redis.messages.RedisActorProtocol;
//import redis.services.AtomicCounter;
//import redis.services.Counter;
//import scala.compat.java8.FutureConverters;
//import scala.concurrent.duration.Duration;
//
//public class RedisMain {
//    public static final Logger logger = LoggerFactory.getLogger( RedisMain.class );
//    private final Counter counter;
//    private ActorSystem actorSystem;
//    private AppConfig configuration;
//    private JedisPool jedisPool;
//    private ActorRef redisSupervisorActor;
//
//    public RedisMain(ActorSystem actorSystem, AppConfig configuration, JedisPool jedisPool,
//                           Counter counter) {
//        this.actorSystem = actorSystem;
//        this.configuration = configuration;
//        this.jedisPool = jedisPool;
//        this.counter = counter;
//        initializeRedisSupervisorActor();
//    }
//
//    private void initializeRedisSupervisorActor() {
//        redisSupervisorActor = actorSystem.actorOf(Props.create(RedisSupervisorActor.class, configuration, jedisPool));
//    }
//
//    public CompletionStage<Void> displayMessages() {
//        logger.info("Calling RedisSupervisorActor {} with message {}", redisSupervisorActor, RedisActorProtocol.DisplayMessages.INSTANCE);
//        return FutureConverters.toJava(ask(redisSupervisorActor, RedisActorProtocol.DisplayMessages.INSTANCE, 10000))
//                .thenAccept(response -> System.out.println("displayMessages:" + response));
//    }
//
//    public CompletionStage<Void> publishMessage() {
//        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String message = "message@" + sf.format(new Date());
//        logger.info("Calling RedisPublisherActor {} with message {}", redisSupervisorActor, message);
//        return FutureConverters.toJava(ask(redisSupervisorActor, new RedisActorProtocol.PublishMessage(message), 10000))
//                .thenAccept(response -> System.out.println("publishMessage:" + response));
//    }
//
//    public void publishCounter() {
//        dispatchCounter(1, TimeUnit.SECONDS);
//    }
//
//    private String dispatchCounter(long interval, TimeUnit timeUnit) {
//        /**
//         * Make Akka scheduler to call publishMessage with counter-generated messages
//         * every 10 seconds.
//         */
//        actorSystem.scheduler().schedule(
//                Duration.create(0, timeUnit),
//                Duration.create(interval, timeUnit),
//                (Runnable) () -> redisSupervisorActor.tell(
//                        new RedisActorProtocol.PublishMessage("count" + counter.nextCount()), ActorRef.noSender()),
//                actorSystem.dispatcher()
//        );
//        return "started 1 seconds counter";
//    }
//
//    private static JedisPool getPool(String ip,int port) {
//        JedisPool pool;
//        JedisPoolConfig config = new JedisPoolConfig();
//        config.setMaxTotal(10);
//        config.setMaxIdle(3);
//        config.setMaxWaitMillis(20 * 1000);;
//        config.setTestOnBorrow(true);
//        config.setTestOnReturn(true);
//        /**
//         *如果你遇到 java.net.SocketTimeoutException: Read timed out exception的异常信息
//         *请尝试在构造JedisPool的时候设置自己的超时值. JedisPool默认的超时时间是2秒(单位毫秒)
//         */
//        pool = new JedisPool(config, ip, port, 5 * 1000);
//        return pool;
//    }
//
//    public static void main(String[] args) throws InterruptedException {
//        ActorSystem system = ActorSystem.create("RedisController");
//        AppConfig config = new AppConfig();
//        JedisPool pool = getPool("localhost", 6379);
//        Counter counter = new AtomicCounter();
//        RedisMain rc = new RedisMain(system, config, pool, counter);
//        rc.publishCounter();
//        Thread.sleep(1000);
//        rc.publishMessage();
//        Thread.sleep(1000);
//        rc.displayMessages();
//        rc.publishMessage();
//        Thread.sleep(1000);
//        rc.displayMessages();
//    }
//}