package messaging.requestreply;

import jdk.nashorn.internal.runtime.ParserException;
import model.loan.LoanRequest;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Properties;

public class ReceiveMessage {

    Connection connection;
    Session session;

    Destination receiveDestination;
    MessageConsumer consumer;

    HashMap<LoanRequest, String> mapInterestRequest = new HashMap<>();


    public void createConsumer(String destination){
        try{
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");

            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            props.put(("queue."+ destination), destination);

            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory)jndiContext.lookup("ConnectionFactory");

            connection = connectionFactory.createConnection();
            session = connection.createSession(false, session.AUTO_ACKNOWLEDGE);

            receiveDestination = (Destination)jndiContext.lookup(destination);
            consumer = session.createConsumer(receiveDestination);

            connection.start();
        } catch (NamingException | JMSException e){
            e.printStackTrace();
        }
    }

    public void addInterestRequest(LoanRequest clientRequest, String interestRequestID){
        mapInterestRequest.put(clientRequest, interestRequestID);
    }

    public void setMessageListener(MessageListener listener) throws JMSException {
        consumer.setMessageListener(listener);
    }

    public void receiveReply(Session session, Message msg) throws JMSException {
        TemporaryQueue destination = session.createTemporaryQueue();

        msg.setJMSReplyTo(destination);

        MessageConsumer consumer = session.createConsumer(destination);



    }
}
