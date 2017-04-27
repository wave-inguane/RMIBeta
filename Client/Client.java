// Client.java
// 
// This sample Java RMI client can perform the
// following operation:
//    Send a message to a remote object that echoes it back in upper case. 
//    
//     Usage:  java Client "My message in quotes"
//  

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

public class Client {

	public static void main(String argv[]) {
		// Validate command line parameters
		if (argv.length < 1) {

			System.out.println("Usage: java Client \"MESSAGE\"");
			System.exit(1);
		}

		String userName = argv[0];


		// Install security manager.  This is only necessary
		// if the remote object's client stub does not reside
		// on the client machine (it resides on the server).
		//  System.setSecurityManager(new SecurityManager());

		// Get a remote reference to the Calendar class
		String strName = "rmi://localhost/CalendarServices";
		System.out.println("Client: Looking up " + strName + "...");
		RemCalendar  remcalendar = null;

		try {
			remcalendar = (RemCalendar) Naming.lookup(strName);
		} catch (Exception e) {
			System.out.println("Client: Exception thrown looking up " + strName);
			System.exit(1);
		}

		// Send a messge to the remote object

		try {
			remcalendar.setUserName(userName);
			String userNameUpperCase = remcalendar.EchoMessage();

			System.out.println("From Server: " + userNameUpperCase);

			boolean flag = false;

			Scanner conIn = new Scanner(System.in);
			String choice = null;
			String skip;						// skip end of line after reading an integer
			boolean keepGoing = true; 					// flag for "choose operation" loop
			int operation = 0; 					// indicates users choice of operation
			
			while (keepGoing) {
				System.out.println("\nChoose a Task: ");
				System.out.println("1:  create calendar");
				System.out.println("2:  view calendar ");
				System.out.println("3:  post event ");
				System.out.println("4:  modify event ");
				System.out.println("5:  delete event ");
				System.out.println("6:  view calendars");
				System.out.println("7:  post in calendar number");
				System.out.println("8:  view any calendar");
				System.out.println("9:  create another calendar ");
				System.out.println("10: exit ");

				if (conIn.hasNextInt())
					operation = conIn.nextInt();
				else {
					System.out.println("................................ ");
					System.out.println("Error: you must enter an integer ");
					System.out.println("Terminating the program... ");
					System.out.println("................................ ");

				}

				skip = conIn.nextLine();
				switch (operation) {
					case 1:
						flag = remcalendar.calendarExist(userName);
						if (flag == false) {
							System.out.println(".......................................... ");
							System.out.println("New calendar created for " + userName);
							System.out.println(".......................................... ");
							flag = remcalendar.createCalendar(userName); // create a calendar for the current user
						} else {
							System.out.println("This " + userName + " username already have a calendar. ");
						}
						break;

					case 2:
						flag = remcalendar.calendarExist(userName);
						if (flag == false) {
							System.out.println("Please create a calendar first \n"
									+ "or select view calendars to select from existing calendars");
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
							System.out.println("Enter event time: Ex: 9-10 AM, April 20, 2017 ");
							String timeInterval = conIn.nextLine();

							System.out.println("Enter event description: Ex: Squash game with Mary ");
							String eventDescription = conIn.nextLine();

							System.out.println("Enter event access control: Ex: Private, Public, Group, and Open  ");
							String accessControl = conIn.nextLine();

							flag = remcalendar.addEvent(timeInterval, eventDescription, accessControl);
							if (flag == true) {
								System.out.println(".................... ");
								System.out.println("Event posted.");
								System.out.println(".................... ");
							} else {
								System.out.println("Post failed! ");
							}
						}
						break;

					case 4:
						flag = remcalendar.calendarExist(userName);
						if (flag != true) {
							System.out.println("Please create a calendar first \n"
									+ "or select view calendars to select from existing calendars");
						} else {
							System.out.println("Enter calendarNumber");
							int calendarNumber = conIn.nextInt();
							skip = conIn.nextLine();

							System.out.println("Enter username");
							userName = conIn.nextLine();


							System.out.println("Enter event number : Ex: 0, 1, 2 ... ");
							int eventNumber = conIn.nextInt();
							skip = conIn.nextLine();

							if (remcalendar.isOwner(userName, calendarNumber) == true) {

								System.out.println("Enter event time: Ex: 9-10 AM, April 20, 2017 ");
								String timeInterval = conIn.nextLine();
								System.out.println("Enter event description: Ex: Squash game with Mary ");
								String eventDescription = conIn.nextLine();
								System.out.println("Enter event access control: Ex: Private, Public, Group, and Open  ");
								String accessControl = conIn.nextLine();

								List<String> event = remcalendar.modifyEvent(eventNumber);
								event.add(0, timeInterval);
								event.add(1, eventDescription);
								event.add(2, accessControl);
							} else {
								System.out.println(" Access denied ");
							}
						}
						break;

					case 5:
						flag = remcalendar.calendarExist(userName);
						if (flag != true) {
							System.out.println("Please create a calendar first \n"
									+ "or select view calendars to select from existing calendars");
						} else {
							System.out.println("Enter calendarNumber");
							int calendarNumber = conIn.nextInt();
							skip = conIn.nextLine();

							//System.out.println("Enter username");
							//userName = conIn.nextLine();

							if (remcalendar.isOwner(userName, calendarNumber) == true) {
								System.out.println("Enter event number : Ex: 0, 1, 2 ... ");
								int eventNumber = conIn.nextInt();
								skip = conIn.nextLine();

								List<String> event = remcalendar.modifyEvent(eventNumber);
								event.add(0, "deleted");
								event.add(1, "deleted");
								event.add(2, "deleted");
								System.out.println(" ............................. ");
								System.out.println(" Event deleted ");
								System.out.println(" ............................. ");
							} else {
								System.out.println(" Access denied ");
							}
						}
						break;

					case 6:
						System.out.println(remcalendar.viewAllCalendars());
						break;

					case 8:
						flag = remcalendar.calendarExist(userName);
						if (flag != true) {
							System.out.println("Please create a calendar first \n"
									+ "or select view calendars to select from existing calendars");
						} else {
							System.out.println("Please  enter calendar number ");
							int index = conIn.nextInt();
							skip = conIn.nextLine();

							System.out.println("Enter username");
							userName = conIn.nextLine();

							remcalendar.viewAnyCalendar(userName, index);
						}
						break;

					case 9:
						System.out.println("Please  enter username: ");
						userName = conIn.nextLine();
						flag = remcalendar.calendarExist(userName);
						if (flag != true) {
							//TODO find work around
							//calendar =  remcalendar.createAnotherCalendar(userName);
							System.out.println(".................... ");
							System.out.println("New calendar created: ");
							System.out.println(".................... ");
						} else {
							System.out.println("Please provide different username. ");
						}
						break;
					case 10:
						keepGoing = false;
						break;

					default:
						System.out.println("Error in operation choice.");
						break;
				}
				//end switch---------------------------------------------------

			}//end while loop
			System.out.println("End of Interactive Test ");

		} catch (Exception e) {

			System.out.println("Client: Exception thrown calling ...");
			e.printStackTrace();
			//System.exit(1);
		}

	}

}


