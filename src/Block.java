import java.util.ArrayList;


public class Block implements Comparable
{
	Chunk chunk;
	short pos;
	short temp;
	short id;
	byte light; //If I need it?
	ArrayList<Byte> elements;
	ArrayList<Ore> ores;
	
	//final constants which only exist in code
	//final String name;
	//final short lowStateChange;
	//final short highStateChange;
	//final short lowState;
	//final short highState;
	//final byte density;
	//final byte lightStrength;
	
	
	
	public Block()
	{
		elements = new ArrayList<Byte>();
		ores = new ArrayList<Ore>();
	}
	
	public Block(Chunk ch, short position)
	{
		elements = new ArrayList<Byte>();
		ores = new ArrayList<Ore>();
		chunk = ch;
		pos = position;
	}
	
	public Block(Chunk ch, Point4D p)
	{
		elements = new ArrayList<Byte>();
		ores = new ArrayList<Ore>();
		chunk = ch;
		pos = (short)( ((short)p.x) | ((short)p.y << 3) | ((short)p.z << 6) | ((short)p.w << 9) );
	}
	
	public void getLight()
	{
		//calculate lighting (if I need it?)
	}
	
	public void addOre(Ore ore)
	{
		ores.add(ore);
	}
	
	//final access methods
	public String getName()
	{
		return "Block";
	}
	
	public short lowStateChange()
	{
		return 0;
	}
	
	public short highStateChange()
	{
		return (short)1000000;
	}
	
	public short lowState()
	{
		return 0;
	}
	
	public short highState()
	{
		return 0;
	}
	
	public byte density()
	{
		return 0;
	}
	
	public byte lightStrength()
	{
		return 0;
	}

	public int compareTo(Object o) 
	{
		//sort by xyzw position
		
		return pos;
	}
	
	public Point4D getPosition()
	{
		Point4D res = new Point4D(chunk.x*8+(pos & 7), chunk.y*8+((pos >> 3) & 7), chunk.z*8+((pos >> 6) & 7), chunk.w*8+((pos >> 9) & 7) );
		System.out.println(res);
		return res;
	}

}
