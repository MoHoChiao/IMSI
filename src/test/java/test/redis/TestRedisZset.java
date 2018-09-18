package test.redis;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import tw.moze.util.dev.XXX;

public class TestRedisZset {
	public static void main(String[] args) {
		Jedis jedis = new Jedis("localhost");
		Pipeline pipeline = jedis.pipelined();
		jedis.flushAll();
		long start = System.currentTimeMillis();
		int COUNT = 1000000; 
		String KEY = "imsi";
		for (int i = 0; i < COUNT; i++) {
			pipeline.zadd(KEY, (double)i, "v" + i);
			if ((i + 1 )% 1000 == 0) {
				pipeline.sync();
				if (((i+1) * 100) % COUNT == 0) {
					System.out.println(((i+1) * 100) / COUNT + "%");
				}
				try {
					pipeline.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				pipeline = jedis.pipelined();
			}
		}
		pipeline.sync();
		long end = System.currentTimeMillis();
		System.out.println("Pipelined SET: " + ((end - start) / 1000.0) + " seconds");
		Long zcard = jedis.zcard(KEY);
		System.out.println("Count Zset = " + zcard);
		start = end;
		jedis.zremrangeByScore(KEY, 0, (double)COUNT/2);
		end = System.currentTimeMillis();
		System.out.println("Remove Range: " + ((end - start) / 1000.0) + " seconds");
		zcard = jedis.zcard(KEY);
		System.out.println("Count Zset = " + zcard);
		
		Random r = new Random(System.currentTimeMillis());
		for (int i = 0; i < 10; i ++) {
			String member = "v" + Math.abs((r.nextInt() % COUNT));
			Double d = jedis.zscore(KEY, member);
			System.out.println("member " + member + " score = " + d);
		}
		jedis.close();
	}
}
