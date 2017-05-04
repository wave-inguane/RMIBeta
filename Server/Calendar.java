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


// Libraries to handle concurrency
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Calendar extends UnicastRemoteObject implements RemCalendar {

	private static final long serialVersionUID = 1L;
	// Varibles to handle concurrency
	final Lock lock = new ReentrantLock();
	final Condition hasNotChanged = lock.newCondition();
	final Condition hasChanged = lock.newCondition();
	int sentinel = -1;

	private ArrayList<RemCalendar> chatClients; // variable to handle notification

	private Map<String, ArrayList<Event>> userCalendar = new TreeMap(); // calendar for the current user
	private Map<String, Map<String, ArrayList<Event>>> createdBy = new TreeMap(); // tracks the calendar that was created by a certain user
	private ArrayList<String> names = new ArrayList<>(); // contains all of the existing users that have calendars
	private ArrayList<String> loggedIn; // anyone who is logged in, i.e. has a Client created 
	private ArrayList<Event> generalAccess;
	private int ownerTracker; // tracks the owner of the calendar
	private String userName; // name of the user


	// Default constructor
	public Calendar() throws RemoteException {
		this.sentinel = -1;
		this.ownerTracker = 0;
		this.chatClients = new ArrayList<RemCalendar>();
		this.loggedIn = new ArrayList<>();
	}

	// Method that registers client for notifications 
	public synchronized void registerChatClient(RemCalendar chatClient) throws RemoteException {
		this.chatClients.add(chatClient);
	}


	// For each Client that was added into group event send a message
	public synchronized void broadcastMessage(String message) throws RemoteException {
		System.out.println("Server: Message > " + "broadcastMessage() invoked");

		int i = 0;
		while (i < chatClients.size()) {
			// Retrieve notification
			RemCalendar client = chatClients.get(i++);
			String name = client.getPosterName();
			//System.out.println("POSTER: "+name);
			if (!message.contains(name))
				client.retrieveMessage(message);
		}

	}

	public String getPosterName() throws RemoteException {
		return "Do not implement the Client code will take care of this";
	}

	// Returns the list of the current registered users
	public ArrayList<String> getActiveUsers() throws RemoteException {
		ArrayList<String> currNames = (ArrayList<String>) names.clone();
		return currNames;
	}

	public void retrieveMessage(String message) throws RemoteException {
		//Do Nothing
		//call client's retrieveMessage
	}

	public String getUserName() throws RemoteException, InterruptedException {
		System.out.println("Server: Message > " + "getUserName() invoked");

		lock.lock();
		try {
			while (sentinel != 0)
				hasChanged.wait();
			//System.out.println("....Enter getUserName critical section....");

			String currentUser = userName;
			--sentinel;

			//System.out.println("....Exit getUserName critical section....");
			hasNotChanged.signal();

			return currentUser;
		} finally {
			lock.unlock();
		}
	}

	// Method that updates/sets/changes the username
	public void setUserName(String name) throws RemoteException, InterruptedException {
		System.out.println("Server: Message > " + "setUserName() invoked");
		lock.lock();
		try {
			while (sentinel != -1)
				hasNotChanged.await();
			// System.out.println("....Enter setUserName critical section....");
			userName = name;
			if (!loggedIn.contains(name))
				loggedIn.add(name);

			++sentinel;

			//System.out.println("....Exit setUserName critical section....");
			hasChanged.signal();
		} finally {
			lock.unlock();
		}
	}

	// Searches if there is a registered client
	public boolean findClient(String client) throws RemoteException {
		if (loggedIn.contains(client))
			return true;
		return false;
	}

	// Checks if the username already exists
	public boolean calendarExist(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "calendarExist() invoked");
		if (names.contains(userName)) {
			return true;
		} else {
			return false;
		}
	}

	// Creates the user calendar with the given userName
	public boolean createCalendar(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "createCalendar() invoked");
		if (!names.contains(userName)) {
			//this.userName = userName;
			try {
				setUserName(userName);
				this.userName = getUserName();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.userCalendar.put(this.userName, new ArrayList<Event>());
			this.createdBy.put(this.userName + ownerTracker++, this.userCalendar);
			this.names.add(userName);
			return true;
		}
		return false;
	}

	// Add an event into the userName's calendar
	// Checks for the overlaps in the userName's calendar
	public boolean addEvent(String userName,
	                        String timeInterval,
	                        String eventDescription,
	                        String accessControl) throws RemoteException {
		System.out.println("Server: Message > " + "addEvent() invoked");

		// Check for the overlaps first
		if (userCalendar != null) {
			String[] currentTimeInterval = new String[2];
			timeInterval = timeInterval.replaceAll(" ", ""); // get rid of the whitespaces in the args timeinterval
			String[] newTimeInterval = timeInterval.split("-"); // get rid of dash and split time into two parts
			ArrayList<Integer> startTime = new ArrayList<Integer>(); // put the first part of the time of calendars into the list
			ArrayList<Integer> endTime = new ArrayList<Integer>(); // put the second part of the time of calendars into the list

			// Gather all the times of the existing user's caluendars into lists
			for (Iterator<Map.Entry<String, ArrayList<Event>>> iterator = userCalendar.entrySet().iterator(); iterator.hasNext(); ) {
				Entry<String, ArrayList<Event>> entry = iterator.next();
				String key = entry.getKey();
				if (key.equalsIgnoreCase(userName) && !accessControl.equals("Group")) {
					ArrayList<Event> apptList = entry.getValue();
					for (Event event : apptList) {
						currentTimeInterval = event.getTime().split("-");
						startTime.add(Integer.parseInt(currentTimeInterval[0]));
						endTime.add(Integer.parseInt(currentTimeInterval[1]));
					}
				}
			}
			// Check if there is any overlap
			if (!startTime.isEmpty()) {
				for (int i = 0; i < startTime.size(); i++) {
					if (Integer.parseInt(newTimeInterval[1]) >= startTime.get(i) &&
							Integer.parseInt(newTimeInterval[0]) <= endTime.get(i)) {
						return false;
					}
				}
			}
		}

		// If no overlap then add an event into the calendar
		Event appt = new Event(timeInterval, eventDescription, accessControl);
		ArrayList<Event> getApptList = userCalendar.get(userName);
		if (getApptList == null) {
			getApptList = new ArrayList<>();
			//this.userName = userName;
			try {
				setUserName(userName);
				this.userName = getUserName();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.userCalendar.put(userName, new ArrayList<Event>());
			getApptList.add(appt);
		} else {
			getApptList.add(appt);
		}

		return true;
	}


	/**
	 * Don't worry about the locks i can add thenm later
	 * you can just work on the functionality of the method
	 */
	public boolean addGroupEvent(String userName,
	                             String timeInterval,
	                             String eventDescription,
	                             String accessControl) throws RemoteException {
		if(userCalendar != null) {
			// Check for group overlaps
			String[] currentTimeInterval = new String[2];
			timeInterval = timeInterval.replaceAll(" ", ""); // get rid of the whitespaces in the args timeinterval
			String[] newTimeInterval = timeInterval.split("-"); // get rid of dash and split time into two parts
			ArrayList<Integer> startTime = new ArrayList<Integer>(); // put the first part of the time of calendars into the list
			ArrayList<Integer> endTime = new ArrayList<Integer>(); // put the second part of the time of calendars into the list

			// Gather all the times of the existing user's caluendars into lists
			for (Iterator<Map.Entry<String, ArrayList<Event>>> iterator = userCalendar.entrySet().iterator(); iterator.hasNext(); ) {
				Entry<String, ArrayList<Event>> entry = iterator.next();
				String key = entry.getKey();
				// check the group events in other calendars
				if (!key.equalsIgnoreCase(userName)) {
					ArrayList<Event> apptList = entry.getValue();
					for (Event event : apptList) {
						if(event.getAccess().equals("Group")) {
							currentTimeInterval = event.getTime().split("-");
							startTime.add(Integer.parseInt(currentTimeInterval[0]));
							endTime.add(Integer.parseInt(currentTimeInterval[1]));
						}
					}
				}
			}
			// Check if there is any overlap
			if (!startTime.isEmpty()) {
				for (int i = 0; i < startTime.size(); i++) {
					if (Integer.parseInt(newTimeInterval[1]) >= startTime.get(i) &&
							Integer.parseInt(newTimeInterval[0]) <= endTime.get(i)) {
						return false;
					}
				}
			}
		}

		//STEP 1:
		boolean flag = false;
		String recordTimeToBeRemoved = "";
		Event eventTobeRemoved = null;
		ArrayList l = null;
		StringBuilder sb = new StringBuilder();
		sb.append(eventDescription + "\n \t\tMembers: ");

		//STEP 2:
		for (String name : userCalendar.keySet()) {
			ArrayList<Event> list = userCalendar.get(name);
			for (Event event : list) {
				event.getTime().replaceAll(" ", "");
				timeInterval = timeInterval.replaceAll(" ", "");
				String[] apptTime = event.getTime().split("-");
				String[] groupTime = timeInterval.split("-");
				if ((Integer.parseInt(apptTime[0]) <= Integer.parseInt(groupTime[0]) &&
						(Integer.parseInt(apptTime[1]) >= Integer.parseInt(groupTime[1]))) &&
						event.getAccess().equalsIgnoreCase("Open")) {
	                if(!sb.toString().contains(name)) {
		                sb.append("" + name + " ");
		            }
	        	}
                if(userName.equals(name) && 
                	(Integer.parseInt(apptTime[0]) <= Integer.parseInt(groupTime[0]) &&
					(Integer.parseInt(apptTime[1]) >= Integer.parseInt(groupTime[1]))) &&
					event.getAccess().equalsIgnoreCase("Open")) {
                		eventTobeRemoved = event;
                		l = list;
				}
			}
		}

		int count = 0;
		//STEP 4:
	    for(int j = 0; j < names.size(); j++) {
	  		if(sb.toString().contains(names.get(j))) {
	  			count++;
	  		}
		}
	  	if(count > 1) {
	  		flag = addEvent(userName, timeInterval, sb.toString(), "Group");
	  		if(eventTobeRemoved != null) {
		    	l.remove(eventTobeRemoved);
			}

	  	} else {
	  		flag = addEvent(userName, timeInterval, eventDescription, accessControl);
	  	}
	  return flag;
	}

	public int memberCount = 0;
    public int openIntervalsCheck = 0;
    public int getMemberCount() {
    	return memberCount;
    }
    public int getOpenIntervalsCheck() {
    	return openIntervalsCheck;
    }

	 // Modify the group event
    public boolean modifyGroup(String userName, 
    						   String groupTime, 
    						   String newGroupTime, 
    						   String newEventDescription) {
    	memberCount = 0;
    	openIntervalsCheck = 0;
    	// Check for group time overlaps
    	if(!names.isEmpty()) {
			String[] currentTimeInterval = new String[2];
			newGroupTime = newGroupTime.replaceAll(" ", "");
			String[] newTimeInterval = newGroupTime.split("-");
			ArrayList<Integer> startTime = new ArrayList<Integer>();
			ArrayList<Integer> endTime = new ArrayList<Integer>();

			for (Iterator<Map.Entry<String, ArrayList<Event>>> iterator = userCalendar.entrySet().iterator(); iterator.hasNext(); ) {
				Entry<String, ArrayList<Event>> entry = iterator.next();
				String key = entry.getKey();
				ArrayList<Event> apptList = entry.getValue();
				for (Event event : apptList) {
					if(event.getAccess().equalsIgnoreCase("Group") && !event.getTime().equals(groupTime.replaceAll(" ", ""))) {
						currentTimeInterval = event.getTime().split("-");
						startTime.add(Integer.parseInt(currentTimeInterval[0]));
						endTime.add(Integer.parseInt(currentTimeInterval[1]));

					}
				}
			}
			if (!startTime.isEmpty()) {
				for (int i = 0; i < startTime.size(); i++) {
					if (Integer.parseInt(newTimeInterval[1]) >= startTime.get(i) &&
							Integer.parseInt(newTimeInterval[0]) <= endTime.get(i)) {
						return false;
					}
				}
			}
		}

		newEventDescription = newEventDescription + "\n \t\t Members:";
    	for (String name : userCalendar.keySet()) {
			ArrayList<Event> list = userCalendar.get(name);
			for(Event event: list) {
				if(event.getDescription().contains(userName) && 
					groupTime.equals(event.getTime()) && 
					event.getAccess().equalsIgnoreCase("Group")) {
					
					memberCount++;
					for(int i = 0; i < names.size(); i++) {
						if(event.getDescription().contains(names.get(i)) && !names.get(i).equalsIgnoreCase(userName)) {
							memberCount++;
						}
					}
					// Check if there is an the newGroupTime contains all of the members open intervals
		    		for (String name1 : userCalendar.keySet()) {
						ArrayList<Event> l = userCalendar.get(name1);
		    			for(Event a: l) {
		    				String[] apptTime = a.getTime().split("-");
					        String[] groupEventTime = newGroupTime.split("-");
					        if ((Integer.parseInt(apptTime[0]) <= Integer.parseInt(groupEventTime[0]) &&
								(Integer.parseInt(apptTime[1]) >= Integer.parseInt(groupEventTime[1]))) &&
								a.getAccess().equalsIgnoreCase("Open")) {
					        	openIntervalsCheck++;
					        }
		    			}
		    		}
		    		if((memberCount-1) == openIntervalsCheck) {
			    		for(int j = 0; j < names.size(); j++) {
			    			if(event.getDescription().contains(names.get(j))){
			    				newEventDescription += " " + names.get(j) + " ";
			    			}
			    		}
			    		event.setDescription(newEventDescription);
			    		event.setTime(newGroupTime);
			    		return true;
			    	}
				}
			}
		}
		return false;
    }


	// Displays the calendar of the userName
	public String viewCalendar(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "viewCalendar() invoked");
		int eventNumber = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("\t\t\t " + userName + "'s  CALENDAR \n");
		sb.append("..................................................................\n");
		sb.append("TIME \t\t EVENT \t\t\t ACCESS\n");
		sb.append("..................................................................\n");
		if (names.contains(userName)) {
			ArrayList<Event> list = userCalendar.get(userName); // gets the list of events for the current user (name)
			if (list != null) {
				for (Event event : list) {
					if(event.getAccess().equalsIgnoreCase("Group") && !event.getDescription().contains(userName)) {
							sb.append(event.getTime() + "\t\t" +
							"---------" + "\t\t" +
							event.getAccess() + "\n");
					} else {
						sb.append(event.getTime() + "\t\t" +
							event.getDescription() + "\t\t" +
							event.getAccess() + "\n");
					}
				}
			}
		}
		sb.append("================================================================\n");
		sb.append("\n");
		return sb.toString();
	}

	// Deletes an event from the userName's calendar
	public boolean deleteEvent(String userName, String eventTime) throws RemoteException {
		System.out.println("Server: Message > " + "deleteEvent() invoked");
		eventTime = eventTime.replaceAll(" ", ""); // get rid of whitespaces in any case
		ArrayList<Event> list = userCalendar.get(userName);
		if (list != null || !list.isEmpty()) {
			for (Event appt : list) {
				if (appt.getTime().equals(eventTime)) {
					list.remove(appt);
					return true;
				}
			}
		}
		return false;
	}

	// Modifies/Updates the event in userName's calendar
	public boolean updateEvent(String userName,
	                           String pickedTime,
	                           String modifiedTime,
	                           String eventDescription,
	                           String accessControl) throws RemoteException {

		// First checks if there is any overlap with the given modifiedTime in userName's calendar
		// Like in addEvent(), this functionality uses two lists that gather the two parts of the time
		if (userCalendar.get(userName) != null || userCalendar.get(userName).size() != 0) {
			ArrayList<Event> list = userCalendar.get(userName);
			for (Event appt : list) {
				if (appt.getTime().equals(pickedTime.replaceAll(" ", ""))) {
					String[] currentTimeInterval = new String[2];
					modifiedTime = modifiedTime.replaceAll(" ", ""); // get rid of the whitespaces in the modifiedTime
					String[] newTimeInterval = modifiedTime.split("-"); // split the modified time into two parts. for example: 10-11 gets split into [0] = 10 and [1] = 11
					ArrayList<Integer> startTime = new ArrayList<Integer>(); // have a list to hold the first part of the time of the existing user's events
					ArrayList<Integer> endTime = new ArrayList<Integer>(); // have a list to hold the second part of the time of the existing user's events

					// Iterate through the user's calendar events and record their time in the lists
					for (Iterator<Map.Entry<String, ArrayList<Event>>> iterator = userCalendar.entrySet().iterator(); iterator.hasNext(); ) {
						Entry<String, ArrayList<Event>> entry = iterator.next();
						String key = entry.getKey();
						if (key.equalsIgnoreCase(userName)) {
							ArrayList<Event> apptList = entry.getValue();
							for (Event event : apptList) {
								if (!event.getTime().equals(pickedTime.replaceAll(" ", ""))) {
									currentTimeInterval = event.getTime().split("-");
									startTime.add(Integer.parseInt(currentTimeInterval[0]));
									endTime.add(Integer.parseInt(currentTimeInterval[1]));
								}
							}
						}
					}
					// If there is any overlap then return false
					if (!startTime.isEmpty()) {
						for (int i = 0; i < startTime.size(); i++) {
							if (Integer.parseInt(newTimeInterval[1]) >= startTime.get(i) &&
									Integer.parseInt(newTimeInterval[0]) <= endTime.get(i)) {
								return false;
							}
						}
					}
					// If there is no overlap, then update the information
					appt.setTime(modifiedTime.replaceAll(" ", ""));
					appt.setDescription(eventDescription);
					appt.setAccess(accessControl);
					return true;
				}
			}
		}
		return false;
	}

	// Create a calendar for a different user
	public boolean createAnotherCalendar(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "createAnotherCalendar() invoked");
		// Check if the calendar exists first
		if (calendarExist(userName) || findClient(userName))
			return false;
		else {
			return createCalendar(userName);
		}
	}

	// Displays all user calendars
	public String viewAllCalendars() throws RemoteException {
		System.out.println("Server: Message > " + "viewAllCalendars() invoked");
		StringBuilder sb = new StringBuilder();
		if (!names.isEmpty()) {
			for (String name : userCalendar.keySet()) {
				sb.append("\t\t\t " + name + "'s  CALENDAR\n");
				sb.append(".......................................................................\n");
				sb.append("TIME \t\t EVENT \t\t\t ACCESS\n");
				sb.append(".......................................................................\n");
				ArrayList<Event> list = userCalendar.get(name);
				if (name.equalsIgnoreCase(userName)) {
					ArrayList<Event> apptList = userCalendar.get(name); // gets the list of events for the current user (name)
					for (Event event : apptList) {
						// Check if the user is the group member
						if(event.getAccess().equalsIgnoreCase("Group") && !event.getDescription().contains(userName)) {
							sb.append(event.getTime() + "\t\t" +
							"---------" + "\t\t" +
							event.getAccess() + "\n");
						} else {
							sb.append(event.getTime() + "\t\t" +
							event.getDescription() + "\t\t" +
							event.getAccess() + "\n");
						}
					}
				} else {
					// Do not display "private" events if the !this.userName
					ArrayList<Event> apptList = userCalendar.get(name); // gets the list of events for the current user (name)
					for (Event event : apptList) {
						if (!event.getAccess().equalsIgnoreCase("Private")) {
							if(event.getAccess().equalsIgnoreCase("Group") && !event.getDescription().contains(userName)) {
								sb.append(event.getTime() + "\t\t" +
								"---------" + "\t\t" +
								event.getAccess() + "\n");
							} else { 
								sb.append(event.getTime() + "\t\t" +
									event.getDescription() + "\t\t" +
									event.getAccess() + "\n");
							}
						}
					}
				}
				sb.append("***********************************************************************\n\n");
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	// Display the calendar of name to this.userName
	// If name is not this.userName, then do not display private events
	public String viewAnyCalendar(String name) throws RemoteException {
		StringBuilder sb = new StringBuilder();
		if ((isOwner(name) == true) && (userName.equals(name))) {
			String result = viewCalendar(userName);
			return result;
		} else {

			sb = new StringBuilder();
			sb.append("\t\t\t " + name + "'s  CALENDAR \n");
			sb.append("..................................................................\n");
			sb.append("TIME \t\t EVENT \t\t\t ACCESS\n");
			sb.append("..................................................................\n");
			if (names.contains(name)) {
				ArrayList<Event> list = userCalendar.get(name); // gets the list of events for the current user (name)
				if (list != null)
					for (Event event : list) {
						if(event.getAccess().equalsIgnoreCase("Group") && !event.getDescription().contains(userName)) {
							sb.append(event.getTime() + "\t\t" +
							"---------" + "\t\t" +
							event.getAccess() + "\n");
						} else if (!event.getAccess().equalsIgnoreCase("Private"))
							sb.append(event.getTime() + "\t\t" +
									event.getDescription() + "\t\t" +
									event.getAccess() + "\n");
					}
			}
			sb.append("================================================================\n");
			sb.append("\n");
		}

		return sb.toString();
	}

	// Post and event in any user's calendar
	public boolean postInAnyCalendar(String name, String timeInterval, String eventDescription, String accessControl) throws RemoteException {
	   
		for (String n : userCalendar.keySet()) {
			if(n != null && name.equalsIgnoreCase(n)) {
				return addEvent(n, timeInterval, eventDescription, accessControl);
			}
		}
		return false;
	}

	// Checks if the userName is the owner of calendar
	private boolean isOwner(String userName) throws RemoteException {

		for (Iterator<Map.Entry<String, Map<String, ArrayList<Event>>>> iterator = createdBy.entrySet().iterator(); iterator.hasNext(); ) {
			Entry<String, Map<String, ArrayList<Event>>> entry = iterator.next();

			String key = entry.getKey();
			Map<String, ArrayList<Event>> calendar = entry.getValue();

			if (calendar != null) {
				for (Iterator<Map.Entry<String, ArrayList<Event>>> iterator2 = calendar.entrySet().iterator(); iterator2.hasNext(); ) {
					Entry<String, ArrayList<Event>> entry2 = iterator2.next();
					String key2 = entry2.getKey();

					if ((key.substring(0, key2.length())).equals(key2)) { //is the owner
						generalAccess = entry2.getValue(); //return the calendar
						return true;
					}
				}
			}
		}
		return false;
	}
}
