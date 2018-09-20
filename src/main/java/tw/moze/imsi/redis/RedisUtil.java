package tw.moze.imsi.redis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import tw.moze.core.conf.Config;
import tw.moze.util.dev.XXX;

public abstract class RedisUtil {
	private static final String host_key = "dms_redis_server_host";
	private static final String port_key = "dms_redis_server_port";

	private static List<Object> hosts = new ArrayList<Object>();
	private static List<Object> ports = new ArrayList<Object>();
	public static JedisCluster instance;

	public static synchronized void initPool() {
		if (instance != null && instance.getClusterNodes().size() > 0)
			return;
		
		List<Object> hosts = Config.getAsList(host_key);
		List<Object> ports = Config.getAsList(port_key);
		
		initPool(hosts, ports);
	}

	public static List<Object> getHosts() {
		return hosts;
	}

	public static List<Object> getPorts() {
		return ports;
	}
	public static synchronized void close() {
		if (instance != null) {
			try {
				instance.close();
				instance = null;
			} catch (IOException e) {}
		}
	}

	public static JedisCluster getResource() {
		initPool();
		return instance;
	}

	private static void initPool(List<Object> ips, List<Object> ports) {
		Set<HostAndPort> jedisClusterNode = new HashSet<HostAndPort>();
		for(int i=0; i<ips.size(); i++) {
			String ip = (String) ips.get(i);
			int port;
			try {
				port = (int) ports.get(i);
			}catch(Exception e) {
				break;
			}
			System.out.println("Connect to Redis: " + ip + ":" + port);
			jedisClusterNode.add(new HostAndPort(ip, port));
		}
		
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(Runtime.getRuntime().availableProcessors() * 4);
		config.setMaxIdle(3);
		config.setMaxWaitMillis(30 * 1000); // write timeout

		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
		
		instance = new JedisCluster(jedisClusterNode, 2000, 2000, 5, config);
	}

	public static void pipelineSync(JedisClusterPipeline pipeline, final int tryCount) {
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
			}finally {
				pipeline.close();
			}
		}
	}
}
