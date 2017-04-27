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

public class Calendar extends UnicastRemoteObject implements RemCalendar
{
    
    private  Map<String, List<String>> calendar;
	private  Map<Integer, Map<String,List<String>>>
	calendars = new TreeMap<Integer, Map<String,List<String>>>();
	private  List<Map<String, List<String>>> allcalendars = new ArrayList<>();
	private  String userName;
	private  List<String> tuple;
	private  int index = 0;
	private  int indexKey;
	private int allCalendarsIndex = 0;
	private  List<String> users = new ArrayList<String>();
	private  Map<String,String> calendarExist = new LinkedHashMap<String,String>();
	private  int prevIndex[] = new int[1];
	
	
    public Calendar() throws RemoteException { };
    
    
    public Calendar(String userName) throws RemoteException{
		indexKey = 0;
		this.userName = userName;
		users.add(userName);
		calendarExist.put(userName, "exist");
		calendar = new TreeMap<>();
		allcalendars.add(this.calendar);
		calendars.put(index, this.calendar);
		prevIndex[0] = index;
		index ++;
	}
    
    public void setUserName(String name) throws RemoteException{
		userName = name;
	}
	
	 public String getUserName() throws RemoteException{
		 return userName;
	 }
	 
	 public boolean calendarExist(String userName) throws RemoteException{

		String exist = calendarExist.get(userName);
		if(exist!=null)
			return true;
		return false;
	 }
	 
	 
	public boolean createCalendar(String userName) throws RemoteException{
	
		if(calendarExist(userName) == false){
			new Calendar(userName);
			return true;
		}
		return false;
	}
	
