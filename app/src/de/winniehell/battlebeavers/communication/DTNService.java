/*
	(c) winniehell (2012)

	This file is part of the game Battle Beavers.

	Battle Beavers is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Battle Beavers is distributed in the hope that it will be fun,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Battle Beavers. If not, see <http://www.gnu.org/licenses/>.
*/

package de.winniehell.battlebeavers.communication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.winniehell.battlebeavers.App;
import de.winniehell.battlebeavers.Settings;
import de.winniehell.battlebeavers.gameplay.Game;
import de.winniehell.battlebeavers.gameplay.GameState;
import de.winniehell.battlebeavers.gameplay.Player;
import de.winniehell.battlebeavers.storage.CustomGSON;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import de.tubs.ibr.dtn.api.Block;
import de.tubs.ibr.dtn.api.Bundle;
import de.tubs.ibr.dtn.api.BundleID;
import de.tubs.ibr.dtn.api.CallbackMode;
import de.tubs.ibr.dtn.api.DTNClient;
import de.tubs.ibr.dtn.api.DTNClient.Session;
import de.tubs.ibr.dtn.api.DataHandler;
import de.tubs.ibr.dtn.api.GroupEndpoint;
import de.tubs.ibr.dtn.api.Registration;
import de.tubs.ibr.dtn.api.ServiceNotAvailableException;
import de.tubs.ibr.dtn.api.SessionDestroyedException;

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
		new GroupEndpoint("dtn://battlebeavers.dtn/client");
	public static final GroupEndpoint SERVER_EID =
		new GroupEndpoint("dtn://battlebeavers.dtn/server");
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

		dtnClient.setDataHandler(dataHandler);
		dtnClient.initialize();

		final Timer timer = new Timer();

		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					if((dtnClient.getDTNService() != null)
							&& dtnClient.getDTNService().isRunning())
					{
						return;
					}
				} catch (final RemoteException e) {
					Log.e(TAG, "Something bad happened to the binder: ", e);
				}

				Log.e(TAG, "Communication problem with DTN service!");
				stopSelf();
			}
		}, 100);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");

		dtnClient.unregister();

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
			executor.execute(new Runnable() {

				@Override
				public void run() {
					queryTask.run();
				}
			});

			stopSelfResult(pStartId);

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

	public static class Message
	{
		public Message(final Game pGame, final GameState pState)
		{
			game = pGame;
			state = pState;
		}

		/** @return file name */
		public String saveToFile(final Context pContext)
		{
			try {
				final File file =
					File.createTempFile("outgoing-", ".msg",
				                        pContext.getExternalCacheDir());

				Log.d(TAG, "writing message to file "+file.getAbsolutePath());

				final FileWriter writer = new FileWriter(file);
				writer.write(CustomGSON.getInstance().toJson(this));
				writer.close();

				Log.d(TAG, "File content: "+CustomGSON.getInstance().toJson(this));

				return file.getAbsolutePath();
			} catch (final IOException e) {
				Log.e(TAG, "Error writing message!", e);
				return null;
			}
		}

		@SerializedName(Game.JSON_TAG)
		private final Game game;

		@SerializedName(GameState.JSON_TAG)
		private final GameState state;
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

	private CustomDTNClient dtnClient;
	private CustomDataHandler dataHandler;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private final IDTNService.Stub stub = new IDTNService.Stub() {

		@Override
		public void sendToServer(final Player pServer,
		                         final String pFileName) {
			try {
				final ParcelFileDescriptor data = ParcelFileDescriptor.open(
					new File(pFileName),
					ParcelFileDescriptor.MODE_READ_ONLY
				);

				Log.d(TAG, "Sending to server...");

				sendFileDescriptor(SERVER_EID, data);
			} catch (final Exception e) {
				Log.e(TAG, "Could not send DTN packet!", e);
			}

			// send to our server
			notifyServer(pFileName);
		}

		@Override
		public void sendToClients(final String pFileName) {
			try {
				final ParcelFileDescriptor data = ParcelFileDescriptor.open(
						new File(pFileName),
						ParcelFileDescriptor.MODE_READ_ONLY
					);

				Log.d(TAG, "Sending to client...");

				sendFileDescriptor(CLIENT_EID, data);
			} catch (final Exception e) {
				Log.e(TAG, "Could not send DTN packet!", e);
			}

			// send to our client
			notifyClient(pFileName);
		}
	};

	private final Runnable queryTask = new Runnable() {
		@Override
		public void run() {
			try {
				Log.d(TAG, "Query task running...");

				while(dtnClient.query());
			} catch (final Exception e) {
				Log.e(TAG, "Problem while querying from DTN client!", e);
			}
		}
	};

	private class CustomDataHandler implements DataHandler
	{	/**
		 * @name debug
		 * @{
		 */
		private final String TAG = CustomDataHandler.class.getName();
		/**
		 * @}
		 */

		private Bundle bundle;
		private File file;
		private ParcelFileDescriptor fileDesc;

		@Override
		public void startBundle(final Bundle pBundle) {
			Log.d(TAG, "startBundle()");
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
					final File cacheDir = getExternalCacheDir();

					if(!cacheDir.exists())
					{
						cacheDir.mkdirs();
					}

					file = File.createTempFile("incoming-", ".msg", cacheDir);
					Log.i(TAG, "Writing "+file.getAbsolutePath());
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
				final String fileName = file.getAbsolutePath();

				executor.execute(new Runnable() {

					@Override
					public void run() {
						try {
							if(destination.equals(SERVER_EID.toString()))
							{
								notifyServer(fileName);
							}
							else if(destination.equals(CLIENT_EID.toString()))
							{
								notifyClient(fileName);
							}
							else
							{
								Log.w(TAG, "Got stuff that we don't want: "
								      + "destination="+destination);

								(new File(fileName)).delete();
							}
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
			Log.i(TAG, "finished!");
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
	        	final Registration registration = new Registration(null);

	        	registration.add(SERVER_EID);
	        	registration.add(CLIENT_EID);

				super.initialize(getBaseContext(), registration);
			} catch (final ServiceNotAvailableException e) {
				// ignore for now
			}
		}

		@Override
		protected void sessionConnected(final Session session) {
			Log.i(TAG, "session connected!");
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

	protected void sendFileDescriptor(final GroupEndpoint pEndpoint, final ParcelFileDescriptor pData)
	               throws SessionDestroyedException, InterruptedException, IOException {
		// this is a workaround for
		//session.sendFileDescriptor(SERVER_EID, 100, pData, channel.size());

		final Session session = dtnClient.getSession();
		final AutoCloseInputStream stream = new AutoCloseInputStream(pData);

		try {
			final FileChannel channel = stream.getChannel();
			final MappedByteBuffer buf = channel.map(
				FileChannel.MapMode.READ_ONLY, 0, channel.size());

			if(!session.send(pEndpoint, getSettings().getDTNLifetime(),
				             Charset.defaultCharset().decode(buf).toString()))
			{
				// could not send
				return;
			}
		}
		finally {
			stream.close();
		}

		Log.d(TAG, "Successfully sent!");
	}

	public Settings getSettings() {
		if(getApplication() instanceof App)
		{
			return ((App) getApplication()).getSettings();
		}
		else
		{
			return null;
		}
	}

	private void notifyClient(final String pFileName) {
		final Intent intent = new Intent(DTNService.this, Client.class);

		intent.setAction(de.tubs.ibr.dtn.Intent.RECEIVE);
		intent.putExtra("file", pFileName);

		startService(intent);
	}

	private void notifyServer(final String pFileName) {
		final Intent intent = new Intent(DTNService.this, Server.class);

		intent.setAction(de.tubs.ibr.dtn.Intent.RECEIVE);
		intent.putExtra("file", pFileName);

		startService(intent);
	}
}
