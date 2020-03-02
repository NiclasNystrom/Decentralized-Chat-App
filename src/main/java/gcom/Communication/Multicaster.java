package gcom.Communication;


import gcom.Utils.iLayer;
import gcom.GroupManagement.iUser;
import gcom.Message.iMessage;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

public abstract class Multicaster implements iLayer, Serializable {

	protected List<iMessage> incomingMsgs;
	protected iUser user;
	public Multicaster() {
		incomingMsgs = new LinkedList<>();
	}

	public abstract void send(iMessage m) throws RemoteException;
	public abstract void receive(iMessage m) throws RemoteException;

	public void setUser(iUser self) throws RemoteException {
		this.user = self;
	}

	public iMessage getMessage() {
		if (incomingMsgs.size() > 0) {
			iMessage m = incomingMsgs.remove(0);
			return m;
		}
		return null;
	}

}
