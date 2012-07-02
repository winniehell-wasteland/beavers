package org.beavers.communication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.beavers.Settings;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.Player;
import org.beavers.storage.CustomGSON;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.tubs.ibr.dtn.api.Block;
import de.tubs.ibr.dtn.api.Bundle;
import de.tubs.ibr.dtn.api.BundleID;
import de.tubs.ibr.dtn.api.DTNClient;
import de.tubs.ibr.dtn.api.DataHandler;
import de.tubs.ibr.dtn.api.EID;

public class CustomDTNDataHandler extends BroadcastReceiver implements DataHandler {

	private static final String TAG = "CustomDTNDataHandler";

	/**
	 * @name intents
	 * @{
	 */
	public static final String SEND_DATA_INTENT = CustomDTNDataHandler.class.getName()+".SEND_PARCEL";
	/**
	 * @}
	 */

	public static class Message
	{
		GameInfo game;

		Message(final GameInfo pGame)
		{
			game = pGame;
		}

		JsonObject toJsonObject()
		{
			return (JsonObject) CustomGSON.getInstance().toJsonTree(this);
		}
	}

	/**
	 * default constructor
	 * @param pContext
	 * @param pDTNClient
	 */
	public CustomDTNDataHandler(final Context pContext, final DTNClient pDTNClient) {
		context = pContext;
		dtnClient = pDTNClient;

		final IntentFilter filter = new IntentFilter(SEND_DATA_INTENT);
		context.registerReceiver(this, filter);

		executor = Executors.newSingleThreadExecutor();
	}

	public void stop() {
		context.unregisterReceiver(this);

		try {
			// stop executor
			executor.shutdown();

			// ... and wait until all jobs are done
			if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void startBundle(final Bundle pBundle) {
		bundle = pBundle;
	}

	@Override
	public void endBundle() {

		final BundleID received = new BundleID(bundle);

		// run the queue and delivered process asynchronously
		executor.execute(new Runnable() {
	        @Override
			public void run() {
				try {
					dtnClient.getSession().delivered(received);
				} catch (final Exception e) {
					System.out.println("Can not mark bundle as delivered.");
				}
	        }
		});

		bundle = null;

	}

	@Override
	public void startBlock(final Block block) {

	}

	@Override
	public void endBlock() {

	}

	@Override
	public void characters(final String data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void payload(final byte[] data) {

		final String destination = bundle.destination;

		// process data asynchronously
		executor.execute(new Runnable() {
	        @Override
			public void run() {
				try {
					final JsonParser parser = new JsonParser();

					final JsonObject json =
						(JsonObject) parser.parse(new String(data, "UTF-8"));

					if(destination.equals(Server.GROUP_EID.toString()))
					{
						Server.handlePayload(context, json);
					}
					else if(destination.equals(Client.GROUP_EID.toString()))
					{
						Client.handlePayload(context, json);
					}
					else
					{
						Log.e(TAG, "Unknown destination: "+destination);
						return;
					}

				} catch (final Exception e) {
					Log.e(TAG, "Problem parsing DTN message!", e);
				}
	        }
		});
	}

	@Override
	public ParcelFileDescriptor fd() {
		return null;
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if(intent.getAction().equals(SEND_DATA_INTENT))
		{
			final EID destination = intent.getParcelableExtra("EID");
			final int lifetime = intent.getIntExtra("lifetime", DEFAULT_LIFETIME);

			try {
				dtnClient.getSession().send(destination, lifetime, intent.getStringExtra("data"));
			} catch (final Exception e) {
				Log.e(TAG, "Could not send DTN packet!", e);
			}
		}
	}

	@Override
	public void progress(final long current, final long length) {
		System.out.println("Payload: " + current + " of " + length + " bytes.");

	}

	@Override
	public void finished(final int startId) {
		// TODO Auto-generated method stub

	}


	/**
	 * send DTN message to clients
	 *
	 * @param pContext activity context
	 * @param pJSON payload in JSON format
	 */
	public static void sendToClients(final Context pContext,
	                                 final JsonObject pJSON) {
		final Intent intent = new Intent(SEND_DATA_INTENT);

		intent.putExtra("EID", (Parcelable) Client.GROUP_EID);
		intent.putExtra("data", pJSON.toString() );

		pContext.sendBroadcast(intent);

		Log.e(TAG, "Sending: "+pJSON.toString());

		// don't forget our client (loopback is suppressed)
		Client.handlePayload(pContext, pJSON);
	}

	/**
	 * send DTN message to server
	 *
	 * @param pContext activity context
	 * @param pServer server
	 * @param pJSON payload in JSON format
	 */
	public static void sendToServer(final Context pContext, final Player pServer, final JsonObject json) {
		if(pServer.equals(Settings.player))
		{
			Server.handlePayload(pContext, json);
		}
		else
		{
			final Intent intent = new Intent(SEND_DATA_INTENT);

			intent.putExtra("EID", (Parcelable) Server.GROUP_EID);
			intent.putExtra("data", json.toString() );

			pContext.sendBroadcast(intent);
		}
	}

	private static final int DEFAULT_LIFETIME = 100;

	private Bundle bundle;
	private final Context context;
	private final DTNClient dtnClient;
	private final ExecutorService executor;
}
