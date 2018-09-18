//package bulkimport.actor;
//
//import akka.actor.UntypedActor;
//import bulkimport.message.ResultCountMsg;
//
//public class ResultCollectionActor extends UntypedActor {
//	private final int workerCount;
//	private int finishedWorkerCount = 0;
//	private int resultCount;
//    public ResultCollectionActor(int workerCount) {
//    	this.workerCount = workerCount;
//    }
//
//    @Override
//    public void onReceive(Object message) throws Exception {
//        if (message instanceof ResultCountMsg) {
//            System.out.println("ResultCollectionActor: ResultCountMsg received");
//            resultCount += ((ResultCountMsg) message).getResultCount();
//            System.out.println("finishedWorkerCount = " + finishedWorkerCount  + ", workerCount = " + workerCount);
//            if (++finishedWorkerCount == workerCount) {
//            	System.out.println("All done!");
//            	System.out.println("Total count = " + resultCount);
//            }
//        } else {
//            unhandled(message);
//        }
//    }
//
//}