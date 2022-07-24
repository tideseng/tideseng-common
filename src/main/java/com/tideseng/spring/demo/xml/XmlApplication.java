package com.tideseng.spring.demo.xml;

import com.tideseng.spring.demo.xml.componentScan.ComponentScanBean;
import com.tideseng.spring.demo.xml.constructor.ConstructorArgBean;
import com.tideseng.spring.demo.xml.lookup.AbstractPay;
import com.tideseng.spring.demo.xml.replace.OrderApi;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.math.BigDecimal;
import java.util.Arrays;

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

    @Test
    public void replace() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring/03spring-replace.xml");
        OrderApi orderApi = applicationContext.getBean(OrderApi.class);
        System.out.println(orderApi.toOrder(1L));
        System.out.println(orderApi.toOrder(Arrays.asList(1L, 2L)));
    }

    @Test
    public void constructor() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring/04spring-constructor.xml");
        ConstructorArgBean constructorArgBean = applicationContext.getBean(ConstructorArgBean.class);
        System.out.println(constructorArgBean);
    }

    @Test
    public void property() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring/05spring-property.xml");
        Student student = applicationContext.getBean(Student.class);
        System.out.println(student.getUsername());
    }

    @Test
    public void componentScan() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring/06spring-componentScan.xml");
        ComponentScanBean bean = applicationContext.getBean(ComponentScanBean.class);
        System.out.println(bean);
    }

}
