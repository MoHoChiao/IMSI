package tw.moze.imsi.reader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
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

/**
 * 將 2 小時以內的檔案資料塞入到 redis 裡面
 * @author edward
 *
 */
//public class DMSReader extends JedisPubSub implements Closeable {
public class DMSReader implements Closeable {
//	private RedisSubscriber subscriber;

	private String[] dirForRead = new String[] {
			"C:/Users/User/Downloads/data/imsi_mapping/src/dms/10.108.61.155",
			"C:/Users/User/Downloads/data/imsi_mapping/src/dms/10.108.61.167",
//			"/data/imsi_mapping/src/dms/10.108.61.155",
//			"/data/imsi_mapping/src/dms/10.108.61.167",
	};

	// filter 完不須對應的欄位之後的檔案會放置此處
	private String[] dirForFiltered = new String[] {
			"C:/Users/User/Downloads/data/imsi_mapping/filtered/dms/10.108.61.155",
			"C:/Users/User/Downloads/data/imsi_mapping/filtered/dms/10.108.61.167",
//			"/data/imsi_mapping/filtered/dms/10.108.61.155",
//			"/data/imsi_mapping/filtered/dms/10.108.61.167",
	};

	private String filePattern = "s1ap_1*.csv";

	// 因檔名在不同機器之間不會重複，因此可以放心的直接以檔名做 key 就好
	private Map<String, Long> fileSizeMap;
	private ThreadPoolExecutor tp;

	public DMSReader() {
		fileSizeMap = new ExpiringMap<>(60*2);
		tp = ExecutorBuilder.newCachedThreadPool(16);
		RedisUtil.initPool();
//		subscriber = new RedisSubscriber("dmsloader", this);
//		subscriber.start();
		runTimer();
	}

	// 為防止 RedisSubscriber 失效，故加上此方法
	public void runTimer() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				XXX.out("Timer fired!");
				guardedProcessAll();
			}

		}, 10 * 1000, 30 * 1000);
	}

	private void guardedProcessAll() {
		long now = System.currentTimeMillis();

		// if the event comes too close, just ignore it
		synchronized (DMSReader.class) {
			if (isRunning || now - prevEventTime < 5 * 1000L)
				return;
			isRunning = true;
		}

		processAll();
		isRunning = false;
		prevEventTime = System.currentTimeMillis();

	}

	/*
	 * 要注意讀到的檔案可能不完全的問題
	 * 本程式受 DMSLoader 寫檔觸發執行，第 1 次執行完在記憶體裡記檔案大小，
	 * 若 第 2 次執行時，檔案大小沒變，代表此檔案未成長.
	 * 則執行完後，dirToWrite
	 */
	private void processAll() {
		Stopwatch sw = Stopwatch.create();
		XXX.out("Start create job");
		CountUpDownLatch latch = new CountUpDownLatch();
		List<Runnable> jobs = new LinkedList<>();

		for (int i = 0; i < dirForRead.length; i++) {
			String dirSrc = dirForRead[i];
			String dirFiltered = dirForFiltered[i];
			XXX.out("Create job " + dirSrc + " ==> " + dirFiltered);
			getAllJobs(jobs, latch, dirSrc, dirFiltered);
		}

		try {
			XXX.out("Job count = " + jobs.size());
			latch.setCount(jobs.size());
			for (Runnable job: jobs)
				tp.execute(job);

//			latch.await(10, TimeUnit.MINUTES); // 當 DMS Loader 突然載入大量資料，會導致處理時間超過 10 分鐘，因此不能這樣寫
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ReaderUtil.setRedisDataTime();
		sw.stop().printTimeDiff("Process All Directories: ");
	}

	private void getAllJobs(final List<Runnable> jobs, final CountUpDownLatch latch,
			final String srcPath, final String dirFiltered) {
		List<String> files = new LinkedList<>();
		List<String> dirs = new LinkedList<>();

		// 超過 2 小時的檔案不處理
//		long in2hours = System.currentTimeMillis() - (2 * 60 * 60 * 1000);
		// 2018-09-10 超過 1 小時的檔案不處理，因為根本處理不完
		long in2hours = System.currentTimeMillis() - (60 * 60 * 1000);
		DirLister.deepListFiles(srcPath, filePattern, in2hours, files, dirs);
		XXX.out("file count = " + files.size());
		// mkdirs in advances
		File dirFilteredFile = new File(dirFiltered);
		if (!dirFilteredFile.exists()) {
			boolean ok = dirFilteredFile.mkdirs();
			XXX.out("Create target directory [" + dirFiltered + "] " + (ok ? "successed." : "failed."));
		}
		for (String srcDirPath: dirs) {
			String partialPath = srcDirPath.substring(srcPath.length());
			String filteredDirPath = dirFiltered + partialPath;
			File filteredDir = new File(filteredDirPath);
			if (!filteredDir.exists()) {
				boolean ok = filteredDir.mkdirs();
				XXX.out("Create target directory [" + filteredDirPath + "] " + (ok ? "successed." : "failed."));
			}
		}

		for (String srcFilePath: files) {
			File srcFile = new File(srcFilePath);
			String partialPath = srcFilePath.substring(srcPath.length());
			String filteredFilePath = dirFiltered + partialPath;
			File filteredFile = new File(filteredFilePath);

			// 若 filteredFile 已存在，代表這個檔案已被處理過了
			if (filteredFile.exists())
				continue;

			jobs.add(new DMSFileProcessor(latch, fileSizeMap, srcFile, filteredFile));
		}
	}

	public static Comparator<String> comp = new Comparator<String>() {
		@Override
		public int compare(String f1, String f2) {
			long t1 = new File(f1).lastModified();
			long t2 = new File(f2).lastModified();

			return (int)(t2 - t1);
		}
	};

//	@Override
//	public void onMessage(String channel, String message) {
//		XXX.out("Receive message [" + channel + "] = " + message);
//		guardedProcessAll();
//	}
    private volatile long prevEventTime = 0L;
	private volatile boolean isRunning = false;

	@Override
	public void close() throws IOException {
		RedisUtil.close();
		if (tp != null)
			tp.shutdown();
	}

	public static void main(String[] args) {
		final DMSReader reader = new DMSReader();
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
