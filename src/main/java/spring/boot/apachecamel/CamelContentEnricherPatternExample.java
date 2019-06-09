package spring.boot.apachecamel;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
import org.apache.camel.util.jndi.JndiContext;
import spring.boot.apachecamel.model.GreetingBean;
import spring.boot.apachecamel.model.family.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CamelContentEnricherPatternExample {
    public static final void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        try {
            camelContext.addRoutes(new RouteBuilder() {
                public void configure() {
                    GroupedExchangeAggregationStrategy aggregationStrategy = new GroupedExchangeAggregationStrategy();
                    from("direct:family")
                            .enrich("direct:dad", aggregationStrategy)
                            .enrich("direct:mom", aggregationStrategy)
                            .enrich("direct:children", aggregationStrategy)
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // Obtain the grouped exchange
                                    List<Exchange> list = exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class);
                                    Dad dad = list.get(0).getIn().getBody(Dad.class);
                                    Mom mom = list.get(1).getIn().getBody(Mom.class);
                                    List<Child> children = list.get(2).getIn().getBody(List.class);
                                    exchange.getIn().setBody(Family.builder().dad(dad).mom(mom).children(children).build());
                                }
                            })
                            .log("################### ${body}");

                    from("direct:dad").process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            exchange.getIn().setBody(Dad.builder().firstName("Alex").lastName("Berg").build());
                        }
                    }).end();

                    from("direct:mom").process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            exchange.getIn().setBody(Mom.builder().firstName("Sarah").lastName("Berg").build());
                        }
                    }).end();

                    from("direct:children").process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            List<Child> children = Stream.iterate(1, x-> x+1).limit(10)
                                    .map(x -> {
                                        if (x % 2 == 0) {
                                            return Child.builder()
                                                    .firstName("Boy " + x)
                                                    .lastName("Berg")
                                                    .gender(Gender.MALE).build();
                                        } else {
                                            return Child.builder()
                                                    .firstName("Girl " + x)
                                                    .lastName("Berg")
                                                    .gender(Gender.FEMALE).build();
                                        }
                                    })
                                    .collect(Collectors.toList());
                            exchange.getIn().setBody(children);
                        }
                    }).end();
                }
            });
            ProducerTemplate template = camelContext.createProducerTemplate();
            camelContext.start();
            template.sendBody("direct:family", "This is an enrich example");
        } finally {
            camelContext.stop();
        }
    }
}
