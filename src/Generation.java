import java.util.Random;

public class Generation 
{
	//holds all the generating things
	long seed = 0;
	Random r;
	BiomeMap biomemap;
	
	public Generation() 
	{
		r = new Random();
		seed = r.nextLong();
	}
}
