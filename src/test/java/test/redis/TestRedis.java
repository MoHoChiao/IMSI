package test.redis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import tw.moze.util.string.StringUtil;

public class TestRedis {
//	@Test
	public void testPerformance() {
		Jedis jedis = new Jedis("localhost");
		long start = System.currentTimeMillis();
		Random r = new Random(start);
		for (int i = 0; i < 300000; i++) {
			String val = StringUtil.getRandomString(r, 100);
			jedis.setex("n" + i, 100000, val);
//			System.out.println("result = " + result);
			if ((i+1)% 10000 == 0) {
				System.out.print(".");
			}
		}
		long end = System.currentTimeMillis();
		 System.out.println("\nSimple SET: " + ((end - start)/1000.0) + " seconds");
		jedis.close();
	}

	// @Test
	public void test1Normal() {
		Jedis jedis = new Jedis("localhost");
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			String result = jedis.set("n" + i, "n" + i);
			System.out.println("result = " + result);
		}
		long end = System.currentTimeMillis();
		System.out.println("Simple SET: " + ((end - start)/1000.0) + " seconds");
		jedis.close();
	}

	// @Test
	public void test2Normal() {
		Jedis jedis = new Jedis("localhost");
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			String result = jedis.get("n" + i);
			System.out.println("result = " + result);
		}
		long end = System.currentTimeMillis();
		System.out.println("Simple SET: " + ((end - start)/1000.0) + " seconds");
		jedis.close();
	}

	// @Test
	public void test2Trans() {
		Jedis jedis = new Jedis("localhost");
		long start = System.currentTimeMillis();
		Transaction tx = jedis.multi();
		for (int i = 0; i < 10; i++) {
			tx.set("t" + i, "t" + i);
		}
		List<Object> results = tx.exec();
		long end = System.currentTimeMillis();
		System.out.println(results);
		System.out.println("Transaction SET: " + ((end - start) / 1000.0) + " seconds");
		jedis.close();
	}

	/**
	 * asynchronous invoke
	 */
//	 @Test
	public void test3Pipelined() {
		Jedis jedis = new Jedis("localhost");
		Pipeline pipeline = jedis.pipelined();
		pipeline.multi();
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			pipeline.set("p" + i, "p" + i);
		}
		pipeline.exec();
		long end = System.currentTimeMillis();
		System.out.println("Pipelined SET: " + ((end - start) / 1000.0) + " seconds");
		jedis.close();
	}

	/**
	 * asynchronous invoke
	 */
