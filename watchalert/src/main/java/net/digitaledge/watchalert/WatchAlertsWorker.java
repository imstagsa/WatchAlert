package net.digitaledge.watchalert;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.settings.Settings;


public class WatchAlertsWorker implements Runnable {

	private String elasticHost = new String("127.0.0.1");
	private String elasticPort = new String("9200");
	private String enableDebug = new String("false");
	
	private int arrayList = 0, key = 0;
	//private Integer retriveLogs = new Integer(0);
	private Integer maxTasks = new Integer(20);
	private ESLogger logger;
	//private String debug = new String("false");
	private WatchAlertTask[] watchAlertTaskList = new WatchAlertTask[maxTasks];
	private Boolean jsonStrated = false, parseValue = false;
	
	public WatchAlertsWorker(Settings settings, ESLogger logger)
	{
		this.logger = logger;
		this.logger.debug("WatchAlertsWorker  created");
		parseConfig(settings);
		this.logger.debug("Executed  parseConfig");
	}

	/**
	 * Parsing configuration and creating list of tasks.
	 * @param settings
	 */
	private void parseConfig(Settings settings)
	{
		logger.debug("Executing  parseConfig...");
		try{	
			
			if(settings.get("watchalert.elastichost") != null)
				elasticHost = settings.get("watchalert.elastichost");
			if(settings.get("watchalert.elasticport") != null)
				elasticPort = settings.get("watchalert.elasticport");
			if(settings.get("watchalert.enabledebug") != null)
				enableDebug = settings.get("watchalert.enabledebug");
			
			for(int i = 0; i < maxTasks; i++)
				if(
					settings.get("watchalert.task"+i+".indice") != null &&
					settings.get("watchalert.task"+i+".querybody") != null &&
					settings.get("watchalert.task"+i+".period") != null &&
					settings.get("watchalert.task"+i+".fields") != null )
				{
					
					logger.info("Found task " + i);
					WatchAlertTask watchAlertTask = new WatchAlertTask();
					watchAlertTask.setIndice(settings.get("watchalert.task"+i+".indice"));
					watchAlertTask.setQuerybody(settings.get("watchalert.task"+i+".querybody"));
					watchAlertTask.setPeriod(Integer.parseInt(settings.get("watchalert.task"+i+".period")));
					watchAlertTask.setFields(settings.get("watchalert.task"+i+".fields"));
					
					if(settings.get("watchalert.task"+i+".timeformat") == null)
						watchAlertTask.setTimeformat("");
					else watchAlertTask.setTimeformat(settings.get("watchalert.task"+i+".timeformat"));
					
					if(settings.get("watchalert.task"+i+".timeZoneDiff") != null )
					{
						watchAlertTask.setTimeZoneDiff(Integer.parseInt(settings.get("watchalert.task"+i+".timeZoneDiff")));
					}
									
					if(settings.get("watchalert.task"+i+".gt") != null )
					{
						watchAlertTask.setCampareFlag("GREATER_THAN");
						String str = settings.get("watchalert.task"+i+".gt");
						watchAlertTask.setGreaterThan(Integer.parseInt(str.trim()));
					}
					else if(settings.get("watchalert.task"+i+".lt") != null )
					{
						watchAlertTask.setCampareFlag("LESS_THAN");
						String str = settings.get("watchalert.task"+i+".lt");
						watchAlertTask.setLessThan(Integer.parseInt(str.trim()));
					}
					
					else if(settings.get("watchalert.task"+i+".keywords")!= null)
					{
						watchAlertTask.setCampareFlag("FIND_KEYWORD");
						watchAlertTask.setKeywords(settings.get("watchalert.task"+i+".keywords"));
					}
					logger.info("watchalert.task"+i+".period:" + settings.get("watchalert.task"+i+".period"));
					
					if(
						settings.get("watchalert.task"+i+".action.smtpserver") != null &&
						settings.get("watchalert.task"+i+".action.smtpfrom") != null &&
						settings.get("watchalert.task"+i+".action.recipients") != null &&
						settings.get("watchalert.task"+i+".action.smtpsubject") != null &&
						settings.get("watchalert.task"+i+".action.smtpbody") != null)
					{
						watchAlertTask.setEmailFlag("YES");
						watchAlertTask.setSmtpServer(settings.get("watchalert.task"+i+".action.smtpserver"));
						watchAlertTask.setSmtpFrom(settings.get("watchalert.task"+i+".action.smtpfrom"));
						watchAlertTask.setRecipients(settings.get("watchalert.task"+i+".action.recipients"));
						watchAlertTask.setSmtpSubject(settings.get("watchalert.task"+i+".action.smtpsubject"));
						watchAlertTask.setSmtpBody(settings.get("watchalert.task"+i+".action.smtpbody"));
						if(settings.get("watchalert.task"+i+".action.smtppassword")!= null)
							watchAlertTask.setSmtpPassword(settings.get("watchalert.task"+i+".action.smtppassword"));
					}
					
					if( settings.get("watchalert.task"+i+".action.httplink") != null &&
						settings.get("watchalert.task"+i+".action.httpbody") != null)
					{
						watchAlertTask.setHttpLink(settings.get("watchalert.task"+i+".action.httplink"));
						watchAlertTask.setHttpBody(settings.get("watchalert.task"+i+".action.httpbody"));
					}
					
					if(watchAlertTask.getCampareFlag().equals("NO_COMPARE"))
						logger.error("No options found for the task "+i+". Please set up: watchalert.task"+i+".gt or watchalert.task"+i+".lt or watchalert.task"+i+".keywords");
					else if (watchAlertTask.getEmailFlag().equals("NO") && watchAlertTask.getHttpLink().length() == 0)
						logger.error("No action defined for the task "+i+". Please set up MAIL or HTTP action, see documentation");
					else	
						watchAlertTaskList[i] = watchAlertTask;
					
					printConfig(watchAlertTask, i);
				}			
			
    	} catch (Exception e) {
    		logger.error(e.toString());
		} 	
	}
	/**
	 * Printing found tasks. For debugging use.
	 * @param settings
	 */
	private void printConfig(WatchAlertTask watchAlertTask, int index)
	{
		try{
			logger.info("--------------------------------------------------------------");
			//logger.info("Found task " + index);
			logger.info("Elastic Host: " + elasticHost);
			logger.info("Elastic Port: " + elasticPort);
			logger.info("Enable debug: " + enableDebug);
			logger.info("Task httpAction: " + watchAlertTask.getHttpLink(), watchAlertTask);
			logger.info("Task httpBody: " + watchAlertTask.getHttpBody(), watchAlertTask);
			logger.info("Task Indice: " + replaceKeywords(watchAlertTask.getIndice(), watchAlertTask));
			logger.info("Task Querybody: " + replaceKeywords(watchAlertTask.getQuerybody(), watchAlertTask));
			logger.info("Task Period: " + watchAlertTask.getPeriod());
			logger.info("Task Fields: " + watchAlertTask.getFields());
			logger.info("Task Keywords: " + watchAlertTask.getKeywords());
			logger.info("Task GreaterThan: " + watchAlertTask.getGreaterThan());
			logger.info("Task LessThan: " + watchAlertTask.getLessThan());
			logger.info("Task TimeZoneDiff: " + watchAlertTask.getTimeZoneDiff());
			if(watchAlertTask.getEmailFlag().equals("YES"))
			{
				logger.info("Task SMTP Server: " + watchAlertTask.getSmtpServer());
				logger.info("Task SMTP From: " + watchAlertTask.getSmtpFrom());
				logger.info("Task SMTP Password: " + watchAlertTask.getSmtpPassword());
				//logger.info("Task recipients: " + watchAlertTask.getsm);
				logger.info("Task SMTP Subject: " + watchAlertTask.getSmtpSubject());
				logger.info("Task SMTP Body: " + watchAlertTask.getSmtpBody());
			}		
			
    	} catch (Exception e) {
    		logger.error(e.toString());
		} 
	}
	
