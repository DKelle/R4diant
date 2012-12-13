import java.util.ArrayList;


//an 8x8x8x8 chunk
public class Chunk 
{
	World world;
	int x = 0;
	int y = 0;
	int z = 0;
	int w = 0;
	Block[][][][] data = new Block[8][8][8][8];
	ArrayList<Entity> entities;
	byte[][][] biome = new byte[8][8][8];
	//hint: this data will probably be accessed wzyx
	
	
	public Chunk(World wor, int a, int b, int c, int d)
	{
		world = wor;
		x = a;
		y = b;
		z = c;
		w = d;
	}
}


/*

{ { },
  { },
  { },
  { },
  { },







*/