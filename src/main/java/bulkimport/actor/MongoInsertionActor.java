//package bulkimport.actor;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import org.bson.Document;
//
//import com.mongodb.MongoClient;
//import com.mongodb.bulk.BulkWriteResult;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoDatabase;
//import com.mongodb.client.model.BulkWriteOptions;
//import com.mongodb.client.model.InsertOneModel;
//import com.mongodb.client.model.WriteModel;
//
//import akka.actor.UntypedActor;
//import bulkimport.message.BatchCompleteMsg;
//import bulkimport.message.CSVRecordBatchMsg;
//import bulkimport.message.EndOfFileMsg;
//
//public class MongoInsertionActor extends UntypedActor {
//
//    private MongoClient mongoClient;
//
//    public MongoInsertionActor(MongoClient mongoClient) {
//        this.mongoClient = mongoClient;
//    }
//
//    @Override
//    public void onReceive(Object message) throws Exception {
//        if (message instanceof CSVRecordBatchMsg) {
//            @SuppressWarnings("unchecked")
//			CSVRecordBatchMsg<Document> csvRecordBatch = (CSVRecordBatchMsg<Document>) message;
//            System.out.println("InsertionActor : Batch no " + csvRecordBatch.getBatchNo() + " received ack");
//            MongoDatabase db = mongoClient.getDatabase("akka-bulkimport");
//            MongoCollection<Document> personColl = db.getCollection("persons");
//
//            List<WriteModel<Document>> wList = new LinkedList<>();
//            List<Document> persons = csvRecordBatch.getRecords();
//            for (Document personDBObject : persons) {
//                if (validate(personDBObject)) {
//                    wList.add(new InsertOneModel<>(personDBObject));
//                }
//            }
//            BulkWriteResult result = personColl.bulkWrite(wList, new BulkWriteOptions().ordered(false));
//
//            BatchCompleteMsg batchComplete = new BatchCompleteMsg(csvRecordBatch.getBatchNo(), result.getInsertedCount());
//            getSender().tell(batchComplete, getSelf());
//        } else if (message instanceof EndOfFileMsg) {
//            System.out.println("InsertionActor: EOF received");
//        } else {
//            unhandled(message);
//        }
//    }
//
//    private boolean validate(Document person) {
//        // execute validation logic
//        return true;
//    }
//}