//package bulkimport.actor;
//
//import java.util.List;
//
//import akka.actor.ActorRef;
//import akka.actor.UntypedActor;
//import akka.routing.Broadcast;
//import bulkimport.message.BatchCompleteMsg;
//import bulkimport.message.CSVRecordBatchMsg;
//import bulkimport.message.EndOfFileMsg;
//import bulkimport.message.ResultCountMsg;
//import io.vertx.ext.jdbc.JDBCClient;
//import tw.moze.core.dao.JdbcDAO;
//
//public class PostgreInsertionActor extends UntypedActor {
//    private ActorRef collectionActor;
//    private JdbcDAO dao;
//    private String insertsql;
//
//    private int rowCount;
//
//	public PostgreInsertionActor(ActorRef collectionActor, JDBCClient jdbcClient) {
//		this.collectionActor = collectionActor;
//		this.dao = new JdbcDAO(jdbcClient);
//		this.dao.setQuote("\"");
//		this.insertsql = dao.prepareInsertSQL("person","Gender","Title","NameSet","Surname","City","StateFull","ZipCode");
//    }
//
//    @Override
//    public void onReceive(Object message) throws Exception {
//    	ActorRef sender = getSender(); // get sender outside of vertx execution block to prevent malfunction
//
//    	if (message instanceof Broadcast) {
//    		message = ((Broadcast) message).message();
//    		System.out.println("Get message from Broadcast");
//    	}
//        if (message instanceof CSVRecordBatchMsg) {
//            @SuppressWarnings("unchecked")
//			CSVRecordBatchMsg<List<Object>> csvRecordBatch =  (CSVRecordBatchMsg<List<Object>>) message;
//            System.out.println("InsertionActor : Batch no " + csvRecordBatch.getBatchNo() + " received ack");
//
//            List<List<Object>> persons = csvRecordBatch.getRecords();
//
//            dao.batchExecuteRowData(insertsql, persons).setHandler(ar->{
//            	if (ar.failed()) {
//            		System.err.println(ar.cause());
//            	}
//            	else {
//                    rowCount += persons.size();
//                    BatchCompleteMsg batchComplete = new BatchCompleteMsg(csvRecordBatch.getBatchNo(), persons.size());
//                    sender.tell(batchComplete, getSelf());
//            	}
//            });
//
//        } else if (message instanceof EndOfFileMsg) {
//            System.out.println("InsertionActor: EOF received");
//            collectionActor.tell(new ResultCountMsg(rowCount), getSelf());
//            rowCount = 0;
//        } else {
//            unhandled(message);
//        }
//    }
//
//}