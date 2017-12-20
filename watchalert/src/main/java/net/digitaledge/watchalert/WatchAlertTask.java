package net.digitaledge.watchalert;

import java.util.ArrayList;
import java.util.List;

public class WatchAlertTask {

	private String httpLink; 
	private String indice;
	private String querybody;
	private String timeformat;
	private Integer period;
	private String campareFlag;
	private String httpBody;
	private Integer greaterThan;
	private Integer lessThan;
	private Integer timeZoneDiff;
	private String emailFlag;
	private String smtpServer;
	private String smtpFrom;
	private String smtpPassword;
	private String smtpSubject;
	private String smtpBody;
	private List<String> recipients;
	private List<String> keywords;
	private List<String> fields;
	private List<WatchAlertReplaceFields> replaceFields;

	private Long nextExecuteTime = new Long(0);
	
	public WatchAlertTask()
	{
		this.indice = new String();
		this.querybody = new String();
		this.period = 10;
		this.greaterThan = 0;
		this.lessThan = 0;
		this.timeZoneDiff = 0;
		this.campareFlag = new String("NO_COMPARE");
		this.keywords = new ArrayList<String>();
		this.fields = new ArrayList<String>();
		this.timeformat = new String("");
		this.httpBody = new String();
		this.httpLink = new String();
		this.emailFlag = new String("NO");
		this.smtpServer = new String();
		this.smtpFrom = new String();
		this.smtpPassword = new String();
		this.smtpSubject = new String();
		this.smtpBody = new String();
		this.recipients = new ArrayList<String>();
		this.replaceFields = new ArrayList<WatchAlertReplaceFields>();
		
	}


	public String getIndice() {
		return indice;
	}
	
	public void setIndice(String indice) {
		this.indice = indice;
	}
	
	public String getQuerybody() {
		return querybody;
	}
	
	public void setQuerybody(String querybody) {
		this.querybody = querybody;
	}
	
	public Integer getPeriod() {
		return period;
	}
	
	public String getHttpLink() {
		return httpLink;
	}

	public void setHttpLink(String httpLink) {
		this.httpLink = httpLink;
	}


	public String getHttpBody() {
		return httpBody;
	}


	public void setHttpBody(String httpBody) {
		this.httpBody = httpBody;
	}
	public void setPeriod(Integer period) {
		this.period = period;
	}
	
	public String getTimeformat() {
		return timeformat;
	}
	
	public void setTimeformat(String timeformat) {
		this.timeformat = timeformat;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(String field) {
		String[] stringArray = field.split(" ");
		for(int i=0; i < stringArray.length; i++)
			this.fields.add(stringArray[i]);
	}
	
	public List<String> getKeywords() {
		return keywords;
	}
	
	public void setKeywords(String keywords) {
		String[] stringArray = keywords.split(" ");
		for(int i=0; i < stringArray.length; i++)
			this.keywords.add(stringArray[i]);
	}
	
	public Long getNextExecuteTime() {
		return nextExecuteTime;
	}
	
	public void setNextExecuteTime(Long nextExecuteTime) {
		this.nextExecuteTime = nextExecuteTime;
	}
	
	public String getCampareFlag() {
		return campareFlag;
	}

	public void setCampareFlag(String campareFlag) {
		this.campareFlag = campareFlag;
	}

	public Integer getGreaterThan() {
		return greaterThan;
	}

	public void setGreaterThan(Integer greaterThan) {
		this.greaterThan = greaterThan;
	}

	public Integer getLessThan() {
		return lessThan;
	}

	public void setLessThan(Integer lessThan) {
		this.lessThan = lessThan;
	}
	
	public Integer getTimeZoneDiff() {
		return timeZoneDiff;
	}

	public void setTimeZoneDiff(Integer timeZoneDiff) {
		this.timeZoneDiff = timeZoneDiff;
	}
	
	
	public String getEmailFlag() {
		return emailFlag;
	}

	public void setEmailFlag(String emailFlag) {
		this.emailFlag = emailFlag;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public String getSmtpFrom() {
		return smtpFrom;
	}

	public void setSmtpFrom(String smtpFrom) {
		this.smtpFrom = smtpFrom;
	}

	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	public String getSmtpSubject() {
		return smtpSubject;
	}

	public void setSmtpSubject(String smtpSubject) {
		this.smtpSubject = smtpSubject;
	}

	public String getSmtpBody() {
		return smtpBody;
	}

	public void setSmtpBody(String smtpBody) {
		this.smtpBody = smtpBody;
	}

	public List<String> getRecipients() {
		return recipients;
	}

	public void setRecipients(String recipients) {
		String[] stringArray = recipients.split(" ");
		for(int i=0; i < stringArray.length; i++)
			this.recipients.add(stringArray[i]);
	}
	
	public List<WatchAlertReplaceFields> getReplaceFields() {
		return replaceFields;
	}

	public void setReplaceFields(String fieldsFor) {
		try{
			String[] stringArray = fieldsFor.split(" ");
			for(int y = 0; y < stringArray.length; y++)
			{
				String[] stringArray2 = stringArray[y].split(":");
				if(stringArray2.length == 2)
				{
					WatchAlertReplaceFields watchAlertReplaceFields = new WatchAlertReplaceFields();
					watchAlertReplaceFields.setField(stringArray2[0]);
					watchAlertReplaceFields.setPattern(stringArray2[1]);
					this.replaceFields.add(watchAlertReplaceFields);
				}
			}
		}
		catch(Exception e){ e.toString();}
	}
}

