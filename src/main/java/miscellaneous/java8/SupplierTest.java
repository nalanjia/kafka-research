package miscellaneous.java8;

import java.util.Random;
import java.util.function.Supplier;

/**
 * https://www.cnblogs.com/webor2006/p/8243874.html
 * java8学习之Supplier与函数式接口总结
 */
public class SupplierTest {
	public static void main(String[] args) {
		//生成新对象
		Supplier<Student> supplier = () -> new Student();
//		Supplier<Student> supplier = Student::new;
		
		Student stu = supplier.get();
		Student stu2 = supplier.get();
		System.out.println(stu.getAge() + " 内存地址不同了吧" + stu);
		System.out.println(stu2.getAge() + " 内存地址不同了吧" + stu2);
	}
	
	static class Student {
		private int age = 0;
		
		public Student() {
			super();
		}
		
		public int getAge() {
			if(age == 0) {
				return new Random().nextInt(10);
			}
			return age;
		}
	}

}
