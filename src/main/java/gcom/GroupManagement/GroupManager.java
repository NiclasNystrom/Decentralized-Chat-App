package gcom.GroupManagement;

import gcom.Gcom;
import gcom.Rmi.NameServer.iNameServer;
import gcom.Utils.ArrayConverter;
import gcom.Utils.LayerDirection;
import gcom.Utils.iLayer;
import gcom.Message.*;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;


public class GroupManager implements iLayer, Serializable, iGroupManager {

	private iNameServer nameserver;
	private iUser self;
	private iUser leader;

	private String groupName;
	public HashMap<UUID, iUser> onlineUsers;


	private Thread threadIsLeaderAlive;

    public GroupManager(iUser user, iNameServer ns) throws RemoteException, NotBoundException {
		this.nameserver = ns;
		this.self = user;
		this.leader = this.self;
		onlineUsers = new HashMap<>();
		onlineUsers.putIfAbsent(user.getId(), user);
    }


	@Override
	public UUID getId() throws RemoteException {
		return self.getId();
	}

	@Override
	public List<iUser> getGroups() throws RemoteException {
		List<iUser> _users = new LinkedList<>();
		for (iUser u : nameserver.getGroups().values()) {
			_users.add(u);
		}
		return _users;
	}

	@Override
	public List<iUser> getLeaders() throws RemoteException {
		return null;
	}

	public iUser getLeader() {
		return leader;
	}

	@Override
	public void joinGroup(String groupname, UUID userID) throws RemoteException {

    	iUser newLeader = nameserver.getLeader(groupname);
		if (newLeader == null) {
			System.err.println("Warning Gm Join: leader does not exist");
			return;
		}

		leader = newLeader;

		Boolean answer = leader.requestJoin(self);
		if (answer != null) {
			if (answer) {
				System.out.println("Got permission to join Group!");
				this.groupName = groupname;
				leader.addUserToGroup(self);
				startThreadForCheckingIfLeaderIsAlive();
			} else {
				System.err.println("Error: Permission Denied!");
				return;
			}
		} else {
			// No eligable answer -> No Leader
			System.out.println("No leader");
			leader = self;
			nameserver.replaceLeader(groupname, self);
			startThreadForCheckingIfLeaderIsAlive();
		}
		this.groupName = groupname;

	}


	@Override
	public void createNewGroup(String groupname) throws RemoteException {

		iUser newLeader = nameserver.getLeader(groupname);
		if (newLeader == null) {
			createGroup(groupname);
			startThreadForCheckingIfLeaderIsAlive();
		} else {
			// Join existing group
			leader = newLeader;
			Boolean answer = leader.requestJoin(self);
			if (answer == null) {

			} else {

				if (answer) {
					leader.addUserToGroup(self);
				} else {
					System.err.println("Error: Permission Denied!");
					return;
				}
			}
		}
		this.groupName = groupname;
	}




	@Override
	public void removeUser(iUser user) throws RemoteException {
		onlineUsers.remove(user.getId());
	}

	@Override
	public void addUser(iUser user) throws RemoteException {

    	if (leader.getId().equals(self.getId())) {

    		for (iUser _user : onlineUsers.values()) {
				if (_user.getId().equals(self.getId()))
					continue;

				_user.addUserToGroup(user);
				user.addUserToGroup(_user);
			}

			user.addUserToGroup(self);
			onlineUsers.putIfAbsent(user.getId(), user);
			sendJoinMessage(user);
			return;
		}
		onlineUsers.putIfAbsent(user.getId(), user);
	}

