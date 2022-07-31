package com.tideseng.spring.demo.xml.loadBeanDefinition.parseDefaultElement.lookup;

import java.math.BigDecimal;

public abstract class AbstractPay {

    public abstract Account getAccount();

    public void doPay(BigDecimal payAmount) {
        checkAccountAmout(payAmount);
        toPay(payAmount);
    }

    private void checkAccountAmout(BigDecimal payAmount){
        BigDecimal amount = getAccount().getAmount();
        if(amount.subtract(payAmount).doubleValue() < 0) {
            System.out.println("账户余额不足");
            throw new IllegalArgumentException("账户余额不足");
        }
    }

    private void toPay(BigDecimal payAmount){
        System.out.println("扣款金额：" + payAmount.doubleValue());
    }

}