//	 @Test
	public void test3_1Pipelined() {
		Jedis jedis = new Jedis("localhost");
		Pipeline pipeline = jedis.pipelined();
		long start = System.currentTimeMillis();
		
		for (int i = 0; i < 10; i++) {
			pipeline.set("p" + i, "p" + i);
		}
		List<Object> results = pipeline.syncAndReturnAll();
		long end = System.currentTimeMillis();
		System.out.println("Pipelined SET: " + ((end - start) / 1000.0) + " seconds");
		jedis.close();
	}

	// @Test
	public void test4combPipelineTrans() {
		Jedis jedis = new Jedis("localhost");
		long start = System.currentTimeMillis();
		Pipeline pipeline = jedis.pipelined();
		pipeline.multi();
		for (int i = 0; i < 100000; i++) {
			pipeline.set("" + i, "" + i);
		}
		pipeline.exec();
		List<Object> results = pipeline.syncAndReturnAll();
		long end = System.currentTimeMillis();
		System.out.println("Pipelined transaction: " + ((end - start) / 1000.0) + " seconds");
		jedis.close();
	}

	// @Test
	public void test5shardNormal() {
		List<JedisShardInfo> shards = Arrays.asList(new JedisShardInfo("localhost", 6379),
				new JedisShardInfo("localhost", 6380));

		ShardedJedis sharding = new ShardedJedis(shards);

		long start = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			String result = sharding.set("sn" + i, "n" + i);
		}
		long end = System.currentTimeMillis();
		System.out.println("Simple@Sharing SET: " + ((end - start) / 1000.0) + " seconds");

		sharding.close();
	}

	// @Test
	public void test6shardpipelined() {
		List<JedisShardInfo> shards = Arrays.asList(new JedisShardInfo("localhost", 6379),
				new JedisShardInfo("localhost", 6380));

		ShardedJedis sharding = new ShardedJedis(shards);

		ShardedJedisPipeline pipeline = sharding.pipelined();
		long start = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			pipeline.set("sp" + i, "p" + i);
		}
		List<Object> results = pipeline.syncAndReturnAll();
		long end = System.currentTimeMillis();
		System.out.println("Pipelined@Sharing SET: " + ((end - start) / 1000.0) + " seconds");

		sharding.close();
	}

	/**
	 * threadsafe method
	 */
	// @Test
	public void test7shardSimplePool() {
		List<JedisShardInfo> shards = Arrays.asList(new JedisShardInfo("localhost", 6379),
				new JedisShardInfo("localhost", 6380));

		ShardedJedisPool pool = new ShardedJedisPool(new JedisPoolConfig(), shards);

		ShardedJedis one = pool.getResource();

		long start = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			String result = one.set("spn" + i, "n" + i);
		}
		long end = System.currentTimeMillis();
		one.close();
		System.out.println("Simple@Pool SET: " + ((end - start) / 1000.0) + " seconds");

		pool.close();
	}

	// @Test
	public void test8shardPipelinedPool() {
		List<JedisShardInfo> shards = Arrays.asList(new JedisShardInfo("localhost", 6379),
				new JedisShardInfo("localhost", 6380));

		ShardedJedisPool pool = new ShardedJedisPool(new JedisPoolConfig(), shards);

		ShardedJedis one = pool.getResource();

		ShardedJedisPipeline pipeline = one.pipelined();

		long start = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			pipeline.set("sppn" + i, "n" + i);
		}
		List<Object> results = pipeline.syncAndReturnAll();
		long end = System.currentTimeMillis();
		one.close();
		System.out.println("Pipelined@Pool SET: " + ((end - start) / 1000.0) + " seconds");
		pool.close();
	}

//	@Test
	public void test9List() {
		Jedis jedis = new Jedis("localhost");
		System.out.println("Connection to server sucessfully");
		jedis.lpush("tutorial-list", "Redis");
		jedis.lpush("tutorial-list", "Mongodb");
		jedis.lpush("tutorial-list", "Mysql");
		List<String> list = jedis.lrange("tutorial-list", 0, 5);
		for (int i = 0; i < list.size(); i++) {
			System.out.println("Stored string in redis:: " + list.get(i));
		}
		jedis.close();
	}

//	@Test
	public void testA_allkey() {
		Jedis jedis = new Jedis("localhost");
		System.out.println("Connection to server sucessfully");

//		Set<String> keys = jedis.keys("*");
		Set<String> keys = jedis.keys("t*");
		for (String key: keys) {
			System.out.println("List of stored keys:: " + key);
		}
		jedis.close();
	}

