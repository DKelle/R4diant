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
		//this is redundant, but I don't think it can be helped 
		//chunk.addBlock() could work, but in the future, different block types...
		//Eh, we need a function to look up a block type by ID. Hmm.
		
		for (int i = 0; i < 8; i++)
		{
			for (int k = 0; k < 8; k++)
			{
				int y = (int)(Math.random()*8);
				int w = (int)(Math.random()*8);
				c.data[i][y][k][w] = new Block(c, (short)(512*w+64*k+8*y+i)); 
			}
		}
		
		//c.data[0][0][0][0] = new Block(c, (short)0_0000);
		//c.data[1][0][0][0] = new Block(c, (short)0_0001);
		//c.data[0][1][0][0] = new Block(c, (short)0_0010);
		
		world.loaded.add(c);
	}
	
}
