import java.util.ArrayList;
import java.util.Collections;


//an 8x8x8x8 chunk
public class Chunk implements Comparable
{
	World world;
	int x = 0; //indices
	int y = 0;
	int z = 0;
	int w = 0;
	//Block[][][][] data = new Block[8][8][8][8];
	ArrayList<Block> data;
	ArrayList<Entity> entities;
	byte[][][] biome = new byte[8][8][8];
	ArrayList<Short> toRender;
	boolean needsUpdate = false;
	//hint: this data will probably be accessed wzyx
	
	
	public Chunk(World wor, int a, int b, int c, int d)
	{
		world = wor;
		x = a;
		y = b;
		z = c;
		w = d;
		
		data = new ArrayList<Block>();
		toRender = new ArrayList<Short>();
	}

	public int compareTo(Object o) 
	{
		if (o instanceof Chunk)
		{
			//world.player
			//compare two chunk objects by their distance from the player..?
			return (int)playerDistance();
		}
		return 0;
	}
	
	public Block findBlock(short position)
	{
		//assuming sorted
		for (Block e : data)
		{
			if (e.pos == position)
				return e;
		}
		return null;
	}
	
	public void prepareForRender()
	{	
		toRender = new ArrayList<Short>();
		if (!needsUpdate && !world.player.isInView(this))
			return;
		if (needsUpdate)
			Collections.sort(data); //in the future, make sure to add new entries in sorted position to avoid this step.
		for (Block e : data)
		{
			//check for open sides
			//if any of them are open, add it to the array as a short position (index)
			if (e instanceof Solid)
			{
				((Solid)e).checkSides();
				if ( ((Solid)e).sides != 0 )
				{
					toRender.add(e.pos);
				}
			}
			else
				toRender.add(e.pos);
		}
		needsUpdate = false;
	}
	
	public double playerDistance()
	{
		Point4D pos = new Point4D(x*8+4, y*8+4, z*8+4, w*8+4);
		return pos.dist(world.player.pos);
	}
	
	public double[] playerDirection()
	{
		Point4D pos = new Point4D(x*8+4, y*8+4, z*8+4, w*8+4);
		return world.player.pos.angles(pos);
	}
}


/*

{ { },
  { },
  { },
  { },
  { },







*/