package gateway;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class MessageSenderGateway {

    Connection connection;
    Session session;
    Destination sendDestination;
    MessageProducer producer;
    Context jndiContext;
    Properties props;

    public MessageSenderGateway(String destination) throws NamingException, JMSException {

        props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

        props.put(("queue." + destination), destination);

        jndiContext = new InitialContext(props);
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");

        connection = connectionFactory.createConnection();
        session = connection.createSession(false, session.AUTO_ACKNOWLEDGE);

    }

    public Message createTextMessage(String body) throws JMSException {

        return session.createTextMessage(body);
    }

    public void send(Message msg, Destination destination){

        try {
            producer = session.createProducer(destination);
            producer.send(msg);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void send(Message msg, String destination){

        try {
            sendDestination = (Destination)jndiContext.lookup(destination);
            producer = session.createProducer(sendDestination);
            producer.send(msg);
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }

    }
}