//	@Test
	public void testB_example() {
        Jedis jedis = new Jedis("localhost");
        try {
            jedis.set("name", "minxr");
            String ss = jedis.get("name");
            System.out.println(ss);

            jedis.append("name", "jintao");
            ss = jedis.get("name");
            System.out.println(ss);

            jedis.set("name", "jintao");
            System.out.println(jedis.get("name"));

            jedis.del("name");
            System.out.println(jedis.get("name"));// null

            /**
             * same as jedis.set("name","minxr"); jedis.set("jarorwar","aaa");
             */
            jedis.mset(
            		"name", "minxr",
            		"jarorwar", "aaa");
            System.out.println(jedis.mget("name", "jarorwar"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	jedis.close();
        }
    }

//	@Test
    public void testC_keyOp() {
        Jedis jedis = new Jedis("localhost");
        System.out.println("=============key==========================");
        // delete all data
        System.out.println(jedis.flushDB());
        System.out.println(jedis.echo("foo"));

        System.out.println(jedis.exists("foo"));

        jedis.set("key", "values");
        System.out.println(jedis.exists("key"));
        jedis.close();
    }

//	@Test
    public void testD_keyOp2() {
        System.out.println("==String==");
        Jedis jedis = new Jedis("localhost");
        // String
        jedis.set("key", "Hello World!");
        String value = jedis.get("key");
        System.out.println(value);

        System.out.println("=============String==========================");
        System.out.println(jedis.flushDB());

        jedis.set("foo", "bar");
        System.out.println(jedis.get("foo"));

        // set when key not exist
        jedis.setnx("foo", "foo not exits");
        System.out.println(jedis.get("foo"));
        // over write
        jedis.set("foo", "foo update");
        System.out.println(jedis.get("foo"));

        // append value
        jedis.append("foo", " hello, world");
        System.out.println(jedis.get("foo"));

        // set expire seconds
        jedis.setex("foo", 2, "foo not exits");
        System.out.println(jedis.get("foo"));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        System.out.println(jedis.get("foo"));
        // get and set
        jedis.set("foo", "foo update");
        System.out.println(jedis.getSet("foo", "foo modify"));

        System.out.println(jedis.getrange("foo", 1, 3));

        System.out.println(jedis.mset("mset1", "mvalue1", "mset2", "mvalue2",
                "mset3", "mvalue3", "mset4", "mvalue4"));
        System.out.println(jedis.mget("mset1", "mset2", "mset3", "mset4"));
        System.out.println(jedis.del("foo", "foo1", "foo3" ));
        jedis.close();
    }

//    @Test
    public void testE_List() {
        System.out.println("==List==");
        Jedis jedis = new Jedis("localhost");
        jedis.del("messages");
        jedis.rpush("messages", "Hello how are you?");
        jedis.rpush("messages", "Fine thanks. I'm having fun with redis.");
        jedis.rpush("messages", "I should look into this NOSQL thing ASAP");

        List<String> values = jedis.lrange("messages", 0, -1);
        System.out.println(values);

        // drop all keys
        System.out.println(jedis.flushDB());
        jedis.lpush("lists", "Vector");
        jedis.lpush("lists", "ArrayList");
        jedis.lpush("lists", "LinkedList");

        // get list length
        System.out.println(jedis.llen("lists"));
        // 排序
		SortingParams sp1 = new SortingParams();
//		sp1.desc();
		sp1.alpha();
        System.out.println(jedis.sort("lists", sp1));
        // 字串
        System.out.println(jedis.lrange("lists", 0, 3));

        jedis.lset("lists", 0, "hello list!");

        System.out.println(jedis.lindex("lists", 1));

        System.out.println(jedis.lrem("lists", 1, "vector"));
        // get data outside range
        System.out.println(jedis.ltrim("lists", 0, 1));
        System.out.println(jedis.lpop("lists"));
		// get all list
		System.out.println(jedis.lrange("lists", 0, -1));

		// 一般SORT用法 最简单的SORT使用方法是SORT key。
		jedis.lpush("mylist", "1");
		jedis.lpush("mylist", "4");
		jedis.lpush("mylist", "6");
		jedis.lpush("mylist", "3");
		jedis.lpush("mylist", "0");
		// List<String> list = redis.sort("sort");// 默认是升序
		SortingParams sortingParameters = new SortingParams();
		sortingParameters.desc();
		// sortingParameters.alpha();//当数据集中保存的是字符串值时，你可以用 ALPHA
		// 修饰符(modifier)进行排序。
		sortingParameters.limit(0, 2);// 可用于分页查询
		List<String> list = jedis.sort("mylist", sortingParameters);// 默认是升序
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}
		jedis.flushDB();
        jedis.close();
    }

//    @Test
    public void testF_Set() {
        System.out.println("==Set==");
        Jedis jedis = new Jedis("localhost");
        jedis.sadd("myset", "1");
        jedis.sadd("myset", "2");
        jedis.sadd("myset", "3");
        jedis.sadd("myset", "4");
        Set<String> setValues = jedis.smembers("myset");
        System.out.println(setValues);

        // 移除myset
        jedis.srem("myset", "4");
        System.out.println(jedis.smembers("myset"));// 获取所有加入的value
        System.out.println(jedis.sismember("myset", "4"));// 判断 member 是否是 key 集合的元素
        System.out.println(jedis.scard("myset"));// 返回集合的元素个数


        System.out.println(jedis.spop("myset"));
        System.out.println(jedis.smembers("myset"));
        //
        jedis.sadd("sets1", "HashSet1");
        jedis.sadd("sets1", "SortedSet1");
        jedis.sadd("sets1", "TreeSet");
        jedis.sadd("sets2", "HashSet2");
        jedis.sadd("sets2", "SortedSet1");
        jedis.sadd("sets2", "TreeSet1");
        // 交集
        System.out.println(jedis.sinter("sets1", "sets2"));
        // 并集
        System.out.println(jedis.sunion("sets1", "sets2"));
        // 差集
        System.out.println(jedis.sdiff("sets1", "sets2"));
        jedis.close();
    }

//    @Test
    public void testG_sortedSet() {
        System.out.println("==SoretedSet==");
        Jedis jedis = new Jedis("localhost");
        jedis.zadd("hackers", 1940, "Alan Kay");
        jedis.zadd("hackers", 1953, "Richard Stallman");
        jedis.zadd("hackers", 1965, "Yukihiro Matsumoto");
        jedis.zadd("hackers", 1916, "Claude Shannon");
        jedis.zadd("hackers", 1969, "Linus Torvalds");
        jedis.zadd("hackers", 1912, "Alan Turing");
        Set<String> setValues = jedis.zrange("hackers", 0, -1);
        System.out.println(setValues);
        Set<String> setValues2 = jedis.zrevrange("hackers", 0, -1);
        System.out.println(setValues2);

        // 清空数据
        System.out.println(jedis.flushDB());
        // 添加数据
        jedis.zadd("zset", 10.1, "hello");
        jedis.zadd("zset", 10.0, ":");
        jedis.zadd("zset", 9.0, "zset");
        jedis.zadd("zset", 11.0, "zset!");
        // 元素个数
        System.out.println(jedis.zcard("zset"));
        // 元素下标
        System.out.println(jedis.zscore("zset", "zset"));
        // 集合子集
        System.out.println(jedis.zrange("zset", 0, -1));
        // 删除元素
        System.out.println(jedis.zrem("zset", "zset!"));
        System.out.println(jedis.zcount("zset", 9.5, 10.5));
        // 整个集合值
        System.out.println(jedis.zrange("zset", 0, -1));
        jedis.close();
    }

//    @Test
    public void testH_Hash() {
        System.out.println("==Hash==");
        Jedis jedis = new Jedis("localhost");
        Map<String, String> pairs = new HashMap<String, String>();
        pairs.put("name", "Akshi");
        pairs.put("age", "2");
        pairs.put("sex", "Female");
        jedis.hmset("kid", pairs);
        List<String> name = jedis.hmget("kid", "name");// 结果是个泛型的LIST
        System.out.println(name);
        // jedis.hdel("kid","age"); //删除map中的某个键值
        System.out.println(jedis.hmget("kid", "pwd")); // 因为删除了，所以返回的是null
        System.out.println(jedis.hlen("kid")); // 返回key为user的键中存放的值的个数
        System.out.println(jedis.exists("kid"));// 是否存在key为user的记录
        System.out.println(jedis.hkeys("kid"));// 返回map对象中的所有key
        System.out.println(jedis.hvals("kid"));// 返回map对象中的所有value

        Iterator<String> iter = jedis.hkeys("kid").iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            System.out.println(key + ":" + jedis.hmget("kid", key));
        }

        List<String> values = jedis.lrange("messages", 0, -1);
        values = jedis.hmget("kid", new String[] { "name", "age", "sex" });
        System.out.println(values);
        Set<String> setValues = jedis.zrange("hackers", 0, -1);
        setValues = jedis.hkeys("kid");
        System.out.println(setValues);
        values = jedis.hvals("kid");
        System.out.println(values);
        pairs = jedis.hgetAll("kid");
        System.out.println(pairs);

        // 清空数据
        System.out.println(jedis.flushDB());
        // 添加数据
        jedis.hset("hashs", "entryKey", "entryValue");
        jedis.hset("hashs", "entryKey1", "entryValue1");
        jedis.hset("hashs", "entryKey2", "entryValue2");
        // 判断某个值是否存在
        System.out.println(jedis.hexists("hashs", "entryKey"));
        // 获取指定的值
        System.out.println(jedis.hget("hashs", "entryKey")); // 批量获取指定的值
        System.out.println(jedis.hmget("hashs", "entryKey", "entryKey1"));
        // 删除指定的值
        System.out.println(jedis.hdel("hashs", "entryKey"));
        // 为key中的域 field 的值加上增量 increment
        System.out.println(jedis.hincrBy("hashs", "entryKey", 123l));
        // 获取所有的keys
        System.out.println(jedis.hkeys("hashs"));
        // 获取所有的values
        System.out.println(jedis.hvals("hashs"));
        jedis.close();
    }

    @Test
    public void testOther() throws InterruptedException {
        Jedis jedis = new Jedis("localhost");

        // keys中传入的可以用通配符
        System.out.println(jedis.keys("*"));
        System.out.println(jedis.keys("*name"));// 返回的sname [sname, name]
        System.out.println(jedis.del("sanmdde"));// 删除key为sanmdde的对象 删除成功返回1
                                                    // 删除失败（或者不存在）返回 0
        System.out.println(jedis.ttl("sname"));// 返回给定key的有效时间，如果是-1则表示永远有效
        jedis.setex("timekey", 10, "min");// 通过此方法，可以指定key的存活（有效时间） 时间为秒
        Thread.sleep(5000);// 睡眠5秒后，剩余时间将为<=5
        System.out.println(jedis.ttl("timekey")); // 输出结果为5
        jedis.setex("timekey", 1, "min"); // 设为1后，下面再看剩余时间就是1了
        System.out.println(jedis.ttl("timekey")); // 输出结果为1
        System.out.println(jedis.exists("key"));// 检查key是否存在
        System.out.println(jedis.rename("timekey", "time"));
        System.out.println(jedis.get("timekey"));// 因为移除，返回为null
        System.out.println(jedis.get("time")); // 因为将timekey 重命名为time
                                                // 所以可以取得值 min
        // persist
        System.out.println("test persist");
        jedis.set("keym2", "123");
        System.out.println("expire keym2 10 : " + jedis.expire("keym2", 10));
        System.out.println("ttl keym2 : " + jedis.ttl("keym2"));
        System.out.println("persist keym2 : " + jedis.persist("keym2"));
        System.out.println("ttl keym2 : " + jedis.ttl("keym2") + "\n");

        // randomkey
        System.out.println("test randomkey");
        System.out.println("randomkey : " + jedis.randomKey() + "\n");

        // rename
        System.out.println("test rename");
        System.out.println("rename keym2 keynew2 : " + jedis.rename("keym2", "keynew2"));
        System.out.println("get keynew2 : " + jedis.get("keynew2") + "\n");

        // type
        System.out.println("test type");
        System.out.println("type keym1 : " + jedis.type("keynew2"));

        System.out.println("del timekey keym2 time : " + jedis.del("timekey", "keym2", "time"));
        jedis.close();
    }

    /**
     * sort list
     * LIST结合hash的排序
     */
