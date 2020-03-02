package gcom.GroupManagement;


import gcom.MessageOrdering.iOrderer;
import gcom.Message.iMessage;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;


public interface iUser extends Remote {
	public UUID getId() throws RemoteException;
	public void setId(UUID id) throws RemoteException;
	public String getUsername() throws RemoteException;
	public void setUsername(String username) throws RemoteException;
	public iOrderer getOrderer() throws RemoteException;
	public void setOrderer(iOrderer orderer) throws RemoteException;

	public Boolean requestJoin(iUser newUser) throws RemoteException;
	public void addUserToGroup(iUser newUser) throws RemoteException;
	public void setLeader(iUser newLeader) throws RemoteException;
	public void setGroupManager(iGroupManager gm) throws RemoteException;
	public iGroupManager getGroupManager() throws RemoteException;

	public HostInfo getHostInfo() throws RemoteException;
	public void initReceiver() throws RemoteException;
	public void sendMessage(iMessage m) throws RemoteException;

}
