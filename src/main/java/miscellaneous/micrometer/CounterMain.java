package miscellaneous.micrometer;

import java.time.format.DateTimeFormatter;

import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.search.Search;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.Data;

public class CounterMain {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


	private static SimpleMeterRegistry registry = new SimpleMeterRegistry();
	
	public static void main(String[] args) throws Exception {
		//meterFilter这个过滤器，是在添加之前起作用的
//		registry.config()
//		.meterFilter(MeterFilter.ignoreTags("channel"))
//		;
		
		
		Order order1 = new Order();
		order1.setOrderId("ORDER_ID_1");
		order1.setAmount(100);
		order1.setChannel("CHANNEL_A");
		order1.setCreateTime("2020-08-29");
		createOrder(order1);
		Order order111 = new Order();
		order111.setOrderId("order111");
		order111.setAmount(100);
		order111.setChannel("CHANNEL_A");
		order111.setCreateTime("2020-08-29");
		createOrder(order111);
		
		
		Order order2 = new Order();
		order2.setOrderId("ORDER_ID_2");
		order2.setAmount(200);
		order2.setChannel("CHANNEL_B");
		order2.setCreateTime("2020-08-30");
		createOrder(order2);
		Order order222 = new Order();
		order222.setOrderId("order222");
		order222.setAmount(200);
		order222.setChannel("CHANNEL_B");
		order222.setCreateTime("2020-08-30");
		createOrder(order222);
		
		Search.in(registry)
			.name("order.create") //过滤
			.tag("channel", "CHANNEL_A") //过滤
			.meters()
			.forEach(each -> {
			StringBuilder builder = new StringBuilder();
			builder.append("name:").append(each.getId().getName())
				.append("\n,tags:").append(each.getId().getTags())
				.append("\n,type:").append(each.getId().getType())
				.append("\n,value:").append(each.measure())
				.append("\n-------------");
			System.out.println(builder.toString());
		});
		
	}

	private static void createOrder(Order order) {
		// 忽略订单入库等操作
		registry.counter("order.create"
				, "channel" //tag必须成对儿出现。这是key
				, order.getChannel() //紧跟着的，是value
				, "createTime"
				, order.getCreateTime()
		)
		.increment();
	}
}

@Data
class Order {
	private String orderId;
	private Integer amount;
	private String channel;
	private String createTime;
}
