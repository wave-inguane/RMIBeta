
// EchoClient.java
// 
// This sample Java RMI client can perform the
// following operation:
//    Send a message to a remote object that echoes it back in upper case. 
//    
//     Usage:  java EchoClient "My message in quotes"
//  
import java.rmi.*;
import java.rmi.server.*;

public class EchoClient 
{
    public static void main(String argv[])
    {
        // Validate command line parameters
        if (argv.length < 1) {
	    
	    System.out.println("Usage: java EchoClient \"MESSAGE\"");
	    System.exit(1);
	}

       

        String strMsg = argv[0];
        

        // Install security manager.  This is only necessary
        // if the remote object's client stub does not reside
        // on the client machine (it resides on the server).
        //  System.setSecurityManager(new SecurityManager());

        // Get a remote reference to the EchoImpl class
        String strName = "rmi://localhost/EchoService";
//      String strName = "rmi://localhost:2934/EchoService";
        System.out.println("Client: Looking up " + strName + "...");
	Echo RemEcho = null;
	
        try {
	
	    RemEcho = (Echo)Naming.lookup(strName);
	}
        catch (Exception e) {
	    
	    System.out.println("Client: Exception thrown looking up " + strName);
	    System.exit(1);
	}

        // Send a messge to the remote object
        
        
	try {
	    String modifiedMsg = RemEcho.EchoMessage(strMsg);
            
	    System.out.println("From Server: "+ modifiedMsg);
	}
	catch (Exception e) {
	    
	    System.out.println("Client: Exception thrown calling EchoMessage().");
	    System.exit(1);
	}
    }
           
}



