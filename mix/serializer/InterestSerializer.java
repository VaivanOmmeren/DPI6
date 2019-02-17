package serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

import java.io.IOException;

public class InterestSerializer {

    ObjectMapper objectMapper = new ObjectMapper();

    public String requestToString(BankInterestRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BankInterestRequest requestFromString(String str) {
        try {
            return objectMapper.readValue(str, BankInterestRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String replyToString(BankInterestReply reply) {
        try {
            return objectMapper.writeValueAsString(reply);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BankInterestReply replyFromString(String str) {
        try {
            return objectMapper.readValue(str, BankInterestReply.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
