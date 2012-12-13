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
		pos = new Point4D(0,0,5,0);
	}
	
	
	public boolean isInView(Chunk c)
	{
		//Note: this is still not fixed. Angles are complicated. :/
		//return true if you can see a chunk (because otherwise it would be a waste of memory)
		if (c.playerDistance() < 10)
			return true;
		
		if (!(Math.abs(c.playerDirection()[0]-yaw) < Math.PI || Math.abs(c.playerDirection()[0]+yaw) < Math.PI))
		{
			System.out.println("Not rendering.");
		}
		
		return (Math.abs(c.playerDirection()[0]-yaw) < Math.PI || Math.abs(c.playerDirection()[0]+yaw) < Math.PI);
		//very primitive, but it should at least save some time
	}
}
