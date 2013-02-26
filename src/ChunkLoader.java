import java.io.*;
import java.util.*;


//Loads the chunks based on the player's position
public class ChunkLoader
{
	World world;
	Player player;
	ArrayList<Point4D> loaded;
	Point4D old_pos;
	Point4D old_playerchunk;
	int load_dis;
	
	String path;
	
	Random r;
	int num_things;
	int[] magnitude;
	double[] x_spread;
	double[] z_spread;
	double[] w_spread;
	double[] x_shift;
	double[] z_shift;
	double[] w_shift;
	
	/**
	 * Constructs a new Chunk Loader which generates the chunks around the player
	 * @param w - the world reference
	 * @param p - the player reference
	 * @param seed - the seed for generation
	 */
	public ChunkLoader(World w, Player p, int seed)
	{
		r = new Random(seed);
		num_things = 1 + r.nextInt(5);
		magnitude = new int[num_things];
		x_spread = new double[num_things];
		z_spread = new double[num_things];
		w_spread = new double[num_things];
		x_shift = new double[num_things];
		z_shift = new double[num_things];
		w_shift = new double[num_things];
		for ( int i = 0; i < num_things; i++ )
		{
			magnitude[i] = 1+r.nextInt(10);
			x_spread[i] = -.2 + .2*r.nextDouble();
			z_spread[i] = -.2 + .2*r.nextDouble();
			w_spread[i] = -.2 + .2*r.nextDouble();
			x_shift[i] = 0; //-10 + 20*r.nextDouble();
			z_shift[i] = 0;//-10 + 10*r.nextDouble();
			w_shift[i] = 0;//-10 + 10*r.nextDouble();
		}
		
		world = w;
		load_dis = world.loaddistance;
		player = p;
		loaded = new ArrayList<Point4D>();
		try{
			path = new File("Runner.java").getCanonicalPath();
		}
		catch(Exception e )
		{
			System.out.println("wat");
		}
		path = path.replace("Runner.java", "");
		path = path.concat("chunk_data\\");
		File dir = new File(path);
		if (!dir.exists())
		{
			dir.mkdir();
		}		
		loadChunks();
	}
	
	/**
	 * Decides whether to load more chunks, and does so, based on the player's position.
	 */
	public void checkStatus()
	{
		//This is too much. You should be comparing their chunk. I'll change that.
		if( player.getChunkCoords().compareTo(old_playerchunk) != 0 || old_playerchunk == null )
		{
			loadChunks();
		}
	}
	
