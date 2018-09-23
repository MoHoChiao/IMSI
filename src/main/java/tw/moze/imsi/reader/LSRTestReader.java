package tw.moze.imsi.reader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import tw.moze.imsi.redis.RedisUtil;
import tw.moze.util.collection.ExpiringMap;
import tw.moze.util.concurrent.CountUpDownLatch;
import tw.moze.util.concurrent.ExecutorBuilder;
import tw.moze.util.dev.XXX;
import tw.moze.util.fileutil.DirLister;
import tw.moze.util.time.Stopwatch;

//public class LSRReader extends JedisPubSub implements Closeable {
public class LSRTestReader implements Closeable {
//	private RedisSubscriber subscriber;

	private String[] dirForRead = new String[] {
			"/data/imsi_mapping_cluster_v1/src/lsr/10.108.200.141/",
			"/data/imsi_mapping_cluster_v1/src/lsr/10.108.200.142/",
			"/data/imsi_mapping_cluster_v1/src/lsr/10.108.200.143/",

//			rsync -rvh root@10.108.200.141:/var/lib/truecall/COMMON_LTE/csv/yyMMdd/HH /data/imsi_mapping_cluster_v1/src/lsr/10.108.200.141/yyMMdd/HH
//			rsync -rvh root@10.108.200.142:/var/lib/truecall/COMMON_LTE/csv/yyMMdd/HH /data/imsi_mapping_cluster_v1/src/lsr/10.108.200.142/yyMMdd/HH
//			rsync -rvh root@10.108.200.143:/var/lib/truecall/COMMON_LTE/csv/yyMMdd/HH /data/imsi_mapping_cluster_v1/src/lsr/10.108.200.143/yyMMdd/HH
	};

	// src file mapping 完後會移到此處
	private String[] dirForMapped = new String[] {
			"/data/imsi_mapping_cluster_v1/mapped/lsr/10.108.200.141/",
			"/data/imsi_mapping_cluster_v1/mapped/lsr/10.108.200.142/",
			"/data/imsi_mapping_cluster_v1/mapped/lsr/10.108.200.143/",
	};

	private Map<String, Long> fileSizeMap;
	private String filePattern = "*.csv.gz";

	private ThreadPoolExecutor tp;
	private SimpleDateFormat dfToSec = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public LSRTestReader() {
		fileSizeMap = new ExpiringMap<>(60*5); // 5 分鐘
		tp = ExecutorBuilder.newCachedThreadPool(6);
		RedisUtil.initPool();
//		subscriber = new RedisSubscriber("lsrloader", this);
//		subscriber.start();
		runTimer();
	}


