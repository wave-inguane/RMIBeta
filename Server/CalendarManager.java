// CalendarManager.java
<<<<<<< HEAD
<<<<<<< HEAD
// Creates and registers the Echo Service

import java.rmi.*;


public class CalendarManager {
	public static void main(String argv[]) {
		try {
			System.setSecurityManager(new SecurityManager());
			System.out.println("Server: Registering Calendar Service");
			Calendar remote = new Calendar();
			Naming.rebind("CalendarServices", remote);
			//	   Replace with line below if rmiregistry is using port 2934
			//	   Naming.rebind("rmi://localhost:2934/CalendarServices", remote);
			System.out.println("Server: Ready...");
		} catch (Exception e) {
			System.out.println("Server: Failed to register Calendar Service: " + e);
		}
	}
}
