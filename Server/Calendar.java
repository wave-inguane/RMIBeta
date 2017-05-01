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
			//this.userName = userName;
			try {
				setUserName(userName);
				this.userName = getUserName();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.userCalendar.put(this.userName, new ArrayList<Appointment>());
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
			//this.userName = userName;
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


	/**
	 * Don't worry about the locks i can add thenm later
	 * you can just work on the functionality of the method
	 */
	public boolean addGroupEvent(String userName,
	                             String timeInterval,
	                             String eventDescription,
	                             String accessControl) throws RemoteException {

		//STEP: 1 look for an open event in other users calendars
		//        one at time by locking and unlocking it

		//iterate throught names (ArrayList)
		//you can use for loop to iterate OR

	          /*
		      for (ListIterator<String> it = names.listIterator(list.size()); it.hasNext(); ) {
		      String userName = it.next();
		      // ...
		          access (userName's calendar) look for open event
		      }
		      */

		//
		//lock()
		// if desired open event exist
		//        get name of the user
		//        addEvent(String userName,String timeInterval,String eventDescription, String accessControl);
		//unlock()
		// this users calendar

		//Next: on your iteration if more open events found
		//Modify the group event on the group event host calendar
		//       to append new group open event member

		//and that's it

		//For each calendar owner that was included on this group event
		//the server should send an notification that they were included in
		//an group event and provide them with details
		//Time and location should be fime


		return false;
	}


	//read only
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
			if (list != null)
				for (Appointment appointment : list) {
					sb.append(appointment.getTime() + "\t\t" +
							appointment.getDescription() + "\t\t" +
							appointment.getAccess() + "\n");
				}
		}
		sb.append("================================================================\n");
		sb.append("\n");
		return sb.toString();
	}

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

	public boolean createAnotherCalendar(String userName) throws RemoteException {
		System.out.println("Server: Message > " + "createAnotherCalendar() invoked");
		if (calendarExist(userName))
			return false;
		else {
			return createCalendar(userName);
		}
	}

	public String viewAllCalendars() throws RemoteException {
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
								appointment.getAccess() + "\n");
					}
				} else {
					ArrayList<Appointment> apptList = userCalendar.get(name);
					for (Appointment appointment : apptList) {
						if (!appointment.getAccess().equalsIgnoreCase("Private")) {
							sb.append(appointment.getTime() + "\t\t" +
									appointment.getDescription() + "\t\t" +
									appointment.getAccess() + "\n");

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
