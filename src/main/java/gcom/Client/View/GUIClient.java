package gcom.Client.View;


import gcom.MessageOrdering.OrdererType;
import gcom.Client.Controller.GuiController;
import gcom.Client.View.Components.CreateGroupWindow;
import gcom.Client.View.Components.EnterHostWindow;
import gcom.Message.iMessage;


import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Hashtable;

public class GUIClient {

    public int MAX_TABS = 2; // Home and one group.

    public  Boolean hasEnteredHostInfo;
    private String username;
    private String host = null;
    private Boolean debugmode = false;
    private HashSet<String> groups;
    private Hashtable<String, String> groupUsers;
    private Boolean hasEnteredGroup;

    private JFrame frame;
    private JPanel homePanel;
    private JPanel groupTab;
    private JPanel debugOutPanel;

    public  JTabbedPane tabspanel;

    private JTable groupTable;
    private DefaultTableModel tableModel;


    public  JTable debugHoldMsgsOut;
    private DefaultTableModel debugtableModel;
    private JTextArea debugTextArea;


    public  JButton _JoinOrRemoveGroupButton;
    public  JButton refreshGroupButton;
    public  JButton holdButton;
    public  JButton removeElementDebugButton;
    public  JButton createLeaveGroupButton;
    public  JButton debugOutUpButton;
    public  JButton debugOutDownButton;


    private CreateGroupWindow groupCreaterWindow;
    private EnterHostWindow hostWindow;
    private GuiController controller;

    public GUIClient() {

        hasEnteredHostInfo = false;
        hasEnteredGroup = false;

        frame = new JFrame("GCOM-Chat");
        frame.setMinimumSize(new Dimension(800, 400));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        tabspanel = new JTabbedPane();
        groupTab  = new JPanel();
        groupUsers = new Hashtable();
        groups = new HashSet<>();

        promptHostInformation();
        groupTable = createGroupTable();
        JScrollPane gsp = createTableScroll(groupTable);

        homePanel = new JPanel();
        homePanel.setLayout(new BorderLayout());
        homePanel.add(gsp, BorderLayout.CENTER);

        createButtons();

        JPanel  lowerGroupButtons = new JPanel();
                lowerGroupButtons.add(_JoinOrRemoveGroupButton);
                lowerGroupButtons.add(createLeaveGroupButton);
                lowerGroupButtons.add(refreshGroupButton);

        debugOutPanel = createDebugPanel();

        groupTab.setLayout(new BorderLayout(2, 3));
        groupTab.add(homePanel, BorderLayout.NORTH); // Center
        groupTab.add(lowerGroupButtons, BorderLayout.CENTER); // Center

        tabspanel.addTab("<Home>", groupTab);

        JPanel  wrapper = new JPanel();
                wrapper.setLayout(new BorderLayout());
                wrapper.add(tabspanel, BorderLayout.CENTER);

        if (isDebugModeSelected()) { // Add debugpanel if mode is in debug.
            wrapper.add(debugOutPanel, BorderLayout.SOUTH);
        }

        frame.add(wrapper);
        frame.pack();
        frame.setVisible(true);

    }

    public void setController(GuiController controller) {
        this.controller = controller;
    }

    public Boolean HasEnteredHostInfo() {
        return hasEnteredHostInfo;
    }

    private void createButtons() {

        _JoinOrRemoveGroupButton = new JButton("Join");
        refreshGroupButton = new JButton("Refresh");
        holdButton = new JButton("Hold");
        removeElementDebugButton = new JButton("Remove");
        createLeaveGroupButton = new JButton("Create Group");

        debugOutUpButton = new JButton("UP");
        debugOutDownButton = new JButton("DOWN");

        refreshGroupButton.setPreferredSize(new Dimension(90, 30));
        _JoinOrRemoveGroupButton.setPreferredSize(new Dimension(90, 30));
        createLeaveGroupButton.setPreferredSize(new Dimension(160, 30));
        holdButton.setPreferredSize(new Dimension(90, 30));
        debugOutUpButton.setPreferredSize(new Dimension(90, 30));
        debugOutDownButton.setPreferredSize(new Dimension(90, 30));

        holdButton.setMinimumSize((new Dimension(90, 30)));
        debugOutUpButton.setMinimumSize((new Dimension(90, 30)));
        debugOutDownButton.setMinimumSize((new Dimension(90, 30)));

        _JoinOrRemoveGroupButton.setEnabled(false);
        createLeaveGroupButton.setEnabled(true);
    }

