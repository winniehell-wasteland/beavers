package org.beavers.communication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import de.tubs.ibr.dtn.api.CallbackMode;
import de.tubs.ibr.dtn.api.DTNClient;
import de.tubs.ibr.dtn.api.Registration;
import de.tubs.ibr.dtn.api.ServiceNotAvailableException;
import de.tubs.ibr.dtn.api.SessionDestroyedException;

public class CustomDTNClient extends DTNClient {

	public CustomDTNClient(final Context pContext) {
		super(pContext.getApplicationInfo().packageName);

		context = pContext;
		executor = Executors.newSingleThreadExecutor();

        // register to RECEIVE intent
		final IntentFilter filter = new IntentFilter(de.tubs.ibr.dtn.Intent.RECEIVE);
		filter.addCategory(pContext.getApplicationInfo().packageName);
        pContext.registerReceiver(intentReceiver, filter);
	}

	@Override
	protected void sessionConnected(final Session session) {
        executor.execute(queryTask);
	}

	@Override
	protected CallbackMode sessionMode() {
		return CallbackMode.SIMPLE;
	}

	@Override
	protected void online() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void offline() {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregister() {
		// unregister intent receiver
		context.unregisterReceiver(intentReceiver);

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

		super.unregister();
	}

	private final Context context;
    private final ExecutorService executor;

	private final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (intent.getAction().equals(de.tubs.ibr.dtn.Intent.RECEIVE))
			{
				// RECEIVE intent received, check for new bundles
				executor.execute(queryTask);
			}
		}
	};

	private final Runnable queryTask = new Runnable() {
		@Override
		public void run() {
			try {
				while(CustomDTNClient.this.query());
			} catch (final SessionDestroyedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	public void initialize(final Registration pRegistration) throws ServiceNotAvailableException {
		super.initialize(context, pRegistration);
	}
}
