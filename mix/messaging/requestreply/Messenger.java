package messaging.requestreply;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import model.loan.LoanRequest;
import org.apache.activemq.broker.Broker;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Properties;

@SuppressWarnings("Duplicates")
public class Messenger {
    ObjectMapper objectMapper = new ObjectMapper();
    Connection connection;
    Session session;
    Destination sendDestination;
    MessageProducer producer;
    Context jndiContext;
    Properties props;


    public Messenger(String destination) throws JMSException, NamingException {
        props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

        props.put(("queue." + destination), destination);

        jndiContext = new InitialContext(props);
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");

        connection = connectionFactory.createConnection();
        session = connection.createSession(false, session.AUTO_ACKNOWLEDGE);

    }

    public void sendLoanRequest(LoanRequest request, MessageListener ClientRequestReply){
        try{

            sendDestination = (Destination)jndiContext.lookup("loanBroker");
            producer = session.createProducer(sendDestination);
            String json = objectMapper.writeValueAsString(request);

            Message msg = session.createTextMessage(json);

            receiveReply(session, msg, ClientRequestReply);
            producer.send(msg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void sendBankInterestRequest(BankInterestRequest interestRequest, MessageListener BrokerBankReply) throws NamingException, JMSException, JsonProcessingException {
        sendDestination = (Destination)jndiContext.lookup("bankFrame");
        producer = session.createProducer(sendDestination);
        String json = objectMapper.writeValueAsString(interestRequest);

        Message msg = session.createTextMessage(json);

        receiveReply(session, msg, BrokerBankReply);
        producer.send(msg);
    }

    public void sendBankInterestReply(BankInterestReply bankReply, Destination replyDestination) throws JMSException, JsonProcessingException {
        producer = session.createProducer(replyDestination);

        String json = objectMapper.writeValueAsString(bankReply);
        Message message = session.createTextMessage(json);
        producer.send(message);
    }

    public void sendLoanReply(LoanReply loanReply, Destination replyDestination) throws JMSException, JsonProcessingException {
        producer = session.createProducer(replyDestination);

        String json = objectMapper.writeValueAsString(loanReply);
        Message message = session.createTextMessage(json);

        producer.send(message);
    }


    public void receiveLoanRequests(MessageListener BrokerClientRequests) throws NamingException, JMSException {
        Destination receiveDestination = (Destination)jndiContext.lookup("loanBroker");
        MessageConsumer consumer = session.createConsumer(receiveDestination);

        connection.start();

        consumer.setMessageListener(BrokerClientRequests);
    }

    public void receiveBrokerMessages(MessageListener BankBrokerRequests) throws NamingException, JMSException {
        Destination receiveDestination = (Destination)jndiContext.lookup("bankFrame");
        MessageConsumer consumer = session.createConsumer(receiveDestination);

        connection.start();

        consumer.setMessageListener(BankBrokerRequests);
    }

    private void receiveReply(Session session, Message msg, MessageListener replyListener) throws JMSException {
        TemporaryQueue destination = session.createTemporaryQueue();

        msg.setJMSReplyTo(destination);

        MessageConsumer consumer = session.createConsumer(destination);
        connection.start();
        consumer.setMessageListener(replyListener);

    }
}