	/**
	 * Sends alert to EMS.
	 */
	private void sendAlert(WatchAlertTask watchAlertTask, String alertString){
		URL url;	
    	try {
    		
    		String alertMessage = replaceKeywords(watchAlertTask.getHttpBody(), watchAlertTask);
    		String alertBody = watchAlertTask.getHttpLink();
    		
    		if(watchAlertTask.getHttpBody().length() > 0)  			
    			alertMessage = alertMessage.replaceAll("%MESSAGE%", alertString);    			
    		else alertMessage = alertString;
    		
    		alertBody = alertBody.replaceAll("%MESSAGE%", alertMessage);
    		alertBody = alertBody.replaceAll(" ", "%20");
     		url = new URL(alertBody);
     		logger.info("Alert string: " + alertBody);
    		HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
    		connection.setRequestMethod("GET");
    		connection.connect();
    		logger.info("Sent Alert: " + connection.getResponseCode());
    		connection.disconnect();
    	} catch (Exception e) {
    		logger.error(e.toString());
		} 	
	}
	
	public void sendEmailWithoutAuth(final WatchAlertTask watchAlertTask, String alertBody)
	{
		try{
			String[] stringArray = watchAlertTask.getSmtpServer().split(":");
			String smtpServer = stringArray[0];
			String smtpPort = new String("25");
			if(stringArray.length == 2)
				smtpPort = stringArray[1];			 	 
			
			String alertMessage = replaceKeywords(watchAlertTask.getSmtpBody(),watchAlertTask);
			alertMessage = alertMessage.replaceAll("%MESSAGE%", alertBody);
		
			Properties props = System.getProperties();
			props.put("mail.smtp.host", smtpServer);
			props.put("mail.debug", "true");
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.socketFactory.fallback", "true");
			props.put("mail.smtp.port", smtpPort);
			props.put("java.net.preferIPv4Stack", "true");
			Session session = Session.getInstance(props);
			Message msg = new MimeMessage(session);
			for(String addressstr : watchAlertTask.getRecipients())
			{
				String[] addressstr2 = addressstr.split(":");
				InternetAddress internetAddress = new InternetAddress(addressstr2[1]);
				if(addressstr2.length > 1)
				{
					if(addressstr2[0].toLowerCase().equals("to"))
						msg.addRecipient(MimeMessage.RecipientType.TO,internetAddress);
					else if(addressstr2[0].toLowerCase().equals("cc"))
						msg.addRecipient(MimeMessage.RecipientType.CC, internetAddress);
					else if(addressstr2[0].toLowerCase().equals("bcc"))
						msg.addRecipient(MimeMessage.RecipientType.BCC, internetAddress);
					else logger.error("Cannot find prefix in \'" +addressstr+"\' . Should be one of the to, cc or bcc.");	        			
				}
				else
					logger.info("Cannot parse address \'" +addressstr+"\' . Should be in format: pefix:email@domain");
			}  
		
			msg.setFrom(new InternetAddress(watchAlertTask.getSmtpFrom()));
			msg.setSubject(watchAlertTask.getSmtpSubject());
			msg.setText(alertMessage);
			Transport transport = session.getTransport();
			transport.connect();
			transport.sendMessage(msg, msg.getAllRecipients());
			transport.close();
		}catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}
	
