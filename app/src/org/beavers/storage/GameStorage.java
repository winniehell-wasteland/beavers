package org.beavers.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.anddev.andengine.util.StreamUtils;
import org.beavers.App;
import org.beavers.R;
import org.beavers.Settings;
import org.beavers.gameplay.Game;
import org.beavers.gameplay.GameInfo;
import org.beavers.ingame.IGameEventsListener;
import org.beavers.ingame.IGameObject;
import org.beavers.ingame.IMenuDialogListener;
import org.beavers.ingame.IRemoveObjectListener;
import org.beavers.ingame.Soldier;
import org.beavers.ingame.Tile;
import org.beavers.ingame.WayPoint;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
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
		context = pContext;
		game = pGame;

		// initialize game object containers
		gameObjects = new HashMap<Tile, IGameObject>();
		teams = new ArrayList<SoldierList>(getSettings().getMaxPlayers());

		for(int i = 0; i < getSettings().getMaxPlayers(); ++i)
		{
			teams.add(new SoldierList());
		}

		loadFromFile();
		//saveToFile();
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
		pWaypoint.setMenuDialogListener(menuListener);
	}

	public Soldier getSoldierByTile(final Tile pTile)
		           throws UnexpectedTileContentException{

		if(!hasSoldierOnTile(pTile))
		{
			throw new UnexpectedTileContentException("No soldier on that tile!");
		}

		return (Soldier) gameObjects.get(pTile);
	}

	public SoldierList getSoldiersByTeam(final int pTeam) {
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

	public void saveToFile() throws FileNotFoundException {

		final Gson gson = CustomGSON.getInstance();

		JsonWriter writer;

		writer = CustomGSON.getWriter(context, getFileName());

		try {
			writer.beginArray();
			for(final HashSet<Soldier> team : teams)
			{
				for(final Soldier soldier : team)
				{
					gson.toJson(soldier, Soldier.class, writer);
				}
			}
			writer.endArray();
		} catch (final IOException e) {
			Log.e(TAG, context.getString(R.string.error_json_writing), e);
		} finally {
			try {
				writer.close();
			} catch (final IOException e) {
				Log.e(TAG, context.getString(R.string.error_json_writing), e);
			}
		}
	}
	
	public void setMenuDialogListener(final IMenuDialogListener mListener){
		menuListener=mListener;
	}
	
	public void setGameEventsListener(final IGameEventsListener eListener){
		gameListener=eListener;
		for(final IGameObject object : gameObjects.values())
		{
			
			if(object instanceof Soldier){
				((Soldier)object).setGameEventsListener(gameListener);
				Log.e("GameListener", ""+gameListener.toString()+" is set");
			}
		}
		
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

	private final Context context;
	private final Game game;

	private final HashMap<Tile, IGameObject> gameObjects;
	private final ArrayList<SoldierList> teams;

	private IRemoveObjectListener removeListener;
	private IMenuDialogListener menuListener;
	private IGameEventsListener gameListener;
	private void addSoldier(final Soldier pSoldier)
	             throws UnexpectedTileContentException {

		if(isTileOccupied(pSoldier.getTile()))
		{
			throw new UnexpectedTileContentException("Tile is not empty!");
		}

		gameObjects.put(pSoldier.getTile(), pSoldier);
		teams.get(pSoldier.getTeam()).add(pSoldier);
		pSoldier.setRemoveObjectListener(removeListener);
	//	pSoldier.setGameEventsListener(gameListener);
	}

	private String getFileName() {
		return game.getDirectory(context) + "/setup.json";
	}

	private Settings getSettings() {
		if(context.getApplicationContext() instanceof App) {
			return ((App) context.getApplicationContext()).getSettings();
		}
		else {
			return null;
		}
	}

	/**
	 * loads the storage from file
	 * @throws FileNotFoundException
	 */
	private void loadFromFile() throws FileNotFoundException {

		if(!(new File(getFileName())).exists()) {
			final GameInfo info = GameInfo.fromFile(context, game);

			Log.d(TAG, "Copying maps/" + info.getMapName() + "/setup.json");

			try {
				final InputStream src = context.getAssets().open(
					"maps/" + info.getMapName() + "/setup.json"
				);

				final FileOutputStream dest = new FileOutputStream(getFileName());

				StreamUtils.copyAndClose(src, dest);
			} catch (final IOException e) {
				Log.e(TAG, "Copying map failed!", e);
				return;
			}
		}

		try {
			Log.d(TAG, "loading: "+StreamUtils.readFully(new FileInputStream(getFileName())));
		} catch (final Exception e) {
			Log.e(TAG, e.getMessage());
			return;
		}

		final Gson gson = CustomGSON.getInstance();
		final JsonReader reader = CustomGSON.getReader(context, getFileName());

		// file does not exist
		if(reader == null) {
			throw new FileNotFoundException(getFileName());
		}

		try {
			reader.beginArray();
			while(reader.hasNext()) {
				final Soldier soldier = gson.fromJson(reader, Soldier.class);

				try {
					addSoldier(soldier);
				} catch (final UnexpectedTileContentException e) {
					Log.e(TAG, "Adding soldier failed!", e);
				}
			}
			reader.endArray();
		} catch (final Exception e) {
			Log.e(TAG, "Loading game storage failed!", e);
		} finally {
			try {
				reader.close();
			} catch (final IOException e) {
				Log.e(TAG, "Closing reader failed!", e);
			}
		}
	}
}
