import org.lwjgl.input.Keyboard;


public class Player extends Living
{
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
	
	public Player(World w)
	{
		world = w;
		cl = new ChunkLoader(world, this);
		world.cl = cl;
		spawn();
		speed =  5  * (1f/(1000f)); //5 meters per second
		rotspeed = (float)(  90  * Math.PI/(180*1000)); //90 degrees per second
	}
	
	public void spawn()
	{
		pos = new Point4D(8,5,8,0);
	}
	
	public void updatePos(){
		pos.y += velocity;
		velocity -= gravity;
	}
	public boolean isInView(Chunk c)
	{
		Point4D point = c.getPosition();
		return (!(-Math.cos(pitch)*Math.sin(yaw)*(point.x - pos.x) + Math.sin(pitch)*(point.y - pos.y) - Math.cos(pitch)*Math.cos(yaw)*(point.z - pos.z) < -4
				||   Math.abs(pos.w - point.w) > 5));
		
		//Note: this is still not fixed. Angles are complicated. :/
		//return true if you can see a chunk (because otherwise it would be a waste of memory)
		//very primitive, but it should at least save some time
	}
	public Chunk getChunk()
	{
		for (Chunk e : world.loaded)
		{
			if (e.x*8 <= pos.x && pos.x <= e.x*8+8 && e.y*8 <= pos.y && pos.y <= e.y*8+8 && e.z*8 <= pos.z && pos.z <= e.z*8+8 && e.w*8 <= pos.w && pos.w <= e.w*8+8)
			{
				return e;
			}
		}
		return null;
	}
}
