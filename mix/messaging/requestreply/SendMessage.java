package messaging.requestreply;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.loan.LoanRequest;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;


public class SendMessage {

    Connection connection;
    Session session;

    Destination sendDestination;
    MessageProducer producer;

    ObjectMapper objectMapper = new ObjectMapper();

    public SendMessage(String destination) throws NamingException, JMSException {

        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

        props.put(("queue." + destination), destination);

        Context jndiContext = new InitialContext(props);
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");

        connection = connectionFactory.createConnection();
        session = connection.createSession(false, session.AUTO_ACKNOWLEDGE);

        sendDestination = (Destination)jndiContext.lookup(destination);
        producer = session.createProducer(sendDestination);
    }

    public void sendLoanRequest(LoanRequest request){
        try{

            String json = objectMapper.writeValueAsString(request);

            Message msg = session.createTextMessage(json);


            ReceiveMessage receiveMessage = new ReceiveMessage();

            receiveMessage.receiveReply(session, msg);
            producer.send(msg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