//    @Test
    public void testSort2() {
        Jedis jedis = new Jedis("localhost");
        jedis.del("user:66", "user:55", "user:33", "user:22", "user:11",
                "userlist");
        jedis.lpush("userlist", "33");
        jedis.lpush("userlist", "22");
        jedis.lpush("userlist", "55");
        jedis.lpush("userlist", "11");

        jedis.hset("user:66", "name", "66-66");
        jedis.hset("user:55", "name", "55-55");
        jedis.hset("user:33", "name", "33-33");
        jedis.hset("user:22", "name", "79-22");
        jedis.hset("user:11", "name", "24-11");
        jedis.hset("user:11", "add", "beijing");
        jedis.hset("user:22", "add", "shanghai");
        jedis.hset("user:33", "add", "guangzhou");
        jedis.hset("user:55", "add", "chongqing");
        jedis.hset("user:66", "add", "xi'an");

        SortingParams sortingParameters = new SortingParams();
        // 符号 "->" 用于分割哈希表的键名(key name)和索引域(hash field)，格式为 "key->field" 。
        sortingParameters.get("user:*->name");
        sortingParameters.get("user:*->add");
//      sortingParameters.by("user:*->name");
//      sortingParameters.get("#");
        List<String> result = jedis.sort("userlist", sortingParameters);
        for (String item : result) {
            System.out.println("item...." + item);
        }
        /**
         * 对应的redis客户端命令是：sort ml get user*->name sort ml get user:*->name get
         * user:*->add
         */
        jedis.close();
    }

	/**
	 * sort set SET结合String的排序
	 */
