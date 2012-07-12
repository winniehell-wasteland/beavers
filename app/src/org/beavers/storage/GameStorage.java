package org.beavers.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.GameState;
import org.beavers.ingame.IGameObject;
import org.beavers.ingame.IRemoveObjectListener;
import org.beavers.ingame.Soldier;
import org.beavers.ingame.Tile;
import org.beavers.ingame.WayPoint;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class GameStorage {

	public GameStorage(final Context pContext, final GameInfo pGame)
	       throws UnexpectedTileContentException {
		context = pContext;
		game = pGame;

		// initialize game object containers
		gameObjects = new HashMap<Tile, IGameObject>();
		teams = new ArrayList<HashSet<Soldier>>(getTeamCount());

		for(int i = 0; i < getTeamCount(); ++i)
		{
			teams.add(new HashSet<Soldier>());
		}

		if(!loadFromFile())
		{
			loadDefaults();
			saveToFile();
		}
	}

	public void addWaypoint(final WayPoint pWaypoint)
	            throws UnexpectedTileContentException {

		if(isTileOccupied(pWaypoint.getTile()))
		{
			throw new UnexpectedTileContentException("Tile is not empty!");
		}

		gameObjects.put(pWaypoint.getTile(), pWaypoint);
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
		if(pTeam < getTeamCount())
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

		gameObjects.remove(soldier);
		teams.get(soldier.getTeam()).remove(soldier);
	}

	public void removeWaypoint(final WayPoint waypoint)
	            throws UnexpectedTileContentException {

		if(!hasWaypointOnTile(waypoint.getTile()))
		{
			throw new UnexpectedTileContentException("No waypoint on that tile!");
		}

		gameObjects.remove(waypoint);
	}

	public void saveToFile() {
		// TODO Auto-generated method stub

	}

	public void setRemoveObjectListener(final IRemoveObjectListener pListener)
	{
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

	private final Context context;
	private final GameInfo game;

	private final HashMap<Tile, IGameObject> gameObjects;
	private final ArrayList<HashSet<Soldier>> teams;

	private int getTeamCount() {
		return 2;
	}

	private void loadDefaults() throws UnexpectedTileContentException {
		// load teams
		addSoldier(new Soldier(0, new Tile(0, 0)));
		addSoldier(new Soldier(0, new Tile(2, 0)));
		addSoldier(new Soldier(1, new Tile(6, 9)));
		addSoldier(new Soldier(1, new Tile(8, 9)));
	}

	private boolean loadFromFile() {
		final Gson gson = CustomGSON.getInstance();
		InputStream file = null;

		try {
			file = context.openFileInput(
				"game-" + game.toString().replace('/', '_')
			);
		} catch (final Exception e) {
			Log.e(TAG, "Could not open file!", e);
			return false;
		}

		try {
			final JsonReader reader = new JsonReader(new InputStreamReader(file, "UTF-8"));

			reader.beginObject();

			assertSection(reader, "state");
			game.setState(GameState.valueOf(reader.nextString()));

			assertSection(reader, "map");
			game.setMapName(reader.nextString());

			assertSection(reader, "teams");

			reader.beginArray();
			while (reader.hasNext()) {
				addSoldier((Soldier) gson.fromJson(reader, Soldier.class));
			}
			reader.endArray();

			reader.endObject();

			reader.close();
		} catch (final Exception e) {
			Log.e(TAG, "Could not read JSON from file!", e);
			return false;
		}

		return true;
	}

	private void addSoldier(final Soldier pSoldier)
	             throws UnexpectedTileContentException {

		if(isTileOccupied(pSoldier.getTile()))
		{
			throw new UnexpectedTileContentException("Tile is not empty!");
		}

		gameObjects.put(pSoldier.getTile(), pSoldier);
		teams.get(pSoldier.getTeam()).add(pSoldier);
	}

	private void assertSection(final JsonReader pReader, final String pSection)
	             throws IOException, Exception {
		if(!pReader.nextName().equals(pSection))
		{
			pReader.close();
			throw new Exception("Expected " + pSection + "!");
		}
	}
}
