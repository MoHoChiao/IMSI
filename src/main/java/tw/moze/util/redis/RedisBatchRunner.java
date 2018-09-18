//package tw.moze.util.redis;
//
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Objects;
//
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.Pipeline;
//
//public abstract class RedisBatchRunner {
//	private Jedis jedis;
//	private int batchSize;
//	private List<String[]> data;
//
//	public RedisBatchRunner(Jedis jedis, int batchSize) {
//		this.jedis = Objects.requireNonNull(jedis);
//		this.batchSize = batchSize;
//		this.data = new LinkedList<>();
//	}
//
//	public void send(String[] item) {
//		data.add(item);
//		if (data.size() == batchSize) {
//			flush();
//		}
//	}
//
//	public void flush() {
//		Pipeline pipeline = jedis.pipelined();
//		Object[] ret = new Object[data.size()];
//		int i = 0;
//		for (String[] item: data) {
//			ret[i] = invoke(pipeline, item);
//			i++;
//		}
//
//		List<Object> results = pipeline.syncAndReturnAll();		// will close pipeline
//		fillResult(ret, results);
//
//		i = 0;
//		for (String[] item: data) {
//			Object result = ret[i];
//			if (result instanceof Throwable)
//				error(item, (Throwable) result);
//			else
//				result(item, result);
//			i++;
//		}
//		data.clear();
//	}
//
//	private void fillResult(Object[] ret, List<Object> result) {
//		for(int retIdx = 0, resIdx = 0; retIdx < ret.length; retIdx++) {
//			if (ret[retIdx] == null) {
//				ret[retIdx] = result.get(resIdx++);
//			}
//		}
//	}
//
//	/**
//	 * 執行對 redis 的呼叫，若傳回的字串非 null, 代表此實作的函式沒有透過 pipeline 呼叫 redis
//	 * @param pipeline
//	 * @param vals
//	 * @return
//	 */
//	public abstract String invoke(Pipeline pipeline, String[] vals);
//	public abstract void result(String[] vals, Object ret);
//	public abstract void error(String[] vals, Throwable err);
//}
