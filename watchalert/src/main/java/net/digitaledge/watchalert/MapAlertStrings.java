package net.digitaledge.watchalert;
import java.util.ArrayList;
import java.util.List;

public class MapAlertStrings {
	
	private String alertString;
	private List<MapVariableValue> alertMapStrings;
	
	
	public MapAlertStrings()
	{
		alertString = new String();
		alertMapStrings = new ArrayList<MapVariableValue>();
	}
	
	public String getAlertString() {
		return alertString;
	}
	public void setAlertString(String alertString) {
		this.alertString = alertString;
	}
	public List<MapVariableValue> getAlertMapStrings() {
		return alertMapStrings;
	}
	public void setAlertMapStrings(List<MapVariableValue> alertMapStrings) {
		this.alertMapStrings = alertMapStrings;
	}
	

}
