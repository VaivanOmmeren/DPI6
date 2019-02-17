package eventListener;

import messaging.requestreply.RequestReply;
import model.loan.LoanReply;
import model.loan.LoanRequest;

public interface LoanReplyListener {
    void onLoanReply(RequestReply<LoanRequest, LoanReply> requestReply);
}