	public void sendJoinMessage(iUser user) {
		try {
			iMessage msg = new MessageFactory().createJoinMessage()
					.setReceivers(getOnlineUsers())
					.setMessage(user.getUsername() + " has joined " + groupName)
					.setGroupName(groupName)
					.setId(self.getId())
					.setSender(self.getUsername()).build();
			Gcom.sendLayers.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendToGroup(iMessage message) throws RemoteException {
		message.setSenderName(self.getUsername());
		message.setGroupName(groupName);
		Gcom.sendLayers.send(message);
	}


	@Override
	public void electLeader(iUser newLeader) throws RemoteException {
		nameserver.replaceLeader(groupName, newLeader);
		Message m = new MessageFactory().createElectMessage()
				.setGroupName(groupName)
				.setId(self.getId())
				.setMessage(self.getUsername() + " has been elected new leader!")
				.setReceivers(getOnlineUsers())
				.setSender(self.getUsername()).build();
		sendToGroup(m);
	}


	public void removeGroupFromNameserver(){
		try {
			iUser _leader = nameserver.getLeader(groupName);
			if (_leader != null) {
				if (_leader.getId().equals(self.getId())) {
					nameserver.removeGroup(groupName);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void reset() {
		onlineUsers.clear();
		leader = null;
		groupName = "";
		if (threadIsLeaderAlive != null)
			threadIsLeaderAlive.interrupt();
	}

	private void createGroup(String gname) throws RemoteException {
		leader = self;
		nameserver.addGroup(gname, self);
		this.groupName = gname;
	}

	public iUser[] getOnlineUsers(){
		List<iUser> us = new ArrayList<>();
		for (iUser u : onlineUsers.values()) {
			us.add(u);
		}
		return ArrayConverter.toUserArray(us);
	}

	public String[] getUsersFromLeader(){

		List<String> un = new ArrayList<>();
		try {
			for (iUser u : leader.getGroupManager().getOnlineUsers()) {
				un.add(u.getUsername());
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return ArrayConverter.toStringArray(un);
	}

	public String[] getUsersOfGroup(String grp){

		List<String> un = new ArrayList<>();
		try {
			iUser _leader = nameserver.getLeader(grp);
			for (iUser u : _leader.getGroupManager().getOnlineUsers()) {
				un.add(u.getUsername());
			}
		} catch (RemoteException e) {
			//e.printStackTrace();
		}
		return ArrayConverter.toStringArray(un);
	}

	public void setNewLeader(iUser nLeader) {
		leader = nLeader;
	}
	public iUser getSelf() {
		return self;
	}


	@Override
	public String getCurrentGroupName() throws RemoteException {
		return groupName;
	}

	public List<iUser> getAllUsersExept(UUID _id) {
		List<iUser> users = new ArrayList();
		for(iUser u : onlineUsers.values()) {
			try {
				if (!u.getId().equals(_id)) {
					users.add(u);
				}
			} catch (RemoteException e) {
				//e.printStackTrace();
			}
		}
		return users;
	}


	public void sendMassLeaveMessage() {
		try {
			String un = self.getUsername();
			iMessage m = new MessageFactory().createMassLeaveMessage()
					.setSender(self.getUsername())
					.setId(self.getId())
					.setReceivers(getOnlineUsers())
					.setMessage(self.getUsername() + " has terminated server. All users are kicked!")
					.setGroupName(groupName).build();
			sendToGroup(m);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String groupname, String username, String message){
		try {
			iMessage msg = new MessageFactory().createMessage()
					.setGroupName(groupname)
					.setSender(username)
					.setReceivers(getOnlineUsers())
					.setMessage(message).setId(self.getId()).build();
			sendToGroup(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void sendLeaveMessage(){
		System.out.println("Sending Leave Message");
		if (onlineUsers.size() > 1) {
			try {
				String un = self.getUsername();
				UUID mId = self.getId();

				iMessage m = new MessageFactory().createLeaveMessage()
							.setSender(self.getUsername())
							.setId(self.getId())
							.setReceivers(ArrayConverter.toUserArray(getAllUsersExept(mId)))
							.setMessage(self.getUsername() + " has left.")
							.setGroupName(groupName).build();
				sendToGroup(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			// If no other member online -> Remove group.
			try {
				nameserver.removeGroup(groupName);
			} catch (RemoteException e) {
			}
		}
	}


	private void onReceiveMessage(iMessage m) {
		try {
			if (m.getMessageType().equals(MessageTypes.LEAVE)) {
				if (m.getId().equals(self.getId())) {
					System.out.println("Left group. Clearing online users!");
					threadIsLeaderAlive.interrupt();
					onlineUsers.clear();
				} else {
					for (iUser u : getOnlineUsers()) {
						if (u.getId().equals(m.getId())) {
							System.out.println("User " + u.getUsername() + " leave!");
							//onlineUsers.remove(u);
							removeUser(u);
							break;
						}
					}
					try {
						if (m.getId().equals(leader.getId())) {
							System.out.println("Replacing leader with myself");
							electLeader(self);
						}
					} catch (Exception e) {
						System.out.println("Replacing leader with myself 2");
						electLeader(self);
					}

				}
			} else if(m.getMessageType().equals(MessageTypes.JOIN)) {
				System.out.println("Join Message received!");
				if (m.getId().equals(self.getId())) {
					System.out.println("Starting continously check for leader");
					startThreadForCheckingIfLeaderIsAlive();
				}

				for(iUser u : m.getMembersToSendMessageTo()) {
					if (!onlineUsers.containsKey(u.getId())) {
						addUser(u);
						break;
					}
				}


			} else if(m.getMessageType().equals(MessageTypes.ELECT)) {
				for (iUser u : getOnlineUsers()) {
					if (u.getId().equals(m.getId())) {
						System.out.println("Replaced leader!");
						leader = u;
						break;
					}
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void triggerNextLayer(LayerDirection dir, iMessage m) {
		switch (dir) {
			case SEND:
				stampPathToUser(m, dir);
				Gcom.sendLayers.nextLayer(m);
				break;
			case RECEIVE:
				stampPathToUser(m, dir);
				onReceiveMessage(m);
				Gcom.receiveLayers.nextLayer(m);
				break;
			default:
				break;
		}
	}

	@Override
	public void stampPathToUser(iMessage m, LayerDirection dir) {
		try {
			m.appendPath("GroupManager("+dir.toString()+")");
			m.setPathCounter(m.getPathCounter()+1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}


	public void startThreadForCheckingIfLeaderIsAlive() {
		threadIsLeaderAlive = new Thread(new Runnable() {
			@Override
			public void run() {

				while(true) {

					try {
						if (leader != null) {
							if (leader.getId() == null) {
								System.out.println("Elect my self as new leader!");
								electLeader(self);
							}
						}
						Thread.sleep(5000);
					} catch (InterruptedException | RemoteException e) {

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							//e1.printStackTrace();
						}
					}
				}

			}
		});
		threadIsLeaderAlive.start();
	}

}
