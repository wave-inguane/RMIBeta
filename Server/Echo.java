// Echo.java interface for the RMI remote object.
// Note:  Interface must extend from java.rmi.Remote
//          Methods must throw RemoteExcpetion

import java.rmi.*;

public interface Echo extends Remote
{
    String EchoMessage(String strMsg) throws RemoteException;
        
}
