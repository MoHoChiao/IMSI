package tw.moze.util.concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorBuilder {
	public static ThreadPoolExecutor newCachedThreadPool(int coreSize) {
		int niceThreadCount = getNiceThreadCount();
		ThreadPoolExecutor executor = new ThreadPoolExecutor(coreSize, // core size
			    niceThreadCount, // max size
			    60, // idle timeout
			    TimeUnit.SECONDS,
			    new LinkedBlockingQueue<Runnable>()); // queue with a size
		executor.allowCoreThreadTimeOut(true);
		return executor;
	}

	private static int getNiceThreadCount() {
		int cores = Runtime.getRuntime().availableProcessors();
		return cores * 2;
	}
}
