//package bulkimport.main;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.actor.Props;
//import akka.routing.RoundRobinPool;
//import bulkimport.actor.FileReaderActor;
//import bulkimport.actor.PostgreInsertionActor;
//import bulkimport.actor.ResultCollectionActor;
//import bulkimport.actor.StdoutInsertionActor;
//import io.vertx.core.Vertx;
//import io.vertx.core.json.JsonObject;
//import io.vertx.ext.jdbc.JDBCClient;
//
//public class Application2 {
//
//    static final int INSERTION_ACTOR_POOL_SIZE = 2;
//    static final int BATCH_SIZE = 100;
//
//    public static void main(String[] args) throws Exception {
//
//    	Vertx vertx = Vertx.vertx();
//        final ActorSystem system = ActorSystem.create("FileImportApp");
//
////        MongoClient mongoClient = new MongoClient("localhost");
////        final ActorRef mongoInsertionActor = system.actorOf(Props.create(MongoInsertionActor.class, mongoClient)
////                .withRouter(new RoundRobinPool(INSERTION_ACTOR_POOL_SIZE)));
//
//        final ActorRef collectionActor = system
//        		.actorOf(Props.create(ResultCollectionActor.class, INSERTION_ACTOR_POOL_SIZE), "coll");
//
//        final ActorRef insertionActor = system
//        		.actorOf(Props.create(StdoutInsertionActor.class, collectionActor)
//                .withRouter(new RoundRobinPool(INSERTION_ACTOR_POOL_SIZE)), "stdout");
//
//        final ActorRef fileReaderActor = system
//                .actorOf(Props.create(FileReaderActor.class, insertionActor, BATCH_SIZE), "file");
//
//        String path = "data/NameList.csv";
//        fileReaderActor.tell(path, ActorRef.noSender());
//        System.out.println();
//        Thread.sleep(5000);
//
//        system.terminate();
//        vertx.close();
//    }
//
//	public static JDBCClient getJdbcClient(Vertx vertx) {
//		final JDBCClient client = JDBCClient.createShared(vertx,
//				new JsonObject().put("url", "jdbc:postgresql://127.0.0.1/imsi")
//						.put("driver_class", "org.postgresql.Driver")
//						.put("user", "moze")
//						.put("password", "edpass")
//						.put("max_pool_size", 30)
//			            .put("min_pool_size", 10)               // 加上這兩個參數，讓超過 3 分鐘沒用的 connection 自動釋放
//			            .put("max_idle_time", 180),
//				"imsi");
//		return client;
//	}
//}