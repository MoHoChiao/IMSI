package tw.moze.util.redis;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import tw.moze.imsi.redis.RedisUtil;

public abstract class RedisBatchRunner2<T> {
	private Jedis jedis;
	private int batchSize;
	private List<String[]> data;

	public RedisBatchRunner2(Jedis jedis, int batchSize) {
		this.jedis = Objects.requireNonNull(jedis);
		this.batchSize = batchSize;
		this.data = new LinkedList<>();
	}

	public void send(String[] item) {
		data.add(item);
		if (data.size() == batchSize) {
			flush();
		}
	}

	public void flush() {
		if (data.isEmpty())
			return;
		Pipeline pipeline = jedis.pipelined();
		List<T> results = new LinkedList<>();
		for (String[] item: data) {
			invoke(pipeline, item, results);
		}

		RedisUtil.pipelineSync(pipeline, 5);

		for (T result: results) {
			result(result);
		}
		data.clear();
	}

	/**
	 * 執行對 redis 的呼叫
	 * @param pipeline
	 * @param vals
	 * @return
	 */
	public abstract void invoke(Pipeline pipeline, String[] vals, List<T> results);
	public abstract void result(T result);
}
