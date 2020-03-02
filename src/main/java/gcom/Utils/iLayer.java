package gcom.Utils;

import gcom.Message.iMessage;

public interface iLayer {

	void triggerNextLayer(LayerDirection dir, iMessage m);
	void stampPathToUser(iMessage m, LayerDirection dir);
}
