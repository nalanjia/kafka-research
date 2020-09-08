package miscellaneous.java8.functionalinterface;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PredicateT {
	public static void main(String[] args) {
		int[] numbers = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
		List<Integer> list = new ArrayList<>();
		for (int i : numbers) {
			list.add(i);
		}
		
		//大于5
		Predicate<Integer> p1 = i -> i > 5;
		//小于20
		Predicate<Integer> p2 = i -> i < 20;
		//偶数
		Predicate<Integer> p3 = i -> i % 2 == 0;
		
		//过滤条件，negate表示取反，偶数的反就是奇数
		List test = list.stream().filter(p1.and(p2).and(p3.negate())).collect(Collectors.toList());
		System.out.println(test.toString());
	}
}
