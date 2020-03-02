package gcom.Client.View.Components;

import gcom.GroupManagement.HostInfo;

import javax.swing.*;
import java.awt.*;


/**
 *  EnterHostWindow: Window that lets user to specify host information.
 *
 *  @author c14nnm
 * */

public class EnterHostWindow {

	private int inputSize = 15;

	private JTextField userInputField;
	private JTextField hostInputField;
	private JTextField portInputField;

	private JComboBox modeOptions;
	private String[] options = {"Normal", "Debug"};

	public EnterHostWindow() {

		JPanel wrapper  = new JPanel(new GridLayout(0, 1));

		JPanel userInputPanel = new JPanel();
		userInputPanel.add(new JLabel("Username: "));
		userInputField = new JTextField(inputSize);
		userInputField.setText("Hans-Groth");
		userInputPanel.add(userInputField);


		JPanel enterHostPanel = new JPanel();
		enterHostPanel.add(new JLabel("Enter host:"));
		hostInputField = new JTextField(inputSize);
		hostInputField.setText("localhost");
		enterHostPanel.add(hostInputField);

		JPanel enterPortPanel = new JPanel();
		enterPortPanel.add(new JLabel("Enter Port:"));
		portInputField = new JTextField(inputSize);
		portInputField.setText("22300");
		enterPortPanel.add(portInputField);

		JPanel guiModePanel = new JPanel();
		modeOptions = new JComboBox(options);
		modeOptions.setSelectedIndex(0);
		modeOptions.setEditable(false);
		guiModePanel.add(modeOptions);


		wrapper.add(userInputPanel);
		wrapper.add(enterHostPanel);
		//wrapper.add(enterPortPanel);
		wrapper.add(guiModePanel);

		int result = JOptionPane.showOptionDialog(null, wrapper, "Host information",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, null, null);

		if (result != JOptionPane.OK_OPTION) {
			System.exit(0);
		}

	}

	public String getHost(){
		return hostInputField.getText();
	}
	public String getUsername(){
		return userInputField.getText();
	}

	public String getMode(){
		return options[modeOptions.getSelectedIndex()];
	}
	public int getPort(){
		return 1099;
		//return 25099;
		//return Integer.parseInt(portInputField.getText());
	}

	public Boolean selectedDebugMode(){
		if (getMode().equals(options[1]))
			return true;
		return false;
	}

	public HostInfo getInfo(){
		return new HostInfo(getHost(), getPort());
	}


}
