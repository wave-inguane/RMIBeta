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

	public String getUserName() throws RemoteException, InterruptedException;

	public void setUserName(String name) throws RemoteException, InterruptedException;

	public boolean calendarExist(String userName) throws RemoteException;

	public boolean createCalendar(String userName) throws RemoteException;

	public boolean addEvent(String userName, String timeInterval, String eventDescription, String accessControl) throws RemoteException;

	public String viewCalendar(String userName) throws RemoteException;

	public boolean deleteEvent(String userName, String eventTime) throws RemoteException;

	public boolean createAnotherCalendar(String userName) throws RemoteException;

	public String viewAllCalendars(String userName) throws RemoteException;

	public boolean updateEvent(String userName, String pickedTime, String modifiedTime, String eventDescription, String accessControl) throws RemoteException;

	public boolean addGroupEvent(String userName,
	                             String timeInterval,
	                             String eventDescription,
	                             String accessControl) throws RemoteException;

	public boolean modifyGroup(String userName,
	                           String groupTime,
	                           String newGroupTime,
	                           String newEventDescription) throws RemoteException;

	public boolean removeMeFromGroup(String userName, String groupTime) throws RemoteException;

	public int getMemberCount() throws RemoteException;

	public int getOpenIntervalsCheck() throws RemoteException;
}