	// 為防止 RedisSubscriber 失效，故加上此方法
	// 現在變成以這個為主要的觸發方法 2018-0522
	public void runTimer() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				XXX.out("Timer fired!");
				guardedProcessAll();
			}

		}, 10 * 1000, 60 * 1000);
	}

	/*
	 * 要注意讀到的檔案可能不完全的問題
	 * 本程式受 DMSLoader 寫檔觸發執行，第 1 次執行完在記憶體裡記檔案大小，
	 * 若 第 2 次執行時，檔案大小沒變，代表此檔案未成長.
	 * 則執行完後，dirToWrite
	 */
	private void processAll() {
		Stopwatch sw = Stopwatch.create();
		CountUpDownLatch latch = new CountUpDownLatch();
		try {


			// 只處理這一小時跟上一小時
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd/HH/");
			Calendar cal = Calendar.getInstance();

			String[] hours = new String[5];
			for (int i = 0; i < hours.length; i++) {
				hours[i] = sdf.format(cal.getTime());
				cal.add(Calendar.HOUR_OF_DAY, -1);
			}

			XXX.out("Query file: " + hours[hours.length-1] + " ~ " + hours[0]);
			long dmsDataTime = ReaderUtil.getRedisDataTime();
			if (dmsDataTime == -1) {
				XXX.out("Unable to get DMS data time, waiting...");
			}

			XXX.out("DMS Data Time = " + dfToSec.format(new Date(dmsDataTime * 1000L)));

			List<Runnable> jobs = new LinkedList<>();
			for (int i = 0; i < dirForRead.length; i++) {
				String dirSrc = dirForRead[i];
				String dirMapped = dirForMapped[i];
				for (String hour : hours)
					getAllJobs(jobs, latch, dirSrc + hour, dirMapped + hour, dmsDataTime);
			}

			XXX.out("Job count = " + jobs.size());
			latch.setCount(jobs.size());
			for (Runnable job: jobs)
				tp.execute(job);
		}
		finally {
			try {
				latch.await();
			} catch (InterruptedException e) {
			}
			sw.stop().printTimeDiff("Process All Directories: ");
		}
	}

	private void getAllJobs(List<Runnable> jobs, CountUpDownLatch latch, String dirSrc, String dirMapped, long dmsDataTime) {
		List<String> files = new LinkedList<>();
		List<String> dirs = new LinkedList<>();
		DirLister.deepListFiles(dirSrc, filePattern, files, dirs);
		// mkdirs in advances
		File dirMappedFile = new File(dirMapped);
		if (!dirMappedFile.exists()) {
			boolean ok = dirMappedFile.mkdirs();
			XXX.out("Create target directory [" + dirMapped + "] " + (ok ? "successed." : "failed."));
		}
		for (String srcDirPath: dirs) {
			String partialPath = srcDirPath.substring(dirSrc.length());
			String mappedDirPath = dirMapped + partialPath;
			File mappedDir = new File(mappedDirPath);
			if (!mappedDir.exists()) {
				boolean ok = mappedDir.mkdirs();
				XXX.out("Create target directory [" + mappedDirPath + "] " + (ok ? "successed." : "failed."));
			}
		}


		for (String srcFilePath: files) {
			// 如果 LSR 檔案時間比已匯入的 DMS 資料時間還大 (新)，那就暫不處理
			long lsrFileTime = ReaderUtil.getLSRFileTime(srcFilePath);
			if (lsrFileTime > dmsDataTime)
				continue;

			File srcFile = new File(srcFilePath);
			// .csv.gz, 我們產生的是 .csv 檔
			String partialPath = srcFilePath.substring(dirSrc.length(), srcFilePath.length() - 3);
			String mappedFilePath = dirMapped + partialPath;
			File mappedFile = new File(mappedFilePath);

			// 若 mappedFile 存在，代表我們已處理過
			if (mappedFile.exists())
				continue;

			XXX.out("Processing [" + srcFilePath + "], file time is " + dfToSec.format(new Date(lsrFileTime * 1000L)));
//			jobs.add(new LSRFileProcessor(latch, fileSizeMap, srcFile, mappedFile)); // XXX 測試完後上線前這一行要移掉
		}
	}

//	@Override
//	public void onMessage(String channel, String message) {
//		XXX.out("Receive message [" + channel + "] = " + message);
//		guardedProcessAll();
//	}

	private void guardedProcessAll() {
		long now = System.currentTimeMillis();

		// if the event comes too close, just ignore it
		// 2018-0530 其實底下這個不用防止重複執行有 overlap 的情況了，
		// 因為我們現在只有使用 timer, 而 timer 是不會有時間 overlap 的
		synchronized (LSRTestReader.class) {
			if (isRunning || now - prevEventTime < 30 * 1000L)
				return;
			isRunning = true;
		}

		processAll();
		isRunning = false;
		prevEventTime = System.currentTimeMillis();

	}

    private volatile long prevEventTime = 0L;
	private volatile boolean isRunning = false;

	@Override
	public void close() throws IOException {
		RedisUtil.close();

		if (tp != null)
			tp.shutdown();
	}

	public static void main(String[] args) {
		final LSRTestReader reader = new LSRTestReader();
		reader.guardedProcessAll();

		Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override
			public void run() {
		    	try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		});

		try {
			new CountDownLatch(1).await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
