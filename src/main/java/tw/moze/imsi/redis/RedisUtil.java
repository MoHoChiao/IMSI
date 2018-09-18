package tw.moze.imsi.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import tw.moze.core.conf.Config;
import tw.moze.util.dev.XXX;

public abstract class RedisUtil {
	private static final String host_key = "dms_redis_server_host";
	private static final String port_key = "dms_redis_server_port";

	private static String host = "localhost";
	private static Integer port = 6379;
	public static JedisPool instance;

	public static synchronized void initPool() {
		if (instance != null)
			return;
		String host = Config.get(host_key).asText();
		Integer port = Config.get(port_key).asInt();
		System.out.println("Connect to Redis: " + host + ":" + port);
		initPool(host, port);
	}

	public static String getHost() {
		return host;
	}

	public static Integer getPort() {
		return port;
	}
	public static synchronized void close() {
		if (instance != null) {
			instance.close();
			instance = null;
		}
	}

	public static Jedis getResource() {
		JedisException ex = null;
		for (int i = 0; i < 3; i++) {
			try {
				return instance.getResource();
			}
			catch (JedisException jex) {
				close();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				initPool();
				ex = jex;
			}
		}
		throw ex;
	}

	private static void initPool(String ip, int port) {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(Runtime.getRuntime().availableProcessors() * 4);
		config.setMaxIdle(3);
		config.setMaxWaitMillis(30 * 1000); // write timeout

		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);

		instance = new JedisPool(config, ip, port, 30 * 1000); // read timeout
	}

	public static void pipelineSync(Pipeline pipeline, final int tryCount) {
		for (int i = 0; i < tryCount; i++) {
			try {
				pipeline.sync();		// will close pipeline
				break;
			}
			catch(JedisConnectionException ex) {
				if (i == tryCount - 1) {
					XXX.err(ex.getMessage());
					ex.printStackTrace();
					throw ex;
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
			}
		}
	}

}
