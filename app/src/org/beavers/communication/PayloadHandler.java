package org.beavers.communication;

import java.io.DataInputStream;
import java.io.IOException;

public interface PayloadHandler {
	void handlePayload(DataInputStream input) throws IOException;
}
