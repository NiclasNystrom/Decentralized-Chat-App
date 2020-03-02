package gcom.Communication;

import gcom.Gcom;
import gcom.Utils.LayerDirection;
import gcom.GroupManagement.iUser;
import gcom.Message.Message;
import gcom.Message.iMessage;
import java.rmi.RemoteException;


public class UnreliableMulticaster extends Multicaster {


    public UnreliableMulticaster() throws RemoteException {
        super();
    }


    @Override
    public void send(iMessage m) throws RemoteException {
        Message m2 = (Message) m;
        for (iUser u : m.getMembersToSendMessageTo()) {
            try {
                Message m3 = (Message) m2.clone();
                u.sendMessage(m3);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void receive(iMessage m)  throws RemoteException {
        stampPathToUser(m, LayerDirection.RECEIVE);
        Gcom.receiveLayers.send(m);
    }


    @Override
    public void triggerNextLayer(LayerDirection dir, iMessage m) {

        switch (dir) {
            case SEND:
                try {
                    stampPathToUser(m, dir);
                    send(m);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                Gcom.sendLayers.reset();
                break;
            case RECEIVE:
                try {
                    //stampPathToUser(m, dir);
                    receive(m);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void stampPathToUser(iMessage m, LayerDirection dir) {
        try {
            m.appendPath("Unreliable("+dir.toString()+")");
            m.setPathCounter(m.getPathCounter()+1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
