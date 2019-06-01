package spring.boot.apachecamel;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import spring.boot.apachecamel.model.GreetingBean;
import spring.boot.apachecamel.model.Transaction;
import spring.boot.apachecamel.processor.XmlToObjectProcessor;
import spring.boot.apachecamel.service.UserService;

import javax.sql.DataSource;

@Slf4j
@SpringBootApplication
public class SpringBootApacheCamelApplication extends RouteBuilder {

    private final String PROPAGATION_NEVER_BEAN_NAME = "PROPAGATION_NEVER_BEAN";
    private final String PROPAGATION_MANDATORY_BEAN_NAME = "PROPAGATION_MANDATORY_BEAN";
    private final String PROPAGATION_REQUIRED_BEAN_NAME = "PROPAGATION_REQUIRED_BEAN";

    @Autowired
    UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootApacheCamelApplication.class, args);
    }

    @Override
    public void configure() throws Exception {

        onException(IllegalArgumentException.class)
                .handled(true)
                //.to("file:src/main/resources/transactions?fileName=deadLetters.txt&fileExist=append")
                .markRollbackOnly();

        from("jetty:http://localhost:8080/throw")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .to("direct:log-all-users")
                .transacted(PROPAGATION_REQUIRED_BEAN_NAME)
                .to("direct:change-status-to-c")
                .to("direct:change-status-to-d")
                .to("direct:throw-exception")
                .to("direct:log-all-users");

        from("direct:throw-exception")
                .log("Here in throw-exception")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        throw new IllegalArgumentException();
                    }
        });

        from("jetty:http://localhost:8080/log")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .to("direct:log-all-users");

        from("direct:log-all-users").log("Here in log-all-users").process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                userService.getAllUsers().stream().forEach(e -> log.info(e + ""));
            }
        });

        from("direct:change-status-to-c")
                .log("Here in change-status-to-c")
                .to("sql:select * from tbl_user where id <= 2")
                .split(body()).streaming()
                .to("sql:update tbl_user set status = 'IN-C' where id = :#${body[id]}");

        from("direct:change-status-to-d")
                .log("Here in change-status-to-d")
                .to("sql:select * from tbl_user where id > 2")
                .split(body()).streaming()
                .to("sql:update tbl_user set status = 'IN-D' where id = :#${body[id]}");
    }

    @Bean ("transactionManager")
    public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

    @Bean(PROPAGATION_NEVER_BEAN_NAME)
    public SpringTransactionPolicy propagationNeverPolicy(PlatformTransactionManager transactionManager) {
        SpringTransactionPolicy springTransactionPolicy = new SpringTransactionPolicy();
        springTransactionPolicy.setTransactionManager(transactionManager);
        springTransactionPolicy.setPropagationBehaviorName("PROPAGATION_NEVER");
        return springTransactionPolicy;
    }

    @Bean(PROPAGATION_MANDATORY_BEAN_NAME)
    public SpringTransactionPolicy propagationMandatoryPolicy(PlatformTransactionManager transactionManager) {
        SpringTransactionPolicy springTransactionPolicy = new SpringTransactionPolicy();
        springTransactionPolicy.setTransactionManager(transactionManager);
        springTransactionPolicy.setPropagationBehaviorName("PROPAGATION_MANDATORY");
        return springTransactionPolicy;
    }

    @Bean(PROPAGATION_REQUIRED_BEAN_NAME)
    public SpringTransactionPolicy propagationRequiredPolicy(PlatformTransactionManager transactionManager) {
        SpringTransactionPolicy springTransactionPolicy = new SpringTransactionPolicy();
        springTransactionPolicy.setTransactionManager(transactionManager);
        springTransactionPolicy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return springTransactionPolicy;
    }

}
