package spring.boot.apachecamel.processor;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

public class XmlToObjectProcessor<T> implements Processor {

    private Class type;

    public XmlToObjectProcessor(Class type) { this.type = type; }

    @Override
    public void process(Exchange exchange) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(type);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        T t = (T) jaxbUnmarshaller.unmarshal(new StringReader(exchange.getIn().getBody(String.class)));
        exchange.getIn().setBody(t);
    }
}