	/**
	 * Sending emails with authorization.
	 * @param watchAlertTask
	 */
	public void sendEmailWithAuth(final WatchAlertTask watchAlertTask, String alertBody)
	{
		logger.error("Sending email..."); 
		Transport trans=null;
		String[] stringArray = watchAlertTask.getSmtpServer().split(":");
		String smtpServer = stringArray[0];
		String smtpPort = new String("25");
		if(stringArray.length == 2)
			smtpPort = stringArray[1];
		
		String alertMessage = replaceKeywords(watchAlertTask.getSmtpBody(),watchAlertTask);
		alertMessage = alertMessage.replaceAll("%MESSAGE%", alertBody);
		
        Properties props = new Properties();  
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", smtpServer); 
        props.put("mail.smtp.port", smtpPort); 
        props.put("mail.smtp.auth", "true");  
        props.put("mail.debug", "true");              

        Session session =  
            Session.getInstance(props, new javax.mail.Authenticator() {  
                protected PasswordAuthentication getPasswordAuthentication() {  
                    return new PasswordAuthentication(watchAlertTask.getSmtpFrom(), watchAlertTask.getSmtpPassword());  
                }  
            }); 

        try {
        	Message msg = new MimeMessage(session);             
        	InternetAddress addressFrom = new InternetAddress(watchAlertTask.getSmtpFrom());  
        	msg.setFrom(addressFrom);
        	       	
        	for(String addressstr : watchAlertTask.getRecipients())
        	{
        		String[] addressstr2 = addressstr.split(":");
        		InternetAddress internetAddress = new InternetAddress(addressstr2[1]);
        		if(addressstr2.length > 1)
        		{
        			if(addressstr2[0].toLowerCase().equals("to"))
        				msg.addRecipient(MimeMessage.RecipientType.TO,internetAddress);
        			else if(addressstr2[0].toLowerCase().equals("cc"))
        				msg.addRecipient(MimeMessage.RecipientType.CC, internetAddress);
        			else if(addressstr2[0].toLowerCase().equals("bcc"))
        				msg.addRecipient(MimeMessage.RecipientType.BCC, internetAddress);
        			else logger.error("Cannot find prefix in \'" +addressstr+"\' . Should be one of the to, cc or bcc.");	        			
        		}
        		else
        			logger.error("Cannot parse address \'" +addressstr+"\' . Should be in format: pefix:email@domain");
        	}     
        	
        	msg.setSubject(watchAlertTask.getSmtpSubject());  
        	msg.setText(alertMessage);
        	trans = session.getTransport("smtp");
        	trans.connect(smtpServer, watchAlertTask.getSmtpFrom(), watchAlertTask.getSmtpPassword());
        	msg.saveChanges();
        	trans.sendMessage(msg, msg.getAllRecipients());
        	trans.close();
        	logger.info("Email successfully sent.");
        }
        catch (MessagingException mex) {
        	logger.error(mex.toString());
        }
	}
	
