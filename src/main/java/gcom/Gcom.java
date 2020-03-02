package gcom;

import gcom.Debugger.Debugger;
import gcom.MessageOrdering.*;
import gcom.Rmi.NameServer.NameServerMain;
import gcom.Rmi.NameServer.iNameServer;
import gcom.Utils.ArrayConverter;
import gcom.Utils.LinkedLayerTransfer;
import gcom.Utils.LayerDirection;
import gcom.Utils.iLayer;
import gcom.Client.Controller.GuiController;
import gcom.Communication.Multicaster;

import gcom.Communication.UnreliableMulticaster;
import gcom.GroupManagement.GroupManager;
import gcom.GroupManagement.HostInfo;
import gcom.GroupManagement.User;
import gcom.GroupManagement.iUser;
import gcom.Message.iMessage;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class Gcom implements iLayer, Serializable {

    public static LinkedLayerTransfer receiveLayers;
    public static LinkedLayerTransfer sendLayers;

    private GuiController controller;
    private Boolean debugMode;
    private String host;
    private UUID id;

    private GroupManager gm;
    private iUser self;

    private Orderer orderer;
    private OrdererType orderType;

    private Multicaster multicaster;

    private HostInfo info;

    private Registry registry;
    private iNameServer nameserver;

    private Debugger debugger;


    public Gcom(UUID id, String host, Boolean debug, GuiController guiController, HostInfo info) throws RemoteException, NotBoundException {


        this.id = id;
        this.controller = guiController;
        this.debugMode = debug;
        this.host = host;
        this.info = info;

        this.nameserver = connectToNameServer(this.host);
        this.multicaster = new UnreliableMulticaster();

        controller.setUserGcom(this);
        this.controller.refreshGroups();
    }

    public void initGcomCreate() throws RemoteException, NotBoundException  {

        this.orderType = this.controller.getView().isUnorderedOrderSelected()   ? OrdererType.UNORDERED
                                                                                : OrdererType.CAUSAL;
        this.orderer = createOrderer(orderType);

        self = new User(controller.getView().getUsername(), id, orderer, info);
        if (orderType.equals(OrdererType.CAUSAL))
            ((CausalOrder)orderer).setSelf(self);

        gm = new GroupManager(self, this.nameserver);

        receiveLayers = new LinkedLayerTransfer(LayerDirection.RECEIVE);
        receiveLayers.addLayer(multicaster);
        if (debugMode) {
            debugger = new Debugger(false, controller.getView(), this.controller);
            receiveLayers.addLayer(debugger);
        }
        receiveLayers.addLayer(orderer);
        receiveLayers.addLayer(gm);
        receiveLayers.addLayer(this);
        receiveLayers.addLayer(controller);


        sendLayers = new LinkedLayerTransfer(LayerDirection.SEND);
        sendLayers.addLayer(controller);
        sendLayers.addLayer(this);
        sendLayers.addLayer(gm);
        sendLayers.addLayer(orderer);
        if (debugMode) {
            sendLayers.addLayer(debugger);
        }
        sendLayers.addLayer(multicaster);

        self.setGroupManager(gm);
    }

    public void initGcomJoin(String name) throws RemoteException, NotBoundException  {

        iUser groupLeader = null;
        groupLeader = getNameserver().getLeader(name);
        if (groupLeader == null) {
            System.err.println("Could not find groupleader!");
            return;
        }

        this.orderType = groupLeader.getOrderer().getType();
        if (orderType == null) // Could not join
            return;
        this.orderer = createOrderer(orderType);

        self = new User(controller.getView().getUsername(), id, orderer, info);
        if (orderType.equals(OrdererType.CAUSAL))
            ((CausalOrder)orderer).setSelf(self);

        gm = new GroupManager(self, this.nameserver);

        receiveLayers = new LinkedLayerTransfer(LayerDirection.RECEIVE);
        receiveLayers.addLayer(multicaster);
        if (debugMode) {
            debugger = new Debugger(false, controller.getView(), this.controller);
            receiveLayers.addLayer(debugger);
        }
        receiveLayers.addLayer(orderer);
        receiveLayers.addLayer(gm);
        receiveLayers.addLayer(this);
        receiveLayers.addLayer(controller);


        sendLayers = new LinkedLayerTransfer(LayerDirection.SEND);
        sendLayers.addLayer(controller);
        sendLayers.addLayer(this);
        sendLayers.addLayer(gm);
        sendLayers.addLayer(orderer);
        if (debugMode) {
            sendLayers.addLayer(debugger);
        }
        sendLayers.addLayer(multicaster);

        self.setGroupManager(gm);
    }


    public Boolean getDebugMode() {
        return debugMode;
    }

    private Orderer createOrderer(OrdererType t){
        return t.equals(OrdererType.CAUSAL) ? new CausalOrder(multicaster, debugMode)
                                            : new UnorderedOrderer(multicaster, debugMode);
    }


    public iNameServer connectToNameServer(String _host){
        try {
            iNameServer ns = NameServerMain.getNameServer(_host);
            return ns;
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createGroup(String groupname){
        try {
            System.out.println("gcom: Attempting to Create group: " + groupname);
            gm.createNewGroup(groupname);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void joinGroup(String groupname, UUID id){

        try {
            System.out.println("gcom: Attempting to join group: " + groupname);
            initGcomJoin(groupname);
            gm.joinGroup(groupname, id);
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Warning: Could not join group. Possibly no selected leader.");
            //e.printStackTrace();
        }
    }



    public String[] getAllGroups(){

        ConcurrentMap<String, iUser> groups = null;

        try {
            groups = nameserver.getGroups();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (groups != null) {
            return ArrayConverter.toStringArray(groups.keySet());
        }
        return null;
    }


    public UUID getId() {
        return id;
    }

    public GroupManager getGroupManager() {
        return gm;
    }

    public iUser getSelf() {
        return self;
    }

    public OrdererType getOrderType() {
        return orderType;
    }

    public iNameServer getNameserver() {
        return nameserver;
    }

    public Debugger getDebugger() {
        return debugger;
    }

    public void reset() {
        gm.reset();
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
                Gcom.receiveLayers.nextLayer(m);
                break;
            default:
                break;
        }
    }

    @Override
    public void stampPathToUser(iMessage m, LayerDirection dir) {
        try {
            m.appendPath("gcom("+dir.toString()+")");
            m.setPathCounter(m.getPathCounter()+1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
