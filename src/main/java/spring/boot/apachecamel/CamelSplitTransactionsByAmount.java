package spring.boot.apachecamel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import spring.boot.apachecamel.model.Transaction;
import spring.boot.apachecamel.processor.XmlToObjectProcessor;

public class CamelSplitTransactionsByAmount {
    public static void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("file:src/main/resources/transactions2?delay=1000&noop=true")
                            .split(body().tokenizeXML("transaction", "transactions"))
                            .streaming()
                            .process(new XmlToObjectProcessor<Transaction>(Transaction.class))
                            .choice()
                                .when(e -> e.getIn().getBody(Transaction.class).getTransfer().getAmount() > 100)
                                    .to("direct:txbig")
                                .otherwise()
                                    .to("direct:txsmall")
                            .endChoice()
                            .end();

                    from("direct:txbig").log("#######BIG: ${body}");

                    from("direct:txsmall").log("#######SML: ${body}");
                }
            });
            camelContext.start();
        } finally {
            camelContext.stop();
        }
    }
}
