package gcom.Message;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 *  VectorClock: Barbone implementation of vector clocks for causal ordering.
 * */

public class VectorClock implements Serializable {


	private HashMap<UUID, Long > clock; // Id, Timestamp
	private long tick;
	private UUID self;


	public VectorClock() {
		clock = new HashMap<>();
		tick = 0;
	}

	public VectorClock(UUID self) {
		clock = new HashMap<>();
		tick = 0;
		this.self = self;
		addId(self);
	}

	public void reset() {
		tick = 0;
		clock.clear();
		addId(self);
	}

	public void addId(UUID id) {
		clock.putIfAbsent(id, (long) 0);
	}

	public HashMap<UUID, Long> getClock() {
		return clock;
	}

	public long getTick() {
		return tick;
	}

	public void incrementTick(){
		tick += 1;
	}

	public Long isMessageInTime(iMessage m) throws RemoteException {

		VectorClock vc = m.getClock();

		// Add new uuids to clock.
		for (UUID id : vc.getClock().keySet()) {
			clock.putIfAbsent(id, vc.getClock().get(id) - 1);

		}

		for (UUID id : vc.getClock().keySet()) {

			Boolean isSender = (id.equals(m.getId()));
			if (isSender) {
				if (vc.getClock().get(m.getId()) - (clock.get(id) + 1L) > 0)
					return 1L;
			} else {
				if (vc.getClock().get(m.getId()) - (clock.get(id)) > 0)
					return 1L;
			}
		}
		return 0L;
	}

	public void printClock() {
		System.out.println("-------- Printing Vectorclock --------");
		for(Map.Entry e : clock.entrySet()) {
			System.out.println(e.getKey().toString() + ": " + (long)e.getValue());
		}
		System.out.println("--------------------------------------");
	}



}
