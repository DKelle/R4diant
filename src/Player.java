import org.lwjgl.input.Keyboard;


public class Player extends Living
{
	Point4D spawn = new Point4D(4,12,4,4);
	World world;
	ChunkLoader cl;
	float roll = 0;
	byte hunger = 0;
	byte thirst = 0;
	byte energy = 0;
	boolean alive = false;
	Inventory inv;
	float movedamp = 1.0f;
	float rotdamp = 1.0f;
	float wdepth = 4;
	double velocity = 0;
	double gravity = .005;
	
	private enum PlayerState
	{
		ONGROUND, JUMPING, FLYING;
	}
	
	PlayerState state = PlayerState.FLYING;
	
	/**
	 * Constructs a new player
	 * @param w - the world reference
	 */
	public Player(World w)
	{
		world = w;
		spawn();
		cl = new ChunkLoader(world, this, 101010);
		world.cl = cl;
		speed =  5  * (1f/(1000f)); //5 meters per second
		rotspeed = (float)(  90  * Math.PI/(180*1000)); //90 degrees per second
	}
	
	/**
	 * Sets the player to the spawn point
	 */
	public void spawn()
	{
		pos = spawn;
	}
	
	/**
	 * This needs to be updated.
	 */
	public void updatePos(){
		pos.y += velocity;
		velocity -= gravity;
	}
	
	/**
	 * Finds out whether a chunk is in the player's view
	 * @param c - the chunk to test
	 * @return whether the chunk is in the player's view
	 */
	public boolean isInView(Chunk c)
	{
		Point4D point = c.getPosition();
		return (!(-Math.cos(pitch)*Math.sin(yaw)*(point.x - pos.x) + Math.sin(pitch)*(point.y - pos.y) - Math.cos(pitch)*Math.cos(yaw)*(point.z - pos.z) < -4
				||   Math.abs(pos.w - point.w) > 5));
		
		//Note: this is still not fixed. Angles are complicated. :/
		//return true if you can see a chunk (because otherwise it would be a waste of memory)
		//very primitive, but it should at least save some time
	}
	
	/**
	 * Gets the chunk at the center of the loaded array, where the player is.
	 * @return the chunk where the player currently is
	 */
	public Chunk getChunkObject()
	{
		return world.loaded[world.loaddistance/2][world.loaddistance/2][world.loaddistance/2][world.loaddistance/2];
		//note: this assumes that the player stays at the center of the loaded chunks. Make sure this is true, or everything will die.
	}
	
	/**
	 * Gets the coordinates of the chunk where the player is
	 * @return the coordinates of the chunk
	 */
	public Point4D getChunkCoords()
	{
		return new Point4D((int)pos.x/8, (int)pos.y/8, (int)pos.z/8, (int)pos.w/8);
	}
	
	/**
	 * Tests to see if the player is in front of or behind a side
	 * @param side - the side to check
	 * @return 1 is in front, -1 if behind, 0 if on
	 */
	public int comparePlane(BlockSide side)
	{
		return side.comparePlane(pos);
	}
}