    public JPanel createDebugPanel(){

        JPanel panel = new JPanel();

        debugHoldMsgsOut = createDebugTable();
        JScrollPane scroll = createTableScroll(debugHoldMsgsOut);

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        //panel.add(Box.createRigidArea(new Dimension(10,1))); // Space
        panel.add(scroll, BorderLayout.CENTER);
        //panel.add(Box.createRigidArea(new Dimension(10,1))); // Space

        JPanel  subDebugBtnPanel = new JPanel();
                subDebugBtnPanel.setLayout(new BoxLayout(subDebugBtnPanel, BoxLayout.Y_AXIS));
                subDebugBtnPanel.add(debugOutUpButton);
                subDebugBtnPanel.add(Box.createRigidArea(new Dimension(1,10))); // Space between btns
                subDebugBtnPanel.add(debugOutDownButton);
                subDebugBtnPanel.add(Box.createRigidArea(new Dimension(1,30))); // Space between btns
                subDebugBtnPanel.add(holdButton);
                subDebugBtnPanel.add(Box.createRigidArea(new Dimension(1,10))); // Space between btns
                //subDebugBtnPanel.add(removeElementDebugButton);
        panel.add(subDebugBtnPanel, BorderLayout.EAST);

        debugTextArea = new JTextArea(5,5);
        JScrollPane _tp = new JScrollPane(debugTextArea);
                    _tp.setBounds(5,5,5,5);

        //panel.add(debugTextArea);
        panel.add(_tp);
        panel.add(Box.createRigidArea(new Dimension(10,1))); // Space


        return panel;
    }

    public JScrollPane createTableScroll(JTable table){
        JScrollPane scroll = new JScrollPane(table);
                    scroll.setAutoscrolls(true);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addColumn("");
        return scroll;
    }

