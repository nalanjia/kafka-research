package miscellaneous.java8.functionalinterface;

/**
 * 函数式接口（Functional Interface）
 * SAM接口，即Single Abstract Method interfaces
 */
@FunctionalInterface
public interface Person {
	
	/**
	 * 在这个接口里面，有且仅有一个抽象方法
	 */
	void sayHello(String msg, String msg2);
}
