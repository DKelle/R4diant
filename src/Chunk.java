import java.nio.FloatBuffer;
import java.util.ArrayList;


//an 8x8x8x8 chunk
public class Chunk implements Comparable
{
	World world;
	int x = 0; //actual xyzw
	int y = 0;
	int z = 0;
	int w = 0;
	int ix, iy, iz, iw; //indices
	
	Block[][][][] data = new Block[8][8][8][8];
	ArrayList<Entity> entities;
	byte[][][] biome = new byte[8][8][8];
	Tree<ArrayList<BlockSide>> chunktree;
	int treesize = 0;
	
	//ArrayList<BlockSide> sides;
	
	boolean needsUpdate = true;
	//hint: this data will probably be accessed wzyx
	
	//x,y,z,w, then indexes in the loaded array
	public Chunk(World wor, int a, int b, int c, int d, int e, int f, int g, int h)
	{
		world = wor;
		x = a;
		y = b;
		z = c;
		w = d;
		ix = e;
		iy = f;
		iz = g;
		iw = h;
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
		//if (!world.player.isInView(this))
		//	return;
		
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
							
							if ((b.sides & 1) == 0)
							{
								addToTree(new BlockSide(b, (byte)0), null);
								//world.existing.add(new BlockSide(b, (byte)0));
							}
							if ((b.sides & 2) == 0)
							{
								addToTree(new BlockSide(b, (byte)1), null);
								//world.existing.add(new BlockSide(b, (byte)1));
							}
							if ((b.sides & 4) == 0)
							{
								addToTree(new BlockSide(b, (byte)2), null);
								//world.existing.add(new BlockSide(b, (byte)2));
							}
							if ((b.sides & 8) == 0)
							{
								addToTree(new BlockSide(b, (byte)3), null);
								//world.existing.add(new BlockSide(b, (byte)3));
							}
							if ((b.sides & 16) == 0)
							{
								addToTree(new BlockSide(b, (byte)4), null);
								//world.existing.add(new BlockSide(b, (byte)4));
							}
							if ((b.sides & 32) == 0)
							{
								addToTree(new BlockSide(b, (byte)5), null);
								//world.existing.add(new BlockSide(b, (byte)5));
							}
						}
					}
				}
			}
		}
		//Collections.sort(toRender);
		needsUpdate = false;
	}
	
	
	public void addToTree(BlockSide e, Tree<ArrayList<BlockSide>> tree)
	{
		if (tree == null)
			tree = chunktree;
		
		if (chunktree == null)
		{
			ArrayList<BlockSide> arr = new ArrayList<BlockSide>();
			arr.add(e);
			chunktree = new Tree<ArrayList<BlockSide>>(arr);
			tree = chunktree;
			treesize++;
			return;
		}
		
		if (tree.head.size() == 0)
		{
			return; //?
		}
		
		if (e.comparePlane(tree.head.get(0)) == 0)
		{
			for (int i = 0; i < tree.head.size(); i++)
			{
				if (e.getPosition().compareTo(tree.head.get(i).getPosition()) == 0)
				{
					return;
				}
			}
			
			tree.head.add(e);
			treesize++;
		}
		else if (e.comparePlane(tree.head.get(0)) < 0)
		{
			if (tree.leafs.size() > 0)
				addToTree(e, tree.leafs.get(0));
			ArrayList<BlockSide> arr = new ArrayList<BlockSide>();
			arr.add(e);
			tree.addLeaf(arr);
			treesize++;
		}
		else if (e.comparePlane(tree.head.get(0)) > 0)
		{
			if (tree.leafs.size() > 1)
				addToTree(e, tree.leafs.get(1));
			
			else if (tree.leafs.size() <= 1)
				tree.addLeaf(new ArrayList<BlockSide>());
			
			ArrayList<BlockSide> arr = new ArrayList<BlockSide>();
			arr.add(e);
			tree.addLeaf(arr);
			treesize++;
		}
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
	
	public void loadFromTree(Tree<ArrayList<BlockSide>> tree, FloatBuffer list)
	{
		//Base things on the player's position
		//If the object is in front of the player, go a level deeper
		//If the object is behind the player, ignore it
		//If the object is on the player, freak out
		
		//jank
		if (tree == null)
		{
			tree = chunktree;
		}
		
		if (chunktree == null)
		{
			//The chunk must be empty!
			return;
		}
		
		if (tree.leafs.size() == 0)
		{
			//render this object
			for (int i = 0; i < tree.head.size(); i++)
			{
				loadDataFromBlockFace(tree.head.get(i), list);
			}
			
		}
		else if (world.player.pos.compareTo(tree.head.get(0).getPosition()) < 0)
		{
			//the object is in front of the player (I think)
			//call this function on the objects behind it
			if (tree.leafs.size() >= 1)
			{
				loadFromTree(tree.leafs.get(0), list);
			}
			//render this object
			for (int i = 0; i < tree.head.size(); i++)
			{
				loadDataFromBlockFace(tree.head.get(i), list);
			}
			if (tree.leafs.size() >= 2)
			{
				loadFromTree(tree.leafs.get(1), list);
			}
			//?
		}
		else if (world.player.pos.compareTo(tree.head.get(0).getPosition()) > 0)
		{
			//the object is behind the player (I think)
			//call this function on the objects in front of it
			if (tree.leafs.size() >= 2)
			{
				loadFromTree(tree.leafs.get(1), list);
			}
			//render this object
			for (int i = 0; i < tree.head.size(); i++)
			{
				loadDataFromBlockFace(tree.head.get(i), list);
			}
			if (tree.leafs.size() >= 1)
			{
				loadFromTree(tree.leafs.get(0), list);
			}
			//?
		}
		else if (world.player.pos.compareTo(tree.head.get(0).getPosition()) == 0)
		{
			//freak out
		}
	}
	
	
	public String toString()
	{
		String str = "";
		for ( int x = 0; x < 8; x++ )
		{
			for ( int y = 0; y < 8; y++ )
			{
				for ( int z = 0; z < 8; z++ )
				{
					for( int w = 0; w < 8; w++ )
					{
						if ( data[x][y][z][w] == null )
							str = str.concat("0 ");
						else
							str = str.concat("1 ");
					}
				}
			}
		}
		return str;
	}
	
	//ALL WHITE ***** (textures actually look correct)
	public void loadDataFromBlockFace(BlockSide side, FloatBuffer list)
	{
		//loads data about the face into the buffer arrays
		Point4D p = side.parent.getPosition();
		float alpha = alphaFunction(p);
		
		float[] tc = side.parent.getTextureCoordinates(side.value);
		
		if (side.value == 2)
		{
			list.put(new Float(p.x+1));list.put(new Float(p.y+1));list.put(new Float(p.z));
			//normal here (x, y, z)
			list.put(new Float(1));list.put(new Float(1));list.put(new Float(1));list.put(new Float(alpha));
			list.put(tc[0]);list.put(tc[1]);
			list.put(new Float(p.x));list.put(new Float(p.y+1));list.put(new Float(p.z));
			list.put(new Float(1));list.put(new Float(1));list.put(new Float(1));list.put(new Float(alpha));
			list.put(tc[2]);list.put(tc[3]);
			list.put(new Float(p.x));list.put(new Float(p.y+1));list.put(new Float(p.z+1));
			list.put(new Float(1));list.put(new Float(1));list.put(new Float(1));list.put(new Float(alpha));
			list.put(tc[4]);list.put(tc[5]);	
			list.put(new Float(p.x+1));list.put(new Float(p.y+1));list.put(new Float(p.z+1));
			list.put(new Float(1));list.put(new Float(1));list.put(new Float(1));list.put(new Float(alpha));
			list.put(tc[6]);list.put(tc[7]);
		}
		
		if (side.value == 3)
		{
			list.put(new Float(p.x+1));list.put(new Float(p.y));list.put(new Float(p.z+1));
			list.put(new Float(1));list.put(new Float(1));list.put(new Float(1));list.put(new Float(alpha));
			list.put(tc[0]);list.put(tc[1]);
			list.put(new Float(p.x));list.put(new Float(p.y));list.put(new Float(p.z+1));
			list.put(new Float(1));list.put(new Float(1));list.put(new Float(1));list.put(new Float(alpha));
			list.put(tc[2]);list.put(tc[3]);
			list.put(new Float(p.x));list.put(new Float(p.y));list.put(new Float(p.z));
			list.put(new Float(1));list.put(new Float(1));list.put(new Float(1));list.put(new Float(alpha));
			list.put(tc[4]);list.put(tc[5]);
			list.put(new Float(p.x+1));list.put(new Float(p.y));list.put(new Float(p.z));
			list.put(new Float(1));list.put(new Float(1));list.put(new Float(1));list.put(new Float(alpha));
			list.put(tc[6]);list.put(tc[7]);
		}
		
		if (side.value == 4)
		{
			list.put(new Float(p.x+1));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[0]);
			list.put(tc[1]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[2]);
			list.put(tc[3]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[4]);
			list.put(tc[5]);
			
			
			list.put(new Float(p.x+1));
			list.put(new Float(p.y));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[6]);
			list.put(tc[7]);
		}
		
		if (side.value == 5)
		{
			list.put(new Float(p.x+1));
			list.put(new Float(p.y));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[0]);
			list.put(tc[1]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[2]);
			list.put(tc[3]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[4]);
			list.put(tc[5]);
			
			
			list.put(new Float(p.x+1));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[6]);
			list.put(tc[7]);
		}
		
		if (side.value == 1)
		{
			list.put(new Float(p.x));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[0]);
			list.put(tc[1]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[2]);
			list.put(tc[3]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[4]);
			list.put(tc[5]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[6]);
			list.put(tc[7]);
		}
		
		if (side.value == 0)
		{
			list.put(new Float(p.x+1));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[0]);
			list.put(tc[1]);
			
			
			list.put(new Float(p.x+1));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[2]);
			list.put(tc[3]);
			
			
			list.put(new Float(p.x+1));
			list.put(new Float(p.y));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[4]);
			list.put(tc[5]);
			
			
			list.put(new Float(p.x+1));
			list.put(new Float(p.y));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[6]);
			list.put(tc[7]);
		}
	}
	
	public float alphaFunction(Point4D p)
	{
		float alpha = 1.0f - (1.0f/world.player.wdepth)*(float)Math.abs(p.w - world.player.pos.w ); //VERY TEMPORARY ALPHA FUNCTION
		if (alpha < 0 )
			alpha = 0;
		if (alpha > 1)
			alpha = 1;
		
		return alpha;
	}
}
