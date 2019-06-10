package spring.boot.apachecamel;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.camel.*;
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

                    onException(SecurityException.class)
                            .handled(true)
                            //.transform(exceptionMessage()) // returns the exception message to consumer instead of propagating exception
                            .transform() // or you can return custom response to consumer
                            .simple("Error reported: ${exception.message} - cannot process this message.")
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    log.info("$$$$$$$$$$$$$$$$$$$$$$$$$$ " + exchange.getIn().getBody());
                                }
                            });

                    onException(NullPointerException.class)
                            .continued(true); // in case of NullPointerException, just skip and continue

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
                            .handled(true); // do not rethrow or propagate back

                    onException(IllegalArgumentException.class)
                            .maximumRedeliveries(2) // redelivery times, the following code will be executed only after the last delivery
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
                            //exchange.getIn().setHeader("random", new Random(System.currentTimeMillis()).nextInt(100));
                            exchange.getIn().setHeader("random", 70);
                        }
                    })
                    .log("@@@@@@@@@@@@@@@@@@@@@@@@@@ random is = ${header[random]}")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            if (Integer.parseInt(exchange.getIn().getHeader("random").toString()) >= 0 &&
                                    Integer.parseInt(exchange.getIn().getHeader("random").toString()) < 25) {
                                exchange.getIn().setBody("Here is an IllegalStateException");
                                throw new IllegalStateException("Here is an IllegalStateException");
                            }
                        }
                    }).process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            if (Integer.parseInt(exchange.getIn().getHeader("random").toString()) >= 25 &&
                                    Integer.parseInt(exchange.getIn().getHeader("random").toString()) < 50) {
                                exchange.getIn().setBody("Here is an IllegalArgumentException");
                                throw new IllegalArgumentException("Here is an IllegalArgumentException");
                            }
                        }
                    }).process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            if (Integer.parseInt(exchange.getIn().getHeader("random").toString()) >= 50 &&
                                    Integer.parseInt(exchange.getIn().getHeader("random").toString()) < 65) {
                                exchange.getIn().setBody("Here is an NullPointerException");
                                throw new NullPointerException("Here is an NullPointerException");
                            }
                        }
                    }).process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            if (Integer.parseInt(exchange.getIn().getHeader("random").toString()) >= 65 &&
                                    Integer.parseInt(exchange.getIn().getHeader("random").toString()) < 75) {
                                exchange.getIn().setBody("Here is an SecurityException");
                                throw new SecurityException("Here is an SecurityException");
                            }
                        }
                    }).process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            if (Integer.parseInt(exchange.getIn().getHeader("random").toString()) >= 75 &&
                                    Integer.parseInt(exchange.getIn().getHeader("random").toString()) < 100) {
                                exchange.getIn().setBody("Here is an ArithmeticException");
                                throw new ArithmeticException("Here is an ArithmeticException");
                            }
                        }
                    })
                    .log("@@@@@@@@@@@@@@@@@@@@@@@@@@ End body: ${body}")
                    .log("@@@@@@@@@@@@@@@@@@@@@@@@@@ End of route")
                    .end();
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
