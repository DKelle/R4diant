import java.util.ArrayList;

//Loads the chunks based on the player's position

public class ChunkLoader 
{
	World world;
	Player player;
	ArrayList<Point4D> loaded;
	
	public ChunkLoader(World w, Player p)
	{
		world = w;
		player = p;
		loaded = new ArrayList<Point4D>();
		temp();
	}
	
	public void checkStatus()
	{
		//checks player's location, and decides if new chunks need to be loaded
	}
	
	public void temp()
	{
		//a temporary function to initialize the land for viewing
		loaded.add(new Point4D(0,0,0,0));
		Chunk c = new Chunk(world, 0,0,0,0);
		c.data.add(new Block(c, (short)0));
		c.data.add(new Block(c, (short)1));
		c.data.add(new Block(c, (short)3));
		c.data.add(new Block(c, (short)5));
		c.data.add(new Block(c, (short)8));
		c.data.add(new Block(c, (short)24));
		c.data.add(new Block(c, (short)64));
		c.data.add(new Block(c, (short)242));
		c.data.add(new Block(c, (short)2414));
		c.data.add(new Block(c, (short)2222));
		c.data.add(new Block(c, (short)5523));
		c.data.add(new Block(c, (short)1222));
		c.data.add(new Block(c, (short)1235));
		c.data.add(new Block(c, (short)2492));
		c.data.add(new Block(c, (short)6232));
		c.data.add(new Block(c, (short)2123));
		
		
		world.loaded.add(c);
	}
	
}
