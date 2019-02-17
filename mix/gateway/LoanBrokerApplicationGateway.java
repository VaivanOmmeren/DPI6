package gateway;

import eventListener.BankRequestListener;
import eventListener.LoanReplyListener;
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
import java.util.List;

@SuppressWarnings("Duplicates")
public class LoanBrokerApplicationGateway {

    private InterestSerializer interestSerializer = new InterestSerializer();
    private LoanSerializer loanSerializer = new LoanSerializer();
    private MessageSenderGateway senderGateway;
    private MessageReceiverGateway receiverGateway;

    private List<LoanReplyListener> loanReplyListenerList = new ArrayList<>();
    private List<BankRequestListener> bankRequestListenerList = new ArrayList<>();
    private HashMap<BankInterestRequest, Destination> interestReplyDestination  = new HashMap<>();

    public LoanBrokerApplicationGateway(String destination){
        try {
            this.senderGateway = new MessageSenderGateway("loanBroker");
            this.receiverGateway = new MessageReceiverGateway(destination);

        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void receiveInterestRequests(){
        receiverGateway.startReceiving("bankFrame", receiveInterestRequest());
    }


    public void applyForLoan(LoanRequest request){

        try {
            Message msg = senderGateway.createTextMessage(loanSerializer.requestToString(request));

            RequestReply<LoanRequest, LoanReply> requestReply = new RequestReply<>(request, null);
            receiverGateway.receiveReply(msg, receiveLoanReply(requestReply));
            senderGateway.send(msg, "loanBroker");
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public void sendInterestReply(RequestReply<BankInterestRequest, BankInterestReply> requestReply){

        try {

            Message msg = senderGateway.createTextMessage(interestSerializer.replyToString(requestReply.getReply()));
            senderGateway.send(msg, interestReplyDestination.get(requestReply.getRequest()));
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }


    public void onLoanReplyArrived(RequestReply<LoanRequest, LoanReply> requestReply){

        for(LoanReplyListener listener: loanReplyListenerList){
            listener.onLoanReply(requestReply);
        }

    }

    public void onInterestRequestArrived(RequestReply<BankInterestRequest, BankInterestReply> interestRequestReply){
        for(BankRequestListener l : bankRequestListenerList){
            l.onBankRequest(interestRequestReply);
        }
    }

    public void addLoanReplyListener(LoanReplyListener listener){
        loanReplyListenerList.add(listener);
    }

    public void addBankRequestListener(BankRequestListener listener){
        bankRequestListenerList.add(listener);
    }

    private MessageListener receiveInterestRequest(){
        return message ->{

            try {
                System.out.println("Received interest request: ");
                BankInterestRequest request = interestSerializer.requestFromString(((TextMessage)message).getText());
                interestReplyDestination.put(request, message.getJMSReplyTo());
                RequestReply<BankInterestRequest, BankInterestReply> requestReply = new RequestReply<>(request, null);
                onInterestRequestArrived(requestReply);

            } catch (JMSException e) {
                e.printStackTrace();
            }
        };
    }


    private MessageListener receiveLoanReply(RequestReply<LoanRequest, LoanReply> loanRequestReply){
        return message -> {

            try {
                System.out.println("received loan reply");
                LoanReply reply = loanSerializer.replyFromString(((TextMessage)message).getText());
                loanRequestReply.setReply(reply);
                onLoanReplyArrived(loanRequestReply);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        };
    }

}
