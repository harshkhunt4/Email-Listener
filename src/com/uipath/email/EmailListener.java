package com.uipath.email;

import javax.mail.*;
import javax.mail.search.FlagTerm;

import java.util.*;

import com.demo.uipath.UiPath;
//OutlookCredential
public class EmailListener {
	private String user;
	private String password;
	private String host = "outlook.office365.com";
	private int port = 993;
	private String folderName = "EmailListener";
	
	
	private void setUserPwd(String mailID,String pass) {
		user = mailID;
		password = pass;
	}
	
	
	private String getProcessName() throws Exception
	{
		String varProcessName = "";
		Session session = Session.getDefaultInstance(new Properties());
	    
		Store store = session.getStore("imaps");
	    store.connect(host, port, user, password);
	    
	    Folder inbox = store.getFolder(folderName);
	    inbox.open(Folder.READ_WRITE);

	    // Fetch unseen messages from inbox folder
	    Message[] messages = inbox.search(
	        new FlagTerm(new Flags(Flags.Flag.SEEN), false));

	    // Sort messages from recent to oldest
	    Arrays.sort( messages, ( m1, m2 ) -> {
	      try {
	        return m2.getSentDate().compareTo( m1.getSentDate() );
	      } catch ( MessagingException e) {
	        throw new RuntimeException(e);
	      }
	    } );
	    
	    if(messages.length > 0)
	    {
	    	Message message = messages[0];
	    	inbox.setFlags(new Message[] {message}, new Flags(Flags.Flag.SEEN), true);
		    System.out.println( 
			          "sendDate: " + message.getSentDate()
			          + " subject:" + message.getSubject());
		    String varSubject = message.getSubject();
		    varProcessName = varSubject.substring(varSubject.indexOf(":")+2);
		    System.out.println("Process Name : "+varProcessName);
	    }
	    else {
	    	throw new Exception("Exception : No new mail received");
	    }
	    
	    
	    inbox.close(false);
	    
	    return varProcessName;
	}
	public static void main(String[] args) throws Exception
	{
		try {
			EmailListener emailListener = new EmailListener();
			emailListener.setUserPwd("yourmailID@gmail.com","yourPassword");
			
			String processName = emailListener.getProcessName();
			
			UiPath Up = new UiPath();
		    Up.getAccessToken();
		    Up.getReleaseKey(processName);
		    Up.getRobotID();
		    Up.startJob();
		}catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
}
