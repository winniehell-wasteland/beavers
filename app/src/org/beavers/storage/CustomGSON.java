package org.beavers.storage;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.anddev.andengine.util.path.Path.Step;
import org.anddev.andengine.util.path.WeightedPath;
import org.beavers.ingame.Soldier;
import org.beavers.ingame.Tile;
import org.beavers.ingame.WayPoint;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

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

	public static JsonReader getReader(final Context pContext,
	                                   final String pFileName) {

		InputStream file = null;

		try {
			file = pContext.openFileInput(pFileName);
		} catch (final FileNotFoundException e) {
			Log.w(TAG, "Could not open file for input! " + e.getMessage());
			return null;
		}

		return new JsonReader(
				new InputStreamReader(file, Charset.defaultCharset())
			);
	}

	public static JsonWriter getWriter(final Context pContext,
	                                   final String pFileName) {

		FileOutputStream file = null;

		try {
			file = pContext.openFileOutput(pFileName, Context.MODE_PRIVATE);
		} catch (final FileNotFoundException e) {
			Log.e(TAG, "Could not open file for output! " + e.getMessage());
			return null;
		}

		return new JsonWriter(
			new OutputStreamWriter(file, Charset.defaultCharset())
		);
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

	/**
	 * @name debug
	 * @{
	 */
	private static final String TAG = CustomGSON.class.getSimpleName();
	/**
	 * @}
	 */

	/** singleton instance */
	private static Gson instance = null;

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

	/** singleton */
	private CustomGSON()
	{

	}
}
