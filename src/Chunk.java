import java.util.ArrayList;
import java.util.Collections;

import org.lwjgl.input.Keyboard;


//an 8x8x8x8 chunk
public class Chunk implements Comparable
{
	World world;
	int x = 0; //indices
	int y = 0;
	int z = 0;
	int w = 0;
	Block[][][][] data = new Block[8][8][8][8];
	ArrayList<Entity> entities;
	byte[][][] biome = new byte[8][8][8];
	
	//ArrayList<BlockSide> sides;
	
	boolean needsUpdate = true;
	//hint: this data will probably be accessed wzyx
	
	
	public Chunk(World wor, int a, int b, int c, int d)
	{
		world = wor;
		x = a;
		y = b;
		z = c;
		w = d;
		
		data = new Block[8][8][8][8];
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
	
	//public void addBlock(short pos) ?
	
	public void prepareForRender()
	{	
		if (!world.player.isInView(this))
			return;
		
		if (!needsUpdate)
		{
			return;
		}
		
		for (int i = 0; i < data.length; i++)
		{
			for (int j = 0; j < data[0].length; j++)
			{
				for (int k = 0; k < data[0][0].length; k++)
				{
					for (int l = 0; l < data[0][0][0].length; l++)
					{
						if (data[i][j][k][l] == null)
							continue;
						//check for open sides
						//if any of them are open, add it to the array as a short position (index)
						data[i][j][k][l].checkSides();
						if ( data[i][j][k][l].sides != Byte.MAX_VALUE )
						{
							Block b = data[i][j][k][l];
							//distance = -cos(pitch)*sin(yaw)*dx + sin(pitch)*dy + cos(pitch)*cos(yaw)*dz
							Player player = b.chunk.world.player; //reference
							Point4D point = b.getPosition();
							
							if ((data[i][j][k][l].sides & 1) == 0)
								world.existing.add(new BlockSide(b, (byte)0));
							if ((b.sides & 2) == 0)
								world.existing.add(new BlockSide(b, (byte)1));
							if ((b.sides & 4) == 0)
								world.existing.add(new BlockSide(b, (byte)2));
							if ((b.sides & 8) == 0)
								world.existing.add(new BlockSide(b, (byte)3));
							if ((b.sides & 16) == 0)
								world.existing.add(new BlockSide(b, (byte)4));
							if ((b.sides & 32) == 0)
								world.existing.add(new BlockSide(b, (byte)5));
						}
					}
				}
			}
		}
		//Collections.sort(toRender);
		needsUpdate = false;
	}
	
	public double playerDistance()
	{
		return world.player.pos.dist(getPosition());
	}
	
	public double[] playerDirection()
	{
		return world.player.pos.angles(getPosition());
	}
	
	public Point4D getPosition()
	{
		return new Point4D(x*8+4, y*8+4, z*8+4, w*8+4);
	}
}
