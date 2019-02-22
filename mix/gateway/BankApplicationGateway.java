package gateway;

import eventListener.BankReplyListener;
import messaging.requestreply.RequestReply;
import model.InterestOverview;
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
import java.util.HashMap;
import java.util.List;

public class BankApplicationGateway {

    InterestSerializer seralizer = new InterestSerializer();
    MessageSenderGateway senderGateway;
    MessageReceiverGateway receiverGateway;
    HashMap<String, MessageSenderGateway> recepientList = new HashMap<>();
    int aggregationID = 0;

    private List<InterestOverview> interestOverviewList = new ArrayList<>();
    private List<BankReplyListener> listeners = new ArrayList<>();

    public BankApplicationGateway(List<String> sendDestinations, String receiveDestination) {

        try {
            for (String s : sendDestinations) {
                recepientList.put(s, new MessageSenderGateway(s));
            }
            receiverGateway = new MessageReceiverGateway(receiveDestination);
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public void receiveInterestReply(){
        receiverGateway.startReceiving("BankReplyQueue", receivedInterestReply());
    }

    public void sendBankInterestRequest(LoanRequest request) {

        Message msg = null;
        BankInterestRequest interestRequest = new BankInterestRequest(request.getAmount(), request.getTime());
        InterestOverview overview = new InterestOverview(request, interestRequest, aggregationID);

        if (request.getAmount() <= 100000 && request.getTime() <= 10) {
            try {
                senderGateway = recepientList.get("ING");
                overview.setRequestsSentOut(overview.getRequestsSentOut() + 1);
                msg = senderGateway.createTextMessage(seralizer.requestToString(interestRequest));
                msg.setIntProperty("AggregationID", overview.getAggregationID());
                senderGateway.send(msg, "ING");
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }
        if (request.getAmount() >= 200000 && request.getAmount() <= 300000 && request.getTime() <= 20) {

            try {
                senderGateway = recepientList.get("ABN AMRO");
                overview.setRequestsSentOut(overview.getRequestsSentOut() + 1);
                msg = senderGateway.createTextMessage(seralizer.requestToString(interestRequest));
                msg.setIntProperty("AggregationID", overview.getAggregationID());
                senderGateway.send(msg, "ABN AMRO");
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }
        if (request.getAmount() >= 250000 && request.getTime() <= 15) {

            try {
                senderGateway = recepientList.get("RABO BANK");
                overview.setRequestsSentOut(overview.getRequestsSentOut() + 1);
                msg = senderGateway.createTextMessage(seralizer.requestToString(interestRequest));
                msg.setIntProperty("AggregationID", overview.getAggregationID());
                senderGateway.send(msg, "RABO BANK");
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

        interestOverviewList.add(overview);
        aggregationID++;

    }

    public void addBankReplyListener(BankReplyListener listener) {
        listeners.add(listener);
    }

    public void onInterestRequest(RequestReply<BankInterestRequest, BankInterestReply> requestReply, LoanRequest request) {
        for (BankReplyListener l : listeners) {
            l.onBankReply(requestReply, request);
        }
    }

    private BankInterestReply findBestRate(List<BankInterestReply> interestReplies){
        BankInterestReply bestInterest = null;

        for(BankInterestReply bir : interestReplies){

            if(bestInterest == null){
                bestInterest = bir;
            }else{
                if(bir.getInterest() < bestInterest.getInterest()){
                    bestInterest = bir;
                }
            }
        }

        return bestInterest;
    }

    private MessageListener receivedInterestReply() {
        return message -> {
            try {
                System.out.println("received an interest reply with aggregation id: " + message.getIntProperty("AggregationID"));
                BankInterestReply interestReply = seralizer.replyFromString(((TextMessage) message).getText());

                for(InterestOverview i : interestOverviewList){
                    if(i.getAggregationID() == message.getIntProperty("AggregationID")){
                        int index = -1;
                        for(BankInterestReply reply : i.getReplyList()){
                            if(reply.getQuoteId().equals(interestReply.getQuoteId())){
                                index = i.getReplyList().indexOf(reply);
                            }
                        }
                        if(index == -1){
                            i.getReplyList().add(interestReply);
                        }

                        if(i.getReplyList().size() == i.getRequestsSentOut()){
                            RequestReply<BankInterestRequest, BankInterestReply> requestReply = new RequestReply<>(i.getInterestRequest(), findBestRate(i.getReplyList()));
                            onInterestRequest(requestReply, i.getLoanRequest());
                        }
                    }
                }

            } catch (JMSException e) {
                e.printStackTrace();
            }
        };
    }
}
