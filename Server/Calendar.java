// Calendar.java
// Implements the remote object
// Note: The object must extend from UnicastRemoteObject
//       The object must implement the associated interface

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class Calendar extends UnicastRemoteObject implements RemCalendar {

	private Map<String, List<String>> calendar;
	private Map<Integer, Map<String, List<String>>>
			calendars = new TreeMap<Integer, Map<String, List<String>>>();
	private List<Map<String, List<String>>> allCalendars = new ArrayList<>();
	private String userName;
	private List<String> tuple;
	private int index = 0;
	private int indexKey;
	private int allCalendarsIndex = 0;
	private List<String> users = new ArrayList<String>();
	//private AList<String> users;
	//private ArrayList<String> calendarExist = new ArrayList<String>();
	private ArrayList<String> calendarExist;
	private int prevIndex[] = new int[1];


	public Calendar() throws RemoteException {
	}

	public Calendar(String userName) throws RemoteException {
		ServerData data = ServerData.load();
		if(data == null) {
			calendarExist = new ArrayList<String>();
		} else {
			calendarExist = data.getUsers();
		}
		indexKey = 0;
		userName = userName;
		users.add(userName);
		calendarExist.add(userName);
		calendar = new TreeMap<>();
		allCalendars.add(this.calendar);
		calendars.put(index, this.calendar);
		prevIndex[0] = index;
		index++;
	}

	public void saveData() {
		// Save data
		if (!ServerData.save(calendarExist)) {
			System.err.println("Can't save data.");
		}
	}

	public String getUserName() throws RemoteException {
		System.out.println("Server: Message > " + "getUserName() invoked");
		return userName;
	}

	public void setUserName(String name) throws RemoteException {
		System.out.println("Server: Message > " + "setUserName() invoked");
		userName = name;
	}

	public boolean calendarExist(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "calendarExist() invoked");
		if(calendarExist.size() == 0) {
			return false;
		} else {
			for(String name: calendarExist) {
				if(name.equalsIgnoreCase(userName)) {
					return true;
				}
			}
			return false;		
		}
	}

	// Method for creating a calendar for the incoming userName
	public boolean createCalendar(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "createCalendar() invoked");
		if (calendarExist(userName) == false) {
			new Calendar(userName);
			return true;
		}
		saveData();
		return false;
	}

	// Method for creating another calendar for a user
	public boolean createAnotherCalendar(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "createAnotherCalendar() invoked");
		// if there are no calendars at all then call createCalendar
		if(allCalendars.isEmpty()) {
			return createCalendar(userName);
		} else {
			ServerData data = ServerData.load();
			if(data == null) {
				users = new ArrayList<String>();
			} else {
				users = data.getUsers();
			}
			users.add(userName); // add a new user into the users list
			calendarExist.add(userName);
			calendar = new TreeMap<>();
			allCalendars.add(this.calendar);
			calendars.put(index++, this.calendar);

			saveData();

			return true;
		}
	}
	public boolean addEvent(String timeInterval,
	                        String eventDescription,
	                        String accessControl) throws RemoteException {
		System.out.println("Server: Message > " + "addEvent() invoked");
		int j = 0;
		tuple = new ArrayList<>();
		tuple.add(0, timeInterval);
		tuple.add(1, eventDescription);
		tuple.add(2, accessControl);

		try {
			List<String> list = calendar.get(userName + j);
			while (list != null) {
				list = calendar.get(userName + j);
				j++;
			}

			calendar.put(userName + indexKey, tuple);
			indexKey++;
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String viewCalendar(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "viewCalendar() invoked");
		StringBuilder sb = new StringBuilder();
		int tuple = 0;
		sb.append("\t\t\t " + userName + "'s  CALENDAR \n");
		sb.append("...............................................\n");
		sb.append("EVENT# \t\t TIME \t\t EVENT \t\t ACCESS\n");
		sb.append("...............................................\n");
		if (calendar != null) {
			for (Iterator<Map.Entry<String, List<String>>> iterator = calendar.entrySet().iterator(); iterator.hasNext(); ) {
				Entry<String, List<String>> entry = iterator.next();
				String key = entry.getKey();

				if (key.equalsIgnoreCase(userName + tuple)) {
					List<String> event = entry.getValue();
					sb.append(tuple + ": " + event.get(0) + "\t\t" + event.get(1) + "\t\t" + event.get(2));
				}
				tuple++;
			}
		}
		return sb.toString();
	}

	public List<String> deleteEvent(int eventNumber) throws RemoteException {
		System.out.println("Server: Message > " + "deleteEvent() invoked");
		return modifyEvent(eventNumber);
	}


	public List<String> modifyEvent(int eventNumber) throws RemoteException {
		System.out.println("Server: Message > " + "modifyEvent() invoked");
		List<String> event = null;

		//String name =

		if (calendars != null)
			for (Iterator<Map.Entry<Integer, Map<String, List<String>>>> iterator = calendars.entrySet().iterator(); iterator.hasNext(); ) {
				Entry<Integer, Map<String, List<String>>> entry = iterator.next();

				Map<String, List<String>> map = entry.getValue();

				if (map != null) {
					for (Iterator<Map.Entry<String, List<String>>> iterator2 = map.entrySet().iterator(); iterator2.hasNext(); ) {
						Entry<String, List<String>> entry2 = iterator2.next();
						String key2 = entry2.getKey();

						if (key2.equalsIgnoreCase(userName + eventNumber)) {
							event = entry2.getValue();
							//System.out.println("................ ");
							//System.out.println(" MODIFIED EVENT ");
							//System.out.println("................ ");
							//System.out.println(key2+": "+event.get(0) +"\t\t"+ event.get(1) +"\t\t"+ event.get(2));
						}
					}
				}
			}
		return event;
	}

	public boolean isOwner(String userName, int calendarNumber) throws RemoteException {
		System.out.println("Server: Message > " + "isOwner() invoked");
		String name = users.get(calendarNumber);
		if (name.equalsIgnoreCase(userName))
			return true;
		return false;
	}


	public String viewAllCalendars() throws RemoteException {
		System.out.println("Server: Message > " + "viewAllCalendars() invoked");

		StringBuilder sb = new StringBuilder();
		int i;
		if (allCalendars != null) {
			for (i = 0; i < allCalendars.size(); i++) {
				sb.append(viewAllCalendarsHelper(allCalendars.get(i)));
			}
		}
		return sb.toString();
	}


	/**
	 * This should be private we have to remove from interface
	 */
	public String viewAllCalendarsHelper(Map<String, List<String>> map) throws RemoteException {
		System.out.println("Server: Message > " + "viewAllCalendarsHelper() invoked");
		StringBuilder sb = new StringBuilder();
		int tuple = 0;
		String name = users.get(allCalendarsIndex = allCalendarsIndex % users.size());
		if (!name.equalsIgnoreCase(null)) {
			// System.out.println("[ " + allCalendarsIndex + " ] \t\t " + name + "'s  CALENDAR");
			sb.append("[ " + allCalendarsIndex + " ] \t\t " + name + "'s  CALENDAR");
		}

		// System.out.println("...............................................");
		// System.out.println("EVENT# \t\t TIME \t\t EVENT \t\t ACCESS");
		// System.out.println("...............................................");
		sb.append("...............................................");
		sb.append("EVENT# \t\t TIME \t\t EVENT \t\t ACCESS");
		sb.append("...............................................");
		if (map != null) {
			for (Iterator<Map.Entry<String, List<String>>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
				Entry<String, List<String>> entry = iterator.next();
				String key = entry.getKey();

				if (key.equalsIgnoreCase(name + tuple)) {
					List<String> event = entry.getValue();
					if (isOwner(userName, allCalendarsIndex) == true)
						sb.append(tuple + ": " + event.get(0) + "\t\t" + event.get(1) + "\t\t" + event.get(2));
						// System.out.println(tuple + ": " + event.get(0) + "\t\t" + event.get(1) + "\t\t" + event.get(2));
					else if (!event.get(2).equalsIgnoreCase("Private"))
						sb.append(tuple + ": " + event.get(0) + "\t\t" + event.get(1) + "\t\t" + event.get(2));
					// System.out.println(tuple + ": " + event.get(0) + "\t\t" + event.get(1) + "\t\t" + event.get(2));
				}
				tuple++;
			}
		}
		sb.append("-------------------------------------------------");
		sb.append("-------------------------------------------------\n");
		// System.out.println("-------------------------------------------------");
		// System.out.println("-------------------------------------------------\n");
		allCalendarsIndex++;
		return sb.toString();
	}

	public String viewAnyCalendar(String userName, int index) throws RemoteException {
		System.out.println("Server: Message > " + "viewAnyCalendar() invoked");
		StringBuilder sb = new StringBuilder();
		int tuple = 0;
		String name = userName;
		if (!name.equalsIgnoreCase(null)) {
			sb.append("\t\t\t " + users.get(index = index % users.size()) + "'s  CALENDAR");
			// System.out.println("\t\t\t " + users.get(index = index % users.size()) + "'s  CALENDAR");
		}

		sb.append("...............................................");
		sb.append("EVENT# \t\t TIME \t\t EVENT \t\t ACCESS");
		sb.append("...............................................");
		if (allCalendars != null) {
			for (Iterator<Map.Entry<String, List<String>>> iterator =
			     allCalendars.get(index = index % users.size()).entrySet().iterator(); iterator.hasNext(); ) {
				Entry<String, List<String>> entry = iterator.next();
				List<String> event = entry.getValue();

				if (isOwner(userName, index) == true)
					// System.out.println(tuple + ": " + event.get(0) + "\t\t" + event.get(1) + "\t\t" + event.get(2));
					sb.append(tuple + ": " + event.get(0) + "\t\t" + event.get(1) + "\t\t" + event.get(2));
				else if (!event.get(2).equalsIgnoreCase("Private"))
					// System.out.println(tuple + ": " + event.get(0) + "\t\t" + event.get(1) + "\t\t" + event.get(2));
					sb.append(tuple + ": " + event.get(0) + "\t\t" + event.get(1) + "\t\t" + event.get(2));

				tuple++;
			}
		}
		sb.append("-------------------------------------------------");
		sb.append("-------------------------------------------------\n");
		// System.out.println("-------------------------------------------------");
		// System.out.println("-------------------------------------------------\n");
		allCalendarsIndex++;

		return sb.toString();
	}

	// Method that checks if there any calendars in a collection
	public boolean isEmpty() throws RemoteException {
		return allCalendars.isEmpty();
	}

	// For testing purposes
	public ArrayList<String> existingUsers() throws RemoteException {
		ServerData data = ServerData.load();
		if(data == null) {
			calendarExist = new ArrayList<String>();
		} else {
			calendarExist = data.getUsers();
		}
		return calendarExist;
	}

	public String EchoMessage() throws RemoteException {
		String capitalizedMsg;
		System.out.println("Server: EchoMessage() invoked...");
		System.out.println("Server: Message > " + userName);
		// compute
		capitalizedMsg = userName;//.toUpperCase();
		// return to client
		return (capitalizedMsg);
	}
}
   
   
   

