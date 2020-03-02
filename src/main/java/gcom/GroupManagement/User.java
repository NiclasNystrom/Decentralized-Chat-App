package gcom.GroupManagement;


import gcom.MessageOrdering.iOrderer;
import gcom.Message.iMessage;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;



public class User extends UnicastRemoteObject implements iUser {
    private HostInfo hostInfo;
    private UUID id;
    private String username;
    private iOrderer orderer;
    private iGroupManager gm;

    public User(String username, UUID id, iOrderer orderer, HostInfo info) throws RemoteException {
        this.id = id;
        this.username = username;
        this.orderer = orderer;
        this.hostInfo = info;
    }
    public User(String username, iOrderer orderer) throws RemoteException {
        this.id = UUID.randomUUID();
        this.username = username;
        this.orderer = orderer;
    }


    public UUID getId() throws RemoteException {
        return id;
    }

    public void setId(UUID id)throws RemoteException  {
        this.id = id;
    }

    public String getUsername()throws RemoteException {
        return username;
    }

    public void setUsername(String username)throws RemoteException  {
        this.username = username;
    }

    public iOrderer getOrderer()throws RemoteException {
        return orderer;
    }

    public void setOrderer(iOrderer orderer)throws RemoteException {
        this.orderer = orderer;
    }

    @Override
    public Boolean requestJoin(iUser newUser) throws RemoteException {
        System.out.println("Somemone wants to joinGroup!");
        return true;
    }

    @Override
    public void addUserToGroup(iUser newUser) throws RemoteException {
        System.out.println("adding new user!");
        gm.addUser(newUser);
    }

    @Override
    public void setLeader(iUser newLeader) throws RemoteException {
        gm.setNewLeader(newLeader);
    }

    @Override
    public void setGroupManager(iGroupManager gm) throws RemoteException {
        this.gm = gm;
    }

    @Override
    public iGroupManager getGroupManager() throws RemoteException {
        return gm;
    }

    @Override
    public HostInfo getHostInfo() throws RemoteException {
        return hostInfo;
    }

    @Override
    public void initReceiver() throws RemoteException {
    }

    @Override
    public void sendMessage(iMessage m) throws RemoteException {
        orderer.getMulticaster().receive(m);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User)) {
            return false;
        }

        User u = (User) obj;
        try {
            if (u.getId().equals(getId()))
				return true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;

    }

}
