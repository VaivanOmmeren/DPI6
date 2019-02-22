package model;

import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanRequest;

import java.util.ArrayList;
import java.util.List;

public class InterestOverview {

    LoanRequest loanRequest;
    BankInterestRequest interestRequest;
    List<BankInterestReply> replyList = new ArrayList<>();
    int requestsSentOut;
    int aggregationID;

    public InterestOverview(LoanRequest loanRequest, BankInterestRequest interestRequest, int aggregationID){
        this.loanRequest = loanRequest;
        this.interestRequest = interestRequest;
        this.aggregationID = aggregationID;
        requestsSentOut = 0;
    }


    public List<BankInterestReply> getReplyList() {
        return replyList;
    }

    public void setReplyList(List<BankInterestReply> replyList) {
        this.replyList = replyList;
    }

    public int getRequestsSentOut() {
        return requestsSentOut;
    }

    public void setRequestsSentOut(int requestsSentOut) {
        this.requestsSentOut = requestsSentOut;
    }

    public int getAggregationID() {
        return aggregationID;
    }

    public void setAggregationID(int aggregationID) {
        this.aggregationID = aggregationID;
    }

    public LoanRequest getLoanRequest() {
        return loanRequest;
    }

    public void setLoanRequest(LoanRequest loanRequest) {
        this.loanRequest = loanRequest;
    }

    public BankInterestRequest getInterestRequest() {
        return interestRequest;
    }

    public void setInterestRequest(BankInterestRequest interestRequest) {
        this.interestRequest = interestRequest;
    }
}
