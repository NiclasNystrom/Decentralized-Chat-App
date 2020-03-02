package gcom.MessageOrdering;


import gcom.Gcom;
import gcom.Utils.LayerDirection;
import gcom.Communication.Multicaster;
import gcom.GroupManagement.iUser;
import gcom.Message.VectorClock;
import gcom.Message.iMessage;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CausalOrder extends Orderer implements iCausalOrder, Serializable {

    private VectorClock clock;
    private iUser self;
    private iMessage mReceive;

    private ConcurrentLinkedQueue<iMessage> queue;

    public CausalOrder(Multicaster mc, Boolean debugmode){

        super(OrdererType.CAUSAL, mc, debugmode);
        queue = new ConcurrentLinkedQueue<>();
        mReceive = null;
    }

    public void setSelf(iUser user) throws RemoteException {
        try {
            clock = new VectorClock(user.getId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.self = user;
    }

    @Override
    public void send(iMessage m) {

        try {

            // Copy Vectorclock.
            VectorClock vc = new VectorClock();
            for(Map.Entry e : clock.getClock().entrySet()) {
                vc.getClock().put((UUID)e.getKey(), (Long)e.getValue());
            }

            // Increment Clock
            clock.incrementTick();


            // Port to copied vectorclock.
            for (Map.Entry e : clock.getClock().entrySet()) {
                if (e.getKey().equals(self.getId())) {
                    vc.getClock().put((UUID)e.getKey(), clock.getTick());
                }
            }

            // Stamp to Message
            m.setClock(vc);
            m.setId(self.getId());
            m.setSenderName(self.getUsername());

            // Send
            Gcom.sendLayers.nextLayer(m);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receive(iMessage m) {

        mReceive = null;

        try {

            if (clock.isMessageInTime(m) <= 0) {

                System.out.println("Causal: Message <"+m.getMessage()+"> in time. Sending it!");

                // Increment all clock values corresponding with Message id.
                for (Map.Entry e : clock.getClock().entrySet()) {
                    if (e.getKey().equals(m.getId())) {
                        clock.getClock().put((UUID)e.getKey(), clock.getClock().get(e.getKey()) + 1);
                    }
                }

                mReceive = m;
            } else {
                queue.add(m);
                System.out.println("Causal: Adding <"+m.getMessage()+"> to queue! Size: " + queue.size());
            }

        } catch (RemoteException e1) {
            e1.printStackTrace();
        }


        if (mReceive != null) {
            Gcom.receiveLayers.nextLayer(mReceive);
        } else {
            Gcom.receiveLayers.reset();
        }

        try {
            processOldMessages();
        } catch (RemoteException e) {
            e.printStackTrace();
        }



    }


    public void processOldMessages() throws RemoteException{

        for(iMessage m : queue) {

            try {

                if (isNext(clock, m)) {

                    System.out.println("Causal:  Message <" + m.getMessage() + "> is now ready to send. " + queue.size());

                    // Increment clockvalues from Message sender.
                    for (Map.Entry e : clock.getClock().entrySet()) {
                        if (e.getKey().equals(m.getId())) {
                            clock.getClock().put((UUID)e.getKey(), clock.getClock().get(e.getKey()) + 1);
                        }
                    }


                    // Find index of layer to start afrom (order in this case).
                    int i = Gcom.receiveLayers.getLayerIndexByClass(CausalOrder.class);
                    if ( i >= 0) {
                        Gcom.receiveLayers.send(m, i);
                    }

                    queue.remove(m);

                    // Reiterate til no values that is futurum/presens exists.
                    processOldMessages();
                    break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void triggerNextLayer(LayerDirection dir, iMessage m) {

        switch (dir) {
            case SEND:
                stampPathToUser(m, dir);
                send(m);
                break;
            case RECEIVE:
                stampPathToUser(m, dir);
                receive(m);
                break;
            default:
                break;
        }
    }

    @Override
    public void stampPathToUser(iMessage m, LayerDirection dir) {
        try {
            m.appendPath("Causal("+dir.toString()+")");
            m.setPathCounter(m.getPathCounter()+1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public Boolean isNext(VectorClock clock, iMessage m){

        VectorClock vc = null;
        UUID senderID = null;

        try {
            vc = m.getClock();
            senderID = m.getId();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        for (UUID id : vc.getClock().keySet()) {
            if (id.equals(senderID)) {
                Boolean isNext = (vc.getClock().get(id) == (clock.getClock().get(id) + 1));
                if (!isNext) {
                    return false;
                }
            } else {
                Boolean ahead = (vc.getClock().get(id) <= clock.getClock().get(id));
                if (!ahead) {
                    return false;
                }
            }
        }

        return true;

    }

}
