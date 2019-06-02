package spring.boot.apachecamel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.saga.InMemorySagaService;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.rest.RestParamType;
import org.apache.camel.util.jndi.JndiContext;
import spring.boot.apachecamel.model.GreetingBean;

public class CamelInMemorySagaExample {
    public static final void main(String[] args) throws Exception {
        JndiContext jndiContext = new JndiContext();
        CamelContext camelContext = new DefaultCamelContext(jndiContext);

        camelContext.addService(new InMemorySagaService());

        try {
            camelContext.addRoutes(new RouteBuilder() {
                public void configure() {

                    from("jetty:http://localhost:8080/transfer")
                            .removeHeaders("CamelHttp*")
                            .saga()
                                .setHeader("id", constant((int)(Math.random() * 6 + 1)))
                                .log("############ ${header[id]}")
                                .setHeader("amount", constant((int)(Math.random() * 1000000 + 1)))
                                .log("############ ${header[amount]}")
                                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                            .log("Executing saga #${header.id}")
                            .to("http4://localhost:8080/credit")
                            .to("http4://localhost:8080/debit");


                    restConfiguration().port(8080).host("localhost");

                    rest().post("/credit")
                            .param().type(RestParamType.header).name("id").required(true).endParam()
                            .param().type(RestParamType.header).name("amount").required(true).endParam()
                            .route()
                            .saga()
                                .propagation(SagaPropagation.MANDATORY)
                                .option("id", header("id"))
                                .option("amount", header("amount"))
                                .compensation("direct:cancelCredit")
                            .log("Credit ${header.amount}$ for account #${header.id} is going to be done...")
                            .log("Credit for account #${header.id} done");

                    from("direct:cancelCredit")
                            .log("Credit for account #${header.id} has been cancelled");



                    rest().post("/debit")
                            .param().type(RestParamType.header).name("id").required(true).endParam()
                            .param().type(RestParamType.header).name("amount").required(true).endParam()
                            .route()
                            .saga()
                                .propagation(SagaPropagation.MANDATORY)
                                .option("id", header("id"))
                                .option("amount", header("amount"))
                                .compensation("direct:cancelDebit")
                            .log("Debit ${header.amount}$ for account #${header.id} is going to be done...")
                            .choice()
                                .when(x -> Math.random() >= 0.05)
                                    .throwException(new RuntimeException("Random failure during payment"))
                            .log("Debit for account #${header.id} done");

                    from("direct:cancelDebit")
                            .log("Debit for account #${header.id} has been cancelled");
                }
            });
            camelContext.start();
            Thread.sleep(600000);
        } finally {
            camelContext.stop();
        }
    }
}
