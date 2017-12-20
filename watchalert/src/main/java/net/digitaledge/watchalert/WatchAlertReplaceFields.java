package net.digitaledge.watchalert;

public class WatchAlertReplaceFields {
	
	private String field;
	private String pattern;
	
	public void WatchAlertFields()
	{
		field = new String();
		pattern = new String();
	}
	
	public void WatchAlertFields(String field, String pattern)
	{
		this.field = field;
		this.pattern = pattern;
	}
	
	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}
