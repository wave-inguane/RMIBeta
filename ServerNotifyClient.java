package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


/////////////////////////////////////////////////////////////////
//CLIENT SIDE
interface ClientRemote extends Remote 
{
    public void timeUp() throws RemoteException;
}

class Client implements ClientRemote 
{
	
    public Client() throws RemoteException 
    {
        UnicastRemoteObject.exportObject(this, 0);
    }

    @Override
    public void timeUp() throws RemoteException {
        System.out.println("Server invoked");
        //alarm go off 
        //display popup timer
    }
}

/////////////////////////////////////////////////////////////////
//SERVER SIDE

interface ServerRemote extends Remote 
{
    public void registerClient(ClientRemote client) throws RemoteException;
}

class Server implements ServerRemote
{
    private volatile ClientRemote client;

    public Server() throws RemoteException 
    {
        UnicastRemoteObject.exportObject(this, 0);
    }

    @Override
    public void registerClient(ClientRemote client) throws RemoteException 
    {
        this.client = client;
    }

    public void AlertClient() throws RemoteException 
    {
        //set Timmer when it goes off
    	//let the client know
    	client.timeUp();
    }
}