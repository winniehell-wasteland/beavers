package org.beavers.communication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.beavers.gameplay.DecisionContainer;
import org.beavers.gameplay.Game;
import org.beavers.gameplay.OutcomeContainer;
import org.beavers.gameplay.Player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import de.tubs.ibr.dtn.api.Block;
import de.tubs.ibr.dtn.api.Bundle;
import de.tubs.ibr.dtn.api.BundleID;
import de.tubs.ibr.dtn.api.CallbackMode;
import de.tubs.ibr.dtn.api.DTNClient;
import de.tubs.ibr.dtn.api.DataHandler;
import de.tubs.ibr.dtn.api.Registration;
import de.tubs.ibr.dtn.api.ServiceNotAvailableException;
import de.tubs.ibr.dtn.api.SessionDestroyedException;

public class Client {

	public Client(final Context pContext, final Player pPlayer)
	{
		this.context = pContext;
		
		dtnClient = new LocalDTNClient(context.getApplicationInfo().packageName);
		
        // register to RECEIVE intent
		IntentFilter receive_filter = new IntentFilter(de.tubs.ibr.dtn.Intent.RECEIVE);
		receive_filter.addCategory(context.getApplicationInfo().packageName);
        context.registerReceiver(dtnReceiver, receive_filter);
        
        dtnClient.setDataHandler(dtnDataHandler);
        
        try {
			dtnClient.initialize(context, new Registration("client"));
		} catch (ServiceNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void finalize() throws Throwable {
		// unregister intent receiver
		context.unregisterReceiver(dtnReceiver);
		
		// unregister at the daemon
		dtnClient.unregister();
		
		// stop executor
		dtnExecutor.shutdown();

		// ... and wait until all jobs are done
		if (!dtnExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
			dtnExecutor.shutdownNow();
		}

		// destroy DTN client
		dtnClient.terminate();
		
		// clear all variables
		dtnExecutor = null;
		dtnClient = null;	
	}
	
	/**
	 * server has announced new game
	 * @param game
	 */
	public void receiveGameInfo(Game game)
	{
		
	}
	
	/**
	 * joins a game
	 * @param game
	 */
	public void joinGame(Game game)
	{
		
	}
	
	/**
	 * responds to server
	 * @param game
	 */
	public void acknowledgeGameReady(Game game)
	{
		
	}
	
	/**
	 * start planning phase
	 */
	public void startPlanningPhase(Game game)
	{
		
		
	}
	
	/**
	 * send decisions to server
	 * @param game
	 * @param decisions
	 */
	public void sendDecisions(Game game, DecisionContainer decisions)
	{
		
	}
	
	/**
	 * receive outcome from server
	 */
	public void receiveOutcome(Game game, OutcomeContainer outcome)
	{
		
	}
	
	/**
	 * quit game
	 * @param player
	 */
	public void abortGame(Game game)
	{
		
	}
	/**
	 * server has quit, inform clients about new server
	 * @param player
	 */
	public void receiveNewServer(Game game, Player player)
	{
		game.setServer(player);
		// TODO: ...
	}

	public Player getPlayer() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Context context;
	
	private class LocalDTNClient extends DTNClient
	{
		public LocalDTNClient(String packageName) {
			super(packageName);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void sessionConnected(Session session) {
	        dtnExecutor.execute(dtnQueryTask);
		}

		@Override
		protected CallbackMode sessionMode() {
			return CallbackMode.SIMPLE;
		}

		@Override
		protected void online() {
			System.out.println("DTN is online.");
		}

		@Override
		protected void offline() {
			System.out.println("DTN is offline.");
		}
	};
	
	private LocalDTNClient dtnClient;
	
	private DataHandler dtnDataHandler = new DataHandler() {

		private BundleID bundle;

		@Override
		public void startBundle(Bundle bundle) {
			this.bundle = new BundleID( bundle );			
		}

		@Override
		public void endBundle() {
			
			final BundleID received = this.bundle;

			// run the queue and delivered process asynchronously
			dtnExecutor.execute(new Runnable() {
		        public void run() {
					try {
						dtnClient.getSession().delivered(received);
					} catch (Exception e) {
						System.out.println("Can not mark bundle as delivered.");
					}
		        }
			});

			this.bundle = null;
			
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
			System.out.println("Got: " + new String(data));
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
		
	};
	
    private ExecutorService dtnExecutor = Executors.newSingleThreadExecutor();

	private Runnable dtnQueryTask = new Runnable() {
		@Override
		public void run() {			
			try {
				while(dtnClient.query());
			} catch (SessionDestroyedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	private BroadcastReceiver dtnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(de.tubs.ibr.dtn.Intent.RECEIVE))
			{
				// RECEIVE intent received, check for new bundles
				dtnExecutor.execute(dtnQueryTask);
			}
		}
	};
}
