package gateway;


import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class MessageReceiverGateway {

    Connection connection;
    Session session;
    Destination destination;
    MessageConsumer consumer;
    Context jndiContext;
    Properties props;

    public MessageReceiverGateway(String destination) throws JMSException, NamingException {

        props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

        props.put(("queue." + destination), destination);

        jndiContext = new InitialContext(props);
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");

        connection = connectionFactory.createConnection();
        session = connection.createSession(false, session.AUTO_ACKNOWLEDGE);

    }

    public MessageReceiverGateway(){

    }


    public void startReceiving(String destination, MessageListener ml){

        try {
            Destination receiveDestination = (Destination)jndiContext.lookup(destination);
            consumer = session.createConsumer(receiveDestination);
            connection.start();

            consumer.setMessageListener(ml);
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }


    }

    public void receiveReply(Message msg, MessageListener ml){


        try {
            TemporaryQueue receiveDestination = session.createTemporaryQueue();
            msg.setJMSReplyTo(receiveDestination);

            MessageConsumer consumer = session.createConsumer(receiveDestination);
            connection.start();
            consumer.setMessageListener(ml);
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