	private void loadChunks()
	{
		Point4D[][][][] need = new Point4D[load_dis][load_dis][load_dis][load_dis];
		for( int i = 0; i < load_dis; i++)
		{
			for ( int j = 0; j < load_dis; j++)
			{
				for ( int k = 0; k < load_dis; k++)
				{
					for ( int l = 0; l < load_dis; l++)
					{
						need[i][j][k][l] = new Point4D(i + (int)player.pos.x/8 - load_dis/2, j + (int)player.pos.y/8 - load_dis/2, k + (int)player.pos.z/8 - load_dis/2, l + (int)player.pos.w/8 - load_dis/2);
					}
				}
			}
		}
		
		Chunk[][][][] new_crap = new Chunk[load_dis][load_dis][load_dis][load_dis];
		//i'm using a new array so that i not only get the correct chunks, but that the chunks are in the correct position in the array
		if ( world.loaded.length != 0 )
		{
			for ( int i = 0; i < world.loaded.length; i++ )
			{
				for ( int j = 0; j < world.loaded.length; j++ )
				{
					for ( int k = 0; k < world.loaded.length; k++ )
					{
						for ( int l = 0; l < world.loaded.length; l++ )
						{
							for ( int a = 0; a < world.loaded.length; a++ )
							{
								for ( int b = 0; b < world.loaded.length; b++ )
								{
									for ( int c = 0; c < world.loaded.length; c++ )
									{
										for ( int d = 0; d < world.loaded.length; d++ )
										{
											//if we find one of the needed chunks already loaded, we take away that chunk from the need array
											if ( world.loaded[a][b][c][d] != null && need[i][j][k][l] != null
													&& world.loaded[a][b][c][d].x == need[i][j][k][l].x
													&& world.loaded[a][b][c][d].y == need[i][j][k][l].y
													&& world.loaded[a][b][c][d].z == need[i][j][k][l].z
													&& world.loaded[a][b][c][d].w == need[i][j][k][l].w)
											{
												need[i][j][k][l] = null;
												//then we put it in the correct place in the new array
												new_crap[i][j][k][l] = world.loaded[a][b][c][d];
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		world.loaded = new_crap;
		
		for( int i = 0; i < load_dis; i++)
		{
			for ( int j = 0; j < load_dis; j++ )
			{
				for ( int k = 0; k < load_dis; k++ )
				{
					for ( int l = 0; l < load_dis; l++)
					{
						getChunk(new Point4D(i + (int)player.pos.x/8 - load_dis/2, j + (int)player.pos.y/8 - load_dis/2, k + (int)player.pos.z/8 - load_dis/2, l + (int)player.pos.w/8 - load_dis/2), i, j, k, l);
					}
				}
			}
		}
		old_pos = new Point4D((int)player.pos.x,(int)player.pos.y,(int)player.pos.z,(int)player.pos.w);
		old_playerchunk = new Point4D((int)player.pos.x/8,(int)player.pos.y/8,(int)player.pos.z/8,(int)player.pos.w/8);
	}
	
	public void getChunk( Point4D p, int ii, int jj, int kk, int ll )
	{
		File get = new File(path+"chunk_"+(int)p.x+"_"+(int)p.y+"_"+(int)p.z+"_"+(int)p.w+".txt");
		if(!get.exists())
		{
			addChunk((int)p.x,(int)p.y,(int)p.z,(int)p.w,ii,jj,kk,ll);
		}
		try{
			Reader read = new BufferedReader(new InputStreamReader(new FileInputStream(get), "US-ASCII"));
			Chunk ch = new Chunk(world,(int)p.x,(int)p.y,(int)p.z,(int)p.w,ii,jj,kk,ll);
			for( int x = 0; x < 8; x++ )
			{
				for (int y = 0; y < 8; y++)
				{
					for ( int z = 0; z < 8; z++ )
					{
						for ( int w = 0; w < 8; w++ )
						{
							int id = (int)read.read();
							if (id > 0)
							{
								Block b = new Block(ch, (short)((512*w)+(64*z)+(8*y)+x));
								b.id = (short)id;
								ch.data[x][y][z][w] = b;
							}	
						}
					}
				}
			}
			world.loaded[ii][jj][kk][ll] = ch;
			loaded.add(p);
			read.close();
		}
		catch( Exception e)
		{
			System.out.println("wat");
		}
	}
	
	private void addChunk( int a, int b, int c, int d, int ii, int jj, int kk, int ll )
	{	
		Chunk ch = new Chunk(world,a,b,c,d,ii,jj,kk,ll);
		for( int w = 0; w < 8; w++)
		{
			for (int x = 0; x < 8; x++)
			{
				for (int z = 0; z < 8; z++)
				{
					int y = generateY(a,b,c,d,x,z,w);
					if ( y > 8  && jj + 1 < load_dis)
					{
						addChunk(a,b+1,c,d,ii,jj+1,kk,ll);
						y = 8;
					}
					for ( int i = 0; i < y; i++ )
					{
						ch.data[x][i][z][w] = new Block(ch, (short)((512*w)+(64*z)+(8*i)+x));
					}
				}
			}
		}
		File f = new File(path+"chunk_"+a+"_"+b+"_"+c+"_"+d+".txt");
		if(!f.exists())
		{
			try
			{
				FileWriter write = new FileWriter(f);
				write.write(ch.toString());
				write.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private int generateY(int a, int b, int c, int d, int x, int z, int w )
	{
		int y = 0;
		x = x+a*8;
		z = z+c*8;
		w = w+d*8;
		for ( int i = 0; i < num_things; i++)
		{
			y += (int)(magnitude[i]*Math.exp(
					  x_spread[i]*Math.pow((x-x_shift[i]),2) 
					+ z_spread[i]*Math.pow((z-z_shift[i]),2)
					+ w_spread[i]*Math.pow((w-w_shift[i]),2)
					));
		}
		y -= 8*b;
		return y+1;
		//return 1;
	}
}