    public JTable createGroupTable(){

        tableModel = new DefaultTableModel();
        JTable table = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int index = table.getSelectedRow();
                if (index >= 0) {
                    String v = (String) table.getValueAt(index, 0);
                    if (groups.contains(v)) {
                        // if group is in list...
                        if (hasJoinedGroup()) {
                            // Or if user is in group (remove mode)
                            if (controller.checkIfUserIsLeaderOfGroup(v)) {
                                // Only leaders can remove group.
                                _JoinOrRemoveGroupButton.setEnabled(true);
                            } else {
                                _JoinOrRemoveGroupButton.setEnabled(false);
                            }

                            createLeaveGroupButton.setEnabled(true);
                        } else {
                            // Do not let user join more groups.
                            _JoinOrRemoveGroupButton.setEnabled(true);
                        }

                    } else {
                        // If there exists no group with name selected. Unable join.
                        _JoinOrRemoveGroupButton.setEnabled(false);
                    }
                }

            }
        });

        table.setBorder(BorderFactory.createLineBorder(Color.black));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    public Boolean hasJoinedGroup() {
        return hasEnteredGroup;
    }

    public void hasJoinedGroup(Boolean hasSelectGroup) {

        this.hasEnteredGroup = hasSelectGroup;
        if (hasSelectGroup) {
            try {
                if (controller.checkIfUserIsLeaderOfGroup(controller.getGcom().getGroupManager().getCurrentGroupName())) {
                    _JoinOrRemoveGroupButton.setText("Remove");
                    createLeaveGroupButton.setEnabled(false);
                    _JoinOrRemoveGroupButton.setEnabled(true);
                } else {
                    _JoinOrRemoveGroupButton.setText("Join");
                    createLeaveGroupButton.setEnabled(false);
                    _JoinOrRemoveGroupButton.setEnabled(false);
                }

                createLeaveGroupButton.setText("Leave");

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            _JoinOrRemoveGroupButton.setText("Join");
            createLeaveGroupButton.setEnabled(true);
            _JoinOrRemoveGroupButton.setEnabled(false);
            createLeaveGroupButton.setText("Create");
        }
    }

    public JTable createDebugTable(){

        String[] colNames = {"Direction", "Message"};
        debugtableModel = new DefaultTableModel();

        JTable table = new JTable(debugtableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int index = table.getSelectedRow();
            }
        });

        debugtableModel.setColumnCount(1);
        debugtableModel.setColumnIdentifiers(colNames);

        table.setBorder(BorderFactory.createLineBorder(Color.black));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDragEnabled(true);

        return table;
    }


    private JPanel createGroupTab(String group) {

        groupUsers.put(group, username);

        JTextArea   chatArea = new JTextArea(3, 16);
                    chatArea.setEditable(false);
        JScrollPane scrollChatArea = new JScrollPane(chatArea);

        DefaultCaret caret = (DefaultCaret) chatArea.getCaret();
                     caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);


        JTextArea messageArea = new JTextArea(4, 45);

        JButton sendButton = new JButton("Send");
                sendButton.setPreferredSize(new Dimension(80, 60));
                sendButton.setName(group + "." + getUsername());

        messageArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    sendButton.doClick();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });


        JScrollPane textPane = new JScrollPane(messageArea);

        JPanel  messagePane = new JPanel();
                messagePane.add(textPane);
                messagePane.add(sendButton);

        JPanel  chattPanel = new JPanel();
                chattPanel.setLayout(new BorderLayout());
                //chattPanel.add(scrollGroupMembers, BorderLayout.EAST);
                chattPanel.add(scrollChatArea, BorderLayout.CENTER);
                chattPanel.add(messagePane, BorderLayout.SOUTH);

        return chattPanel;
    }


    public void removeGroupTab() {
        tabspanel.removeTabAt(tabspanel.getTabCount()-1); // The tab to outer right.
        groups.remove(tabspanel.getTitleAt(tabspanel.getTabCount()-1));
        createLeaveGroupButton.setEnabled(true);
    }

    public String addGroupTab() {

        if (tabspanel.getTabCount() >= MAX_TABS) {
            JOptionPane.showMessageDialog(tabspanel, "Error: You're already inside a chat! Leave it before picking another one.");
            return null;
        }

        String group_name = groupCreaterWindow.getGroupname();
        if (group_name.length() < 1) {
            JOptionPane.showMessageDialog(tabspanel, "Error: The groupname must be more than zero characters.");
            return null;
        }

        if (groups.contains(group_name)) {
            System.err.println("Error: Group already exists!");
            return null;
        }

        tabspanel.addTab(group_name, createGroupTab(group_name));
        tableModel.addRow(new Object[]{group_name});
        groups.add(group_name);

        switchToTab();
        return group_name;
    }


    public boolean isDebugModeSelected(){
        return debugmode;
    }
    public void createGuiMessage(String message) {
        JOptionPane.showMessageDialog(tabspanel, message);
    }

    public void promptHostInformation() {

        hostWindow = null;
        while (true) {
            hostWindow = new EnterHostWindow();
            if (hostWindow.getUsername().length() < 1) {
                JOptionPane.showMessageDialog(tabspanel, "Username must contain atleast one character!");
                continue;
            }
            if (hostWindow.getHost().length() < 1) {
                JOptionPane.showMessageDialog(tabspanel, "Host must contain atleast one character!");
                continue;
            }
            break;
        }

        username = hostWindow.getUsername();
        host = hostWindow.getHost();
        debugmode = hostWindow.selectedDebugMode();
        hasEnteredHostInfo = true;

    }

    public EnterHostWindow getHostWindow() {
        return hostWindow;
    }

    public String getHost() {
        return host;
    }

    public void updateGroups(String[] _groups) {

        DefaultTableModel dm = (DefaultTableModel) groupTable.getModel();
                          dm.getDataVector().removeAllElements();
                          dm.fireTableDataChanged();

        for (String group : _groups) {

            groups.add(group);
            tableModel.addRow(new Object[]{group});
        }
    }

    public void addWindowListenerLeade(WindowListener w) {
        frame.addWindowListener(w);
    }
    public void addActionListenerJoin(ActionListener a) {
        _JoinOrRemoveGroupButton.addActionListener(a);
    }
    public void addActionListenerCreate(ActionListener a) {
        createLeaveGroupButton.addActionListener(a);
    }
    public void addActionListererRefresh(ActionListener a) {
        refreshGroupButton.addActionListener(a);
    }
    public void addActionListererHold(ActionListener a) {
        holdButton.addActionListener(a);
    }
    public void addActionListererDebugOutUp(ActionListener a) {
        debugOutUpButton.addActionListener(a);
    }
    public void addActionListererDebugOutDown(ActionListener a) {
        debugOutDownButton.addActionListener(a);
    }





    public String showGroupCreation() {
        groupCreaterWindow = new CreateGroupWindow();

        while(!groupCreaterWindow.isDone) {
            continue;
        }

        if (groupCreaterWindow.returnNumber == JOptionPane.YES_OPTION) {
            return groupCreaterWindow.getGroupname();
        }
        return null;

    }
    public JTextArea getDebugTextArea(){
        return debugTextArea;
    }

    public String getUsername() {
        return username;
    }


    public void addJoinTab(String group) {
        if (group.length() > 0 && (tabspanel.getTabCount() < MAX_TABS)) {
            tabspanel.addTab(group, createGroupTab(group));
        }
    }

    public boolean isUnorderedOrderSelected(){
        return groupCreaterWindow.getSelectedOrder().equals(OrdererType.UNORDERED) ? true : false;
    }

    public String getGroupNameFromTable() {

        int row = groupTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (String)groupTable.getValueAt(row, 0);
    }

    public void switchToTab() {
        tabspanel.setSelectedIndex(tabspanel.getTabCount()-1);
    }
    public void clearDebugArea() {
        DefaultTableModel dtm =  (DefaultTableModel) debugHoldMsgsOut.getModel();
        dtm.setRowCount(0);

    }
    public void appendToDebugArea(iMessage m, String _InOrOut, OrdererType type) {
        DefaultTableModel model = (DefaultTableModel) debugHoldMsgsOut.getModel();
        try {
            String _in = _InOrOut;
            String _m = m.getMessage();
            if (type.equals(OrdererType.UNORDERED)) {
                model.addRow(new Object[]{_InOrOut, m.getMessage(), "-", "1"});
            } else  {
                model.addRow(new Object[]{_InOrOut, m.getMessage(), m.getClock().toString(), m.getPathCounter()});
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    public void appendSystemMessage(String group, String message) {
        if (getTextArea(group, 1).getRows() > 15) {
            getTextArea(group, 1).selectAll();
            getTextArea(group, 1).replaceSelection("");
        }
        getTextArea(group, 1).append(message + "\n");
    }


    public void appendMessage(String group, String message, String name) {
        if (getTextArea(group, 1).getRows() > 15) {
            getTextArea(group, 1).selectAll();
            getTextArea(group, 1).replaceSelection("");
        }
        getTextArea(group, 1).append(name + ": " + message + "\n");
    }

    public String getMessage(String groupname) {

        for (int i = tabspanel.getTabCount() - 1; i >= 1; i--) {

            if (tabspanel.getTitleAt(i).equals(groupname)) {
                JComponent tab = (JComponent) tabspanel.getComponentAt(i);

                // Get text.
                String message = ((JTextArea)((JScrollPane)((JPanel)tab.getComponent(1))
                                                                      .getComponent(0))
                                                                      .getViewport()
                                                                      .getView())
                                                                      .getText();
                // Clear text.
                ((JTextArea)((JScrollPane)((JPanel)tab.getComponent(1))
                        .getComponent(0))
                        .getViewport()
                        .getView())
                        .setText("");
                return message;
            }
        }
        return null;
    }

    public void addSendListenerToGroup(String groupname, ActionListener a) {

        for (int i = tabspanel.getTabCount() - 1; i >= 1; i--) {
            if (tabspanel.getTitleAt(i).equals(groupname)) {
                ((JButton)((JPanel)((JComponent)tabspanel.getComponentAt(i)).getComponent(1)).getComponent(1)).addActionListener(a);
            }
        }
    }

    private JTextArea getTextArea(String groupname, int j) {

        for (int i = tabspanel.getTabCount() - 1; i >= 1; i--) {
            if (tabspanel.getTitleAt(i).equals(groupname)) {
                JComponent tab = (JComponent) tabspanel.getComponentAt(i);
                return (JTextArea) ((JScrollPane)tab.getComponent(0)).getViewport().getView();
            }
        }

        return null;
    }

}
