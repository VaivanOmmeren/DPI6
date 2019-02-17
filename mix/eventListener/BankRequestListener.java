package eventListener;

import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

public interface BankRequestListener {

    void onBankRequest(RequestReply<BankInterestRequest, BankInterestReply> requestReply);
}
