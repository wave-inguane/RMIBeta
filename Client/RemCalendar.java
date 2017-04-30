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
	String EchoMessage() throws RemoteException;

	public String getUserName() throws RemoteException;

	public void setUserName(String name) throws RemoteException;

	public boolean calendarExist(String userName) throws RemoteException;

	public boolean createCalendar(String userName) throws RemoteException;

	public boolean addEvent(String userName, String timeInterval, String eventDescription, String accessControl) throws RemoteException;

	public String viewCalendar(String userName) throws RemoteException;

	public boolean deleteEvent(String userName, String eventTime) throws RemoteException;

	public boolean createAnotherCalendar(String userName) throws RemoteException;

	public boolean isOwner(String userName, int calendarNumber) throws RemoteException;

	public String viewAllCalendarsHelper(Map<String, List<String>> map) throws RemoteException;

	public ArrayList<String> modifyEvent(String userName, int eventNumber) throws RemoteException;

	public String viewAllCalendars() throws RemoteException;

	public boolean updateEvent(ArrayList<String> newEvent, String userName, int eventNumber) throws RemoteException;
}