	private String replaceKeywords(String str, WatchAlertTask watchAlertTask)
	{
		try{
			
			str = str.replaceAll("%YEAR%", getDateTime("yyyy", watchAlertTask.getTimeZoneDiff()));
			str = str.replaceAll("%MONTH%", getDateTime("MM", watchAlertTask.getTimeZoneDiff()));
			str = str.replaceAll("%DAY%", getDateTime("dd", watchAlertTask.getTimeZoneDiff()));
			//if(watchAlertTask.getMessage().length() > 0)
			//	str = str.replaceAll("%MESSAGE%", watchAlertTask.getMessage());
			if(watchAlertTask.getTimeformat().length() > 0)
			{
				str = str.replaceAll("%TIMESTAMP%", getDateTime(watchAlertTask.getTimeformat(), watchAlertTask.getTimeZoneDiff()));
				str = str.replaceAll("%TIMESTAMP-PERIOD%", getDateTimeMinusPeriod(watchAlertTask.getTimeformat(), watchAlertTask.getTimeZoneDiff(), watchAlertTask.getPeriod()));
			}
			else
			{
				str = str.replaceAll("%TIMESTAMP%", getTimeStamp(0, watchAlertTask.getTimeZoneDiff()));
				str = str.replaceAll("%TIMESTAMP-PERIOD%", getTimeStamp(watchAlertTask.getPeriod(), watchAlertTask.getTimeZoneDiff()));
			}
			//logger.info("After watchAlertTask.getTimeformat().length()");
			str = str.replaceAll("%EPOCHTIME%", Long.toString(getEpochTime()));
			str = str.replaceAll("%CURDATE%", getDateTime(watchAlertTask.getTimeformat(), watchAlertTask.getTimeZoneDiff()));	
			str = str.replaceAll("%HOST%", "4443");
			
			return str;
		
    	} catch (Exception e) {
    		logger.error(e.toString());
    		return null;
		} 		
	}

	private String getTimeStamp(Integer seconds, Integer diff)
	{
		DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat df2 = new SimpleDateFormat("HH:mm:ss.SSS");
		Date today = Calendar.getInstance().getTime();
		today.setHours(today.getHours() + diff);
		today.setSeconds(today.getSeconds() - seconds);
		return  df1.format(today) + "T"+df2.format(today)+"Z";
	}
	
	private String getDateTime(String format, Integer diff)
	{
		DateFormat df = new SimpleDateFormat(format);
		Date today = Calendar.getInstance().getTime();
		today.setHours(today.getHours() + diff);
		today.setSeconds(today.getSeconds() - diff);
		return  df.format(today);
		
		/*DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		LocalDateTime now = LocalDateTime.now().minusHours(diff);
		return now.format(formatter);*/
		
	}
	
	private String getDateTimeMinusPeriod(String format, Integer diff, Integer seconds)
	{
		DateFormat df = new SimpleDateFormat(format);
		Date today = Calendar.getInstance().getTime();
		today.setHours(today.getHours() + diff);
		today.setSeconds(today.getSeconds() - seconds);
		return  df.format(today);
	}
	
	private Long getEpochTime()
	{
		return Instant.now().getEpochSecond();
	}
	
