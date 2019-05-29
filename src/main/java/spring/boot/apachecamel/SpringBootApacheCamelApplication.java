package spring.boot.apachecamel;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import spring.boot.apachecamel.model.GreetingBean;
import spring.boot.apachecamel.model.Transaction;
import spring.boot.apachecamel.processor.XmlToObjectProcessor;

import javax.sql.DataSource;

@SpringBootApplication
public class SpringBootApacheCamelApplication extends RouteBuilder {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootApacheCamelApplication.class, args);
    }

    @Override
    public void configure() throws Exception {
        from("jetty:http://localhost:8080/hello")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .transacted()
                .to("direct:b")
                .log("Direct B")
                .to("direct:c")
                .log("Direct C ${body}")
                .to("bean:greetingBean?method=hello(World!)")
                .log("Greeting Bean message: ${body}");


        from("direct:b").log("HERE IN B");
        from("direct:c").log("HERE IN C");

        from("file:src/main/resources/transactions?delay=1000")
                .split(body().tokenizeXML("transaction", "transactions"))
                .streaming()
                /*.choice()
                    .when(xpath("/transaction/transfer[amount > 100]"))
                        .to("direct:txbig")
                    .otherwise()
                        .to("direct:txsmall")
                .endChoice()*/
                .process(new XmlToObjectProcessor<Transaction>(Transaction.class))
                .choice()
                    .when(e -> e.getIn().getBody(Transaction.class).getTransfer().getAmount() > 100)
                        .to("direct:txbig")
                    .otherwise()
                        .to("direct:txsmall")
                .endChoice().end();

        from("direct:txbig").log("#######BIG: ${body}");/*
                .process(new ObjectToXmlProcessor<Transaction>())
                .to("file:src/main/resources/big?fileExist=append");*/

        from("direct:txsmall").log("#######SML: ${body}");/*
                .to("file:src/main/resources/small?fileExist=append");*/
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

    @Bean("greetingBean")
    public GreetingBean greetingBean() {
        return new GreetingBean();
    }

}
