package gcom.Message;


import gcom.GroupManagement.iUser;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface iMessage extends Remote {

    MessageTypes getMessageType() throws RemoteException;
    List<iUser> getMembersToSendMessageTo()throws RemoteException;
    void setRecipients(iUser... u) throws RemoteException;
    void setClock(VectorClock vc) throws RemoteException;
    VectorClock getClock() throws RemoteException;
    String getMessage() throws RemoteException;
    void setMessage(String msg) throws RemoteException;
    String getSenderName() throws RemoteException;
    void setSenderName(String name) throws RemoteException;
    String getReceiverName() throws RemoteException;
    void setReceiverName(String name) throws RemoteException;
    String getGroupName() throws RemoteException;
    void setGroupName(String name) throws RemoteException;
    UUID getId() throws RemoteException;
    void setId(UUID id) throws RemoteException;
    String getPath() throws RemoteException;
    void appendPath(String path) throws RemoteException;
    int getPathCounter() throws RemoteException;
    void setPathCounter(int pathCounter) throws RemoteException;
}
