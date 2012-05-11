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
	
	public CustomDTNDataHandler(final DTNClient pDTNClient, final PayloadHandler pPayloadHandler) {
		dtnClient = pDTNClient;
		payloadHandler = pPayloadHandler;
		
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
		bundle = new BundleID( pBundle );	
		
		//pBundle.source
	}

	@Override
	public void endBundle() {
		
		final BundleID received = bundle;

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
		
		System.out.println("test1 "+data.length+" "+tmpData.length);
		
		// process data asynchronously
		executor.execute(new Runnable() {
	        public void run() {
				try {
					System.out.println("test2");
					final DataInputStream input = new DataInputStream(new ByteArrayInputStream(tmpData));
					CustomDTNDataHandler.this.payloadHandler.handlePayload(input);
					System.out.println("test3");
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
	
	private BundleID bundle;
	private final DTNClient dtnClient;
	private ExecutorService executor;
	private final PayloadHandler payloadHandler;
}
