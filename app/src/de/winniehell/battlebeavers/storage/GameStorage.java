/*
	(c) winniehell, wintermadnezz (2012)

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

package de.winniehell.battlebeavers.storage;

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
import de.winniehell.battlebeavers.App;
import de.winniehell.battlebeavers.R;
import de.winniehell.battlebeavers.Settings;
import de.winniehell.battlebeavers.gameplay.Game;
import de.winniehell.battlebeavers.gameplay.GameInfo;
import de.winniehell.battlebeavers.ingame.IGameEventsListener;
import de.winniehell.battlebeavers.ingame.IGameObject;
import de.winniehell.battlebeavers.ingame.IMenuDialogListener;
import de.winniehell.battlebeavers.ingame.IObjectPositionListener;
import de.winniehell.battlebeavers.ingame.Soldier;
import de.winniehell.battlebeavers.ingame.Tile;
import de.winniehell.battlebeavers.ingame.WayPoint;

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

	public void addSoldier(final Soldier pSoldier)
	             throws UnexpectedTileContentException {

		if(isTileOccupied(pSoldier.getTile()))
		{
			throw new UnexpectedTileContentException("Tile is not empty!");
		}

		gameObjects.put(pSoldier.getTile(), pSoldier);
		teams.get(pSoldier.getTeam()).add(pSoldier);
		pSoldier.setPositionListener(positionListener);
		pSoldier.setGameEventsListener(gameListener);
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
		pWaypoint.setPositionListener(positionListener);
		pWaypoint.setMenuDialogListener(menuListener);
	}

	public Soldier getSoldierById(final int pSoldierId) {
		for(final SoldierList team : teams) {
			for(final Soldier soldier : team) {
				if(soldier.getId() == pSoldierId) {
					return soldier;
				}
			}
		}
		
		return null;
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

	public void removeSoldiers() throws UnexpectedTileContentException {
		for(SoldierList team : teams) {
			SoldierList oldList = (SoldierList) team.clone();
			
			for(final Soldier soldier : oldList) {
				soldier.detachSelf();
				removeSoldier(soldier);
			}
		}
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

	public void setPositionListener(final IObjectPositionListener pListener)
	{
		positionListener = pListener;

		for(final IGameObject object : gameObjects.values())
		{
			object.setPositionListener(pListener);
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

	private IObjectPositionListener positionListener;
	private IMenuDialogListener menuListener;
	private IGameEventsListener gameListener;

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