	private void getNewLogs(WatchAlertTask watchAlertTask)
	{
		key = 0;
		arrayList = 0;
		parseValue = false;
		jsonStrated = false;
		List<String> activeAlert = new ArrayList<String>();
		HashMap<Integer, MapVariableValue> nodes = new HashMap<Integer, MapVariableValue>();
		try {
			Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
			logger.info("getNewLogs sending request to socket.");
			String urlParameters = replaceKeywords(watchAlertTask.getQuerybody(), watchAlertTask);
			logger.info(urlParameters);
			byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
			Socket socket = new Socket(InetAddress.getByName(elasticHost), Integer.parseInt(elasticPort));
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.print("GET " + replaceKeywords(watchAlertTask.getIndice(), watchAlertTask) + " HTTP/1.1\r\n");
			pw.print("Host: "+ InetAddress.getByName(elasticHost)+":"+Integer.parseInt(elasticPort)+"\r\n");
			pw.print("Accept: */*\r\n");
			pw.print("Content-Length: " + Integer.toString(postData.length) +"\r\n");
			pw.print("Content-Type: application/x-www-form-urlencoded\r\n");
			pw.print("\r\n");
			pw.print(urlParameters);
			pw.flush();
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			    
			String t;
			logger.info("getNewLogs starting receiving from socket.");
			logger.info("123");
			while((t = br.readLine()) != null)
			{			
				String str = t.trim();
				parseLine(str, nodes);
				
				if(jsonStrated && arrayList <= 0)
				{
					jsonStrated = false;
					break;
				}
				
			}
			br.close();
			if(socket.isConnected())
				socket.close();
			logger.info("nodes.size(): " + nodes.size());
			for(int i = 0; i< nodes.size(); i++)
			{
				logger.info("Node key: " + i + "  Variable: " + nodes.get(i).getVariable() + "  value: " + nodes.get(i).getValue());
				if(nodes.get(i).getVariable() != null)
				{
					for(String field: watchAlertTask.getFields())
					{
						if(nodes.get(i).getVariable().equals(field))
						{
							if(nodes.get(i).getValue() != null)
							{	
								if(watchAlertTask.getCampareFlag().equals("LESS_THAN"))
								{
									Double value1 = Double.parseDouble(nodes.get(i).getValue());
									if(value1 < watchAlertTask.getLessThan())
									{
										activeAlert.add(nodes.get(i).getValue());
										logger.info("Found less than value: " + value1 +" in " + watchAlertTask.getLessThan());
									}
								}
								else if(watchAlertTask.getCampareFlag().equals("GREATER_THAN"))
								{
									Double value1 = Double.parseDouble(nodes.get(i).getValue());
									if(value1 > watchAlertTask.getGreaterThan())
									{
										activeAlert.add(nodes.get(i).getValue());
										logger.info("Found greater than value: " + value1 +" in " + watchAlertTask.getGreaterThan());
									}
								}
								else if(watchAlertTask.getCampareFlag().equals("FIND_KEYWORD"))
								{
									boolean firealert = false;
									for(String keyword: watchAlertTask.getKeywords())
									{
										//logger.info("Camparing keyword: " + keyword  +" in " + nodes.get(i).getValue());
										if(nodes.get(i).getValue().contains(keyword))
										{
											firealert = true;
											logger.info("Found keyword: " + keyword  +" in " + nodes.get(i).getValue());
										}
									}
									if (firealert) activeAlert.add(nodes.get(i).getValue());
								}
							}
						}
					}
				}
			}
			logger.info("activeAlert length: " + activeAlert.size());
			for(String alertBody : activeAlert)
			{
				logger.info("watchAlertTask.getEmailFlag(): " + watchAlertTask.getEmailFlag());
				if(watchAlertTask.getEmailFlag().equals("YES"))
					if(watchAlertTask.getSmtpPassword().length()>0) sendEmailWithAuth(watchAlertTask, alertBody);
					else sendEmailWithoutAuth(watchAlertTask, alertBody);
				if(watchAlertTask.getHttpLink().length() > 1)
					sendAlert(watchAlertTask, alertBody);
			}
			Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
			logger.info("Spend time: " + (timestamp2.getTime() - timestamp1.getTime()) + "ms");
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}
	
	public void executeJob()
	{
		Long now = getEpochTime();

		for(int i = 0; i < maxTasks; i++)
		{
			if(watchAlertTaskList[i] != null)
			{
				logger.info("watchAlertTaskList index " + i);
				WatchAlertTask watchAlertTask = watchAlertTaskList[i];
				logger.info("Task next timestamp: " + watchAlertTask.getNextExecuteTime() + " and now timestamp: " + now);
				if(watchAlertTask.getNextExecuteTime() <= now)
				{
					logger.info("getNewLogs");
					getNewLogs(watchAlertTask);
					watchAlertTask.setNextExecuteTime(getEpochTime() + watchAlertTask.getPeriod());
				}
			}
		}
	}
	
	@Override
	public void run() 
	{
		logger.info("WatchAlertsWorker run");
		while (true) 
		{
			logger.info("executeJob()");
			executeJob();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.error(e.toString());
			}
		}
	}
	
