package gcom.Client;

import gcom.Gcom;
import gcom.Client.Controller.GuiController;
import gcom.Client.View.GUIClient;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.UUID;

public class ClientMain {

	private static GuiController controller;
	private static GUIClient view;
	private static Gcom gcom;

	public static void main(String[] args) {

		UUID id = UUID.randomUUID();

		view = new GUIClient();
		controller = new GuiController(view, id);
		view.setController(controller);

		while(!view.HasEnteredHostInfo()) {
			// Do nothing til Done
		}

		try {
			gcom = new Gcom(id, view.getHost(), view.getHostWindow().selectedDebugMode(), controller, view.getHostWindow().getInfo());
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}


}
