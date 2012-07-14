package org.beavers.storage;


import java.io.IOException;

import org.anddev.andengine.util.path.Path.Step;
import org.anddev.andengine.util.path.WeightedPath;
import org.beavers.ingame.Soldier;
import org.beavers.ingame.Tile;
import org.beavers.ingame.WayPoint;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

/**
 * customized {@link Gson}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class CustomGSON {

	/**
	 * @name helper functions
	 * @{
	 */
	/** ensure the next element has the given Name */
	public static void assertElement(final JsonReader pReader,
	                                 final String pName)
	            throws IOException, Exception {
		if(!pReader.nextName().equals(pName))
		{
			pReader.close();
			throw new Exception("Expected " + pName + "!");
		}
	}
	/**
	 * @}
	 */

	/** get singleton instance */
	public static Gson getInstance()
	{
		if(instance == null)
		{
			final GsonBuilder builder = new GsonBuilder();

			try {
				final Class<?>[] classes = {
					WeightedPath.class,
					Soldier.class,
					Step.class,
					Tile.class,
					WayPoint.class
				};

				for(final Class<?> c : classes)
				{
					setupSerialization(builder, c);
				}
			} catch (final Exception e) {
				Log.e(CustomGSON.class.getName(),
				      "Could not setup serialization!", e);
			}

			instance = builder.create();
		}

		return instance;
	}

	/** setup serialization/deserialization for given class */
	private static void setupSerialization(final GsonBuilder pBuilder,
	                                       final Class<?> pClass)
	                    throws ClassNotFoundException,
                               InstantiationException,
                               IllegalAccessException {
		final String prefix = CustomGSON.class.getPackage().getName() + "."
	                          + pClass.getSimpleName();

		pBuilder.registerTypeAdapter(
			pClass, Class.forName(prefix+"Serializer").newInstance());
		pBuilder.registerTypeAdapter(
			pClass, Class.forName(prefix+"Deserializer").newInstance());
	}

	/** singleton instance */
	private static Gson instance = null;

	/** singleton */
	private CustomGSON()
	{

	}
}
