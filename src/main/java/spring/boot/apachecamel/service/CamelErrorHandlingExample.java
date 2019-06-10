package spring.boot.apachecamel.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

@Slf4j
public class CamelErrorHandlingExample {
    public static final void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        try {
            camelContext.addRoutes(new RouteBuilder() {
                public void configure() {


                    from("direct:error").process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            //exchange.getIn().setHeader("random", new Random(System.currentTimeMillis()).nextInt(100));
                            exchange.getIn().setHeader("random", 70);
                        }
                    });
                }
            });
            ProducerTemplate template = camelContext.createProducerTemplate();
            camelContext.start();
            template.sendBody("direct:exception", "This is an exception example");
        } finally {
            camelContext.stop();
        }
    }
}
