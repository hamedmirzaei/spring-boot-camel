package spring.boot.apachecamel.processor;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

public class ObjectToXmlProcessor<T> implements Processor {

    Class<T> clazz;

    @Override
    public void process(Exchange exchange) throws Exception {
        //Create JAXB Context
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        //Create Marshaller
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        //Required formatting??
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        //Print XML String to Console
        StringWriter sw = new StringWriter();
        //Write XML to StringWriter
        jaxbMarshaller.marshal(exchange.getIn().getBody(clazz), sw);
        //Verify XML Content
        String xmlContent = sw.toString();
        exchange.getIn().setBody(xmlContent);
    }
}
