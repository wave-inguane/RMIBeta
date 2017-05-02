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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Calendar extends UnicastRemoteObject implements RemCalendar {

	// Variables to handle concurrency
	final Lock lock = new ReentrantLock();
	final Condition hasNotChanged = lock.newCondition();
	final Condition hasChanged = lock.newCondition();
	int sentinel = -1;

	private Map<String, ArrayList<Appointment>> userCalendar = new TreeMap(); // calendar for the current user
	private ArrayList<String> names = new ArrayList<>();
	private String userName;
	// private Map<Integer, <Map<String, ArrayList<String>>> groupEvents = new TreeMap(); // String refers to the name that is inside a Group event, ArrayList is his times
	private int groupEventIndex = 0;

	public Calendar() throws RemoteException {
		sentinel = -1;
	}

   /*
	public String getUserName() throws RemoteException {
		System.out.println("Server: Message > " + "getUserName() invoked");
		return userName;
	}
	

	public void setUserName(String name) throws RemoteException {
		System.out.println("Server: Message > " + "setUserName() invoked");
		this.userName = name;
	}
	*/

	public String getUserName() throws RemoteException, InterruptedException {
		System.out.println("Server: Message > " + "getUserName() invoked");

		lock.lock();
		try {
			while (sentinel != 0)
				hasChanged.wait();
			System.out.println("....Enter getUserName critical section....");

			String currentUser = userName;
			--sentinel;

			System.out.println("....Exit getUserName critical section....");
			hasNotChanged.signal();

			return currentUser;
		} finally {
			lock.unlock();
		}
	}

	public void setUserName(String name) throws RemoteException, InterruptedException {
		System.out.println("Server: Message > " + "setUserName() invoked");
		lock.lock();
		try {
			while (sentinel != -1)
				hasNotChanged.await();
			System.out.println("....Enter setUserName critical section....");

			userName = name;
			++sentinel;

			System.out.println("....Exit setUserName critical section....");
			hasChanged.signal();
		} finally {
			lock.unlock();
		}
	}

	public boolean calendarExist(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "calendarExist() invoked");
		if (names.contains(userName)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean createCalendar(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "createCalendar() invoked");
		if (!names.contains(userName)) {
			this.userCalendar.put(userName, new ArrayList<Appointment>());
			this.names.add(userName);
			return true;
		}
		return false;
	}

	public boolean addEvent(String userName,
	                        String timeInterval,
	                        String eventDescription,
	                        String accessControl) throws RemoteException {
		System.out.println("Server: Message > " + "addEvent() invoked");

		if (userCalendar != null) {
			String[] currentTimeInterval = new String[2];
			timeInterval = timeInterval.replaceAll(" ", "");
			String[] newTimeInterval = timeInterval.split("-");
			ArrayList<Integer> startTime = new ArrayList<Integer>();
			ArrayList<Integer> endTime = new ArrayList<Integer>();

			for (Iterator<Map.Entry<String, ArrayList<Appointment>>> iterator = userCalendar.entrySet().iterator(); iterator.hasNext(); ) {
				Entry<String, ArrayList<Appointment>> entry = iterator.next();
				String key = entry.getKey();
				if (key.equalsIgnoreCase(userName)) {
					ArrayList<Appointment> apptList = entry.getValue();
					for (Appointment appointment : apptList) {
						currentTimeInterval = appointment.getTime().split("-");
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
		Appointment appt = new Appointment(timeInterval, eventDescription, accessControl);
		ArrayList<Appointment> getApptList = userCalendar.get(userName);
		if (getApptList == null) {
			getApptList = new ArrayList<>();
			try {
				setUserName(userName);
				this.userName = getUserName();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.userCalendar.put(userName, new ArrayList<Appointment>());
			getApptList.add(appt);
		} else {
			getApptList.add(appt);
		}

		return true;
	}

	public boolean addGroupEvent(String userName,
                            String timeInterval,
                            String eventDescription,
                            String accessControl) throws RemoteException {
             
	  //STEP 1:
	  boolean flag = false;     
	  StringBuilder sb = new StringBuilder();
	  sb.append(eventDescription + "\n \t\tMembers: ");
	  
	  //STEP 3: 
	  int i = 0;
	  String addMe;
	  while(i < names.size()){
	    ArrayList<Appointment> list = userCalendar.get(addMe = names.get(i++));
	    
	    for(Appointment appointment: list) {
	    	String[] apptTime = appointment.getTime().split("-");
	        String[] groupTime = timeInterval.split("-");
	        if((Integer.parseInt(apptTime[0]) >= Integer.parseInt(groupTime[0]) &&
	            (Integer.parseInt(apptTime[0]) < Integer.parseInt(groupTime[1]))) &&
	            ((Integer.parseInt(apptTime[1]) > Integer.parseInt(groupTime[0])) &&
	            (Integer.parseInt(apptTime[1]) <= Integer.parseInt(groupTime[1]))) && 
	            appointment.getAccess().equalsIgnoreCase("Open")) {
	               	if(!sb.toString().contains(userName)) {
	                	sb.append("" + userName + " " + addMe + " ");
	                } else if(!sb.toString().contains(addMe)) {
	                	sb.append("" + addMe + " ");
	                }
	         }
	      }
	  }
	  // sb.append("\n");
	  
	  //STEP 4:
	  boolean isGroup = false; 
	  for(int j = 0; j < names.size(); j++) {
	  	if(sb.toString().contains(names.get(j))) {
	  		isGroup = true;
	  		break;
	  	}
	  }
	  if(isGroup) {
	  	flag = addEvent(userName, timeInterval, sb.toString(), "Group");
	  } else {
	  	flag = addEvent(userName, timeInterval, sb.toString(), accessControl);
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

			for (Iterator<Map.Entry<String, ArrayList<Appointment>>> iterator = userCalendar.entrySet().iterator(); iterator.hasNext(); ) {
				Entry<String, ArrayList<Appointment>> entry = iterator.next();
				String key = entry.getKey();
				ArrayList<Appointment> apptList = entry.getValue();
				for (Appointment appointment : apptList) {
					if(!appointment.getTime().equals(groupTime) && appointment.getAccess().equalsIgnoreCase("Group")) {
						currentTimeInterval = appointment.getTime().split("-");
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

    	int i = 0;
    	String addMe;
    	newEventDescription = newEventDescription + "\n \t\t Members:";
    	while(i < names.size()){
		    ArrayList<Appointment> list = userCalendar.get(addMe = names.get(i++));
		      
		    for(Appointment appointment: list) {
		    	if(groupTime.equals(appointment.getTime()) && appointment.getAccess().equalsIgnoreCase("Group")) {

		    		// Get the number of participants in the group
		    		for(int j = 0; j < names.size(); j++) {
		    			if(appointment.getDescription().contains(names.get(j)) && !names.get(j).equals(userName)){
		    				memberCount++;
		    			}
		    		}
		    		// Check if there is an the newGroupTime contains all of the members open intervals
		    		for(int j = 0; j < names.size(); j++) {
		    			ArrayList<Appointment> l = userCalendar.get(names.get(j));
		    			for(Appointment a: l) {
		    				String[] apptTime = a.getTime().split("-");
					        String[] groupEventTime = newGroupTime.split("-");
					        if(((Integer.parseInt(apptTime[0]) >= Integer.parseInt(groupEventTime[0]) &&
					            (Integer.parseInt(apptTime[0]) < Integer.parseInt(groupEventTime[1]))) &&
					            ((Integer.parseInt(apptTime[1]) > Integer.parseInt(groupEventTime[0])) &&
					            (Integer.parseInt(apptTime[1]) <= Integer.parseInt(groupEventTime[1])))) && 
					            a.getAccess().equalsIgnoreCase("Open")) {

					        	openIntervalsCheck++;
					        }
		    			}
		    		}
		    		if(memberCount == openIntervalsCheck) {
			    		for(int j = 0; j < names.size(); j++) {
			    			if(appointment.getDescription().contains(names.get(j))){
			    				newEventDescription += " " + names.get(j) + " ";
			    			}
			    		}
			    		appointment.setDescription(newEventDescription);
			    		appointment.setTime(newGroupTime);
			    		return true;
			    	} else {
			    		return false;
			    	}
		    	}
		 	}
		}
		return false;
    }	

    public boolean removeMeFromGroup(String userName, String groupTime) {
    	if(!names.isEmpty()) {
    		for (Iterator<Map.Entry<String, ArrayList<Appointment>>> iterator = userCalendar.entrySet().iterator(); iterator.hasNext(); ) {
				Entry<String, ArrayList<Appointment>> entry = iterator.next();
				String key = entry.getKey();
				ArrayList<Appointment> apptList = entry.getValue();
				for (Appointment appointment : apptList) {
					if(appointment.getTime().equals(groupTime) 
						&& appointment.getAccess().equalsIgnoreCase("Group")
						&& appointment.getDescription().contains(userName)) {
						
						String replaced = appointment.getDescription().replaceAll(userName, ""); // remove user from group event
						appointment.setDescription(replaced);
						return true;
					}
				}
			}
    	}
    	return false;
    }


	// Display the calendar for the current user
	public String viewCalendar(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "viewCalendar() invoked");
		int eventNumber = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("\t\t\t " + userName + "'s  CALENDAR \n");
		sb.append("..................................................................\n");
		sb.append("TIME \t\t EVENT \t\t\t ACCESS\n");
		sb.append("..................................................................\n");
		if (names.contains(userName)) {
			ArrayList<Appointment> list = userCalendar.get(userName);
			if (list != null) {
				for (Appointment appointment : list) {
					sb.append(appointment.getTime() + "\t\t" +
						appointment.getDescription() + "\t\t" +
						appointment.getAccess() + "\n\n");
				}
			}
		}
		sb.append("================================================================\n");
		sb.append("\n");
		return sb.toString();
	}

	// Delete event
	public boolean deleteEvent(String userName, String eventTime) throws RemoteException {
		System.out.println("Server: Message > " + "deleteEvent() invoked");
		eventTime = eventTime.replaceAll(" ", "");
		ArrayList<Appointment> list = userCalendar.get(userName);
		if (list != null || !list.isEmpty()) {
			for (Appointment appt : list) {
				if (appt.getTime().equals(eventTime)) {
					list.remove(appt);
					return true;
				}
			}
		}
		return false;
	}

	// Modify event
	public boolean updateEvent(String userName,
	                           String pickedTime,
	                           String modifiedTime,
	                           String eventDescription,
	                           String accessControl) throws RemoteException {

		if (userCalendar.get(userName) != null || userCalendar.get(userName).size() != 0) {
			ArrayList<Appointment> list = userCalendar.get(userName);
			for (Appointment appt : list) {
				if (appt.getTime().equals(pickedTime.replaceAll(" ", ""))) {
					String[] currentTimeInterval = new String[2];
					modifiedTime = modifiedTime.replaceAll(" ", "");
					String[] newTimeInterval = modifiedTime.split("-");
					ArrayList<Integer> startTime = new ArrayList<Integer>();
					ArrayList<Integer> endTime = new ArrayList<Integer>();

					for (Iterator<Map.Entry<String, ArrayList<Appointment>>> iterator = userCalendar.entrySet().iterator(); iterator.hasNext(); ) {
						Entry<String, ArrayList<Appointment>> entry = iterator.next();
						String key = entry.getKey();
						if (key.equalsIgnoreCase(userName)) {
							ArrayList<Appointment> apptList = entry.getValue();
							for (Appointment appointment : apptList) {
								if (!appointment.getTime().equals(pickedTime.replaceAll(" ", ""))) {
									currentTimeInterval = appointment.getTime().split("-");
									startTime.add(Integer.parseInt(currentTimeInterval[0]));
									endTime.add(Integer.parseInt(currentTimeInterval[1]));
								}
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
					appt.setTime(modifiedTime.replaceAll(" ", ""));
					appt.setDescription(eventDescription);
					appt.setAccess(accessControl);
					return true;
				}
			}
		}
		return false;
	}

	// Creates a calendar for another user
	public boolean createAnotherCalendar(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "createAnotherCalendar() invoked");
		if (calendarExist(userName))
			return false;
		else {
			return createCalendar(userName);
		}
	}

	//TODO add the username into the args of the method
	// Displays all users' calendars
	public String viewAllCalendars(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "viewAllCalendars() invoked");
		StringBuilder sb = new StringBuilder();
		if (!names.isEmpty()) {
			for (String name : userCalendar.keySet()) {
				sb.append("\t\t\t " + name + "'s  CALENDAR\n");
				sb.append(".......................................................................\n");
				sb.append("TIME \t\t EVENT \t\t\t ACCESS\n");
				sb.append(".......................................................................\n");
				ArrayList<Appointment> list = userCalendar.get(name);
				if (name.equalsIgnoreCase(userName)) {
					ArrayList<Appointment> apptList = userCalendar.get(name);
					for (Appointment appointment : apptList) {
						sb.append(appointment.getTime() + "\t\t" +
							appointment.getDescription() + "\t\t" +
							appointment.getAccess() + "\n\n");
					}
				} else {
					ArrayList<Appointment> apptList = userCalendar.get(name);
					for (Appointment appointment : apptList) {
						if (!appointment.getAccess().equalsIgnoreCase("Private")) {
							sb.append(appointment.getTime() + "\t\t" +
								appointment.getDescription() + "\t\t" +
								appointment.getAccess() + "\n\n");
						}
					}
				}
				sb.append("***********************************************************************\n\n");
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}
