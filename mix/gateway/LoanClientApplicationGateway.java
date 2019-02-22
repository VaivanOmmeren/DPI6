package gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import eventListener.LoanRequestListener;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import model.loan.LoanRequest;
import serializer.InterestSerializer;
import serializer.LoanSerializer;

import javax.jms.*;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("Duplicates")
public class LoanClientApplicationGateway {

    LoanSerializer serializer = new LoanSerializer();
    InterestSerializer interestSerializer = new InterestSerializer();
    MessageSenderGateway senderGateway;
    MessageReceiverGateway receiverGateway;
    private ArrayList<LoanRequestListener> loanRequestListeners = new ArrayList<>();
    private HashMap<LoanRequest, Destination> loanRequestDestinationList = new HashMap<>();

    public LoanClientApplicationGateway(String sendDestination, String receiveDestination){

        try {
            this.senderGateway = new MessageSenderGateway(sendDestination);
            this.receiverGateway = new MessageReceiverGateway(receiveDestination);
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void receiveLoanRequest(){
       receiverGateway.startReceiving("loanBroker", loanRequestReceived());
    }

    public void onLoanRequestArrived(RequestReply<LoanRequest, LoanReply> requestReply){
        for(LoanRequestListener l : loanRequestListeners){
            l.onLoanRequest(requestReply);
        }

    }

    public void sendLoanReply(RequestReply<LoanRequest, LoanReply> loanRequestReply){

        try {
            Message msg = senderGateway.createTextMessage(serializer.replyToString(loanRequestReply.getReply()));

            senderGateway.send(msg, loanRequestDestinationList.get(loanRequestReply.getRequest()));
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public void addLoanRequestListener(LoanRequestListener listener){
        loanRequestListeners.add(listener);
    }


    private MessageListener loanRequestReceived(){
        return msg ->{
            try {
                System.out.println("loanRequestReceived" + msg);
                LoanRequest request = serializer.requestFromString(((TextMessage)msg).getText());
                loanRequestDestinationList.put(request, msg.getJMSReplyTo());
                onLoanRequestArrived(new RequestReply<>(request, null));

            } catch (JMSException e) {
                e.printStackTrace();
            }
        };
    }
}
