package gcom.GroupManagement;


import gcom.Message.iMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface iGroupManager extends Remote {
	UUID getId() throws RemoteException;
	List<iUser> getGroups() throws RemoteException;
	List<iUser> getLeaders() throws RemoteException;
	void joinGroup(String groupname, UUID userID) throws RemoteException;
	void createNewGroup(String groupname) throws RemoteException;
	void removeUser(iUser user) throws RemoteException;
	void addUser(iUser user) throws RemoteException;
	void sendToGroup(iMessage message) throws RemoteException;
	void electLeader(iUser newLeader) throws RemoteException;
	String[] getUsersFromLeader() throws RemoteException;
	String[] getUsersOfGroup(String grp) throws RemoteException;
	iUser[] getOnlineUsers() throws RemoteException;
	void setNewLeader(iUser nLeader) throws RemoteException;
	public iUser getSelf() throws RemoteException;
	public String getCurrentGroupName() throws RemoteException;
}
