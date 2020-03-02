package gcom.Rmi.NameServer;

import gcom.GroupManagement.iUser;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class NameServer extends UnicastRemoteObject implements iNameServer {

    private ConcurrentHashMap<String, iUser> leaders;

    public NameServer() throws RemoteException {
        leaders = new ConcurrentHashMap();
    }


    public synchronized ConcurrentHashMap<String, iUser> getGroups() throws RemoteException{
        return leaders;
    }


    @Override
    public Boolean addGroup(String groupName, iUser leader)
            throws RemoteException {

        if(!leaders.containsKey(groupName)) {
            leaders.put(groupName, leader);
            return true;
        }

        return false;
    }

    @Override
    public void removeGroup(String groupName) throws RemoteException{
        leaders.remove(groupName);
    }

    @Override
    public boolean replaceLeader(String groupName, iUser m) throws RemoteException {

        try{

            if (leaders.containsKey(groupName)) {
                leaders.replace(groupName, m);
            } else {
                leaders.put(groupName, m);
            }

            return true;
        } catch (Exception e) {
            leaders.put(groupName,m);
            return true;
        }
    }

    @Override
    public iUser getLeader(String groupName) {
        return leaders.get(groupName);
    }

}
