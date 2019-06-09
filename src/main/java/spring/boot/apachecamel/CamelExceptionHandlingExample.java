package spring.boot.apachecamel;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
import spring.boot.apachecamel.model.family.*;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CamelExceptionHandlingExample {
    public static final void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        try {
            camelContext.addRoutes(new RouteBuilder() {
                public void configure() {

                    onException(IllegalStateException.class, ArithmeticException.class)
                            .useOriginalMessage() // this gives the original exchange
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    Throwable caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
                                    log.info("$$$$$$$$$$$$$$$$$$$$$$$$$$ " + caused.getMessage());
                                }
                            })
                            .log("########################## IllegalStateException|ArithmeticException: ${body}")
                            .handled(true);

                    onException(IllegalArgumentException.class)
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    IllegalArgumentException caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, IllegalArgumentException.class);
                                    log.info("$$$$$$$$$$$$$$$$$$$$$$$$$$ " + caused.getMessage());
                                    //exchange.getContext().createProducerTemplate().send("mock:error", exchange);
                                }
                            })
                            .log("########################## IllegalArgumentException: ${body}")
                            .handled(true);

                    from("direct:exception").process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            exchange.getIn().setHeader("random", new Random(System.currentTimeMillis()).nextInt(100));
                        }
                    }).process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            if (Integer.parseInt(exchange.getIn().getHeader("random").toString()) < 30) {
                                exchange.getIn().setBody("Here is an IllegalStateException");
                                throw new IllegalStateException("Here is an IllegalStateException");
                            }
                        }
                    }).process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            if (Integer.parseInt(exchange.getIn().getHeader("random").toString()) < 60) {
                                exchange.getIn().setBody("Here is an IllegalArgumentException");
                                throw new IllegalArgumentException("Here is an IllegalArgumentException");
                            }
                        }
                    }).process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            if (Integer.parseInt(exchange.getIn().getHeader("random").toString()) <= 100) {
                                exchange.getIn().setBody("Here is an ArithmeticException");
                                throw new ArithmeticException("Here is an ArithmeticException");
                            }
                        }
                    }).end();
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
