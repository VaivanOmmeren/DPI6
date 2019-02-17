package eventListener;

import messaging.requestreply.RequestReply;
import model.loan.LoanReply;
import model.loan.LoanRequest;

public interface LoanRequestListener {
    void onLoanRequest(RequestReply<LoanRequest, LoanReply> requestReply);
}
