package serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import java.io.IOException;

public class LoanSerializer {

    ObjectMapper objectMapper = new ObjectMapper();

    public String requestToString(LoanRequest request){

        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public LoanRequest requestFromString(String str){
        try {
            return objectMapper.readValue(str, LoanRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String replyToString(LoanReply reply){
        try {
            return objectMapper.writeValueAsString(reply);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public LoanReply replyFromString(String str){
        try {
            return objectMapper.readValue(str, LoanReply.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

