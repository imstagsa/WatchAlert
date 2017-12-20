WatchAlerts plugin for Elasticsearch 2.4.1

Installation:
Download archive and install
	bin/plugin install file://path/to/file/watchalert-x.x.x.zip

Configuration elasticsearch.yml:
Each task must defined by required options. All options must be started with watchalert.taskX, where X is number of task.

Common options:
	watchalert.elastichost - IP or FQDN for Elasticsearch server. Default 127.0.0.1. Not required.
	watchalert.elasticport - Elasticsearch server port. Default 9200. Not required.
	watchalert.enabledebug - enable debug output. Possible values: true or false. Default false. Not required.

TASK Required configuration for each task:	
	watchalert.taskX.indice - for example: '/paloalto_logs-2017.01/_search?pretty'.
	watchalert.taskX.querybody - JOSN query: Example:'{"fields":["message"],"query":{"term":{"GenerateTime":"2017/01/10 16:56:32"}}}'.
	watchalert.taskX.period - checking period in seconds, for example 10.
	watchalert.taskX.fields - search keywords in fields. The list must be separated by space.
	The task must has either watchalert.taskX.keywords or watchalert.taskX.gt or watchalert.taskX.lt option. 
Additional options for each task:
	watchalert.taskX.keywords - List of words separated by space which will try to find in query results. For example: fail error auth.
	watchalert.taskX.timeformat - Time format. Default "yyyy/MM/dd HH:mm:ss".
	watchalert.taskX.value_string - reserved for future use.
	watchalert.taskX.gt - greater than. If this option is configured then plugin will try  convert value in specific fields to double and will compare it with value in this option.   
	watchalert.taskX.lt - less than. If this option is configured then plugin will try convert value in specific fields to double and will compare it with value in this option. It received value is less than expected then an alert event will fired.
	watchalert.taskX.timeZoneDiff - time zone difference in hours. Can be positive or negative. 
	watchalert.taskX.replaceFields - contain pair of words. First word is the field name in JSON. Second word is passphrases and must be uppercase. Each pair must be separated by space, the words inside pair must be separated by ":" sing. Example: newword:NEWWORD. When plugin will find %NEWWORD% in text than it will be replaces by value of field newword.  
Actions: each task must has at minimum one action. Supported two actions: send HTTP request and  	

ACTION MAIL: each task can have only one set of email settings. All of settings bellow must be present for each task except smtppassword.
	watchalert.taskX.action.smtpserver	- must contain SMTP server server. It can be in two formats : hostname and hostname:port. Default port is 25.   
	watchalert.taskX.action.smtpfrom - mail from, also used for authentication.
	watchalert.taskX.action.smtppassword - mail password. Not required. No  In case if not exists no authentication will be happened. 
	watchalert.taskX.action.recipients - list of recipients separated by space. Each record must be in format: prefix:email@domain. Prefix should be one of following: to, cc,  bcc.
	watchalert.taskX.action.smtpsubject - mail subject.
	watchalert.taskX.action.smtpbody - mail body.

ACTION HTTP: sending HTTP request, method GET. Both parameters must be present.
	watchalert.taskX.action.httplink - EMS host. Should contain POST query.
	watchalert.taskX.action.httpbody - Contains message string. If defined, it will be replace all %MESSAGE% pattern.

Reserved passphrases.
	%YEAR% - in format yyyy.
	%MONTH%  - in fromat mm, possible value 01-12.
	%DAY% - in format dd, possible value 01-31.
	%CURDATE% - see watchalert.taskX.timeformat.
	%MESSAGE% - message body. May be replaced by watchalert.taskX.message.
	%HOST% - existing host ID in EMS.
	%EPOCHTIME% - Gets the number of seconds from the Java epoch of 1970-01-01T00:00:00Z.
	%TIMESTAMP% - in format  yyyy-MM-ddTHH:mm:ss.SSSZ.
	%TIMESTAMP-PERIOD% - Current datetime minus period.Format yyyy-MM-ddTHH:mm:ss.SSSZ.
	 