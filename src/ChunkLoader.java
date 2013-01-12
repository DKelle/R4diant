import java.util.ArrayList;
import java.util.Random;

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
		//loaded.add(new Point4D(0,0,0,0));
		//Chunk c = new Chunk(world, 0,0,0,0);
		//this is redundant, but I don't think it can be helped 
		//chunk.addBlock() could work, but in the future, different block types...
		//Eh, we need a function to look up a block type by ID. Hmm.
		
		//Easy way:
		//genChunks(1,1,1,1,122);
		
		/* Not sure:
		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 1; j++)
			{
				for (int k = 0; k < 2; k++)
				{
					for (int l = 0; l < 2; l++)
					{
						add(i, j, k, l);
					}
				}
			}
		}
		*/
		
		///* Simple debugger:
		loaded.add(new Point4D(0,0,0,0));
		Chunk c = new Chunk(world, 0,0,0,0);
		c.data[0][0][0][0] = new Block(c, (short)0_0000);
		//c.data[1][0][0][0] = new Block(c, (short)0_0001);
		//c.data[0][1][0][0] = new Block(c, (short)0_0010);
		//c.data[1][1][0][0] = new Block(c, (short)0_0011);
		//c.data[0][0][1][0] = new Block(c, (short)0_0100);
		//c.data[1][0][1][0] = new Block(c, (short)0_0101);
		world.loaded.add(c);
		//*/
		
		/* Ian's thing:
		addChunk(0,0,0,0);
		addChunk(1,0,0,0);
		addChunk(1,0,-1,0);
		addChunk(0,0,-1,0);
		addChunk(1,0,-2,0);
		addChunk(0,0,-2,0);
		addChunk(2,0,0,0);
		addChunk(2,0,-1,0);
		addChunk(2,0,-2,0);
		addChunk(0,0,1,0);
		//c.data[0][0][0][0] = new Block(c, (short)0_0000);
		//c.data[1][0][0][0] = new Block(c, (short)0_0001);
		//c.data[0][1][0][0] = new Block(c, (short)0_0010);
		
		//world.loaded.add(c);
		*/
	}
	
	public void add(int x, int y, int z, int w)
	{
		loaded.add(new Point4D(x,y,z,w));
		Chunk c = new Chunk(world, x,y,z,w);
		
		
		for (int i = 0; i < 8; i++)
		{
			for (int k = 0; k < 8; k++)
			{
				int j = (int)(Math.random()*0);
				int l = (int)(Math.random()*1);
				c.data[i][j][k][l] = new Block(c, (short)(512*l+64*k+8*j+i)); 
			}
		}
		
		world.loaded.add(c);
	}
	
	public void genChunks(int xs, int ys, int zs, int ws, long seed)
	{
		Random r = new Random(seed);
		int hills = (int)(r.nextInt(4*xs + 4*ys + 4*zs + 4*ws));
		
		double[] xspr = new double[hills];
		double[] yspr = new double[hills];
		double[] zspr = new double[hills];
		double[] wspr = new double[hills];
		double[] xoff = new double[hills];
		double[] zoff = new double[hills];
		double[] woff = new double[hills];
		
		for (int i = 0; i < hills; i++)
		{
			xspr[i] = 2*xs*r.nextDouble();
			yspr[i] = 8*ys*r.nextDouble();
			zspr[i] = 2*zs*r.nextDouble();
			wspr[i] = 2*ws*r.nextDouble();
			xoff[i] = 8*xs*r.nextDouble();
			zoff[i] = 8*zs*r.nextDouble();
			woff[i] = 8*ws*r.nextDouble();
		}
		
		int[][][] heightmap = new int[8*xs][8*zs][8*ws];
		
		for (int i = 0; i < 8*xs; i++)
		{
			for (int k = 0; k < 8*zs; k++)
			{
				for (int l = 0; l < 8*ws; l++)
				{
					double val = 0;
					
					// ys e^(-xs(x-xo)^2 - zs(z-zo)^2 - ws(w-wo)^2)
					for (int h = 0; h < hills; h++)
					{
						val += yspr[h]*Math.exp(-xspr[h]*Math.pow(i-xoff[h], 2) - zspr[h]*Math.pow(k-zoff[h], 2) - wspr[h]*Math.pow(l-woff[h], 2));
					}
					
					if (val > 8)
						val = 8; //I hope I don't need this >.>
					
					heightmap[i][k][l] = (int)(val+0.5);
				}
			}
		}
		
		Chunk[][][] chunks = new Chunk[xs][zs][ws];
		
		for (int i = 0; i < xs; i++)
		{
			for (int k = 0; k < zs; k++)
			{
				for (int l = 0; l < ws; l++)
				{
					loaded.add(new Point4D(i, 0, k, l));
					chunks[i][k][l] = new Chunk(world, i, 0, k, l);
				}
			}
		}
		
		for (int x = 0; x < heightmap.length; x++)
		{
			for (int z = 0; z < heightmap[0].length; z++)
			{
				for (int w = 0; w < heightmap[0][0].length; w++)
				{
					for (int i = heightmap[x][z][w]; i >= 0; i--)
					{
						chunks[x/8][z/8][w/8].data[x%8][i%8][z%8][w%8] = new Block(chunks[x/8][z/8][w/8], (short)( (x%8) +  8*(i%8) + 64*(z%8) +  512*(w%8) ));
					}
				}
			}
		}
		
		for (int i = 0; i < chunks.length; i++)
		{
			for (int k = 0; k < chunks[0].length; k++)
			{
				for (int l = 0; l < chunks[0][0].length; l++)
				{
					world.loaded.add(chunks[i][k][l]);
				}
			}
		}
		
		//THAT WAS A LOT OF WORK
	}
	
	
	
	
	short magnitude = 3;
	float x_spread = -.25f;
	float z_spread = -.25f;
	float w_spread = -.25f;
	float x_shift = 3.5f;
	float z_shift = 3.5f;
	float w_shift = 3.5f;
	public void addChunk( int a, int b, int c, int d )
	{		
		loaded.add(new Point4D(a,b,c,d));
		Chunk ch = new Chunk(world,a,b,c,d);
		for( int w = 0; w < 1; w++)
		{
			for (int x = 0; x < 8; x++)
			{
				for (int z = 0; z < 8; z++)
				{
					int y = generateY(a,b,c,d,x,z,w);
					for ( int i = 0; i < y; i++ )
					{
						ch.data[x][i][z][w] = new Block(ch, (short)((512*w)+(64*z)+(8*i)+x));
					}
				}
			}
		}
		world.loaded.add(ch);
	}
	public int generateY(int a, int b, int c, int d, int x, int z, int w )
	{
		
		return (int)(magnitude*Math.exp((x_spread*Math.pow((x-x_shift),2) 
				+ z_spread*Math.pow((z-z_shift),2) + w_spread*Math.pow((w-w_shift),2)))) + 1;
	}
	//this is a nice idea, but doesn't really work that well.
	short constant = 1;
	public void modifyEquation(int a, int b, int c, int d)
	{
		Point4D[] around = new Point4D[4];
		int[] ind = new int[4];
		//not going to worry about this yet
		Point4D above = new Point4D();
		int ind_above = -1;
		Point4D below = new Point4D();
		int ind_below = -1;
		for( int i = 0; i < loaded.size(); i++)
		{
			if( (int)loaded.get(i).x == a-1 )
			{
				around[0] = loaded.get(i);
				ind[0] = i;
			}
			if( (int)loaded.get(i).x == a+1 )
			{
				around[2] = loaded.get(i);
				ind[2] = i;
			}
			if( (int)loaded.get(i).z == c-1 )
			{
				around[1] = loaded.get(i);
				ind[1] = i;
			}
			if( (int)loaded.get(i).z == c+1 )
			{
				around[3] = loaded.get(i);
				ind[3] = i;
			}
			if( (int)loaded.get(i).y == b+1 )
			{
				above = loaded.get(i);
				ind_above = i;
			}
			if( (int)loaded.get(i).y == b-1 )
			{
				below = loaded.get(i);
				ind_below = i;
			}
		}
		// error checking if no chunks surrounding
		boolean[] skip = new boolean[4];
		for ( int i = 0; i < 4; i++ )
		{
			if ( around[i] == null )
				skip[i] = true;
		}
		int[][] map = new int[4][8];
		for( int i = 0; i < 4; i++ )
		{
			int temp_a = 0;
			int temp_c = 0;
			if ( i == 0 )
			{
				temp_a = 7;
			}
			else if ( i == 1 )
			{
				temp_c = 7;
			}
			else if ( i == 2 )
			{
				temp_a = 0;
			}
			else if ( i == 3 )
			{
				temp_c = 0;
			}
			int just_in_case = (int)(Math.random()*5);
			for( int j = 0; j < 8; j++ )
			{
				int highest = -1;
				//random numbers if no chunk
				if ( skip[i] )
				{
					highest = just_in_case;
				}
				//nonrandom numbers if chunk
				else
				{
					for ( int k = 0; k < 8; k++ )
					{
						try
						{
							Block test = new Block();
							if ( i == 0 || i == 2 )
								test = world.loaded.get(ind[i]).data[temp_a][k][j][d];
							if ( i == 1 || i == 3 )
								test = world.loaded.get(ind[i]).data[j][k][temp_c][d];
						}
						catch(Exception e)
						{
							highest = k-1;
						}
					}
				}
				map[i][j] = highest;
			}
		}
		//the actual algorithm 
		// 0 is left, 2 is right, 1 is back, 3 is forward
		int highest = -1;
		int highest_side = -1;
		int lowest = 9;
		for ( int i = 0; i < 4; i++ )
		{
			for( int j = 0; j < 8; j++ )
			{
				if ( map[i][j] > highest )
				{
					highest = map[i][j];
					highest_side = i;
				}
				if ( map[i][j] < lowest )
				{
					lowest = map[i][j];
				}
			}
		}
		int high_high = -1;
		int high_high_pos = 0;
		int high_low = 8;
		for ( int i = 0; i < 8; i++ )
		{
			if ( map[highest_side][i] > high_high )
			{
				high_high = map[highest_side][i];
				high_high_pos = i;
			}
			if ( map[highest_side][i] < high_low  )
				high_low  = map[highest_side][i];
		}
		//constant done
		constant = (short)(highest-lowest);
		//magnitude done
		magnitude = (short)(highest-constant+2);
		//shift and spread
		if ( highest_side == 0 || highest_side == 2 )
		{
			x_shift = (float)(Math.random()*2 + 4);
			z_shift = (float)Math.abs(high_high_pos + Math.random()*2 - Math.random()*2);
			x_spread = -(float)(high_high/(high_low+1f) * .25); 
			z_spread = -(float)(highest/(lowest+2f) *.25);
			System.out.println("derp");
		}
		else
		{
			x_shift = (float)Math.abs(high_high_pos + Math.random()*2 - Math.random()*2);
			z_shift = (float)(Math.random()*2 + 5);
			x_spread = -(float)(highest/(lowest+2f) *.25);
			z_spread = -(float)(high_high/(high_low+1f) * .25);
			System.out.println("derp");
		}
	}
}