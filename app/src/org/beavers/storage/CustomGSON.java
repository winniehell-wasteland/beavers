package org.beavers.storage;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * customized {@link Gson}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class CustomGSON {

	/** get singleton instance */
	public static Gson getInstance()
	{
		if(instance == null)
		{
			final GsonBuilder builder = new GsonBuilder();

			try {
				setupSerialization(builder, "Path");
				setupSerialization(builder, "Soldier");
				setupSerialization(builder, "Tile");
				setupSerialization(builder, "WayPoint");
			} catch (final ClassNotFoundException e) {
				Log.e(CustomGSON.class.getName(),
				      "Could not setup serialization!", e);
			}

			instance = builder.create();
		}

		return instance;
	}

	/** setup serialization/deserialization for class with given name */
	private static void setupSerialization(final GsonBuilder pBuilder,
	                                       final String pClassName)
	                    throws ClassNotFoundException {
		pBuilder.registerTypeAdapter(Class.forName(pClassName),
                                     Class.forName(pClassName+"Serializer"));
		pBuilder.registerTypeAdapter(Class.forName(pClassName),
                                     Class.forName(pClassName+"Deserializer"));
	}

	/** singleton instance */
	private static Gson instance = null;

	/** singleton */
	private CustomGSON()
	{

	}
}
