package tw.moze.imsi.loader;

import java.io.Closeable;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

/**
 * 從 DMS 上抓取檔案，預設是抓取 1 小時以內的檔案, 要注意每個機器時間可能沒 sync 的問題
 * @author edward
 *
 */
public class DMSLoader implements Closeable {
	private int hoursToGrep = 1;
	private ThreadPoolExecutor tp;
	private volatile long lastRun = 0;
	private volatile boolean isRunning = false;

	public DMSLoader() {
		tp = ExecutorBuilder.newCachedThreadPool(6);
//		RedisUtil.initPool();

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

		}, 3 * 1000, 20 * 1000);
	}


	private Map<String, Long> lastData = new HashMap<>();

	private void setLastTime(String localPath, long lastTime) {
		synchronized (lastData) {
			Long prevLastDataTime = lastData.get(localPath);
			if (prevLastDataTime  == null) {
				lastData.put(localPath, lastTime);
			}
			else {
				lastData.put(localPath, Math.max(prevLastDataTime, lastTime));
			}
		}
	}

	private void runLoaderBatch() {
		Stopwatch sw = Stopwatch.create();
		// filename example: s1ap_1_prb_MSP6000-11154_ipx_reports_data_1490063700_60_0_5.csv
		String filePattern = "s1ap_1*_%d*.csv";
		// 其中 %d 部分是 unix time, 精度到秒 Java 的 System.currentTimeMillis() 要除以 1000 樣式才一致

		String[] pathPairs = new String[] {
				"hammer@10.108.61.155:/ftproot/forwarding/::/data/imsi_mapping_cluster_v1/src/dms/10.108.61.155",
				"hammer@10.108.61.167:/ftproot/forwarding/::/data/imsi_mapping_cluster_v1/src/dms/10.108.61.167"
		};

		CountUpDownLatch latch = new CountUpDownLatch();
		List<RsyncRunable> jobs = getAllRsyncJobs(latch, pathPairs, filePattern);
		if (jobs.isEmpty())
			return;

		latch.setCount(jobs.size());

		for (RsyncRunable job : jobs) {
			tp.execute(job);
		}

//		Jedis jedis = null;
		try {
			latch.await(10, TimeUnit.MINUTES);
//			jedis = RedisUtil.getResource();
//			jedis.publish("dmsloader", lastData.toString());
		} catch (Exception e) {
			XXX.err("jedis.publish failded, bypass error!");
		} finally {
//			if (jedis != null)
//				jedis.close();
		}
		sw.stop().printTimeDiff("Run loader batch tooks: ");
	}

	// pathPair is separated by "::"
	private List<RsyncRunable> getAllRsyncJobs(CountUpDownLatch latch, String[] pathPairs, String filePattern) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<RsyncRunable> ret = new LinkedList<>();
		for (String pathPair : pathPairs) {
			String[] p = pathPair.split("::");
			if (p.length != 2) {
				throw new RuntimeException("Error path pair format! pathPair is separated by '::'.");
			}

			long timeTo = System.currentTimeMillis() / 1000L;

			// 抓取 1 小時以內的檔案
			// 而且要注意，上次取回的檔案可能只是半個檔
			long timeFrom = timeTo - (hoursToGrep * 60 * 60) - (10 * 60);

			// 1 分鐘是 60 秒，100 秒約 1.7 分鐘; 把 from 跟 to 除以 100，得到 timestamp 的 prefix
			// 從 from 到 to 之間，約要 40 次 rsync 呼叫
			timeTo /= 1000;
			timeTo += 1;
			timeFrom /= 1000;

			XXX.out("Rsync " + sdf.format(new Date(timeFrom * 1000 * 1000)) + " ~ " + sdf.format(new Date(timeTo * 1000 * 1000)));

			for (long i = timeFrom; i <= timeTo; i++) {
				String filePattternWithTime = String.format(filePattern, i);
				RsyncRunable r = new RsyncRunable(latch, p[0], p[1], filePattternWithTime);
				ret.add(r);
			}
		}
		return ret;
	}

	class RsyncRunable implements Runnable {
		private String[] cmd;
		private CountUpDownLatch latch;
		private String localPath;


		public RsyncRunable(CountUpDownLatch latch, String remotePath, String localPath, String filePattternWithTime) {
			// rsync -rzvh --include 's1ap_1*' --exclude '*' hammer@10.108.61.155:/ftproot/forwarding/ /data/imsi_mapping_cluster_v1/src/dms/10.108.61.155
			// rsync -rzvh --include 's1ap_1*' --exclude '*' hammer@10.108.61.167:/ftproot/forwarding/ /data/imsi_mapping_cluster_v1/src/dms/10.108.61.167

			this.latch = latch;
			cmd = new String[] { "rsync", "-rzvht", "--include", filePattternWithTime,
					"--exclude", "*", remotePath, localPath};
			this.localPath = localPath;
		}

		@Override
		public void run() {
			try {
				ShellRunner sr = new ShellRunner(cmd);
				XXX.out("cmd = " + sr.getCmdString());
				sr.exec(new OutputHandler() {
					@Override
					public void onLine(String line) {
						System.out.println(line);

						// s1ap_1_prb_MSP6000-12251_ipx_reports_data_1490404980_60_0_7.csv
						String[] la = line.split("data_");
						if (la.length != 2)
							return;

						la = la[1].split("_", 2);
						if (la.length < 2)
							return;

						long lastTime = Long.valueOf(la[0])/100L;
						setLastTime(localPath, lastTime);
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

	public static void main(String[] argv) throws InterruptedException {
		final DMSLoader loader = new DMSLoader();
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
