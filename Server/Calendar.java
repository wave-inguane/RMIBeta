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


//concurrency
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Calendar extends UnicastRemoteObject implements RemCalendar{

	private static final long serialVersionUID = 1L;
	//handle concurrency
	final Lock lock = new ReentrantLock();
	final Condition hasNotChanged  = lock.newCondition(); 
	final Condition hasChanged = lock.newCondition(); 
	int sentinel = -1;
	
	
	//handle notification
	private ArrayList<RemCalendar> chatClients;
	
	private Map<String, ArrayList<Event>> userCalendar  = new TreeMap(); // calendar for the current user
	private Map<String , Map<String, ArrayList<Event>>> createdBy = new TreeMap();
	private ArrayList<String> names = new ArrayList<>();
	private ArrayList<String> loggedIn;
	private ArrayList<Event> generalAccess;
	private int ownerTracker;
	
	private String userName;
	
 
	public Calendar() throws RemoteException {
		this.sentinel = -1;
		this.ownerTracker = 0;
		this.chatClients = new ArrayList<RemCalendar>();
		this.loggedIn = new ArrayList<>();
	}
	

	public synchronized void registerChatClient(RemCalendar chatClient) throws RemoteException {
		this.chatClients.add(chatClient);	
	}


	//for each Client that was added into group event sent a message
	public synchronized void broadcastMessage(String message) throws RemoteException {
	System.out.println("Server: Message > " + "broadcastMessage() invoked");
	
		int i = 0;
		while( i < chatClients.size()){
			//retrieve notification
			RemCalendar client = chatClients.get(i++);
			 String name = client.getPosterName();
			 //System.out.println("POSTER: "+name);
			 if(!message.contains(name))
			 client.retrieveMessage(message);
		}
   
	}
	
	public String getPosterName() throws RemoteException{
	 return "Do not implement the Client code will take care of this";
	}
	
	
	public ArrayList<String> getActiveUsers()throws RemoteException{
	     ArrayList<String> currNames = (ArrayList<String>)names.clone();
		 return currNames;
	}
	
	public void retrieveMessage(String message) throws RemoteException{
	  //Do Nothing
	  //call client's retrieveMessage
	}

	public void setUserName(String name) throws RemoteException, InterruptedException  {
		System.out.println("Server: Message > " + "setUserName() invoked");
	     lock.lock();
	     try {
	         while (sentinel != -1) 
	         hasNotChanged.await();
	        // System.out.println("....Enter setUserName critical section....");
	         
	         userName = name;
	         if(!loggedIn.contains(name))
	         loggedIn.add(name);
	         
	         ++sentinel;
	         
	       //System.out.println("....Exit setUserName critical section....");
	       hasChanged.signal();
	     } finally {
	       lock.unlock();
	     }
	   }
	
	public String getUserName() throws RemoteException, InterruptedException  {
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
	   
	   
	   public boolean findClient(String client)throws RemoteException{
	      if(loggedIn.contains(client))
	        return true;
	        return false;
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
		if (!names.contains(userName)) {
			//this.userName = userName;
			try{
			setUserName(userName);
			this.userName = getUserName();
			}catch (InterruptedException e){
			  e.printStackTrace();
			}
			this.userCalendar.put(this.userName, new ArrayList<Event>());
			this.createdBy.put(this.userName + ownerTracker++, this.userCalendar); 
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

		if(userCalendar != null) {
			String[] currentTimeInterval = new String[2];
			timeInterval = timeInterval.replaceAll(" ", "");
			String[] newTimeInterval = timeInterval.split("-");
			ArrayList<Integer> startTime = new ArrayList<Integer>();
			ArrayList<Integer> endTime = new ArrayList<Integer>();

			for (Iterator<Map.Entry<String, ArrayList<Event>>> iterator = userCalendar.entrySet().iterator(); iterator.hasNext(); ) {
				Entry<String, ArrayList<Event>> entry = iterator.next();
				String key = entry.getKey();
				if (key.equalsIgnoreCase(userName)) {
					ArrayList<Event> apptList = entry.getValue();
					for(Event event: apptList) {
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
		Event appt = new Event(timeInterval, eventDescription, accessControl);
		ArrayList<Event> getApptList = userCalendar.get(userName);
		if(getApptList == null) {
			getApptList = new ArrayList<>();
			//this.userName = userName;
			try{
			setUserName(userName);
			this.userName = getUserName();
			}catch (InterruptedException e){
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
	         
	  //STEP 1:
	  boolean flag = false;
	 
	  StringBuilder sb = new StringBuilder();
	  sb.append(eventDescription);
	  
	  //STEP 3: 
	  int i = 0;
	  String addMe;
	  while(i < names.size()){
	 	 ArrayList<Event> list = userCalendar.get(addMe = names.get(i++));
	 	 
	 	 for(Event event: list) {
	 		 String[] apptTime = event.getTime().split("-");
             String[] groupTime = timeInterval.split("-");
             if((Integer.parseInt(apptTime[0]) >= Integer.parseInt(groupTime[0]) &&
                (Integer.parseInt(apptTime[0]) < Integer.parseInt(groupTime[1]))) &&
                ((Integer.parseInt(apptTime[1]) > Integer.parseInt(groupTime[0])) &&
                (Integer.parseInt(apptTime[1]) <= Integer.parseInt(groupTime[1]))) && event.getAccess().equalsIgnoreCase("Open")) {
                	sb.append(addMe + "\n");     
                            
             }
	 	 }
	  }
	  
	  //STEP 4: 
	  flag = addEvent(userName,timeInterval,sb.toString(),accessControl);
	         
	  return flag;
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
		if(names.contains(userName)) {
			ArrayList<Event> list = userCalendar.get(userName);
			if(list!=null)
			for(Event event: list) {
				sb.append(event.getTime() + "\t\t" + 
					event.getDescription() + "\t\t" + 
					event.getAccess() + "\n");
			}
		}
		sb.append("================================================================\n");
		sb.append("\n");
		return sb.toString();
	}

	public boolean deleteEvent(String userName, String eventTime) throws RemoteException {
		System.out.println("Server: Message > " + "deleteEvent() invoked");
		eventTime = eventTime.replaceAll(" ", "");
		ArrayList<Event> list = userCalendar.get(userName);
		if(list != null || !list.isEmpty()) {
			for(Event appt: list) {
				if(appt.getTime().equals(eventTime)) {
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
							   
		if(userCalendar.get(userName) != null || userCalendar.get(userName).size() != 0) {
			ArrayList<Event> list = userCalendar.get(userName);
			for(Event appt: list) {
				if(appt.getTime().equals(pickedTime.replaceAll(" ", ""))) {
					String[] currentTimeInterval = new String[2];
					modifiedTime = modifiedTime.replaceAll(" ", "");
					String[] newTimeInterval = modifiedTime.split("-");
					ArrayList<Integer> startTime = new ArrayList<Integer>();
					ArrayList<Integer> endTime = new ArrayList<Integer>();

					for (Iterator<Map.Entry<String, ArrayList<Event>>> iterator = userCalendar.entrySet().iterator(); iterator.hasNext(); ) {
						Entry<String, ArrayList<Event>> entry = iterator.next();
						String key = entry.getKey();
						if (key.equalsIgnoreCase(userName)) {
							ArrayList<Event> apptList = entry.getValue();
							for(Event event: apptList) {
								if(!event.getTime().equals(pickedTime.replaceAll(" ", ""))) {
									currentTimeInterval = event.getTime().split("-");
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
		if(!names.isEmpty()){
			for(String name: userCalendar.keySet()) {
				sb.append("\t\t\t " + name + "'s  CALENDAR\n");
				sb.append(".......................................................................\n");
				sb.append("TIME \t\t EVENT \t\t\t ACCESS\n");
				sb.append(".......................................................................\n");
				ArrayList<Event> list = userCalendar.get(name);
				if (name.equalsIgnoreCase(userName)) {
					ArrayList<Event> apptList = userCalendar.get(name);
					for(Event event: apptList) {
						sb.append(event.getTime() + "\t\t" + 
								event.getDescription() + "\t\t" + 
								event.getAccess() + "\n");
					}
				} else {
					ArrayList<Event> apptList = userCalendar.get(name);
					for(Event event: apptList) {
						if(!event.getAccess().equalsIgnoreCase("Private")) {
							sb.append(event.getTime() + "\t\t" + 
								event.getDescription() + "\t\t" + 
								event.getAccess() + "\n");
							
						}
					}
				}
				sb.append("***********************************************************************\n\n");
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
     public String viewAnyCalendar(String name) throws RemoteException{
	  StringBuilder sb = new StringBuilder();
    	 if((isOwner(name) == true) && (userName.equals(name))){
    		String result = viewCalendar(userName);
    		 return result;
    	 }else{
    	
 		 sb = new StringBuilder();
 		sb.append("\t\t\t " + name + "'s  CALENDAR \n");
 		sb.append("..................................................................\n");
 		sb.append("TIME \t\t EVENT \t\t\t ACCESS\n");
 		sb.append("..................................................................\n");
 		if(names.contains( name )) {
 			ArrayList<Event> list = userCalendar.get(name);
 			if(list!=null)
 			for(Event event: list) {
 				if(!event.getAccess().equalsIgnoreCase("Private"))
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
	 
     public boolean postInAnyCalendar(String name, String timeInterval,String eventDescription, String accessControl)  throws RemoteException{
    	 
    	 if((isOwner(name) == true) && (userName.equals(name)))
    		 return addEvent(userName, timeInterval, eventDescription,accessControl);
    		 
          if(accessControl.equalsIgnoreCase("Group")){
        	  //check for conflicting events
    	    return addEvent(name, timeInterval, eventDescription,accessControl);
          }
    			 	 
      return false;
    }
    
    private boolean isOwner(String userName)  throws RemoteException{

    	for (Iterator<Map.Entry<String, Map<String, ArrayList<Event>>>> iterator = createdBy.entrySet().iterator(); iterator.hasNext(); ) {
			Entry<String, Map<String, ArrayList<Event>>> entry = iterator.next();

			String key = entry.getKey();
			Map<String, ArrayList<Event>> calendar = entry.getValue();

			if (calendar != null) {
				for (Iterator<Map.Entry<String, ArrayList<Event>>> iterator2 = calendar.entrySet().iterator(); iterator2.hasNext(); ) {
					Entry<String, ArrayList<Event>> entry2 = iterator2.next();
					String key2 = entry2.getKey();

					if((key.substring(0,key2.length())).equals(key2)) { //is the owner
						generalAccess  = entry2.getValue(); //return the calendar
						return true;
					}
				}
			}
		}
    	return false;
	}
	
	
}
