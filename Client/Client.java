// Client.java
// 
// This sample Java RMI client can perform the
// following operation:
//    Send a message to a remote object that echoes it back in upper case. 
//    
//     Usage:  java Client "My message in quotes"
//  

import javax.sound.sampled.*;
import java.io.*;
import java.awt.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Random;

public class Client extends UnicastRemoteObject implements RemCalendar{

	private static final long serialVersionUID = 1L;
	
	private String chatClientName = null;
	private static RemCalendar chatServer = null;
	private static RemCalendar remcalendar = null;

	protected Client(String chatClientName, RemCalendar chatServer) throws RemoteException {
		this.chatClientName = chatClientName;
		this.chatServer = chatServer;
		//register those who have been included in the group event
		chatServer.registerChatClient(this);
	}
	
	
	public String getPosterName() throws RemoteException {
	  return this.chatClientName;
    }
    
	//retrieve a notification sent by a chat server
	public void retrieveMessage(String message) throws RemoteException {
	
	    System.out.println("\n&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		System.out.println("\t" + "$ Notification from the server $" );
		System.out.println(message);
	    System.out.println("\n&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");

	}
	

	
	public void broadcastMessage(String message)throws RemoteException{
	//Do Nothing
	//The server takes care of the notifications
	}
	
	
	public void registerChatClient(RemCalendar chatClient) throws RemoteException{
	//Do Nothing
	//The server takes care of registering the clients
	}

    //End New code

	public static void main(String argv[]) {
		// Validate command line parameters
		if (argv.length < 1) {

			System.out.println("Usage: java Client \"username\"");
			System.exit(1);
		}

		String userName = argv[0];

		// Get a remote reference to the Calendar class
		String strName = "rmi://localhost/CalendarServices";
		// String strName = "rmi://localhost:2934/CalendarServices";
		System.out.println("Client: Looking up " + strName + "...");
		//RemCalendar remcalendar = null;

		try {

			remcalendar = (RemCalendar) Naming.lookup(strName);
			chatServer = remcalendar;
			
		} catch (Exception e) {

			System.out.println("Client: Exception thrown looking up " + strName);
			System.exit(1);
		}

		try {
			remcalendar.setUserName(userName);
			userName = remcalendar.getUserName();
			//register client
		    Client client = new Client(userName, chatServer);
			
			System.out.println("From Server: " + userName);

			boolean flag = false;

			Scanner conIn = new Scanner(System.in);
			String choice = null;
			String skip;//skip end of line after reading an integer
			boolean keepGoing; //flag for "choose operation" loop
			int operation = 0; //indicates users choice of operation
			keepGoing = true;
			while (keepGoing) {
				System.out.println("\nChoose a Task: ");
				System.out.println("1: create my calendar");
				System.out.println("2: view my calendar ");
				System.out.println("3: post my event ");
				System.out.println("4: modify my event ");
				System.out.println("5: delete my event ");
				System.out.println("6: view all calendars");
				System.out.println("7: add a group event");
				System.out.println("9: create another calendar ");
				System.out.println("10: exit ");
				System.out.println("11: switch users ");

				if (conIn.hasNextInt())
					operation = conIn.nextInt();
				else {
					System.out.println("\n................................ ");
					System.out.println("Error: you must enter an integer ");
					System.out.println("Terminating the program... ");
					System.out.println("................................ ");
				}
				skip = conIn.nextLine();
				switch (operation) {

					case 1:
						flag = remcalendar.calendarExist(userName);
						if (flag != true) {
							flag = remcalendar.createCalendar(userName);
							System.out.println("\n.........................................");
							System.out.println("New calendar created for " + userName);
							System.out.println(".........................................");

						} else {
							System.out.println("The username already exists. Provide a different username");
						}
						break;

					case 2:
						flag = remcalendar.calendarExist(userName);
						if (flag != true) {
							System.out.println("Please create a calendar first \n"
									+ "or select view option 6: view all calendars");
						} else {
							String result = remcalendar.viewCalendar(userName);
							System.out.println(result);
						}
						break;

					case 3:
						flag = remcalendar.calendarExist(userName);
						if (flag != true) {
							System.out.println("Please create a calendar first \n"
									+ "or select view calendars to select from existing calendars");
						} else {
							boolean check = false;
							System.out.println("Enter event time: (Ex: 9-10 or 17-19)");
							String timeInterval = conIn.nextLine();
							if(timeInterval.contains("-")) {
								int count = 0;
								for(int i = 0; i < timeInterval.length(); i++) {
									if(timeInterval.charAt(i) == '-') {
										count++;
										if(count > 1) {
											System.out.println("\nPlease make sure you correct entered numbers.\n");
											check = true;
											break;
										}
									} else if(!Character.isDigit(timeInterval.charAt(i))) {
										System.out.println("\nPlease make sure you entered correct numbers.\n");
										check = true;
										break;
									}
								}
							} else if(!timeInterval.contains("-")) {
								System.out.println("\nPlease make sure you entered correct numbers.\n");
								check = true;
							} 

							if(!check) {
								timeInterval = timeInterval.replaceAll(" ", "");
								String[] checkTime = timeInterval.split("-");
								if(Integer.parseInt(checkTime[0]) > Integer.parseInt(checkTime[1])) {
									System.out.println("\nWrong time entered! Please try again.\n");
									break;
								}

								System.out.println("Enter event description: Ex: Squash game with Mary ");
								String eventDescription = conIn.nextLine();

								System.out.println("Enter event access control: Ex: Private, Public, Group, and Open  ");
								String accessControl = conIn.nextLine();

								flag = remcalendar.addEvent(userName, timeInterval, eventDescription, accessControl);
								if (flag == true) {
									System.out.println("\n.........................................");
									System.out.println("Event posted.");
									System.out.println("...........................................");
	
						            //----------------IF OPEN EVENT NOTIFY ALL ACTIVE USERS----------
									//get all active users and register them except yourself
									 //Notify them
	                                 if(accessControl.equalsIgnoreCase("Open")){
                                   
									 	 //Send Notification to others
									 	
							            chatServer.broadcastMessage(userName + " : " +"have an open event from :"+ timeInterval);
							         }
									//-------------------------End notification---------------------
									
								} else {
									System.out.println("\n\nPost failed! Time overlap!");
								}
							}
						}
						break;

					case 4:
						flag = remcalendar.calendarExist(userName);
						if (flag != true) {
							System.out.println("Please create a calendar first \n"
									+ "or select view calendars to select from existing calendars");
						} else {
							String result = remcalendar.viewCalendar(userName);
							System.out.println(result);

							System.out.println("First, enter the event time you'd like to change: (Ex: 9-10 or 17-19)");
							String pickedTime = conIn.nextLine();

							System.out.println("Now enter the time you'd like to change to: (Ex: 11-14 or 21-22)");
							String modifiedTime = conIn.nextLine();

							System.out.println("Enter event description: Ex: Squash game with Mary");
							String eventDescription = conIn.nextLine();

							System.out.println("Enter event access control: Ex: Private, Public, Group, and Open");
							String accessControl = conIn.nextLine();

							boolean isUpdated = remcalendar.updateEvent(userName, 
																		pickedTime, 
																		modifiedTime, 
																		eventDescription, 
																		accessControl);
							if(isUpdated) {
								System.out.println("\nThe event has been modified.\n");
							} else {
								System.out.println("\nCannot modify an event because of time overlap or the calendar is empty.\n");
							}
						}
						break;

					case 5:
						flag = remcalendar.calendarExist(userName);
						if (flag != true) {
							System.out.println("Please create a calendar first \n"
									+ "or select view calendars to select from existing calendars");
						} else {
							System.out.println(remcalendar.viewCalendar(userName));
							System.out.println("\nEnter the time of the event you'd like to delete (e.g: 9-10 or 17-19): ");
							String eventTime = conIn.nextLine();
							boolean isDeleted = remcalendar.deleteEvent(userName, eventTime);
							if(isDeleted) {
								System.out.println("\nThe event has been deleted.\n");
							} else {
								System.out.println("\nThere is no current event with that time." +
								 " Therefore, nothing has been deleted\n");
							}
						}
						break;
					case 7:
                        flag = remcalendar.calendarExist(userName);
                        if (flag != true) {
                            System.out.println("Please create a calendar first \n"
                                    + "or select view calendars to select from existing calendars");
                        } else {
                            boolean check = false;
                            System.out.println("Enter event time: (Ex: 9-10 or 17-19)");
                            String timeInterval = conIn.nextLine();
                            if (timeInterval.contains("-")) {
                                int count = 0;
                                for (int i = 0; i < timeInterval.length(); i++) {
                                    if (timeInterval.charAt(i) == '-') {
                                        count++;
                                        if (count > 1) {
                                            System.out.println("\nPlease make sure you correct entered numbers.\n");
                                            check = true;
                                            break;
                                        }
                                    } else if (!Character.isDigit(timeInterval.charAt(i))) {
                                        System.out.println("\nPlease make sure you entered correct numbers.\n");
                                        check = true;
                                        break;
                                    }
                                }
                            } else if (!timeInterval.contains("-")) {
                                System.out.println("\nPlease make sure you entered correct numbers.\n");
                                check = true;
                            }

                            if (!check) {
                                timeInterval = timeInterval.replaceAll(" ", "");
                                String[] checkTime = timeInterval.split("-");
                                if (Integer.parseInt(checkTime[0]) > Integer.parseInt(checkTime[1])) {
                                    System.out.println("\nWrong time entered! Please try again.\n");
                                    break;
                                }

                                System.out.println("Enter event description: Ex: Squash game with Mary ");
                                String eventDescription = conIn.nextLine();

                                System.out.println("Enter event access control: Ex: Private, Public, Group, and Open  ");
                                String accessControl = conIn.nextLine();

                                flag = remcalendar.addGroupEvent(userName, timeInterval, eventDescription, accessControl);
                                if (flag == true) {
                                    System.out.println("\n.........................................");
                                    System.out.println("Event posted.");
                                    System.out.println(".........................................");
                                } else {
                                    System.out.println("\n\nPost failed! Time overlap!");
                                }
                            }
                        }
                        break;
					case 6:
						String result = remcalendar.viewAllCalendars();
						System.out.println(result);
						break;

					case 9:
						System.out.println("Please enter username: ");
						userName = conIn.nextLine();
						flag = remcalendar.calendarExist(userName);
						if (flag != true) {
							flag = remcalendar.createAnotherCalendar(userName);
							System.out.println("...................................");
							System.out.println("Another calendar created for " + userName);
							System.out.println("...................................");
						} else {
							System.out.println("The username is already registered.");
						}
						break;

					case 10:
						keepGoing = false;
						break;

					case 11:
						System.out.println("Please enter a username you'd like to switch to: ");
						userName = conIn.nextLine();
						flag = remcalendar.calendarExist(userName);
						if (!flag) {
							System.out.println("There is no user with that name.");
						} else {
							remcalendar.setUserName(userName);
							System.out.println("Username is switched to " + remcalendar.getUserName());
						}
						break;

					default:
						System.out.println("Error in operation choice.");
						break;
				}
			}//end while loop
			System.out.println("End of Interactive Test ");

		} catch (Exception e) {
			System.out.println("Client: Exception thrown calling ...");
			e.printStackTrace();
		}

	}
	
	//--------------------Make the compiler happy------------------------------------
	//------Do not re-implement these methods just call remote calendar if needed----
	
	public String getUserName() throws RemoteException, InterruptedException{
		return remcalendar.getUserName();
	}

	public void setUserName(String name) throws RemoteException, InterruptedException{
			remcalendar.setUserName(name);
	}

	public boolean calendarExist(String userName) throws RemoteException{
		return remcalendar.calendarExist(userName);
	}

	public boolean createCalendar(String userName) throws RemoteException{
		return remcalendar.createCalendar(userName);
	}

	public boolean addEvent(String userName, String timeInterval, String eventDescription, String accessControl) throws RemoteException{
		return remcalendar.addEvent(userName, timeInterval, eventDescription, accessControl);
	}

	public String viewCalendar(String userName) throws RemoteException{
		return remcalendar. viewCalendar(userName);
	}

	public boolean deleteEvent(String userName, String eventTime) throws RemoteException{
		return remcalendar.deleteEvent(userName, eventTime); 
	}

	public boolean createAnotherCalendar(String userName) throws RemoteException{
		return remcalendar.createAnotherCalendar(userName);
	}

	public String viewAllCalendars() throws RemoteException{
		return remcalendar.viewAllCalendars();
	}

	public boolean updateEvent(String userName, String pickedTime, String modifiedTime, String eventDescription, String accessControl) throws RemoteException{
		return remcalendar.updateEvent(userName, pickedTime, modifiedTime, eventDescription, accessControl) ;
	}
	
	public boolean addGroupEvent(String userName,
							String timeInterval,
	                        String eventDescription,
	                        String accessControl) throws RemoteException {
	                        return remcalendar.addGroupEvent(userName,
							timeInterval,
	                        eventDescription,
	                        accessControl);
	                        }
	                        
	public ArrayList<String> getActiveUsers()throws RemoteException{
	    return remcalendar.getActiveUsers();
	 }
	//-----End of making the compiler happy------------------------------

}



