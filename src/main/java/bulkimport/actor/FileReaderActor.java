//package bulkimport.actor;
//
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//
//import akka.actor.ActorRef;
//import akka.actor.UntypedActor;
//import akka.routing.Broadcast;
//import au.com.bytecode.opencsv.CSVReader;
//import bulkimport.message.BatchCompleteMsg;
//import bulkimport.message.CSVRecordBatchMsg;
//import bulkimport.message.EndOfFileMsg;
//
//public class FileReaderActor extends UntypedActor {
//    private ActorRef insertionActor;
//    private int batchSize;
//    private int batchSentCounter = 0;
//    private int batchCompleteCounter = 0;
//
//    public FileReaderActor() {
//    }
//
//    public FileReaderActor(ActorRef insertionActor, int batchSize) {
//        this.insertionActor = insertionActor;
//        this.batchSize = batchSize;
//    }
//
//    @Override
//    public void onReceive(Object message) throws Exception {
//
//        if (message instanceof String) {
//            readAndInsertCSV((String) message);
//        }
//        else if(message instanceof BatchCompleteMsg){
//            batchCompleteCounter++;
//            BatchCompleteMsg batchComplete = (BatchCompleteMsg)message;
//            System.out.println("Reader: Batch no "+batchComplete.getBatchNo()+"complete ack ["+batchSentCounter+":"+batchCompleteCounter+"]");
//            if(batchSentCounter == batchCompleteCounter){
//                System.out.println("All batches completed successfully !!");
//                getContext().stop(getSelf());
//            }
//        }
//        else {
//            unhandled(message);
//        }
//    }
//    private void readAndInsertCSV(String filePath) {
//    	 CSVReader reader = null;
//
//         try {
//             reader = new CSVReader(new FileReader(filePath));
//
//             CSVRecordBatchMsg<List<String>> csvRecordBatch = new CSVRecordBatchMsg<>();
//             int i = 0;
//
//             String [] nextLine;
//             // skip header
//             nextLine = reader.readNext();
//
//             while ((nextLine = reader.readNext()) != null) {
//            	 List<String> row = Arrays.asList(nextLine);
//                 csvRecordBatch.add(row);
//                 if(i%batchSize == 0){
//                     batchSentCounter++;
//                     csvRecordBatch.setBatchNo(batchSentCounter);
//                     insertionActor.tell(csvRecordBatch, getSelf());
//                     csvRecordBatch = new CSVRecordBatchMsg<>();
//                 }
//                 i++;
//             }
//
//             // Last batch maybe pending if there are less than batch size left over records. Sending last batch of such records explicitly
//             if(csvRecordBatch.size() != 0){
//                 batchSentCounter++;
//                 csvRecordBatch.setBatchNo(batchSentCounter);
//                 insertionActor.tell(csvRecordBatch, getSelf());
//             }
//             insertionActor.tell(new Broadcast(new EndOfFileMsg()), getSelf());
//             System.out.println("FileReaderActor: EOF sent");
//
//         } catch (Exception e) {
//             System.out.println("Error in CsvFileReader !!!" + e.getMessage());
//         } finally {
//             try {
//                 reader.close();
//             } catch (IOException e) {
//                 ;
//             }
//         }
//
//    }
//
//}