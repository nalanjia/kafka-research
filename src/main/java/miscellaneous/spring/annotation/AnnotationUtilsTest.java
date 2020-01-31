package miscellaneous.spring.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import com.aebiz.controller.ProducerController;

public class AnnotationUtilsTest {
	public static void main(String[] args) {

		//类注解
		RestController ann = AnnotationUtils.findAnnotation(ProducerController.class, RestController.class);
		System.out.println("类注解RestController存在吗：" + ann);
		
		Autowired ann2 = AnnotationUtils.findAnnotation(ProducerController.class, Autowired.class);
		System.out.println("类注解Autowired存在吗：" + ann2);
		
		
	}
	
	
}
