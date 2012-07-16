package org.beavers.communication;

import android.os.ParcelFileDescriptor;

import org.beavers.gameplay.Player;

/**
 * DTN communication service interface
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 * @see @{link DTNService}
 */
interface IDTNService {
	void sendToServer(in Player pServer, in String pFileName);
	void sendToClients(in String pFileName);
}