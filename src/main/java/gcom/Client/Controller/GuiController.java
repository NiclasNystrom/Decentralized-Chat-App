package gcom.Client.Controller;


import gcom.MessageOrdering.OrdererType;
import gcom.Rmi.NameServer.NameServerMain;
import gcom.Rmi.NameServer.iNameServer;
import gcom.Utils.ArrayConverter;
import gcom.Utils.LayerDirection;
import gcom.Utils.iLayer;
import gcom.GroupManagement.iUser;
import gcom.Message.*;
import gcom.Gcom;

import gcom.Client.View.GUIClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class GuiController implements iLayer, Serializable {

    private static GuiController guiController;
    private GUIClient view;
    private Gcom gcom;
    private UUID selfID;
    private static Hashtable<String, Gcom> gcomClients;


    public GuiController(GUIClient view, UUID id)  {

        this.view = view;
        this.selfID = id;
        guiController = this;
        gcomClients = new Hashtable<>();

        view.holdButton.setText("Hold");
        view.addActionListenerCreate(this.listener_CreateGroupBtn());
        view.addActionListenerJoin(this.listener_JoinRemoveGroupBtn());
        view.addActionListererRefresh(this.listener_RefreshBtn());
        view.addActionListererHold(this.listener_HoldBtn());
        view.addActionListererDebugOutDown(this.listener_DebugDownBtn());
        view.addActionListererDebugOutUp(this.listener_DebugUpBtn());
        view.addWindowListenerLeade(this.listener_OnExit());

        refreshGroups();

    }

    public void setUserGcom(Gcom gcom){
        this.gcom = gcom;
    }


    public ActionListener listener_CreateGroupBtn() {

        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                if (view.hasJoinedGroup()) {
                    gcom.getGroupManager().sendLeaveMessage();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    view.removeGroupTab();
                    view.switchToTab();
                    view.hasJoinedGroup(false);
                    view.createLeaveGroupButton.setText("Create");
                    view.refreshGroupButton.doClick();
                    view.createLeaveGroupButton.setEnabled(true);
                    refreshGroups();
                    return;
                }



                if (view.tabspanel.getTabCount() >= view.MAX_TABS) {
                    view.createGuiMessage("You're already inside a chat!");
                    return;
                }

                String username  = view.getUsername();
                String groupName = view.showGroupCreation();

                if (groupName == null) {
                    return;
                } else {
                    if (groupName.length() < 0) {
                        view.createGuiMessage("Insufficient info when creating group!");
                        return;
                    }
                }

                try {
                    gcom.initGcomCreate();
                    gcom.createGroup(groupName);

                    if(view.addGroupTab() != null) {
                        view.addSendListenerToGroup(groupName, listener_SendBtn());
                        gcomClients.put(groupName, gcom);
                        view.hasJoinedGroup(true);
                    }
                } catch (RemoteException | NotBoundException e1) {
                    e1.printStackTrace();
                }
            }
        };
    }

    public ActionListener listener_JoinRemoveGroupBtn() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String groupName = view.getGroupNameFromTable();

                if (!view.hasJoinedGroup()) {

                    view.addJoinTab(groupName);
                    gcom.joinGroup(groupName, selfID);

                    String[] users = gcom.getGroupManager().getUsersOfGroup(groupName);
                    //View.refreshUsernames(groupName, users);
                    view.addSendListenerToGroup(groupName, listener_SendBtn());
                    view.switchToTab();

                } else {

                    if (!checkIfUserIsLeaderOfGroup(groupName)) {
                        System.err.println("Only Groupleaders can remove group!");
                        return;
                    }

                    gcom.getGroupManager().sendMassLeaveMessage();
                    refreshGroups();
                }
            }
        };
    }

    private ActionListener listener_SendBtn() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String information = (String)((JButton)e.getSource()).getName();
                String groupname = information.split("\\.")[0];
                String username  = information.split("\\.")[1];
                String message   = view.getMessage(groupname);

                gcom.getGroupManager().sendMessage(groupname, username, message);
            }
        };
    }

    public WindowListener listener_OnExit(){
        return new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
                if (!view.hasJoinedGroup()) {
                    System.exit(0);
                    return;
                }
                gcom.getGroupManager().sendLeaveMessage();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        };
    }

    public ActionListener listener_RefreshBtn() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshGroups();
            }
        };
    }

    public void refreshGroups() {

        if (gcom == null) {
            // Check with nameserver if no previous record.
            try {
                iNameServer ns = NameServerMain.getNameServer(view.getHost());
                ConcurrentHashMap<String, iUser> _groups = ns.getGroups();
                view.updateGroups(ArrayConverter.toStringArray(_groups));
            } catch (RemoteException | NotBoundException e1) {
                e1.printStackTrace();
            }
        } else {
            // Update previous record.
            view.updateGroups(gcom.getAllGroups());
        }
    }



    public ActionListener listener_DebugUpBtn() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (view.debugHoldMsgsOut.getSelectedRowCount() < 1) {
                    System.err.println("Warning: Must select row");
                }

                int index = view.debugHoldMsgsOut.getSelectedRow();
                if (index > 0) {
                    DefaultTableModel m = (DefaultTableModel) view.debugHoldMsgsOut.getModel();
                    m.moveRow(index, index, index - 1);
                    view.debugHoldMsgsOut.setRowSelectionInterval(index-1, index-1);
                    gcom.getDebugger().swapOut(index, index - 1);
                }

            }
        };
    }

    public ActionListener listener_DebugDownBtn() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (view.debugHoldMsgsOut.getSelectedRowCount() < 1) {
                    System.err.println("Warning: Must select row");
                }

                int index = view.debugHoldMsgsOut.getSelectedRow();
                if (index < (view.debugHoldMsgsOut.getRowCount() - 1)) {
                    DefaultTableModel m = (DefaultTableModel) view.debugHoldMsgsOut.getModel();
                    m.moveRow(index, index, index + 1);
                    view.debugHoldMsgsOut.setRowSelectionInterval(index+1, index+1);
                    gcom.getDebugger().swapOut(index, index + 1);
                }
            }
        };
    }


    public ActionListener listener_HoldBtn() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Boolean doHold = !gcom.getDebugger().holdMessages;
                String txtBtn  = doHold ? "Release" : "Hold";

                gcom.getDebugger().setHoldMessages(doHold);
                view.holdButton.setText(txtBtn);
            }
        };
    }


    public Boolean checkIfUserIsLeaderOfGroup(String gname){
        try {
            iUser leader = gcom.getNameserver().getLeader(gname);
            if (leader != null) {
                if (leader.getId().equals(gcom.getSelf().getId())) {
                    return true;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Gcom getGcom() {
        return gcom;
    }
    public GUIClient getView() {
        return view;
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
                messageEventHandlerReceive(m);
                Gcom.receiveLayers.nextLayer(m);
                break;
            default:
                break;
        }
    }

    public void printClock(iMessage m) {
        if (!guiController.gcom.getOrderType().equals(OrdererType.CAUSAL))
            return;
        try {
            m.getClock().printClock();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void printPath(String path, int c){
        String[] p = path.split("-");
        System.out.println("------- Printing Path ----------");
        System.out.println(path);
        for (String _p : p)
            System.out.println("Path: " + _p);
        System.out.println("Counter: " + c);
        System.out.println("--------------------------------");
    }

    @Override
    public void stampPathToUser(iMessage m, LayerDirection dir) {
        try {
            m.appendPath("GuiController("+dir.toString()+")");
            m.setPathCounter(m.getPathCounter()+1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void appendDebugInformation(iMessage m){

        if (!gcom.getOrderType().equals(OrdererType.CAUSAL))
            return;

        JTextArea area = view.getDebugTextArea();
        try {
            area.append("\nPrinting information for m: <"+m.getMessage()+"> from " + m.getSenderName() + " in group " + m.getGroupName() + "\n");
            String[] p = m.getPath().split("-");
            area.append("------- Printing Path ----------"  + "\n");
            int i = 1;
            for (String _p : p) {
                area.append("Path " + i + ": " + _p + "\n");
                i++;
            }
            area.append("Counter: " + m.getPathCounter() + "\n");
            area.append("--------------------------------\n");


            HashMap<String, iUser> userList = new HashMap<>();
            for(iUser u : gcom.getGroupManager().getOnlineUsers()) {
                userList.putIfAbsent(u.getId().toString(), u);
            }
            System.out.println("-------- Printing Vectorclock --------");
            for(Map.Entry e : m.getClock().getClock().entrySet()) {
                String k = ((UUID)e.getKey()).toString();
                area.append(k + ": " + (Long)e.getValue() + "\n");
            }
            System.out.println("--------------------------------------");
            area.append("-------------------------------------------------------------------------------------------------------\n");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void messageEventHandlerReceive(iMessage m) {
        try {
            String gname = gcom.getGroupManager().getCurrentGroupName();
            String un = m.getSenderName();
            String[] usernames = ArrayConverter.toStringArray(gcom.getGroupManager().getOnlineUsers());
            UUID id = m.getId();

            switch (m.getMessageType()) {
                case JOIN:

                    view.appendSystemMessage(gname, m.getMessage());

                    if (!view.hasJoinedGroup()) {
                        view.hasJoinedGroup(true);
                    }
                    break;
                case MESSAGE:
                    view.appendMessage(gname, m.getMessage(), m.getSenderName());
                    if (gcom.getDebugMode())
                        appendDebugInformation(m);
                    break;
                case LEAVE:
                    view.appendSystemMessage(gname, m.getMessage());

                    if (id.equals(gcom.getId())) {
                        view.hasJoinedGroup(false);
                        refreshGroups();
                    }

                    break;
                case ELECT:
                    view.appendSystemMessage(gname, m.getMessage());

                    if (gcom.getGroupManager().getLeader().getId().equals(gcom.getSelf().getId())) {
                        view._JoinOrRemoveGroupButton.setText("Remove");
                        refreshGroups();
                    }

                    break;
                case MASSLEAVE:
                    view.appendSystemMessage(gname, m.getMessage());
                    view.removeGroupTab();
                    view.switchToTab();

                    try {
                        gcom.getGroupManager().removeGroupFromNameserver();
                        String[] groups = ArrayConverter.toStringArray(Collections.list(gcom.getNameserver().getGroups().keys()));
                        gcom.reset();
                        view.updateGroups(groups);
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }
                    view.hasJoinedGroup(false);
                    refreshGroups();
                    break;
                default:
                    System.out.println("Controller: null");
                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
