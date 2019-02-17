package eventListener;

import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanRequest;

public interface BankReplyListener {

    void onBankReply(RequestReply<BankInterestRequest, BankInterestReply> requestReply, LoanRequest request);
}
