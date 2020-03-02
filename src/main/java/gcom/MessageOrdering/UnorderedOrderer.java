package gcom.MessageOrdering;

import gcom.Gcom;
import gcom.Utils.LayerDirection;
import gcom.Communication.Multicaster;
import gcom.Message.iMessage;
import java.rmi.RemoteException;

public class UnorderedOrderer extends Orderer  {


    public UnorderedOrderer(Multicaster caster, Boolean debugmode) {
        super(OrdererType.UNORDERED, caster, debugmode);
    }


    @Override
    public void send(iMessage m) {
        System.out.println(getClass().getSimpleName() + ": Sending Message.");
        // Do nothing
    }

    @Override
    public void receive(iMessage m) {
        System.out.println(getClass().getSimpleName() + ": Receiving Message.");
        // Do nothing
    }

    @Override
    public void triggerNextLayer(LayerDirection dir, iMessage m) {

        switch (dir) {
            case SEND:
                System.out.println("Update orderer send");
                send(m);
                stampPathToUser(m, dir);
                Gcom.sendLayers.nextLayer(m);
                break;
            case RECEIVE:
                System.out.println("Update orderer receive");
                receive(m);
                stampPathToUser(m, dir);
                Gcom.receiveLayers.nextLayer(m);
                break;
            default:
                break;
        }
    }

    @Override
    public void stampPathToUser(iMessage m, LayerDirection dir) {
        try {
            m.appendPath("Unordered("+dir.toString()+")");
            m.setPathCounter(m.getPathCounter()+1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
