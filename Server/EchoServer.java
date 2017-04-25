// EchoServer.java
// Creates and registers the Echo Service

import java.rmi.*;


public class EchoServer {
	public static void main(String argv[]) {
		try {
			System.setSecurityManager(new SecurityManager());
			System.out.println("Server: Registering Echo Service");
			EchoImpl remote = new EchoImpl();
			Naming.rebind("EchoService", remote);
			//	   Replace with line below if rmiregistry is using port 2934
			//	   Naming.rebind("rmi://localhost:2934/EchoService", remote);
			System.out.println("Server: Ready...");
		} catch (Exception e) {
			System.out.println("Server: Failed to register Echo Service: " + e);
		}
	}
}
