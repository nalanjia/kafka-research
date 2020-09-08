package miscellaneous.micrometer;

import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class Timers {
	private SimpleMeterRegistry registry = new SimpleMeterRegistry();

	public void record() {
		Timer timer = registry.timer("simple");
		timer.record(() -> {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		
		System.out.println("timer : " + timer.totalTime(TimeUnit.MILLISECONDS));
	}

	public void sample() {
		Timer.Sample sample = Timer.start();
		
		new Thread(() -> {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			Timer timer = registry.timer("sample");
			long cost = sample.stop(timer);
			System.out.println("timer2 : " + cost);
		}).start();
		
	}

	public static void main(String[] args) {
		Timers test = new Timers();
//		test.record();
		test.sample();
	}
}
