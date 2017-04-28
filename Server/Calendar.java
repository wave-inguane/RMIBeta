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
	private Map<String, ArrayList<Appointment>> userCalendar; // calendar for the current user
	private ArrayList<Map<String, ArrayList<Appointment>>> allUserCalendars = new ArrayList<>();
	private ArrayList<String> names = new ArrayList<>();
	private int userKeys = 0;



	private Map<String, List<String>> calendar;
	private Map<Integer, Map<String, List<String>>>
			calendars = new TreeMap<Integer, Map<String, List<String>>>();
	private List<Map<String, List<String>>> allcalendars = new ArrayList<>();
	private String userName;
	private List<String> tuple;
	private int index = 0;
	private int indexKey;
	private int allCalendarsIndex = 0;
	private List<String> users = new ArrayList<String>();
	private Map<String, String> calendarExist = new LinkedHashMap<String, String>();
	private int prevIndex[] = new int[1];

	private Map<String, List<String>> mapUpdate;


	public Calendar() throws RemoteException {
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
		if(names.contains(userName)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean createCalendar(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "createCalendar() invoked");
		if (calendarExist(userName) == false) {
			this.userCalendar = new TreeMap();
			this.userCalendar.put(userName, new ArrayList<>());
			this.allUserCalendars.add(userCalendar);
			this.names.add(userName);
			this.userName = userName;

			// indexKey = 0;
			// this.userName = userName;
			// users.add(userName);
			// calendarExist.put(userName, "exist");
			// calendar = new TreeMap<>();
			// allcalendars.add(this.calendar);
			// calendars.put(index, this.calendar);
			// prevIndex[0] = index;
			// index++;
			return true;
		}
		return false;
	}

	public boolean addEvent(String userName,
							String timeInterval,
	                        String eventDescription,
	                        String accessControl) throws RemoteException {
		System.out.println("Server: Message > " + "addEvent() invoked");

		if(userCalendar != null) {
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
					for(Appointment appointment: apptList) {
						if(!appointment.getTime().equals("0")) {
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
		}
		Appointment appt = new Appointment(timeInterval, eventDescription, accessControl);
		ArrayList<Appointment> getApptList = userCalendar.get(userName);
		if(getApptList == null) {
			getApptList = new ArrayList<>();
			this.userName = userName;
			this.userCalendar.put(userName, new ArrayList<>());
			getApptList.add(appt);
		} else {
			getApptList.add(appt);
		}
		
		return true;
	}

	public String viewCalendar(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "viewCalendar() invoked");
		int eventNumber = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("\t\t\t " + userName + "'s  CALENDAR \n");
		sb.append("..................................................................\n");
		sb.append("EVENT# \t\t TIME \t\t EVENT \t\t\t ACCESS\n");
		sb.append("..................................................................\n");
		if (userCalendar != null) {
			for(int i = 0; i < allUserCalendars.size(); i++) {
				Map<String, ArrayList<Appointment>> map = allUserCalendars.get(i);
				for (Iterator<Map.Entry<String, ArrayList<Appointment>>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
					Entry<String, ArrayList<Appointment>> entry = iterator.next();
					String key = entry.getKey();
					if (key.equalsIgnoreCase(userName)) {
						ArrayList<Appointment> apptList = entry.getValue();
						for(Appointment appointment: apptList) {
							if(!appointment.getTime().equals("0")){
								sb.append(String.valueOf(eventNumber++) + "\t\t" +
									appointment.getTime() + "\t\t" + 
									appointment.getDescription() + "\t\t" + 
									appointment.getAccess() + "\n");
							}
						}
					}
				}
			}
		}
		return sb.toString();
	}

	public boolean deleteEvent(String userName, int eventNumber) throws RemoteException {
		System.out.println("Server: Message > " + "deleteEvent() invoked");
		int chosenEvent = 0;
		if (userCalendar != null) {
			for(int i = 0; i < allUserCalendars.size(); i++) {
				Map<String, ArrayList<Appointment>> map = allUserCalendars.get(i);
				for (Iterator<Map.Entry<String, ArrayList<Appointment>>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
					Entry<String, ArrayList<Appointment>> entry = iterator.next();
					String key = entry.getKey();
					if (key.equalsIgnoreCase(userName)) {
						ArrayList<Appointment> apptList = entry.getValue();
						for(Appointment appointment: apptList) {
							if(chosenEvent == eventNumber) {
								appointment.setTime("0");
								return true;
							} else {
								chosenEvent++;
							}
						}
					}
				}
			}
		}
		// for (Iterator<Map.Entry<String, ArrayList<Appointment>>> iterator = userCalendar.entrySet().iterator(); iterator.hasNext(); ) {
		// 	Entry<String, ArrayList<Appointment>> entry = iterator.next();
		// 	String key = entry.getKey();
		// 	if (key.equalsIgnoreCase(userName)) {
		// 		ArrayList<Appointment> apptList = entry.getValue();
		// 		for(Appointment appointment: apptList) {
		// 			if(chosenEvent == eventNumber) {
		// 				appointment.setTime("0");
		// 				return true;
		// 			} else {
		// 				chosenEvent++;
		// 			}
		// 		}
		// 	}
		// }
		return false;
	}

	public ArrayList<String> modifyEvent(String userName, int eventNumber) throws RemoteException {
		System.out.println("Server: Message > " + "modifyEvent() invoked");
		ArrayList<String> event = null;
		if(userCalendar != null) {
			int chosenEvent = 0;
			for (Iterator<Map.Entry<String, ArrayList<Appointment>>> iterator = userCalendar.entrySet().iterator(); iterator.hasNext(); ) {
				Entry<String, ArrayList<Appointment>> entry = iterator.next();
				String key = entry.getKey();
				if (key.equalsIgnoreCase(userName)) {
					ArrayList<Appointment> apptList = entry.getValue();
					for(Appointment appointment: apptList) {
						if(chosenEvent == eventNumber) {
							event = new ArrayList<>();
							event.add(0, appointment.getTime());
							event.add(1, appointment.getDescription());
							event.add(2, appointment.getAccess());
							return event;
						} else{
							chosenEvent++;
						}
					}
				}
			}
		}
		return event;
	}


	public boolean updateEvent(ArrayList<String> newEvent, String userName, int eventNumber) throws RemoteException {
		int chosenEvent = 0;
		for (Iterator<Map.Entry<String, ArrayList<Appointment>>> iterator = userCalendar.entrySet().iterator(); iterator.hasNext(); ) {
			Entry<String, ArrayList<Appointment>> entry = iterator.next();
			String key = entry.getKey();
			if (key.equalsIgnoreCase(userName)) {
				ArrayList<Appointment> apptList = entry.getValue();
				for(Appointment appointment: apptList) {
					if(chosenEvent == eventNumber) {
						// Check for the time overlaps
						String[] currentTimeInterval = new String[2];
						String timeInterval = newEvent.get(0).replaceAll(" ", "");
						String[] newTimeInterval = timeInterval.split("-");
						ArrayList<Integer> startTime = new ArrayList<Integer>();
						ArrayList<Integer> endTime = new ArrayList<Integer>();

						chosenEvent = 0;
						for (Iterator<Map.Entry<String, ArrayList<Appointment>>> iterator1 = userCalendar.entrySet().iterator(); iterator1.hasNext(); ) {
							Entry<String, ArrayList<Appointment>> entry1 = iterator1.next();
							String key1 = entry1.getKey();
							if (key1.equalsIgnoreCase(userName)) {
								ArrayList<Appointment> apptList1 = entry1.getValue();
								for(Appointment appointment1: apptList1) {
									if(chosenEvent != eventNumber) {
										currentTimeInterval = appointment1.getTime().split("-");
										startTime.add(Integer.parseInt(currentTimeInterval[0]));
										endTime.add(Integer.parseInt(currentTimeInterval[1]));
									} else {
										chosenEvent++;
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
						appointment.setTime(newEvent.get(0));
						appointment.setDescription(newEvent.get(1));
						appointment.setAccess(newEvent.get(2));
						return true;
					} else{
						chosenEvent++;
					}
				}
			}
		}
		return false;
	}

	public boolean createAnotherCalendar(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "createAnotherCalendar() invoked");
		if (calendarExist(userName))
			return false;
		else {
			return createCalendar(userName);
		}
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
		if (allcalendars != null) {
			for (i = 0; i < allUserCalendars.size(); i++) {
				sb.append(viewAllCalendarsHelper(allUserCalendars.get(i)));
			}
		}
		userKeys = 0;
		return sb.toString();
	}


	/**
	 * This should be private we have to remove from interface
	 */
	public String viewAllCalendarsHelper(Map<String, ArrayList<Appointment>> map) throws RemoteException {
		System.out.println("Server: Message > " + "viewAllCalendarsHelper() invoked");
		StringBuilder sb = new StringBuilder();
		for (Iterator<Map.Entry<String, ArrayList<Appointment>>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
			if(userKeys < names.size()) {
				Entry<String, ArrayList<Appointment>> entry = iterator.next();
				String name = entry.getKey();
				sb.append("\t\t\t\t " + name + "'s  CALENDAR\n");
				userKeys++;
				break;
			}
		}
		sb.append(".......................................................................\n");
		sb.append("EVENT# \t\t TIME \t\t EVENT \t\t\t ACCESS\n");
		sb.append(".......................................................................\n");
		if (map != null) {
			for (Iterator<Map.Entry<String, ArrayList<Appointment>>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
				Entry<String, ArrayList<Appointment>> entry = iterator.next();
				String key = entry.getKey();
				if (key.equalsIgnoreCase(userName)) {
					int eventNumber = 0;
					ArrayList<Appointment> apptList = entry.getValue();
					for(Appointment appointment: apptList) {
						if(!appointment.getTime().equals("0")) {
							sb.append(String.valueOf(eventNumber++) + "\t\t" +
									appointment.getTime() + "\t\t" + 
									appointment.getDescription() + "\t\t" + 
									appointment.getAccess() + "\n");
						}
					}
				} else {
					int eventNumber = 0;
					ArrayList<Appointment> apptList = entry.getValue();
					for(Appointment appointment: apptList) {
						if(!appointment.getAccess().equalsIgnoreCase("Private")) {
							if(!appointment.getTime().equals("0")) {
								sb.append(String.valueOf(eventNumber++) + "\t\t" +
									appointment.getTime() + "\t\t" + 
									appointment.getDescription() + "\t\t" + 
									appointment.getAccess() + "\n");
							}
						}
					}
				}
			}
		}
		sb.append(".......................................................................\n\n");
		return sb.toString();
	}

	public String EchoMessage() throws RemoteException {
		String capitalizedMsg;
		System.out.println("Server: EchoMessage() invoked...");
		System.out.println("Server: Message > " + userName);

		//compute
		capitalizedMsg = userName;//.toUpperCase();

		//return to client
		return (capitalizedMsg);
	}
}
