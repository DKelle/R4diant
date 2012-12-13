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
	Block[][][][] data = new Block[8][8][8][8];
	ArrayList<Entity> entities;
	byte[][][] biome = new byte[8][8][8];
	ArrayList<Block> toRender;
	
	ArrayList<BlockSide> sides;
	
	//ArrayList<BlockSide> right;
	//ArrayList<BlockSide> left;
	//ArrayList<BlockSide> up;
	//ArrayList<BlockSide> down;
	//ArrayList<BlockSide> front;
	//ArrayList<BlockSide> back;
	
	boolean needsUpdate = false;
	//hint: this data will probably be accessed wzyx
	
	
	public Chunk(World wor, int a, int b, int c, int d)
	{
		world = wor;
		x = a;
		y = b;
		z = c;
		w = d;
		
		data = new Block[8][8][8][8];
		toRender = new ArrayList<Block>();
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
		toRender = new ArrayList<Block>();
		if (!needsUpdate && !world.player.isInView(this))
			return;
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
							toRender.add(data[i][j][k][l]);
						}
					}
				}
			}
		}
		Collections.sort(toRender);
		makeSideSoup();
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
	
	
	
	public void makeSideSoup()
	{
		sides = new ArrayList<BlockSide>();
		/*
		make a side soup
		right = new ArrayList<BlockSide>();
		left  = new ArrayList<BlockSide>();
		up    = new ArrayList<BlockSide>();
		down  = new ArrayList<BlockSide>();
		front = new ArrayList<BlockSide>();
		back  = new ArrayList<BlockSide>();
		*/
		
		//add sides to the list if they are visible
		for (Block e : toRender)
		{
			if ((e.sides & 1) == 0)
				sides.add(new BlockSide(e, (byte)0));
			if ((e.sides & 2) == 0)
				sides.add(new BlockSide(e, (byte)1));
			if ((e.sides & 4) == 0)
				sides.add(new BlockSide(e, (byte)2));
			if ((e.sides & 8) == 0)
				sides.add(new BlockSide(e, (byte)3));
			if ((e.sides & 16) == 0)
				sides.add(new BlockSide(e, (byte)4));
			if ((e.sides & 32) == 0)
				sides.add(new BlockSide(e, (byte)5));
			
			/*
			if ((e.sides & 1) == 0)
				right.add(new BlockSide(e, (byte)0));
			if ((e.sides & 2) == 0)
				left.add(new BlockSide(e, (byte)1));
			if ((e.sides & 4) == 0)
				up.add(new BlockSide(e, (byte)2));
			if ((e.sides & 8) == 0)
				down.add(new BlockSide(e, (byte)3));
			if ((e.sides & 16) == 0)
				front.add(new BlockSide(e, (byte)4));
			if ((e.sides & 32) == 0)
				back.add(new BlockSide(e, (byte)5));
				*/
		}
		
		Collections.sort(sides);
		/*
		Collections.sort(right);
		Collections.sort(left);
		Collections.sort(up);
		Collections.sort(down);
		Collections.sort(front);
		Collections.sort(back);
		*/
	}
	
}

class BlockSide implements Comparable
{
	Block parent;
	byte value = -1;
	//temporary
	String id;
	//0-7: +x, -x, +y, -y, +z, -z, (+w, -w)
	
	public BlockSide(Block p, byte v)
	{
		parent = p;
		value = v;
		
		//temporary
		id = parent.getPosition().toString()+" "+v;
	}

	public int compareTo(Object o) 
	{
		if (!(o instanceof BlockSide))
			return 0;
		
		//player.pos.x -= player.movedamp * player.speed * delta * Math.cos(player.pitch) * Math.sin(player.yaw);
		//player.pos.y += player.movedamp * player.speed * delta * Math.sin(player.pitch);
		//player.pos.z -= player.movedamp * player.speed * delta * Math.cos(player.pitch) * Math.cos(player.yaw);
		
		//distance = -cos(pitch)*sin(yaw)*dx + sin(pitch)*dy + cos(pitch)*cos(yaw)*dz
		Point4D point = parent.getPosition().add(getOffset());
		Point4D other = ((BlockSide)o).parent.getPosition().add( ((BlockSide)o).getOffset() );
		
		
		//double distance = -Math.cos(player.pitch)*Math.sin(player.yaw)*(point.x - player.pos.x) + Math.sin(player.pitch)*(point.y - player.pos.y) + Math.cos(player.pitch)*Math.cos(player.yaw)*(point.z - player.pos.z)						
		Player player = parent.chunk.world.player; //reference
		Point4D playerpos = player.pos; //this needs to be without w or w will screw things up here
		
		double dista = -Math.cos(player.pitch)*Math.sin(player.yaw)*(point.x - player.pos.x) + Math.sin(player.pitch)*(point.y - player.pos.y) - Math.cos(player.pitch)*Math.cos(player.yaw)*(point.z - player.pos.z);						
		double distb = -Math.cos(player.pitch)*Math.sin(player.yaw)*(other.x - player.pos.x) + Math.sin(player.pitch)*(other.y - player.pos.y) - Math.cos(player.pitch)*Math.cos(player.yaw)*(other.z - player.pos.z);						

		//double dista = parent.getPosition().add(getOffset()).dist(new Point4D(playerpos.x, playerpos.y, playerpos.z, 0));
		//double distb = ((BlockSide)o).parent.getPosition().add(getOffset()).dist(new Point4D(playerpos.x, playerpos.y, playerpos.z, 0));
		
		//this large number makes the distinction higher
		//the negative sorts in reverse order
		return (int)(-1000*1000*(dista - distb));
		
	}
	
	public Point4D getOffset()
	{
		Point4D res = new Point4D(0,0,0,0);
		
		if (value == 0)
			res.x = 0.5;
		else if (value == 1)
			res.x = -0.5;
		else if (value == 2)
			res.y = 0.5;
		else if (value == 3)
			res.y = -0.5;
		else if (value == 4)
			res.z = 0.5;
		else if (value == 5)
			res.z = -0.5;
		
		//this added to make sure that the sides still render in the right place. It's messy, but necessary.
		res.w = -parent.getPosition().w;
		
		return res;
	}
}