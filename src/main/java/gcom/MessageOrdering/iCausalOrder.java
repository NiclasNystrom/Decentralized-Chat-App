package gcom.MessageOrdering;

import gcom.GroupManagement.iUser;
import java.rmi.RemoteException;

public interface iCausalOrder extends iOrderer {
	void setSelf(iUser user) throws RemoteException;
}
