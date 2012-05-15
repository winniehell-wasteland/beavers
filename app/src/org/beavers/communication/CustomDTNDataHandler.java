package org.beavers.communication;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.os.ParcelFileDescriptor;
import de.tubs.ibr.dtn.api.Block;
import de.tubs.ibr.dtn.api.Bundle;
import de.tubs.ibr.dtn.api.BundleID;
import de.tubs.ibr.dtn.api.DTNClient;
import de.tubs.ibr.dtn.api.DataHandler;

public class CustomDTNDataHandler implements DataHandler {
	
	public CustomDTNDataHandler(final DTNClient pDTNClient, final Server pServer, final Client pClient) {
		dtnClient = pDTNClient;
		server = pServer;
		client = pClient;
		
		executor = Executors.newSingleThreadExecutor();
	}
	
	public void stop() {		
		try {
			// stop executor
			executor.shutdown();

			// ... and wait until all jobs are done
			if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void startBundle(Bundle pBundle) {
		bundle = pBundle;
	}

	@Override
	public void endBundle() {
		
		final BundleID received = new BundleID(bundle);

		// run the queue and delivered process asynchronously
		executor.execute(new Runnable() {
	        public void run() {
				try {
					dtnClient.getSession().delivered(received);
				} catch (Exception e) {
					System.out.println("Can not mark bundle as delivered.");
				}
	        }
		});

		bundle = null;
		
	}

	@Override
	public void startBlock(Block block) {
		// TODO Auto-generated method stub
	}

	@Override
	public void endBlock() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void characters(String data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void payload(byte[] data) {
		
		final byte[] tmpData = data.clone();
		final Bundle tmpBundle = bundle;
		
		// process data asynchronously
		executor.execute(new Runnable() {
	        public void run() {
				try {
					final DataInputStream input = new DataInputStream(new ByteArrayInputStream(tmpData));
					
					if(tmpBundle.destination.equals(Server.GROUP_EID.toString()))
					{
						server.handlePayload(input);
					}
					else if(tmpBundle.destination.equals(Client.GROUP_EID.toString()))
					{
						client.handlePayload(input);
					}
					else
					{
						System.out.println("Unknown destination: "+tmpBundle.destination);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
		});
	}

	@Override
	public ParcelFileDescriptor fd() {
		// TODO Auto-generated method stub
		assert false;
		
		return null;
	}

	@Override
	public void progress(long current, long length) {
		System.out.println("Payload: " + current + " of " + length + " bytes.");
		
	}

	@Override
	public void finished(int startId) {
		// TODO Auto-generated method stub
		
	}
	
	private Bundle bundle;
	private final DTNClient dtnClient;
	private ExecutorService executor;
	private final Client client;
	private final Server server;
}
