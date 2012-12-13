
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
	
	public Player(World w)
	{
		world = w;
		cl = new ChunkLoader(world, this);
		world.cl = cl;
	}
	
	public void spawn()
	{
		
	}
}
