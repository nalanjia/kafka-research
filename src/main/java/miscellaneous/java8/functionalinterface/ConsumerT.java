package miscellaneous.java8.functionalinterface;

import java.util.function.Consumer;

public class ConsumerT {

	public static void method(String s, Consumer<String> con1, Consumer<String> con2) {
		//奇怪的写法
		//看似调用了2个方法，andThen和accept
		//其实，实际效果是，仅仅调用了andThen
		con1.andThen(con2).accept(s);
	}

	public static void main(String[] args) {
		method("Hello", (t) -> {
			//t是Hello
			System.out.println(t.toUpperCase());
		}, (t) -> {
			//t是Hello
			System.out.println(t.toLowerCase());
		});
	}
}
