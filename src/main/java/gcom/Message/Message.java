package gcom.Message;

import gcom.GroupManagement.iUser;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Message extends UnicastRemoteObject implements iMessage, Serializable, Cloneable {
	public MessageTypes type;
	private String message;

	private List<iUser> sendToMembers;
	private String _fromMember;
	private String _receiverName;
	private String groupname;
	private UUID senderID;
	private VectorClock clock;
	private String path;
	private int pathCounter;

	public Message(MessageTypes type) throws RemoteException {
		this.type = type;
		sendToMembers = new ArrayList<>();
		groupname = "";
		_receiverName = "";
		_fromMember = "";
		message = "";
		path = "Start";
		pathCounter = 0;
	}

	@Override
	public MessageTypes getMessageType() throws RemoteException {
		return type;
	}

	@Override
	public List<iUser> getMembersToSendMessageTo() throws RemoteException {
		return sendToMembers;
	}

	@Override
	public void setRecipients(iUser... u) throws RemoteException {
		for(iUser _u : u) {
			sendToMembers.add(_u);
		}
	}

	@Override
	public void setClock(VectorClock vc) throws RemoteException {
		this.clock = vc;
	}

	@Override
	public VectorClock getClock() throws RemoteException {

		return clock;
	}

	@Override
	public String getMessage() throws RemoteException {
		return message;
	}

	@Override
	public void setMessage(String msg) throws RemoteException {
		message = msg;
	}

	@Override
	public String getSenderName() throws RemoteException {
		return _fromMember;
	}

	@Override
	public void setSenderName(String name) throws RemoteException {
		_fromMember = name;
	}

	@Override
	public String getReceiverName() throws RemoteException {
		return _receiverName;
	}

	@Override
	public void setReceiverName(String name) throws RemoteException {
		_receiverName = name;
	}


	@Override
	public String getGroupName() throws RemoteException {
		return groupname;
	}

	@Override
	public void setGroupName(String name) throws RemoteException {
		groupname = name;
	}

	@Override
	public UUID getId() throws RemoteException {
		return senderID;
	}

	@Override
	public void setId(UUID id) throws RemoteException {
		senderID = id;
	}


	public String getPath()throws RemoteException  {
		return path;
	}

	public void appendPath(String path) throws RemoteException {
		String p = "-"+path;
		this.path = this.path + p;
		//this.path = path;
	}

	public int getPathCounter() throws RemoteException {
		return pathCounter;
	}

	public void setPathCounter(int pathCounter) throws RemoteException {
		this.pathCounter = pathCounter;
	}
}
