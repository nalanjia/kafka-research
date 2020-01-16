package miscellaneous.java8.functionalinterface;

public class Test {
	public static void main(String[] args) {
		Person personSay = (parma1, param2) -> System.out.println("Hello " + parma1 + "," + param2);
		personSay.sayHello("你好", "美女");
	}
}
