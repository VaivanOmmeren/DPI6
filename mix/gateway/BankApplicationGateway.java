package gateway;

import eventListener.BankReplyListener;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanRequest;
import serializer.InterestSerializer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;

public class BankApplicationGateway {

    InterestSerializer seralizer = new InterestSerializer();
    MessageSenderGateway senderGateway;
    MessageReceiverGateway receiverGateway;

    private List<BankReplyListener> listeners = new ArrayList<>();

    public BankApplicationGateway(String sendDestination, String receiveDestination){

        try {
            senderGateway = new MessageSenderGateway(sendDestination);
            receiverGateway = new MessageReceiverGateway(receiveDestination);
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public void sendBankInterestRequest(LoanRequest request){

        Message msg = null;
        try {
            BankInterestRequest interestRequest = new BankInterestRequest(request.getAmount(), request.getTime());
            msg = senderGateway.createTextMessage(seralizer.requestToString(interestRequest));
            RequestReply<BankInterestRequest, BankInterestReply> interestRequestReply = new RequestReply<>(interestRequest, null);
            receiverGateway.receiveReply(msg, receivedInterestReply(interestRequestReply, request));
            senderGateway.send(msg, "bankFrame");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void addBankReplyListener(BankReplyListener listener){
        listeners.add(listener);
    }

    public void onInterestRequest(RequestReply<BankInterestRequest, BankInterestReply> requestReply, LoanRequest request){
        for(BankReplyListener l : listeners){
            l.onBankReply(requestReply, request);
        }
    }

    private MessageListener receivedInterestReply(RequestReply<BankInterestRequest, BankInterestReply> requestReply, LoanRequest request){
        return message -> {


            try {
                System.out.println("received interest reply");
                BankInterestReply interestReply = seralizer.replyFromString(((TextMessage)message).getText());
                requestReply.setReply(interestReply);
                onInterestRequest(requestReply, request);

            } catch (JMSException e) {
                e.printStackTrace();
            }
        };
    }
}
