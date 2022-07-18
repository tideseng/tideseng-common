package com.tideseng.spring.demo.xml;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Spring应用只需要引入spring-context包即可，该包
 * @author jiahuan
 */
public class XmlApplication {

    @Test
    public void simple() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring/01spring-simple.xml");
        Student bean = applicationContext.getBean(Student.class);
        System.out.println(bean.getUsername());
    }

}
