package com.tideseng.spring.demo.xml.loadBeanDefinition.parseDefaultElement.lookup;

import java.math.BigDecimal;

public class AliAccount implements Account {

    @Override
    public BigDecimal getAmount() {
        System.out.println("获取支付宝账户金额");
        return BigDecimal.valueOf(1);
    }

}
