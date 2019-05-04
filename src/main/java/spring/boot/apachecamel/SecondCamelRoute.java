package spring.boot.apachecamel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SecondCamelRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:hello?period={{timer.period}}").routeId("hello").routeGroup("hello-group")
                .transform().method("simpleBean", "saySomething")
                /*.filter(simple("${body} contains 'foo'"))
                .to("log:foo")
                .end()*/
                .to("stream:out");
    }

}
