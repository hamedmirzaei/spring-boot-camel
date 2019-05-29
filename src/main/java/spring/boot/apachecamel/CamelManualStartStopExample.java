package spring.boot.apachecamel;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.jndi.JndiContext;
import spring.boot.apachecamel.model.GreetingBean;

public class CamelManualStartStopExample {
    public static final void main(String[] args) throws Exception {
        JndiContext jndiContext = new JndiContext();
        jndiContext.bind("greetingBean", new GreetingBean());
        CamelContext camelContext = new DefaultCamelContext(jndiContext);
        try {
            camelContext.addRoutes(new RouteBuilder() {
                public void configure() {
                    from("direct:exampleName").to("bean:greetingBean?method=hello");
                }
            });
            ProducerTemplate template = camelContext.createProducerTemplate();
            camelContext.start();
            template.sendBody("direct:exampleName", "This is bean example");
            System.out.println(jndiContext.lookup("greetingBean"));
        } finally {
            camelContext.stop();
        }
    }
}
