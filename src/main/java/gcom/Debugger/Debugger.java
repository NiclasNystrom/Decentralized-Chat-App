package gcom.Debugger;

import gcom.Gcom;
import gcom.Utils.LayerDirection;
import gcom.Utils.iLayer;
import gcom.Client.Controller.GuiController;
import gcom.Client.View.GUIClient;
import gcom.Message.MessageTypes;
import gcom.Message.iMessage;

import java.rmi.RemoteException;
import java.util.*;

public class Debugger implements iLayer {

	public List<iMessage> inMess;
	public List<iMessage> outMess;

	public Boolean holdMessages;
	private GUIClient view;
	private GuiController controller;

	public Debugger(Boolean hold, GUIClient view, GuiController controller) {
		inMess = new ArrayList<>();
		outMess = new ArrayList<>();
		holdMessages = hold;
		this.view = view;
		this.controller = controller;
	}


	public List<iMessage> getOutMessages(){
		return outMess;
	}

	public void addInMess(iMessage m) throws RemoteException {
		if (m.getMessageType().equals(MessageTypes.MESSAGE)) {
			inMess.add(m);
			view.appendToDebugArea(m, "In", controller.getGcom().getOrderType());
		}
	}


	public void addOutMess(iMessage m) throws RemoteException {
		if (m.getMessageType().equals(MessageTypes.MESSAGE)) {
			outMess.add(m);
			view.appendToDebugArea(m, "Out", controller.getGcom().getOrderType());
		}
	}




	public void removeInMess(int index) {
		inMess.remove(index);
	}
	public void removeOutMess(int index) {
		outMess.remove(index);
	}
	public void removeOutMess(iMessage m) {
		outMess.remove(m);
	}
	public void removeOutMess(String message) throws RemoteException {
		int i = 0;
		for (iMessage m : outMess) {
			if (m.getMessage().equals(message)) {
				break;
			}
			i++;
		}
		removeOutMess(i);
	}

	public void setHoldMessages(boolean enabled){
		holdMessages = enabled;
		if (!holdMessages) {
			flushIn();
			flushOut();
		}
	}


	public void flushOut(){
		Collections.reverse(outMess);
		for (int j = outMess.size()-1; j >= 0; j--) {
			testPrintMsgsInQueue();
			iMessage m2 = outMess.get(j);
			outMess.remove(j);

			try {
				if (m2.getId().equals(controller.getGcom().getId())) {
					// You're the sender thus send it.
					Gcom.sendLayers.send(m2, Gcom.sendLayers.getLayerIndexByClass(Debugger.class));
				} else {
					// Someone else sent it. Receive it.
					Gcom.receiveLayers.send(m2, Gcom.receiveLayers.getLayerIndexByClass(Debugger.class));
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			//gcom.sendLayers.send(m2, gcom.sendLayers.getLayerSize() - 2);
		}
		outMess.clear();
		view.clearDebugArea();
	}

	public void flushIn(){
		Collections.reverse(inMess);
		for (int j = inMess.size()-1; j >= 0; j--) {
			iMessage m2 = inMess.get(j);
			inMess.remove(j);
			Gcom.receiveLayers.send(m2, Gcom.receiveLayers.getLayerIndexByClass(Debugger.class));
		}
		inMess.clear();
		//View.clearDebugArea();
	}

	public void removeOutAt(int i) {
		if (i < outMess.size()) {
			outMess.remove(i);
		} else {
			System.err.println("Warning RemoveOutAt: i >= size of outqueue");
		}
		//testPrintMsgsInQueue();
	}


	public void swapOut(int i, int j) {
		if ((i < outMess.size()) && (j < outMess.size())) {

			if (i == j) {
				System.err.println("Warning SwapOut: i == j");
				return;
			}
			Collections.swap(outMess, i, j);
			//testPrintMsgsInQueue();

		} else {
			System.err.println("Warning SwapOut: i >= size of outqueue");
		}
	}


	public void testPrintMsgsInQueue() {
		System.out.println("----Print test----");
		for (iMessage m : outMess) {
			try {
				System.out.println(m.getMessage());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		System.out.println("------------------\n");

	}

	@Override
	public void triggerNextLayer(LayerDirection dir, iMessage m) {

		switch (dir) {
			case SEND:
				stampPathToUser(m, dir);
				if (holdMessages) {
					try {
						addOutMess(m);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					Gcom.sendLayers.reset();
				} else {
					if (outMess.size() > 0) {
						flushOut();
					} else {
						Gcom.sendLayers.nextLayer(m);
					}
				}

				break;
			case RECEIVE:
				stampPathToUser(m, dir);
				if (holdMessages) {
					try {
						addInMess(m);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					Gcom.receiveLayers.reset();
				} else {
					if (inMess.size() > 0) {
						flushIn();
					} else {
						Gcom.receiveLayers.nextLayer(m);
					}
				}

				//gcom.receiveLayers.nextLayer(m);
				break;
			default:
				break;
		}
	}

	@Override
	public void stampPathToUser(iMessage m, LayerDirection dir) {
		try {
			m.appendPath("Debugger("+dir.toString()+")");
			m.setPathCounter(m.getPathCounter()+1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
