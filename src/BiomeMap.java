//Generates and writes biomes

public class BiomeMap 
{
	long seed = 0;
	Generation gen;
	
	public BiomeMap(Generation g)
	{
		gen = g;
		gen.seed = seed;
	}
	
	//Uses voronoi diagrams to make biomes
}
