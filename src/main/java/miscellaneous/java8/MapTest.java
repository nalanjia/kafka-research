package miscellaneous.java8;

import java.util.HashMap;
import java.util.Map;

public class MapTest {

	public static void main(String[] args) {
		//只针对KEY不存在，才进Function的逻辑（新建VALUE并塞入map）
		//KEY存在的话，根本就不进Function的逻辑，VALUE不会变
//		testComputeIfAbsent();
		
		
		//不论KEY是否存在，都会进入BiFunction的逻辑（决定VALUE，完全由BiFunction的逻辑来决定）
		testCompute();
	}
	
	public static void testCompute() {
		Map<String, String> map = new HashMap<>();
		
		String value = map.compute("key1", (k, v) -> {
			//走这里，因为v确实是null
			if(v == null) {
				return "newValue";
			}
			return k + "Value";
		});
		
		System.out.println(value);
		System.out.println(map);
		
		String value2 = map.compute("key1", (k, v) -> {
			if(v == null) {
				return "newValue";
			}
			//v不为null，这里覆盖了v的值
			return k + "Value";
		});
		
		System.out.println(value2);
		System.out.println(map);
	}
	
	public static void testComputeIfAbsent() {
		Map<String, String> map = new HashMap<>();
		map.put("hasValue", "yesThisIsValue");
		String value = map.computeIfAbsent("key1", 
			k -> {
				if("key1".equals(k)) {
					return "value1";
				} else {
					return "valueOther";
				}
			}
		);
		System.out.println("KEY不存在，根据KEY，补全VALUE，并塞入map");
		System.out.println(map);
		
		
		String value2 = map.get("key1");
		System.out.println("能够查到VALUE吗：" +value2);
		
		String value3 = map.computeIfAbsent("hasValue", 
				k -> {
					if("key1".equals(k)) {
						return "value1";
					} else {
						return "valueOther";
					}
				}
			);
		
		System.out.println("KEY本来就存在，什么都不做，VALUE还是原来的VALUE");
		System.out.println(map);
	}
}
