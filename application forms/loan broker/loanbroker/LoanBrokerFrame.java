package loanbroker;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import static java.lang.Math.toIntExact;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import messaging.requestreply.Messenger;
import messaging.requestreply.RequestReply;
import messaging.requestreply.SendMessage;
import model.bank.*;
import messaging.requestreply.ReceiveMessage;
import model.loan.LoanReply;
import model.loan.LoanRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class LoanBrokerFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
	private JList<JListLine> list;
	Messenger messengerClient;
	Messenger messengerBank;
	ObjectMapper objectMapper = new ObjectMapper();
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoanBrokerFrame frame = new LoanBrokerFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	/**
	 * Create the frame.
	 */
	public LoanBrokerFrame() throws JMSException, NamingException {
		setTitle("Loan Broker");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{46, 31, 86, 30, 89, 0};
		gbl_contentPane.rowHeights = new int[]{233, 23, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 7;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		list = new JList<JListLine>(listModel);
		scrollPane.setViewportView(list);

		messengerClient = new Messenger("loanBroker");
		messengerClient.receiveLoanRequests(BrokerClientRequest());

		messengerBank = new Messenger("bankFrame");
	}

	private MessageListener BrokerBankReply(RequestReply<BankInterestRequest, BankInterestReply> bankRequestReply){
		return message -> {
			try {
				BankInterestReply interestReply = objectMapper.readValue(((TextMessage)message).getText(), BankInterestReply.class);
				bankRequestReply.setReply(interestReply);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		};
	}

	private MessageListener BrokerClientRequest(){
		return msg -> {
			System.out.println("received message: " + msg);
			try {
				LoanRequest request = objectMapper.readValue(((TextMessage)msg).getText(), LoanRequest.class);
				add(request);


				BankInterestRequest interestRequest = new BankInterestRequest(request.getAmount(), request.getTime());
				RequestReply<BankInterestRequest, BankInterestReply> bankRequestReply = new RequestReply<>(interestRequest, null);
				messengerBank.sendBankInterestRequest(interestRequest, BrokerBankReply(bankRequestReply));

			} catch (JMSException e) {
				e.printStackTrace();
			} catch (com.fasterxml.jackson.core.JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NamingException e) {
				e.printStackTrace();
			}
		};
	}

	 private JListLine getRequestReply(LoanRequest request){
	     
	     for (int i = 0; i < listModel.getSize(); i++){
	    	 JListLine rr =listModel.get(i);
	    	 if (rr.getLoanRequest() == request){
	    		 return rr;
	    	 }
	     }
	     
	     return null;
	   }
	
	public void add(LoanRequest loanRequest){		
		listModel.addElement(new JListLine(loanRequest));		
	}
	

	public void add(LoanRequest loanRequest,BankInterestRequest bankRequest){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankRequest != null){
			rr.setBankRequest(bankRequest);
            list.repaint();
		}		
	}
	
	public void add(LoanRequest loanRequest, BankInterestReply bankReply){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankReply != null){
			rr.setBankReply(bankReply);;
            list.repaint();
		}		
	}


}
