package miscellaneous.micrometer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * http://micrometer.io/docs/concepts
 * 
 * https://www.cnblogs.com/rolandlee/p/11343848.html
 * JVM应用度量框架Micrometer实战
 * 
 * https://www.ibm.com/developerworks/cn/java/j-using-micrometer-to-record-java-metric/index.html
 * 使用 Micrometer 记录 Java 应用性能指标
 * 
 * https://blog.csdn.net/weixin_34184561/article/details/91664913
 * 聊聊springboot2的micrometer
 */
public class Counters {

	private SimpleMeterRegistry registry = new SimpleMeterRegistry();

	public void counter() {
		Counter counter1 = registry.counter("simple1");
		System.out.println("counter1初始化：" + counter1.count());
		counter1.increment(2.0);
		System.out.println("counter1变化后：" + counter1.count());
		
		Counter counter2 = Counter.builder("simple2").description("A simple counter").tag("tag1", "a")
				.register(registry);
		System.out.println("counter2初始化：" + counter2.count());
		counter2.increment();
		System.out.println("counter2变化后：" + counter2.count());
		
		
		Counter counter3 = registry.counter("http.request", "createOrder", "/order/create");
		counter3.increment();
		counter3.increment();
		System.out.println("counter3：" + counter3.measure());
		
		
	}

	public static void main(String[] args) {
		Counters test = new Counters();
		test.counter();
	}
}
