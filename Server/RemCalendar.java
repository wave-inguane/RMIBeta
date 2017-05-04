// RemCalendar.java interface for the RMI remote object.
// Note:  Interface must extend from java.rmi.Remote
//          Methods must throw RemoteExcpetion

import java.rmi.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public interface RemCalendar extends Remote {
	
	// Method that returns the current user's name
	public String getUserName() throws RemoteException, InterruptedException;

	// Method that updates/sets the userName to name
	public void setUserName(String name) throws RemoteException, InterruptedException;

	// Method that checks whether the userName exists
	public boolean calendarExist(String userName) throws RemoteException;

	// Method that creates a calendar for the userName
	public boolean createCalendar(String userName) throws RemoteException;

	// Method that add an event into userName's calendar
	public boolean addEvent(String userName, String timeInterval, String eventDescription, String accessControl) 
			throws RemoteException;

	// Method that displays the userName's calendar events
	public String viewCalendar(String userName) throws RemoteException;

	// Method that removes an event from userName's calendar based on eventTime
	public boolean deleteEvent(String userName, String eventTime) throws RemoteException;

	// Method that creates another user based on userName
	public boolean createAnotherCalendar(String userName) throws RemoteException;

	// Method that displays all of the users' existing calendar events
	public String viewAllCalendars() throws RemoteException;

	// Method that updates/modifies the userName's event based on pickedTime
	public boolean updateEvent(String userName, String pickedTime, String modifiedTime, String eventDescription, String accessControl) throws RemoteException;
	
	// Method that adds group event into userName's calendar
 	public boolean addGroupEvent(String userName, String timeInterval, String eventDescription, String accessControl) throws RemoteException;

 	// Method that modifies the data of the group
 	public boolean modifyGroup(String userName, String groupTime, String newGroupTime, String newEventDescription) throws RemoteException;		
	
	// Method that registers client for notifications
	public void registerChatClient(RemCalendar chatClient) throws RemoteException;
	
	// For each Client that was added into group event send a message
	public void broadcastMessage(String message)throws RemoteException;  
	 
	// Method that retrieves mesasage
	public void retrieveMessage(String message) throws RemoteException;
	
	// Method that returns the list of the current registered users
	public ArrayList<String> getActiveUsers()throws RemoteException;
	
    public String getPosterName() throws RemoteException;    
    
    // Method that searches if there is a registered client
    public boolean findClient(String client)throws RemoteException;  
    
    // Method that displays the usernName's calendar
    // If userName is not equal to this.userName then private events are not displayed
    public String viewAnyCalendar(String userName) throws RemoteException;   
   	
   	// The next two methods are purely for testing purposes
   	public int getMemberCount() throws RemoteException;

   	public int getOpenIntervalsCheck() throws RemoteException;                       
}
