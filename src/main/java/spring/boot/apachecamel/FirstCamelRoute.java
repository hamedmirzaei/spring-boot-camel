package spring.boot.apachecamel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class FirstCamelRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:firstCamelRoute").log("Camel body: ${body}");
    }

}
