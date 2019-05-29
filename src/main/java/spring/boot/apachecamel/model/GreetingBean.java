package spring.boot.apachecamel.model;

import java.util.ArrayList;
import java.util.List;

public class GreetingBean {

    private List<String> messages = new ArrayList();

    public String hello(String msg) {
        String helloMsg = "Hello " + msg;
        messages.add(helloMsg);
        return helloMsg;
    }

    public String toString() {
        return messages.toString();
    }
}
