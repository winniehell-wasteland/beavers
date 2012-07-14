package org.beavers.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.beavers.App;
import org.beavers.Settings;
import org.beavers.gameplay.Game;
import org.beavers.gameplay.GameInfo;
import org.beavers.ingame.IGameObject;
import org.beavers.ingame.IRemoveObjectListener;
import org.beavers.ingame.Soldier;
import org.beavers.ingame.Tile;
import org.beavers.ingame.WayPoint;
import org.beavers.storage.CustomGSON.WrongElementException;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * used to load a game state from file (and save it)
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class GameStorage {

	/**
	 * default constructor, loads game state from file
	 * @param pContext
	 * @param pGame
	 * @throws UnexpectedTileContentException
	 */
	public GameStorage(final Context pContext, final Game pGame)
	       throws Exception {
		this(pContext, pGame, null);
	}

	public GameStorage(final Context pContext, final Game pGame,
	                   final String pMapName)
	       throws Exception{
		context = pContext;
		game = pGame;

		info = null;

		// initialize game object containers
		gameObjects = new HashMap<Tile, IGameObject>();
		teams = new ArrayList<HashSet<Soldier>>(getSettings().getMaxPlayers());

		for(int i = 0; i < getSettings().getMaxPlayers(); ++i)
		{
			teams.add(new HashSet<Soldier>());
		}

		loadFromFile();
		loadDefaults();
	}

	/**
	 * add waypoint to container
	 * @param pWaypoint a waypoint
	 * @throws UnexpectedTileContentException
	 */
	public void addWaypoint(final WayPoint pWaypoint)
	            throws UnexpectedTileContentException {

		if(isTileOccupied(pWaypoint.getTile()))
		{
			throw new UnexpectedTileContentException("Tile is not empty!");
		}

		gameObjects.put(pWaypoint.getTile(), pWaypoint);
		pWaypoint.setRemoveObjectListener(removeListener);
	}

	public Soldier getSoldierByTile(final Tile pTile)
		           throws UnexpectedTileContentException{

		if(!hasSoldierOnTile(pTile))
		{
			throw new UnexpectedTileContentException("No soldier on that tile!");
		}

		return (Soldier) gameObjects.get(pTile);
	}

	public HashSet<Soldier> getSoldiersByTeam(final int pTeam) {
		if(pTeam < getSettings().getMaxPlayers())
		{
			return teams.get(pTeam);
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}

	public WayPoint getWaypointByTile(final Tile pTile)
	       throws UnexpectedTileContentException{

		if(!hasWaypointOnTile(pTile))
		{
			throw new UnexpectedTileContentException("No waypoint on that tile!");
		}

		return (WayPoint) gameObjects.get(pTile);
	}

	public boolean hasSoldierOnTile(final Tile pTile) {
		return (gameObjects.get(pTile) instanceof Soldier);
	}

	public boolean hasWaypointOnTile(final Tile pTile) {
		return (gameObjects.get(pTile) instanceof WayPoint);
	}

	public boolean isTileOccupied(final Tile pTile) {
		return gameObjects.containsKey(pTile);
	}

	/**
	 * move a {@link Soldier} to a new tile
	 * @param pSoldier moving soldier
	 * @param pFrom old position
	 * @param pTo new position
	 */
	public void moveSoldier(final Soldier pSoldier,
	                        final Tile pFrom, final Tile pTo) {
		if(hasSoldierOnTile(pFrom))
		{
			gameObjects.remove(pFrom);
			gameObjects.put(pTo, pSoldier);
		}
	}

	public void removeSoldier(final Soldier soldier)
                throws UnexpectedTileContentException {

		if(!hasSoldierOnTile(soldier.getTile()))
		{
			throw new UnexpectedTileContentException("No soldier on that tile!");
		}

		gameObjects.remove(soldier.getTile());
		teams.get(soldier.getTeam()).remove(soldier);
	}

	public void removeWaypoint(final WayPoint pWaypoint)
	            throws UnexpectedTileContentException {

		if(!hasWaypointOnTile(pWaypoint.getTile()))
		{
			throw new UnexpectedTileContentException("No waypoint on that tile!");
		}

		gameObjects.remove(pWaypoint.getTile());
	}

	public boolean saveToFile() {

		final Gson gson = CustomGSON.getInstance();
		final JsonWriter writer = CustomGSON.getWriter(context, getFileName());

		try {
			writer.beginObject();

			writer.name(GameInfo.JSON_TAG);
			gson.toJson(info, GameInfo.class, writer);

			writer.name(Soldier.JSON_TAG_COLLECTION);
			writer.beginArray();

			for(final HashSet<Soldier> team : teams)
			{
				for(final Soldier soldier : team)
				{
					gson.toJson(soldier, Soldier.class, writer);
				}
			}

			writer.endArray();

			writer.endObject();

		} catch (final IOException e) {
			Log.e(TAG, "Could not write file!", e);
			return false;
		} finally {
			try {
				writer.close();
			} catch (final IOException e) {
				Log.e(TAG, "Could not close writer!", e);
				return false;
			}
		}

		return true;
	}

	public void setRemoveObjectListener(final IRemoveObjectListener pListener)
	{
		removeListener = pListener;

		for(final IGameObject object : gameObjects.values())
		{
			object.setRemoveObjectListener(pListener);
		}
	}

	@SuppressWarnings("serial")
	public static class UnexpectedTileContentException extends Exception {
		public UnexpectedTileContentException(final String pMessage) {
			super(pMessage);
		}
	}

	/**
	 * @name debug
	 * @{
	 */
	private static final String TAG = GameStorage.class.getName();
	/**
	 * @}
	 */

	/** JSON tag for map name */
	private static final String JSON_TAG_MAP = "map";

	private final Context context;
	private final Game game;

	private GameInfo info = null;

	private final HashMap<Tile, IGameObject> gameObjects;
	private final ArrayList<HashSet<Soldier>> teams;

	private IRemoveObjectListener removeListener;

	private void addSoldier(final Soldier pSoldier)
	             throws UnexpectedTileContentException {

		if(isTileOccupied(pSoldier.getTile()))
		{
			throw new UnexpectedTileContentException("Tile is not empty!");
		}

		gameObjects.put(pSoldier.getTile(), pSoldier);
		teams.get(pSoldier.getTeam()).add(pSoldier);
		pSoldier.setRemoveObjectListener(removeListener);
	}

	private String getFileName() {
		return game.getDirectory(context) + "/state.json";
	}

	private Settings getSettings() {
		if(context.getApplicationContext() instanceof App) {
			return ((App) context.getApplicationContext()).getSettings();
		}
		else {
			return null;
		}
	}

	private void loadDefaults() throws UnexpectedTileContentException {
		// load teams
		addSoldier(new Soldier(0, new Tile(0, 0)));
		addSoldier(new Soldier(0, new Tile(2, 0)));
		addSoldier(new Soldier(1, new Tile(6, 9)));
		addSoldier(new Soldier(1, new Tile(8, 9)));
	}

	/**
	 * loads the storage from file
	 * @return map name
	 * @throws IOException
	 * @throws WrongElementException
	 * @throws UnexpectedTileContentException
	 * @throws JsonSyntaxException
	 * @throws JsonIOException
	 */
	private String loadFromFile()
	               throws IOException, WrongElementException, JsonIOException,
	                      JsonSyntaxException, UnexpectedTileContentException {
		final Gson gson = CustomGSON.getInstance();
		final JsonReader reader = CustomGSON.getReader(context, getFileName());

		// file does not exist
		if(reader == null) {
			throw new FileNotFoundException(getFileName());
		}

		String mapName = null;

		reader.beginObject();

		CustomGSON.assertElement(reader, JSON_TAG_MAP);
		mapName = reader.nextString();

		CustomGSON.assertElement(reader, Soldier.JSON_TAG_COLLECTION);

		reader.beginArray();
		while (reader.hasNext()) {
			addSoldier((Soldier) gson.fromJson(reader, Soldier.class));
		}
		reader.endArray();

		reader.endObject();

		reader.close();

		return mapName;
	}
}
