// Echo.java interface for the RMI remote object.
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

public interface Echo extends Remote {
	String EchoMessage() throws RemoteException;

	public String getUserName() throws RemoteException;

	public void setUserName(String name) throws RemoteException;

	public boolean calendarExist(String userName) throws RemoteException;

	public boolean createCalendar(String userName) throws RemoteException;

	public boolean addEvent(String timeInterval, String eventDescription, String accessControl) throws RemoteException;

	public String viewCalendar(String userName) throws RemoteException;

	public List<String> deleteEvent(int eventNumber) throws RemoteException;

	public EchoImpl createAnotherCalendar(String userName) throws RemoteException;

	public boolean isOwner(String userName, int calendarNumber) throws RemoteException;

	public String viewAllCalendarsHelper(Map<String, List<String>> map) throws RemoteException;

	public String viewAnyCalendar(String userName, int index) throws RemoteException;

	public List<String> modifyEvent(int eventNumber) throws RemoteException;

	public String viewAllCalendars() throws RemoteException;

}
