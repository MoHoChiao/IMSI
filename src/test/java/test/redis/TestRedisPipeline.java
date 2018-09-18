package test.redis;

import java.util.LinkedList;
import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import tw.moze.util.dev.XXX;

public class TestRedisPipeline {
	public static void main(String[] args) {
		Jedis jedis = new Jedis("localhost");

		System.out.println("jedis.isConnected()=" + jedis.isConnected());

		Pipeline pipeline = jedis.pipelined();

		System.out.println("jedis.isConnected()=" + jedis.isConnected());

		long start = System.currentTimeMillis();
		List<Response<String>> res = new LinkedList<>();
		for (int i = 0; i < 10000000; i++) {
			Response<String> ret = pipeline.set("k" + i, "v" + i);
			res.add(ret);
		}
		pipeline.sync();
		System.out.println("jedis.isConnected()=" + jedis.isConnected());
		for(Response<String> ret: res) {
			String val = ret.get();
			XXX.out(val);
		}

		pipeline = jedis.pipelined();
		res = new LinkedList<>();
		for (int i = 0; i < 10000000; i++) {
			Response<String> ret = pipeline.get("k" + i);
			res.add(ret);
		}
		pipeline.sync();
		for(Response<String> ret: res) {
			String val = ret.get();
			XXX.out(val);
		}

		long end = System.currentTimeMillis();
		System.out.println("Pipelined SET: " + ((end - start) / 1000.0) + " seconds");
		jedis.close();
	}
}
