// EchoImpl.java
// Implements the remote object
// Note: The object must extend from UnicastRemoteObject
//       The object must implement the associated interface

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;

public class EchoImpl extends UnicastRemoteObject
    implements Echo
{
    
    public EchoImpl() throws RemoteException { };
    
    
    public String EchoMessage(String Msg) throws RemoteException
    {
	String capitalizedMsg;

        System.out.println("Server: EchoMessage() invoked...");
        System.out.println("Server: Message > " + Msg);
	capitalizedMsg = Msg.toUpperCase();
        return(capitalizedMsg);
    }
}
   
   
   

