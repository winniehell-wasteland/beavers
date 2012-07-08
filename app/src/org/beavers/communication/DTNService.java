package org.beavers.communication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.Player;
import org.beavers.storage.CustomGSON;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import de.tubs.ibr.dtn.api.Block;
import de.tubs.ibr.dtn.api.Bundle;
import de.tubs.ibr.dtn.api.BundleID;
import de.tubs.ibr.dtn.api.CallbackMode;
import de.tubs.ibr.dtn.api.DTNClient;
import de.tubs.ibr.dtn.api.DataHandler;
import de.tubs.ibr.dtn.api.GroupEndpoint;
import de.tubs.ibr.dtn.api.Registration;
import de.tubs.ibr.dtn.api.ServiceNotAvailableException;

/**
 * service for DTN communication
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class DTNService extends Service {

	/**
	 * @name debug
	 * @{
	 */
	private static final String TAG = DTNService.class.getName();
	/**
	 * @}
	 */

	/**
	 * @name endpoints
	 * @{
	 */
	public static final GroupEndpoint CLIENT_EID =
		new GroupEndpoint("dtn://beavergame.dtn/client");
	public static final GroupEndpoint SERVER_EID =
		new GroupEndpoint("dtn://beavergame.dtn/server");
	/**
	 * @}
	 */

	@Override
	public IBinder onBind(final Intent pIntent) {
		return stub;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		super.onCreate();

		dtnClient = new CustomDTNClient();
		dataHandler = new CustomDataHandler();
		executor = Executors.newSingleThreadExecutor();

		dtnClient.setDataHandler(dataHandler);
		dtnClient.initialize();

		client = new Client.Connection();
		Intent intent = new Intent(DTNService.this, Client.class);
		bindService(intent, client, BIND_AUTO_CREATE);

		server = new Server.Connection();
		intent = new Intent(DTNService.this, Server.class);
		bindService(intent, server, BIND_AUTO_CREATE);

		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					if((dtnClient.getDTNService() != null)
							&& dtnClient.getDTNService().isRunning())
					{
						return;
					}
				} catch (final RemoteException e) {
					// something bad happened to the binder
				}

				final NotificationManager notificationManager =
					(NotificationManager) getSystemService(NOTIFICATION_SERVICE);

				final Notification notification =
					new Notification(0,
					                 "Communication problem with DTN service!",
					                 System.currentTimeMillis());

				notificationManager.notify(1, notification);

				stopSelf();
			}
		});
	}

	@Override
	public void onDestroy() {

		dtnClient.unregister();

		if(client != null)
		{
			unbindService(client);
		}

		if(server != null)
		{
			unbindService(server);
		}

		try {
			// stop executor
			executor.shutdown();

			// ... and wait until all jobs are done
			if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (final InterruptedException e) {
			Log.e(TAG, "Interrupted while processing executor queue!", e);
		}

		dtnClient.terminate();
	};

	@Override
	public int onStartCommand(final Intent pIntent, final int pFlags,
			                  final int pStartId) {

		Log.d(TAG, "onStartCommand()");

		if(pIntent.getAction().equals(de.tubs.ibr.dtn.Intent.RECEIVE))
		{
        	final int stopId = pStartId;

			executor.execute(new Runnable() {

				@Override
				public void run() {
					queryTask.run();

					stopSelfResult(stopId);
				}
			});

        	return START_STICKY;
		}

        return START_NOT_STICKY;
	}

	public static class Connection implements ServiceConnection
	{
		public IDTNService getService() {
			return service;
		}

		@Override
		public void onServiceConnected(final ComponentName pName,
		                               final IBinder pService) {
			service = IDTNService.Stub.asInterface(pService);
		}

		@Override
		public void onServiceDisconnected(final ComponentName pName) {
			service = null;
		}

		private IDTNService service;
	}

	@SuppressWarnings("unused")
	public static class Message
	{
		public Message(final Context pContext, final GameInfo pGame)
		{
			context = pContext;
			game = pGame;
		}

		public ParcelFileDescriptor getFile()
		{
			try {
				final File file = File.createTempFile("outgoing-", ".msg",
				                                context.getExternalCacheDir());

				final FileWriter writer = new FileWriter(file);
				writer.write(CustomGSON.getInstance().toJson(this));
				writer.close();

				return ParcelFileDescriptor.open(
					file, ParcelFileDescriptor.MODE_READ_ONLY
				);
			} catch (final IOException e) {
				Log.e(TAG, "Error writing message!", e);
				return null;
			}
		}

		private transient final Context context;
		private final GameInfo game;
	}

	/**
	 * intent receiver
	 */
	public static class Receiver extends BroadcastReceiver
	{
		public Receiver() {
			Log.d(TAG, "Receiver created...");
		}

		@Override
		public void onReceive(final Context pContext, final Intent pIntent) {
			Log.d(TAG, "onReceive()");

			// waken the creature
			final Intent intent = new Intent(pContext, DTNService.class);
			intent.setAction(pIntent.getAction());
			pContext.startService(intent);
		}
	}

	/**
	 * @name higher level services
	 * @{
	 */
	private Client.Connection client;
	private Server.Connection server;
	/**
	 * @}
	 */

	private CustomDTNClient dtnClient;
	private CustomDataHandler dataHandler;
	private ExecutorService executor;

	private final IDTNService.Stub stub = new IDTNService.Stub() {

		@Override
		public void sendToServer(final Player pServer,
		                         final ParcelFileDescriptor pData) {

			// send to our server
			try {
				server.getService().handleData(pData);
			} catch (final RemoteException e) {
				Log.e(TAG, "Server could not handle loopback packet!", e);
				return;
			}

			final long length = pData.getStatSize();

			try {
				dtnClient.getSession().sendFileDescriptor(
					SERVER_EID, 100, pData, length
				);
			} catch (final Exception e) {
				Log.e(TAG, "Could not send DTN packet!", e);
			}
		}

		@Override
		public void sendToClients(final ParcelFileDescriptor pData) {

			// send to our client
			try {
				client.getService().handleData(pData);
			} catch (final RemoteException e) {
				Log.e(TAG, "Client could not handle loopback packet!", e);
				return;
			}

			final long length = pData.getStatSize();

			try {
				dtnClient.getSession().sendFileDescriptor(
					CLIENT_EID, 100, pData, length
				);
			} catch (final Exception e) {
				Log.e(TAG, "Could not send DTN packet!", e);
			}
		}
	};

	private final Runnable queryTask = new Runnable() {
		@Override
		public void run() {
			try {
				while(dtnClient.query());
			} catch (final Exception e) {
				Log.e(TAG, "Problem while querying from DTN client!", e);
			}
		}
	};

	private class CustomDataHandler implements DataHandler
	{
		private Bundle bundle;
		private File file;
		private ParcelFileDescriptor fileDesc;

		@Override
		public void startBundle(final Bundle pBundle) {
			bundle = pBundle;
		}

		@Override
		public void endBundle() {
			final BundleID bundleId = new BundleID(bundle);

			executor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						dtnClient.getSession().delivered(bundleId);
					} catch (final Exception e) {
						Log.e(TAG, "Problem with marking bundle delivered!", e);
					}
				}
			});

			bundle = null;
		}

		@Override
		public void startBlock(final Block block) {
			if ((block.type == 1) && (file == null))
			{
				Log.d(TAG, "startBlock()");

				try {
					file = File.createTempFile("incoming-", ".msg", getExternalCacheDir());
					Log.e(TAG, "Writing "+file.getAbsolutePath());
				} catch (final IOException e) {
					Log.e(TAG, "Problem with creating file!", e);
					file = null;
				}
			}
		}

		@Override
		public void endBlock() {
			if(fileDesc != null)
			{
				try {
					fileDesc.close();
				} catch (final IOException e) {
					Log.e(TAG, "Could not close file descriptor!", e);
				}

				fileDesc = null;
			}

			if(file != null)
			{
				final String destination = bundle.destination;
				final File input = file;

				executor.execute(new Runnable() {

					@Override
					public void run() {
						try {
							final ParcelFileDescriptor fileDesc =
								ParcelFileDescriptor.open(
									input,
									ParcelFileDescriptor.MODE_READ_ONLY
								);

							if(destination.equals(SERVER_EID))
							{
								server.getService().handleData(fileDesc);
							}
							else if(destination.equals(CLIENT_EID))
							{
								client.getService().handleData(fileDesc);
							}
							else
							{
								Log.w(TAG, "Got stuff that we don't want: "
								      + "destination="+destination);
							}

							fileDesc.close();
							input.delete();
						} catch (final Exception e) {
							Log.e(TAG,
								"Could not forward payload to other services!",
								e);
						}
					}
				});

				file = null;
			}
		}

		@Override
		public void characters(final String data) {
			Log.e(TAG, "THIS SHOULD NOT BE CALLED!");
		}

		@Override
		public void payload(final byte[] data) {
			Log.e(TAG, "THIS SHOULD NOT BE CALLED!");
		}

		@Override
		public ParcelFileDescriptor fd() {

			try {
				fileDesc = ParcelFileDescriptor.open(
					file, ParcelFileDescriptor.MODE_CREATE
					      | ParcelFileDescriptor.MODE_READ_WRITE
				);

				return fileDesc;
			} catch (final FileNotFoundException e) {
				Log.e(TAG, "Could not get file descriptor!", e);
			}

			return null;
		}

		@Override
		public void progress(final long current, final long length) {
			if((current != 0) && (current != length))
			{
				Log.i(TAG, "Receiving "+current+" of "+length);
			}
		}

		@Override
		public void finished(final int startId) {
			stopSelfResult(startId);
		}

	}

	private class CustomDTNClient extends DTNClient
	{
		public CustomDTNClient()
		{
			super(getApplicationInfo().packageName);
		}

		public void initialize() {
	        try {
	        	final Registration registration = new Registration("game/beavers");

	        	registration.add(SERVER_EID);
	        	registration.add(CLIENT_EID);

				super.initialize(getBaseContext(), registration);
			} catch (final ServiceNotAvailableException e) {
				// ignore for now
			}
		}

		@Override
		protected void sessionConnected(final Session session) {
	        executor.execute(queryTask);
		}

		@Override
		protected CallbackMode sessionMode() {
			return CallbackMode.FILEDESCRIPTOR;
		}

		@Override
		protected void online() {

		}

		@Override
		protected void offline() {

		}
	}
}
