package com.tideseng.spring.demo.xml.loadBeanDefinition.parseDefaultElement.lookup;

import java.math.BigDecimal;

public class WeixinAccount implements Account {

    @Override
    public BigDecimal getAmount() {
        System.out.println("获取微信账户金额");
        return BigDecimal.valueOf(0.2);
    }

}
