package miscellaneous.java8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StreamTest {

	public static void main(String[] args) {
		//List变Map（键重复，值覆盖）
//		list2map();
		
		//List变List
//		list2list();
		
		//Map更优雅的迭代方式：forEach
//		mapForeach();
		
		//Map按key排序
		mapSortByKey();
		
		
		
	}
	
	/**
	 * Map按key排序
	 */
	public static void mapSortByKey() {
		Map<String, String> map = new HashMap<>();
		map.put("3", "....");
		map.put("1", "....");
		map.put("4", "....");
		map.put("2", "....");
		//按key排序
		Map result = map.entrySet().stream()
			    .sorted(Map.Entry.comparingByKey())
			    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
			    (oldValue, newValue) -> oldValue, LinkedHashMap::new));		
		System.out.println(result);
	}
	
	/**
	 * Map更优雅的迭代方式：forEach
	 */
	public static void mapForeach() {
		Map<String, Object> infoMap = new HashMap<>();
		infoMap.put("name", "Zebe");
		infoMap.put("site", "www.zebe.me");
		infoMap.put("email", "zebe@vip.qq.com");
		// 传统的Map迭代方式
//		for (Map.Entry<String, Object> entry : infoMap.entrySet()) {
//		    System.out.println(entry.getKey() + "：" + entry.getValue());
//		}
		// JDK8的迭代方式
		infoMap.forEach((key, value) -> {
		    System.out.println(key + "：" + value);
		});
	}
	
	/**
	 * List变List
	 */
	public static void list2list() {
		List<Topic> topics = new ArrayList<Topic>();
		
		Topic t1 = new Topic("name2");
		Topic t2 = new Topic("name1");
		Topic t3 = new Topic("name2");
		topics.add(t1);
		topics.add(t2);
		topics.add(t3);
		
		List<String> list = topics.stream()
		.map(Topic::getName)
		.collect(Collectors.toList());
		System.out.println(list);
	}
	
	/**
	 * List变Map
	 */
	public static void list2map() {
		List<Topic> topics = new ArrayList<Topic>();
		
		Topic t1 = new Topic("name2");
		Topic t4 = new Topic("name1");
		Topic t22 = new Topic("name2");
		topics.add(t1);
		topics.add(t4);
		topics.add(t22);
		System.out.println(t1.getName() + " " + t1.hashCode());
		System.out.println(t4.getName() + " " + t4.hashCode());
		System.out.println(t22.getName() + " " + t22.hashCode());
		
		//List变Map
		Map<String, Topic> topicNameToTopic = new HashMap<>();
		topics.forEach(t -> topicNameToTopic.compute(t.getName(), (k, v) -> t));
		//打印Map
		topicNameToTopic.forEach((k, v) -> {
			System.out.println("key:value = " + k + ":" + v.hashCode());
			}
		);
		System.out.println("结论：List转Map，键重复，会丢数据");
	}
	
	
	static class Topic {
		private String name;

		public Topic(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "Topic对象" + name;
		}
		
	}
}
