package gcom.Utils;

import gcom.Message.iMessage;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 *  LinkedLayerTransfer: Implementation of the custom linked layer transfer of messages between all the modules.
 *
 *  @author c14nnm
 * */

public class LinkedLayerTransfer {

	private LayerDirection dir;
	private LinkedList<iLayer> layers;
	private ConcurrentLinkedQueue<HashMap<iMessage, Integer>> queue;
	private int i;


	public LinkedLayerTransfer(LayerDirection dir){
		this.dir = dir;
		layers = new LinkedList<>();
		queue = new ConcurrentLinkedQueue<>();
		reset();
	}

	public void addLayer(iLayer e) {
		layers.add(e);
	}

	public void reset() {
		i = 0;
		if (queue.size() > 0) {
			queue.poll();
			if (queue.size() > 0) {
				Map.Entry _e = queue.peek().entrySet().iterator().next();
				send((iMessage) _e.getKey());
			}
		}
	}

	public boolean containsObject(iMessage m) {
		for (HashMap<iMessage, Integer> hm : queue) {
			Map.Entry _e = hm.entrySet().iterator().next();
			iMessage m2 = (iMessage) _e.getKey();
			try {
				if (m2.getMessage().equals(m.getMessage()) && m2.getSenderName().equals(m.getSenderName()) && m2.getId().equals(m.getId())) {
					return true;
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public HashMap<iMessage, Integer> createEntry(iMessage m, int index){
		if (!containsObject(m)) {
			HashMap<iMessage, Integer> _e = new HashMap<>();
			_e.put(m, index);
			return _e;
		}
		return null;
	}




	/**
	 *  Initiates a stream of sending a message through all layers.
	 *  @param  m - message to send
	 *  @author c14nnm
	 * */
	public void send(iMessage m) {

		if (queue.size() > 0) {
			if (m != null) {
				if (!containsObject(m)) {
					HashMap<iMessage, Integer> _e = createEntry(m, 0);
					if (_e != null)
						queue.add(_e);
				}
			}

			if (i == 0) {
				HashMap<iMessage, Integer> e = queue.peek();
				if (e != null) {
					Map.Entry _e = e.entrySet().iterator().next();
					i = (int) _e.getValue();
					nextLayer((iMessage)_e.getKey());
				}
			}  else if (i == (layers.size()-1)) {
				reset();
			}
		} else {
			if (m != null) {

				if (!containsObject(m)) {
					HashMap<iMessage, Integer> _e = createEntry(m, 0);
					if (_e != null)
						queue.add(_e);

				}

				if (i == 0) {
					nextLayer(m);
				}  else if (i == (layers.size()-1)) {
					reset();
				}
			}
		}
	}


	/**
	 *  Initiates a stream of sending a message through all layers starting from index sIndex
	 *  @param  m - message to send
	 *  @param  sIndex
	 *  @author c14nnm
	 * */
	public void send(iMessage m, int sIndex) {

		HashMap<iMessage, Integer> e = createEntry(m, sIndex);
		if (e != null) {
			queue.add(e);
			send(null);
		}

	}




	/**
	 *  Send message to next layer.
	 *  @param  m - message to send
	 *  @author c14nnm
	 * */
	public void nextLayer(iMessage m){

		if (i >= (layers.size()-1)) {
			reset();
			return;
		}
		i++;
		((iLayer)(layers.get(i))).triggerNextLayer(dir, m);
	}


	public int getLayerSize(){
		return layers.size();
	}

	public int getLayerIndexByClass(Class c) {
		int j = 0;
		for (iLayer l : layers) {
			if (l.getClass().equals(c))
				return j;
			j++;
		}
		return -1;
	}


}
