package tw.moze.util.time;

public class Stopwatch {
	private long startTime;
	private long diffTime;
	public static Stopwatch create() {
		return new Stopwatch();
	}

	public Stopwatch() {
		this.startTime = System.currentTimeMillis();
	}

	public Stopwatch start() {
		this.startTime = System.currentTimeMillis();
		return this;
	}

	public Stopwatch stop() {
		long now = System.currentTimeMillis();
		diffTime = now - startTime;
		return this;
	}

	public long getDiffTime() {
		return diffTime;
	}

	public void printTimeDiff(String prefix) {
		double seconds = diffTime / 1000d;
		System.out.println(prefix + seconds + " seconds.");
	}
}
