//package bulkimport.actor;
//
//import java.util.List;
//
//import org.bson.Document;
//
//import akka.actor.ActorRef;
//import akka.actor.UntypedActor;
//import bulkimport.message.BatchCompleteMsg;
//import bulkimport.message.CSVRecordBatchMsg;
//import bulkimport.message.EndOfFileMsg;
//import bulkimport.message.ResultCountMsg;
//
//public class StdoutInsertionActor extends UntypedActor {
//    private ActorRef collectionActor;
//    private int rowCount;
//
//	public StdoutInsertionActor(ActorRef collectionActor) {
//		this.collectionActor = collectionActor;
//    }
//
//    @Override
//    public void onReceive(Object message) throws Exception {
//        if (message instanceof CSVRecordBatchMsg) {
//            @SuppressWarnings("unchecked")
//			CSVRecordBatchMsg<List<String>> csvRecordBatch = (CSVRecordBatchMsg<List<String>>) message;
//            System.out.println("InsertionActor : Batch no " + csvRecordBatch.getBatchNo() + " received ack");
//
//            List<List<String>> persons = csvRecordBatch.getRecords();
//            for (List<String> person : persons) {
////                System.out.println(">>" + person.toJson());
//            }
//
//            rowCount += persons.size();
//            BatchCompleteMsg batchComplete = new BatchCompleteMsg(csvRecordBatch.getBatchNo(), persons.size());
//            getSender().tell(batchComplete, getSelf());
//        } else if (message instanceof EndOfFileMsg) {
//            System.out.println("InsertionActor: EOF received");
//            collectionActor.tell(new ResultCountMsg(rowCount), getSelf());
//        } else {
//            unhandled(message);
//        }
//    }
//
//}