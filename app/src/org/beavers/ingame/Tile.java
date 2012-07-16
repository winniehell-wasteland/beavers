package org.beavers.ingame;

import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.util.path.Direction;
import org.anddev.andengine.util.path.Path.Step;

/**
 * helper class for map tile
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class Tile {

	/**
	 * @name size constants
	 * @{
	 */
	public static final int TILE_HEIGHT = 64;
	public static final int TILE_WIDTH = TILE_HEIGHT;
	/**
	 * @}
	 */

	/**
	 * default constructor
	 * @param pColumn tile column
	 * @param pRow tile row
	 */
	public Tile(final int pColumn, final int pRow)
	{
		column = pColumn;
		row = pRow;
	}

	/**
	 * copy tile position from {@link Step}
	 * @param pStep Step
	 */
	public Tile(final Step pStep) {
		this(pStep.getTileColumn(), pStep.getTileRow());
	}

	/**
	 * copy tile position from {@link TMXTile}
	 * @param pTile TMXTile
	 */
	public Tile(final TMXTile pTile)
	{
		this(pTile.getTileColumn(), pTile.getTileRow());
	}

	public Tile(final int[] pComponents) {
		this(pComponents[0], pComponents[1]);
	}

	@Override
	public boolean equals(final Object pOther) {
		if(pOther instanceof Tile)
		{
			final Tile other = (Tile) pOther;

			return (getColumn() == other.getColumn())
			        && (getRow() == other.getRow());
		}

		return false;
	}

	/**
	 * @return x-coordinate of tile center
	 */
	public int getCenterX()
	{
		return getX() + TILE_WIDTH/2;
	}

	/**
	 * @return y-coordinate of tile center
	 */
	public int getCenterY()
	{
		return getY() + TILE_HEIGHT/2;
	}

	/**
	 * @return tile column
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * @return direction to other tile
	 */
	public Direction getDirectionTo(final Tile pTo)
	{
		return Direction.fromDelta(pTo.getColumn() - getColumn(),
			pTo.getRow() - getRow());
	}

	/**
	 * @return tile row
	 */
	public int getRow() {
		return row;
	}

	/**
	 * @return tile height
	 */
	public int getTileHeight() {
		return TILE_HEIGHT;
	}

	/**
	 * @return tile width
	 */
	public int getTileWidth() {
		return TILE_WIDTH;
	}

	/**
	 * @return x-coordinate of tile
	 */
	public int getX()
	{
		return TILE_WIDTH*column;
	}

	/**
	 * @return y-coordinate of tile
	 */
	public int getY()
	{
		return TILE_HEIGHT*row;
	}

	@Override
	public int hashCode() {
		return ((column << 16) | row);
	}

	/**
	 * @return tile at given coordinates
	 */
	public static Tile fromCoordinates(final float pX, final float pY) {
		return new Tile((int) Math.floor(pX/TILE_WIDTH), (int) Math.floor(pY/TILE_HEIGHT));
	}

	@Override
	public String toString() {
		return "(" + column + ", " + row + ")";
	}

	/**
	 * @name tile position
	 * @{
	 */
	private final int column;
	private final int row;
	/**
	 * @}
	 */
}