	public int parseVariable(String line, int startPos, HashMap<Integer, MapVariableValue> nodes)
	{
		int pos = -1, y = 1;
		String value = new String();
		boolean isParenness = false;
		for (int i = startPos ; i< line.length() - startPos; i++)
		{
			//System.out.println(line.charAt(i));
			if(line.charAt(i) == '"')
			{
				startPos = startPos + y;
				isParenness = true;
				break;
			}
			if(line.charAt(i) != ' ')
			{
				startPos = startPos + y - 1;
				break;
			}
			y++;
		}
		
		if(line.charAt(startPos) == '"')
		{
			startPos = startPos + 1;
			isParenness = true;
		}
		
		int lineLength = line.length();
		for (int i = startPos ; i < lineLength; i++)
		{	
			pos = i;
			if(isParenness)
			{
				if(line.charAt(i) != '"') 
						value = value + line.charAt(i);
				else
				{
					if(line.charAt(i-1) == '\\')
					{
						value = value + line.charAt(i);
					}
					else
					{
						pos = i;
						break;
					}	
				}
			}
			else
			{		
				if(line.charAt(i) != ',' && line.charAt(i) != '{' && line.charAt(i) != '}' && line.charAt(i) != ':' && line.charAt(i) != '\n' && line.charAt(i) != ' ' )
				{
					value = value + line.charAt(i);
				}
				else
				{
					pos = i - 1;
					break;
				}
			}
		}
		//logger.info("Nodes size before:" + nodes.size() + "  parseValue: " + parseValue + "  value: " + value);
		if(parseValue)
		{
			logger.info("Nodes add value: " + value);
			//nodes.get(key).setValue(value);
			nodes.get(key - 1).setValue(value);
		}
		else
		{
			logger.info("Nodes add variable:  " + value);
			nodes.put(key, new MapVariableValue(value));
			key++;
		}
		//logger.info("Nodes size after:" + nodes.size());
		//logger.info("Node key: " + key);
		//logger.info("Node key: " + key + "  Variable: " + nodes.get(key - 1).getVariable());
		//logger.info("Node key: " + key + "  Variable: " + nodes.get(key - 1).getVariable() + "  value: " + nodes.get(key - 1).getValue());
		return pos;
	}
	
	public void parseLine(String line, HashMap<Integer, MapVariableValue> nodes)
	{
		//logger.info("Nodes size:" + nodes.size());
		String str = line.trim();
		logger.info(str);
		for (int y=0; y<str.length(); y++)
		{
			if(str.charAt(y) == '{')
			{
				parseValue = false;
				logger.info("================== FOUND { ====================");
				jsonStrated = true;
				arrayList += 1;
			}
			if(jsonStrated)
			{
				if(str.charAt(y) == '"')
					y = parseVariable(str, y, nodes);

				if(str.charAt(y) != ' '  
					&& str.charAt(y) != '{'
					&& str.charAt(y) != '}'
					&& str.charAt(y) != '['
					&& str.charAt(y) != ']'
					&& str.charAt(y) != '\"'
					&& str.charAt(y) != ','
					&& str.charAt(y) != ' '
					&& str.charAt(y) != ':'
					)
				{
					y = parseVariable(str, y, nodes);
				
					if(y >= str.length())
						break;
				}
			
				if(str.charAt(y) == ':')
					parseValue = true;
			
				if(str.charAt(y) == ',')
					parseValue = false;
			
				if(str.charAt(y) == '}')
				{
					//jsonStarted = true;
					arrayList -= 1;
					//logger.info("================== FOUND } ====================");
				}
			}
		}
	}
}