		public boolean addEvent(String timeInterval, String eventDescription, String accessControl)throws RemoteException{

		int j = 0;
		tuple = new ArrayList<>();
		tuple.add(0, timeInterval);
		tuple.add(1, eventDescription);
		tuple.add(2, accessControl);

		try{
			List<String> list = calendar.get(userName+j);
			while(list!=null){
				list = calendar.get(userName+j);
				j++;
			}

			calendar.put(userName+indexKey, tuple);
			indexKey++;
		}catch(ClassCastException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
		public void viewCalendar(String userName)throws RemoteException{

		int tuple = 0;
		System.out.println("\t\t\t "+userName+ "'s  CALENDAR");
		System.out.println("...............................................");
		System.out.println("EVENT# \t\t TIME \t\t EVENT \t\t ACCESS");
		System.out.println("...............................................");
		if(calendar!=null)
			for(Iterator<Map.Entry<String, List<String>>> iterator = calendar.entrySet().iterator();iterator.hasNext();)
			{
				Entry<String, List<String>> entry = iterator.next();
				String key = entry.getKey();

				if(key.equalsIgnoreCase(userName+tuple)){

					List<String> event = entry.getValue();
					System.out.println(tuple+": "+event.get(0) +"\t\t"+ event.get(1) +"\t\t"+ event.get(2));

				}
				tuple++;
			}
	}
	
	public List<String> deleteEvent(int eventNumber)throws RemoteException{
		return  modifyEvent(eventNumber);
	}
	
	
	public List<String>  modifyEvent(int eventNumber)throws RemoteException{
		List<String> event = null;

		//String name =

		if(calendars!=null)
			for(Iterator< Map.Entry< Integer, Map<String,List<String>> > > iterator = calendars.entrySet().iterator();iterator.hasNext();)
			{
				Entry<Integer, Map<String,List<String>> > entry = iterator.next();

				Map<String,List<String>> map = entry.getValue();

				if(map!=null)
					for(Iterator<Map.Entry<String, List<String>>> iterator2 = map.entrySet().iterator();iterator2.hasNext();)
					{
						Entry<String, List<String>> entry2 = iterator2.next();
						String key2 = entry2.getKey();

						if(key2.equalsIgnoreCase(userName+eventNumber)){

							event = entry2.getValue();
							//System.out.println("................ ");
							//System.out.println(" MODIFIED EVENT ");
							//System.out.println("................ ");
							//System.out.println(key2+": "+event.get(0) +"\t\t"+ event.get(1) +"\t\t"+ event.get(2));
						}
					}
			}
		return event;
	}

	public  Calendar createAnotherCalendar(String userName)throws RemoteException{
		int  j = 0;
		Map<String, List<String>> map = calendars.get(j);
		while(map!=null){
			map = calendars.get(j);
			j++;
		}

		index  = prevIndex[0] + 1;

		return new Calendar (userName);
	}

	
	public boolean isOwner(String userName, int calendarNumber) throws RemoteException{
		String name = users.get(calendarNumber);
		if(name.equalsIgnoreCase(userName))
			return true;
		return false;
	}
	
	
	public  void viewAllCalendars()throws RemoteException{
		int i;
		if(allcalendars!=null)
			for(i = 0; i < allcalendars.size(); i++)
				viewAllCalendarsHelper(allcalendars.get(i));
	}
	
	
	public void viewAllCalendarsHelper(Map<String,List<String>> map)throws RemoteException{

		int tuple = 0;
		String name = users.get(allCalendarsIndex = allCalendarsIndex%users.size());
		if(!name.equalsIgnoreCase(null))
			System.out.println("[ " + allCalendarsIndex  +" ] \t\t " +name+ "'s  CALENDAR");
		System.out.println("...............................................");
		System.out.println("EVENT# \t\t TIME \t\t EVENT \t\t ACCESS");
		System.out.println("...............................................");
		if(map!=null)
			for(Iterator<Map.Entry<String, List<String>>> iterator = map.entrySet().iterator();iterator.hasNext();)
			{
				Entry<String, List<String>> entry = iterator.next();
				String key = entry.getKey();

				if(key.equalsIgnoreCase(name+tuple)){
					List<String> event = entry.getValue();
					if(isOwner(userName, allCalendarsIndex)==true)
						System.out.println(tuple+": "+event.get(0) +"\t\t"+ event.get(1) +"\t\t"+ event.get(2));
					else if(!event.get(2).equalsIgnoreCase("Private"))
						System.out.println(tuple+": "+event.get(0) +"\t\t"+ event.get(1) +"\t\t"+ event.get(2));
				}
				tuple++;
			}
		System.out.println("-------------------------------------------------");
		System.out.println("-------------------------------------------------\n");
		allCalendarsIndex++;
	}

	public  void viewAnyCalendar(String userName, int index)throws RemoteException{

		int tuple = 0;
		String name = userName;
		if(!name.equalsIgnoreCase(null))
			System.out.println("\t\t\t "+users.get(index = index%users.size())+ "'s  CALENDAR");
		System.out.println("...............................................");
		System.out.println("EVENT \t\t TIME \t\t EVENT \t\t ACCESS");
		System.out.println("...............................................");
		if(allcalendars!=null)
			for(Iterator<Map.Entry<String, List<String>>> iterator =
			allcalendars.get(index = index%users.size()).entrySet().iterator();iterator.hasNext();)
			{
				Entry<String, List<String>> entry = iterator.next();
				List<String> event = entry.getValue();

				if(isOwner(userName, index)==true)
					System.out.println(tuple+": "+event.get(0) +"\t\t"+ event.get(1) +"\t\t"+ event.get(2));
				else if(!event.get(2).equalsIgnoreCase("Private"))
					System.out.println(tuple+": "+event.get(0) +"\t\t"+ event.get(1) +"\t\t"+ event.get(2));

				tuple++;
			}
		System.out.println("-------------------------------------------------");
		System.out.println("-------------------------------------------------\n");
		allCalendarsIndex++;
	}


	
    public String EchoMessage() throws RemoteException
    {
	String capitalizedMsg;

        System.out.println("Server: EchoMessage() invoked...");
        System.out.println("Server: Message > " + userName);
        
        //compuete
	    capitalizedMsg = userName;//.toUpperCase();
	
	    //return to client
        return(capitalizedMsg);
    }
}
   
   
   

