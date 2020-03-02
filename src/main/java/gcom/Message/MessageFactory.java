package gcom.Message;

import gcom.GroupManagement.GroupManager;
import gcom.GroupManagement.iUser;
import java.rmi.RemoteException;
import java.util.UUID;

public class MessageFactory {

	private Message m;

	public MessageFactory createMessage() {
		try {
			m = new Message(MessageTypes.MESSAGE);
			return this;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}


	public MessageFactory createJoinMessage() {
		try {
			m = new Message(MessageTypes.JOIN);
			return this;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public MessageFactory createLeaveMessage() {
		try {
			m = new Message(MessageTypes.LEAVE);
			return this;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public MessageFactory createMassLeaveMessage() {
		try {
			m = new Message(MessageTypes.MASSLEAVE);
			return this;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}


	public MessageFactory createElectMessage() {
		try {
			m = new Message(MessageTypes.ELECT);
			return this;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}


	public MessageFactory setMessage(String msg) {
		try {
			m.setMessage(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return this;
	}

	public MessageFactory setReceivers(iUser...users) {
		try {
			m.setRecipients(users);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return this;
	}

	public MessageFactory setSender(String name) {
		try {
			m.setSenderName(name);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return this;
	}

	public MessageFactory setGroupName(String name) {
		try {
			m.setGroupName(name);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return this;
	}

	public MessageFactory setId(UUID id) {
		try {
			m.setId(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return this;
	}


	public Message build(){
		return m;
	}


	public void send(GroupManager gm) {
		try {
			gm.sendToGroup(m);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
