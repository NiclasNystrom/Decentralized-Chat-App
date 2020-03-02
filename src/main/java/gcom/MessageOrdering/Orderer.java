package gcom.MessageOrdering;


import gcom.Utils.iLayer;
import gcom.Communication.Multicaster;
import java.io.Serializable;

public abstract class Orderer implements iLayer, iOrderer, Serializable {

	private OrdererType type;
	protected Multicaster multicaster;
	protected Boolean debug;

	public Orderer(OrdererType type, Multicaster mc, Boolean debugmode){
		this.type = type;
		this.multicaster = mc;
		this.debug = debugmode;
	}

	public OrdererType getType() {
		return type;
	}

	public void setType(OrdererType type) {
		this.type = type;
	}

	public Multicaster getMulticaster() {
		return multicaster;
	}

	public void setMulticaster(Multicaster multicaster) {
		this.multicaster = multicaster;
	}


}
