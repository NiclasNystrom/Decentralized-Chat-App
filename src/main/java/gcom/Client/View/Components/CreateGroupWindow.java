package gcom.Client.View.Components;

import gcom.MessageOrdering.OrdererType;
import gcom.Communication.UnreliableMulticaster;

import javax.swing.*;
import java.awt.*;

public class CreateGroupWindow {

	private String[] multicasterOptions = {"Unreliable"};
	private String[] orderOptions = {"Unordered", "Causal"};

	private JComboBox multicasterCombo;
	private JComboBox orderCombo;

	private JTextField groupNameField;

	public Boolean isDone;
	public int returnNumber;

	public CreateGroupWindow() {
		isDone = false;

		JPanel  wrapper = new JPanel(new GridLayout(0, 1));
		wrapper.add(new JLabel("Multicaster options: "));

		multicasterCombo = new JComboBox(multicasterOptions);
		multicasterCombo.setSelectedIndex(0);
		multicasterCombo.setEditable(false);
		wrapper.add(multicasterCombo);

		wrapper.add(Box.createVerticalStrut(5)); // Space

		wrapper.add(new JLabel("Order options: "));
		orderCombo = new JComboBox(orderOptions);
		orderCombo.setSelectedIndex(0);
		orderCombo.setEditable(false);
		wrapper.add(orderCombo);

		wrapper.add(Box.createVerticalStrut(5)); // Space

		JLabel enterGroupNameLabel = new JLabel("Enter groupname: ");
		groupNameField = new JTextField();
		wrapper.add(enterGroupNameLabel);
		wrapper.add(groupNameField);
		//wrapper.add(groupNameInput);

		returnNumber = JOptionPane.showConfirmDialog(null,
				wrapper, "Create Group",  JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);


		switch (returnNumber) {
			case  JOptionPane.OK_OPTION:
				isDone = true;
				break;
			case  JOptionPane.NO_OPTION:
				isDone = true;
				break;
			default:
				isDone = true;
				break;
		}
	}


	public String getGroupname() {
		return groupNameField.getText();
	}

	public OrdererType getSelectedOrder(){
		switch (orderCombo.getSelectedIndex()) {
			case 0:
				return OrdererType.UNORDERED;
			case 1:
				return OrdererType.CAUSAL;
			default:
				return OrdererType.UNORDERED;
		}
	}

	public String getSelectedMulticaster(){
		return UnreliableMulticaster.class.getSimpleName();
	}

}
