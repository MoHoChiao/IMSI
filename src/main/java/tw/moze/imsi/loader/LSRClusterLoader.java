package tw.moze.imsi.loader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//import redis.clients.jedis.Jedis;
import tw.moze.core.shell.ShellRunner;
import tw.moze.core.shell.ShellRunner.OutputHandler;
import tw.moze.imsi.redis.RedisUtil;
//import tw.moze.imsi.redis.RedisUtil;
import tw.moze.util.concurrent.CountUpDownLatch;
import tw.moze.util.concurrent.ExecutorBuilder;
import tw.moze.util.dev.XXX;
import tw.moze.util.time.Stopwatch;

public class LSRClusterLoader implements Closeable {
	private ThreadPoolExecutor tp;
	private volatile long lastRun = 0;
	private volatile boolean isRunning = false;
	public LSRClusterLoader() {
		tp = ExecutorBuilder.newCachedThreadPool(6);
		RedisUtil.initPool();

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				// make sure we have intervals between executions
				if (isRunning || now - lastRun < 10 * 1000) {
					XXX.out("!!!");
					return;
				}
				XXX.out("Run loader batch");
				isRunning = true;
				runLoaderBatch();
				isRunning = false;
				lastRun = System.currentTimeMillis();
			}

		}, 3 * 1000, 60 * 1000);
	}

	private void runLoaderBatch() {
		Stopwatch sw = Stopwatch.create();

		String[] pathPairs = new String[] {
				"root@10.108.200.141:/var/lib/truecall/COMMON_LTE/csv/::/data/imsi_mapping_cluster_v1/src/lsr/10.108.200.141/",
				"root@10.108.200.142:/var/lib/truecall/COMMON_LTE/csv/::/data/imsi_mapping_cluster_v1/src/lsr/10.108.200.142/",
				"root@10.108.200.143:/var/lib/truecall/COMMON_LTE/csv/::/data/imsi_mapping_cluster_v1/src/lsr/10.108.200.143/"
		};
		// 來源目錄結構檔名結構是 yyMMdd/HH/yyyyMMddHH.{unix_time_sec}.csv
		// 抓本小時及前一小時的檔案目錄

		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd/HH/");

		Calendar c = Calendar.getInstance();

		String[] subdirs = new String[5];

		for(int i = 0; i < subdirs.length; i++) {
			subdirs[i] = sdf.format(c.getTime());
			c.add(Calendar.HOUR_OF_DAY, -1);
		}

		CountUpDownLatch latch = new CountUpDownLatch();
		List<RsyncRunable> jobs = getAllRsyncJobs(latch, pathPairs, subdirs);
		if (jobs.isEmpty())
			return;

		latch.setCount(jobs.size());

		for (RsyncRunable job: jobs) {
			tp.execute(job);
		}

//		Jedis jedis = null;
		try {
			latch.await(10, TimeUnit.MINUTES);
//			jedis = RedisUtil.getResource();
//			jedis.publish("lsrloader", subdirs[0]);
		} catch (Exception e) {
			XXX.err("jedis.publish failded, bypass error!");
		} finally {
//			if (jedis != null)
//				jedis.close();
		}
		sw.stop().printTimeDiff("Run loader batch tooks: ");
	}

	// pathPair is separated by "::"
	private List<RsyncRunable> getAllRsyncJobs(CountUpDownLatch latch, String[] pathPairs, String[] subDirs) {
		List<RsyncRunable> ret = new LinkedList<>();
		for (String pathPair : pathPairs) {
			String[] p = pathPair.split("::");
			if (p.length != 2) {
				throw new RuntimeException("Error path pair format! pathPair is separated by '::'.");
			}

			for (String subDir: subDirs) {
				String remotePath = p[0] + subDir;
				String localPath = p[1] + subDir;

				RsyncRunable r = new RsyncRunable(latch, remotePath, localPath);
				ret.add(r);
			}
		}
		return ret;
	}

	class RsyncRunable implements Runnable {
		private String[] cmd;
		private CountUpDownLatch latch;
		private String localPath;


		public RsyncRunable(CountUpDownLatch latch, String remotePath, String localPath) {
//			rsync -rvh root@10.108.200.141:/var/lib/truecall/COMMON_LTE/csv/yyMMdd/HH /data/imsi_mapping_cluster_v1/src/lsr/10.108.200.141/yyMMdd/HH
//			rsync -rvh root@10.108.200.142:/var/lib/truecall/COMMON_LTE/csv/yyMMdd/HH /data/imsi_mapping_cluster_v1/src/lsr/10.108.200.142/yyMMdd/HH
//			rsync -rvh root@10.108.200.143:/var/lib/truecall/COMMON_LTE/csv/yyMMdd/HH /data/imsi_mapping_cluster_v1/src/lsr/10.108.200.143/yyMMdd/HH
			this.latch = latch;
			cmd = new String[] {"rsync", "-rvht", remotePath, localPath};
			this.localPath = localPath;
		}

		@Override
		public void run() {
			try {
				ShellRunner sr = new ShellRunner(cmd);
				XXX.out("cmd = " + sr.getCmdString());
				new File(localPath).mkdirs();
				sr.exec(new OutputHandler() {
					@Override
					public void onLine(String line) {
						System.out.println(line);
					}
				}, 15 * 1000);
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				latch.countDown();
				XXX.out("Job done. remains: " + latch.getCount());
			}
		}
	}

	@Override
	public void close() throws IOException {
		RedisUtil.close();
		if (tp != null)
			tp.shutdown();

	}

	public static void main(String[] args) throws InterruptedException {
		final LSRClusterLoader loader = new LSRClusterLoader();
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override
			public void run() {
		    	try {
					loader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		});
	}


}
