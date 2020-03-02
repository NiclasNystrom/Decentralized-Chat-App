package gcom.MessageOrdering;


import gcom.Communication.Multicaster;
import gcom.Message.iMessage;
import java.rmi.RemoteException;

public interface iOrderer   {
    void send(iMessage m) throws RemoteException;
    void receive(iMessage m) throws RemoteException;
    OrdererType getType() throws RemoteException;
    void setType(OrdererType type) throws RemoteException;
    Multicaster getMulticaster() throws RemoteException;
    void setMulticaster(Multicaster multicaster) throws RemoteException;
}
