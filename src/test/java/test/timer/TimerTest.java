package test.timer;

import java.util.Timer;
import java.util.TimerTask;

import tw.moze.util.dev.XXX;

public class TimerTest {
	public static void main(String[] args) {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				XXX.out("run");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}, 0, 2 * 1000);

	}
}
