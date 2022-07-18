package com.tideseng.spring.demo.xml;

import com.tideseng.spring.demo.xml.lookup.AbstractPay;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.math.BigDecimal;

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

    @Test
    public void lookup() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring/02spring-lookup.xml");
        AbstractPay abstractPay = applicationContext.getBean(AbstractPay.class);
        abstractPay.doPay(BigDecimal.valueOf(0.5));
    }

}