//	@Test
	public void testSort3() {
		Jedis jedis = new Jedis("localhost");
		jedis.del("tom:friend:list", "score:uid:123", "score:uid:456", "score:uid:789", "score:uid:101", "uid:123",
				"uid:456", "uid:789", "uid:101");

		jedis.sadd("tom:friend:list", "123"); // tom的好友列表
		jedis.sadd("tom:friend:list", "456");
		jedis.sadd("tom:friend:list", "789");
		jedis.sadd("tom:friend:list", "101");

		jedis.set("score:uid:123", "1000"); // 好友对应的成绩
		jedis.set("score:uid:456", "6000");
		jedis.set("score:uid:789", "100");
		jedis.set("score:uid:101", "5999");

		jedis.set("uid:123", "{'uid':123,'name':'lucy'}"); // 好友的详细信息
		jedis.set("uid:456", "{'uid':456,'name':'jack'}");
		jedis.set("uid:789", "{'uid':789,'name':'jay'}");
		jedis.set("uid:101", "{'uid':101,'name':'jolin'}");

		SortingParams sortingParameters = new SortingParams();

		sortingParameters.desc();
		// sortingParameters.limit(0, 2);
		// 注意GET操作是有序的，GET user_name_* GET user_password_*
		// 和 GET user_password_* GET user_name_*返回的结果位置不同
		sortingParameters.get("#");// GET 还有一个特殊的规则—— "GET #"
									// ，用于获取被排序对象(我们这里的例子是 user_id )的当前元素。
		sortingParameters.get("uid:*");
		sortingParameters.get("score:uid:*");
		sortingParameters.by("score:uid:*");
		// 对应的redis 命令是./redis-cli sort tom:friend:list by score:uid:* get # get
		// uid:* get score:uid:*
		List<String> result = jedis.sort("tom:friend:list", sortingParameters);
		for (String item : result) {
			System.out.println("item..." + item);
		}
		jedis.close();
	}

//    @Test
    public void testDB() {
       Jedis jedis = new Jedis("localhost");
       System.out.println(jedis.select(0));// select db-index
                                           // 通过索引选择数据库，默认连接的数据库所有是0,默认数据库数是16个。返回1表示成功，0失败
       System.out.println(jedis.dbSize());// dbsize 返回当前数据库的key数量
       System.out.println(jedis.keys("*")); // 返回匹配指定模式的所有key
       System.out.println(jedis.randomKey());
       jedis.flushDB();// 删除当前数据库中所有key,此方法不会失败。慎用
       jedis.flushAll();// 删除所有数据库中的所有key，此方法不会失败。更加慎用
       jedis.close();
   }
}
