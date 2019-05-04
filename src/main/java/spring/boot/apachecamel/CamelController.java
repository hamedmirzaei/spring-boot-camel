package spring.boot.apachecamel;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CamelController {

    @Autowired
    ProducerTemplate producerTemplate;

    @RequestMapping(value = "/")
    private void startCamelEndpoint() {
        producerTemplate.sendBody("direct:firstCamelRoute", "Calling via Spring Boot Rest Controller");
    }

}
