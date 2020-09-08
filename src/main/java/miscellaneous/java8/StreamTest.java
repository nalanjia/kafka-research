package miscellaneous.java8;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamTest {

	public static void main(String[] args) {
		//List变Map（键重复，值覆盖）
//		list2map();
		
		//List变List
//		list2list();
		
		//List排序
//		listSort();
		
		//List过滤
//		list2listFilter();
		
		//Map更优雅的迭代方式：forEach
//		mapForeach();
		
		//Map按key排序
//		mapSortByKey();
		
		//平铺
		flag();
	}
	
	public static void flag() {
		List<Topic> list = new ArrayList<>();
		List<Topic> list2 = new ArrayList<>();
		Topic t1 = new Topic("name1");
		Topic t2 = new Topic("name2");
		Topic t3 = new Topic("name3");
		Topic t4 = new Topic("name4");
		list.add(t1);
		list.add(t2);
		list2.add(t3);
		list2.add(t4);
		
		List<TopicList> tl = new ArrayList<>();
		tl.add(new TopicList(list));
		tl.add(new TopicList(list2));
		tl.add(null);
		tl.add(null);
		tl.add(null);
		tl.add(null);
		tl.add(null);
		tl.add(null);
		tl.add(null);
		
		System.out.println("1.列表的数量：" + tl.size());
		
		//过滤null
		Stream<TopicList> resnull = tl.stream()
			.filter(Objects::nonNull);
		System.out.println("2.过滤掉null的数量："
//			+ resnull.count() //调用了count()，这个stream就算作废了，不能再继续运行了
			);
		
		//map的意思，将列表拿出来，组成新的Stream
		Stream<List<Topic>> res1 = resnull
			.map(TopicList::getList);
		System.out.println("3.取得属性，组成新的Stream："
//			+ res1.count()
			);
		
//		//将列表中的东西拿出来
		Stream<Topic> res2 = res1.flatMap(Collection::stream);
		System.out.println("4.flat扁平化之后："
//			+ res2.count()
				);
//		//变成列表
		List<Topic> res3 = res2.collect(Collectors.toList());
		System.out.println("5.最终变成List：" + res3.size());
		
		List<Topic> res4 = tl.stream()
			.filter(Objects::nonNull)
			.map(TopicList::getList)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());
		System.out.println("【一起写】不分开写：" + res4.size());
		
		
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
	
	public static void listSort() {
		List<Topic> topics = new ArrayList<Topic>();
		
		Topic t1 = new Topic("name2", 10);
		Topic t4 = new Topic("name2", 2);
		Topic t2 = new Topic("name1");
		Topic t3 = new Topic("name7");
		topics.add(t1);
		topics.add(t2);
		topics.add(t3);
		topics.add(t4);
		
		//reverseOrder表示降序
		List<Topic> list = topics.stream()
		.sorted(Comparator.comparing(Topic::getName,Comparator.reverseOrder()).thenComparing(Topic::getAge, Comparator.reverseOrder()))
		.collect(Collectors.toList());
		
		list.forEach(t -> {
			System.out.println(t.getName() + " " + t.getAge());
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
		.distinct().collect(Collectors.toList());
		System.out.println(list);
	}
	public static void list2listFilter() {
		List<Topic> topics = new ArrayList<Topic>();
		
		Topic t1 = new Topic("name2");
		Topic t2 = new Topic("name1");
		Topic t3 = new Topic("name2");
		topics.add(t1);
		topics.add(t2);
		topics.add(t3);
		
		List<Topic> list = topics.stream()
				.filter(t -> !t.getName().equals("name1"))
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
	
	static class TopicList {
		private List<Topic> list;
		public TopicList(List<Topic> tl) {
			this.list = tl;
		}
		public List<Topic> getList() {
			return list;
		}

		public void setList(List<Topic> list) {
			this.list = list;
		}
		
	}
	
	static class Topic {
		private String name;
		private int age;

		public Topic(String name) {
			this.name = name;
		}
		public Topic(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		@Override
		public String toString() {
			return "Topic对象" + name;
		}
		
	}
}
