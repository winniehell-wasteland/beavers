package org.beavers.storage;

import java.lang.reflect.Type;
import java.util.Iterator;

import org.anddev.andengine.util.path.Path;
import org.anddev.andengine.util.path.Path.Step;
import org.beavers.ingame.Soldier;
import org.beavers.ingame.Tile;
import org.beavers.ingame.WayPoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CustomGSON {

	public static Gson getInstance()
	{
		if(instance == null)
		{
			final GsonBuilder builder = new GsonBuilder();

			builder.registerTypeAdapter(Path.class, new PathSerializer());
			builder.registerTypeAdapter(Path.class, new PathDeserializer());

			builder.registerTypeAdapter(Step.class, new StepSerializer());
			builder.registerTypeAdapter(Step.class, new StepDeserializer());

			builder.registerTypeAdapter(Soldier.class, new SoldierSerializer());
			builder.registerTypeAdapter(Soldier.class, new SoldierDeserializer());

			builder.registerTypeAdapter(Tile.class, new TileSerializer());
			builder.registerTypeAdapter(Tile.class, new TileDeserializer());

			builder.registerTypeAdapter(WayPoint.class, new WayPointSerializer());
			builder.registerTypeAdapter(WayPoint.class, new WayPointDeserializer());

			instance = builder.create();
		}

		return instance;
	}

	private static Gson instance = null;

	/**
	 * serializer class for {@link Path}
	 */
	private static class PathSerializer implements JsonSerializer<Path> {
		@Override
		public JsonElement serialize(final Path pSrc, final Type pType,
		                             final JsonSerializationContext pContext) {
			if(pSrc == null)
			{
				return JsonNull.INSTANCE;
			}

			final JsonArray array = new JsonArray();

			for(int i = 0; i < pSrc.getLength(); ++i)
			{
				array.add(pContext.serialize(pSrc.getStep(i)));
			}

			return array;
		}
	}

	/**
	 * deserializer class for {@link Path}
	 */
	private static class PathDeserializer implements JsonDeserializer<Path> {
	    @Override
	    public Path deserialize(final JsonElement pJson, final Type pType,
	                            final JsonDeserializationContext pContext)
	                            throws JsonParseException {
	    	if(!pJson.isJsonArray())
	    	{
	    		return null;
	    	}

	    	final Path path = new Path();

	    	for(final JsonElement step : pJson.getAsJsonArray())
	    	{
	    		path.append((Step) pContext.deserialize(step, Step.class));
	    	}

	        return path;
	    }
	}

	/**
	 * serializer class for {@link Step}
	 */
	private static class StepSerializer implements JsonSerializer<Step> {
		@Override
		public JsonElement serialize(final Step pSrc, final Type pType,
		                             final JsonSerializationContext pContext) {
			if(pSrc == null)
			{
				return JsonNull.INSTANCE;
			}

			final JsonArray array = new JsonArray();

			array.add(pContext.serialize(pSrc.getTileColumn()));
			array.add(pContext.serialize(pSrc.getTileRow()));

			return array;
		}
	}

	/**
	 * deserializer class for {@link Step}
	 */
	private static class StepDeserializer implements JsonDeserializer<Step> {
	    @Override
	    public Step deserialize(final JsonElement pJson, final Type pType,
	                            final JsonDeserializationContext pContext)
	                            throws JsonParseException {
	    	if(!pJson.isJsonArray())
	    	{
	    		return null;
	    	}

	    	final JsonArray array = pJson.getAsJsonArray();

	    	if(array.size() != 2)
	    	{
	    		return null;
	    	}

	        return dummyPath.new Step(array.get(0).getAsInt(),
	        	array.get(1).getAsInt());
	    }

	    /** necessary to create new instances of Step */
	    static private Path dummyPath = new Path();
	}

	/**
	 * serializer class for {@link Soldier}
	 */
	private static class SoldierSerializer implements JsonSerializer<Soldier> {
		@Override
		public JsonElement serialize(final Soldier pSrc, final Type pType,
		                             final JsonSerializationContext pContext) {
			if(pSrc == null)
			{
				return JsonNull.INSTANCE;
			}

			final JsonObject object = new JsonObject();

			object.add("team", pContext.serialize(pSrc.getTeam()));
			object.add("tile", pContext.serialize(pSrc.getTile()));
			object.add("view_angle", pContext.serialize(pSrc.getRotation()));
			object.add("waypoints", pContext.serialize(pSrc.getWaypoints()));

			return object;
		}
	}

	/**
	 * deserializer class for {@link Soldier}
	 */
	private static class SoldierDeserializer implements JsonDeserializer<Soldier> {
	    @Override
	    public Soldier deserialize(final JsonElement pJson, final Type pType,
	                            final JsonDeserializationContext pContext)
	                            throws JsonParseException {
	    	if(!pJson.isJsonObject())
	    	{
	    		return null;
	    	}

	    	final JsonObject object = pJson.getAsJsonObject();

	    	if(!object.has("team") || !object.has("tile")
	    	   || !object.has("view_angle") || !object.has("waypoints")
	    	   || !object.get("waypoints").isJsonArray())
	    	{
	    		return null;
	    	}

	    	final Soldier soldier = new Soldier(object.get("team").getAsInt(),
	    		(Tile) pContext.deserialize(object.get("tile"), Tile.class));

	    	soldier.setRotation(object.get("view_angle").getAsFloat());

	    	final JsonArray waypoints = object.get("waypoints").getAsJsonArray();

	    	final Iterator<JsonElement> it = waypoints.iterator();

	    	final WayPoint firstWaypoint =
	    		pContext.deserialize(it.next(), WayPoint.class);

	    	if(firstWaypoint.getAim() != null)
	    	{
	    		soldier.getFirstWaypoint().setAim(
	    			firstWaypoint.getAim().getTile());
	    	}

	    	while(it.hasNext())
	    	{
	    		final WayPoint waypoint =
	    			pContext.deserialize(it.next(),WayPoint.class);

	    		soldier.addWayPoint(waypoint);
	    	}

	        return soldier;
	    }
	}

	/**
	 * serializer class for {@link Tile}
	 */
	private static class TileSerializer implements JsonSerializer<Tile> {
		@Override
		public JsonElement serialize(final Tile pSrc, final Type pType,
		                             final JsonSerializationContext pContext) {
			if(pSrc == null)
			{
				return JsonNull.INSTANCE;
			}

			final JsonArray array = new JsonArray();

			array.add(pContext.serialize(pSrc.getColumn()));
			array.add(pContext.serialize(pSrc.getRow()));

			return array;
		}
	}

	/**
	 * deserializer class for {@link Tile}
	 */
	private static class TileDeserializer implements JsonDeserializer<Tile> {
	    @Override
	    public Tile deserialize(final JsonElement pJson, final Type pType,
	                            final JsonDeserializationContext pContext)
	                            throws JsonParseException {
	    	if(!pJson.isJsonArray())
	    	{
	    		return null;
	    	}

	    	final JsonArray array = pJson.getAsJsonArray();

	    	if(array.size() != 2)
	    	{
	    		return null;
	    	}

	        return new Tile(array.get(0).getAsInt(),
	        	array.get(1).getAsInt());
	    }
	}

	/**
	 * serializer class for {@link WayPoint}
	 */
	private static class WayPointSerializer implements JsonSerializer<WayPoint> {
		@Override
		public JsonElement serialize(final WayPoint pSrc, final Type pType,
		                             final JsonSerializationContext pContext) {
			return JsonNull.INSTANCE;
		}
	}

	/**
	 * deserializer class for {@link WayPoint}
	 */
	private static class WayPointDeserializer implements JsonDeserializer<WayPoint> {
	    @Override
	    public WayPoint deserialize(final JsonElement pJson, final Type pType,
	                            final JsonDeserializationContext pContext)
	                            throws JsonParseException {
	    	return null;
	    }
	}
}
