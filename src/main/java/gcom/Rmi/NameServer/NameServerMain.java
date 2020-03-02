package gcom.Rmi.NameServer;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class NameServerMain {

    public static iNameServer getNameServer(String host) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(host);
        return (iNameServer) registry.lookup(iNameServer.class.getSimpleName());
    }


    public static void main(String[]args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            //Registry registry = LocateRegistry.createRegistry(25099);
            iNameServer iNameServer = new NameServer();
            registry.rebind(iNameServer.class.getSimpleName(), iNameServer);

            System.err.println("Nameserver established!");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
