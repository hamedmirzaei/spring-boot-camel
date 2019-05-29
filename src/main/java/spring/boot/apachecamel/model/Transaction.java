package spring.boot.apachecamel.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "transaction")
public class Transaction implements Serializable {
    private Transfer transfer;

    public Transfer getTransfer() {
        return transfer;
    }

    public void setTransfer(Transfer transfer) {
        this.transfer = transfer;
    }

    public Transaction() {
        super();
    }

    public Transaction(Transfer transfer) {
        super();
        this.transfer = transfer;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transfer=" + transfer +
                '}';
    }

}
