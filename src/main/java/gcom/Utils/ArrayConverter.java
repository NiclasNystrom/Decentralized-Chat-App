package gcom.Utils;

import gcom.GroupManagement.iUser;
import gcom.Message.iMessage;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ArrayConverter {

	public static String[] toStringArray(Set<String> set) {
		return set.stream().toArray(String[]::new);
	}
	public static String[] toStringArray(List<String> set) {
		return set.stream().toArray(String[]::new);
	}
	public static String[] toStringArray(ConcurrentHashMap<String, iUser> map) {
		List<String> keys = new ArrayList<>();
		for (String k : map.keySet()) {
			keys.add(k);
		}
		return toStringArray(keys);
	}
	public static String[] toStringArray(HashMap<String, iUser> map) {
		List<String> keys = new ArrayList<>();
		for (String k : map.keySet()) {
			keys.add(k);
		}
		return toStringArray(keys);
	}
	public static String[] toStringArray(iUser[] users) {

		List<String> keys = new ArrayList<>();
		for (iUser u: users) {
			try {
				keys.add(u.getUsername());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return toStringArray(keys);
	}

	public static iUser[] toUserArray(Set<iUser> set) {
		return set.stream().toArray(iUser[]::new);
	}
	public static iUser[] toUserArray(List<iUser> set) {
		return set.stream().toArray(iUser[]::new);
	}
	public static iUser[] toUserArray(ConcurrentLinkedDeque<iUser> set) {
		return set.stream().toArray(iUser[]::new);
	}
	public static iUser[] toUserArray(HashMap<UUID, iUser> map) {
		Collection<iUser> users = map.values();
		List<iUser> userlist = new ArrayList<>(users);
		return toUserArray(userlist);
	}
	public static iUser[] toUserArray(Collection<iUser> c) {
		List<iUser> userlist = new ArrayList<>(c);
		return toUserArray(userlist);
	}

	public static List<iUser> toUserList(iUser[] users) {

		List<iUser> users2 = new LinkedList<>();
		for (iUser u : users) {
			users2.add(u);
		}
		return users2;
	}
	public static iMessage[] toMessageArray(Set<iMessage> set) {
		return set.stream().toArray(iMessage[]::new);
	}




}
