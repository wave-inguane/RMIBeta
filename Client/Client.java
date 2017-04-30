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
//      String strName = "rmi://localhost:2934/CalendarServices";
		System.out.println("Client: Looking up " + strName + "...");
		RemCalendar remcalendar = null;

		try {

			remcalendar = (RemCalendar) Naming.lookup(strName);
		} catch (Exception e) {

			System.out.println("Client: Exception thrown looking up " + strName);
			System.exit(1);
		}

		try {
			remcalendar.setUserName(userName);
			String userNameUpperCase = remcalendar.EchoMessage();

			System.out.println("From Server: " + userNameUpperCase);

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
							System.out.println("Enter event time: (Ex: 9-10 or 17-19)");
							String timeInterval = conIn.nextLine();

							System.out.println("Enter event description: Ex: Squash game with Mary ");
							String eventDescription = conIn.nextLine();

							System.out.println("Enter event access control: Ex: Private, Public, Group, and Open  ");
							String accessControl = conIn.nextLine();

							flag = remcalendar.addEvent(userName, timeInterval, eventDescription, accessControl);
							if (flag == true) {
								System.out.println("\n.........................................");
								System.out.println("Event posted.");
								System.out.println(".........................................");
							} else {
								System.out.println("\n\nPost failed! Time overlap!");
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

}



