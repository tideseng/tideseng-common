package com.tideseng.spring.demo.xml.context;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ContextBean {

    private String username = "章佳欢";

    private String password = "123456";

}
