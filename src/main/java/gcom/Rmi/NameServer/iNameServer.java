package gcom.Rmi.NameServer;

import gcom.GroupManagement.iUser;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

public interface iNameServer extends Remote {
    ConcurrentHashMap<String, iUser> getGroups() throws RemoteException;
    iUser getLeader(String groupName) throws RemoteException;
    Boolean addGroup(String groupName, iUser leader) throws RemoteException;
    void removeGroup(String groupName) throws RemoteException;
    boolean replaceLeader(String groupName, iUser m) throws RemoteException;
}
