package spring.boot.apachecamel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import spring.boot.apachecamel.model.Transaction;
import spring.boot.apachecamel.processor.ObjectToXmlProcessor;

public class CamelSplitTransactionsByAmountToFile {
    public static void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("file:src/main/resources/transactions2?delay=1000&noop=true")
                            .split(body().tokenizeXML("transaction", "transactions"))
                            .streaming()
                            .choice()
                            .when(xpath("/transaction/transfer[amount > 100]"))
                            .to("direct:txbig")
                            .otherwise()
                            .to("direct:txsmall")
                            .endChoice()
                            .end();

                    from("direct:txbig")
                            .process(new ObjectToXmlProcessor<Transaction>())
                            .to("file:src/main/resources/big?fileExist=append");

                    from("direct:txsmall")
                            .process(new ObjectToXmlProcessor<Transaction>())
                            .to("file:src/main/resources/small?fileExist=append");
                }
            });
            camelContext.start();
        } finally {
            camelContext.stop();
        }
    }
}
