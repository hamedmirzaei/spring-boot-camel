package spring.boot.apachecamel;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.io.FileInputStream;
import java.io.InputStream;

public class CamelXPathExample {
    public static void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("direct:start")
                            .split(xpath("//order[@product='electronics']/items/item/text()"))
                            .to("stream:out")
                            .end();
                }
            });
            InputStream is = new FileInputStream("src/main/resources/orders.xml");
            camelContext.start();
            ProducerTemplate template = camelContext.createProducerTemplate();
            template.sendBody("direct:start", is);
        } finally {
            camelContext.stop();
        }
    }
}
