package com.tideseng.spring.demo.xml.componentScan;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ComponentScanBean {

    private String username = "章佳欢";

    private String password = "123456";

}
