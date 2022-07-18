package com.tideseng.spring.demo.xml.replace;

import java.util.List;

public class OrderApi {

    /**
     * 当需要进行业务功能增强，但又不希望在原来基础上修改，可以使用replaced-method标签
     */
    public String toOrder(Long goodsId) {
        System.out.println("原始下单方法商品Id: " + goodsId);
        return "1111";
    }

    public String toOrder(List<Long> goodsIds) {
        System.out.println("原始下单方法商品Id列表: " + goodsIds);
        return "2222";
    }

}
