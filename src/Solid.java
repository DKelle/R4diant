import java.util.Collections;


public class Solid extends Block
{
	byte sides = 0;
	byte rot = 0;
	byte strain = 0;
	
	//final byte strainlimit
	//final byte boundhard
	//final byte toolslipdetach
	
	//format of position: w: 000 z: 000 y: 000 x: 000
	
	//check methods
	public void checkSides()
	{
		//check to see which sides are exposed to air
		//assuming sorted chunk
		if (!(chunk.findBlock((short)(pos-1)) instanceof Solid)) //x-
			sides += 1;
		if (!(chunk.findBlock((short)(pos+1)) instanceof Solid)) //x+
			sides += 2;
		if (!(chunk.findBlock((short)(pos-8)) instanceof Solid)) //y-
			sides += 4;
		if (!(chunk.findBlock((short)(pos+8)) instanceof Solid)) //y+
			sides += 8;
		if (!(chunk.findBlock((short)(pos-64)) instanceof Solid)) //z-
			sides += 16;
		if (!(chunk.findBlock((short)(pos+64)) instanceof Solid)) //z+
			sides += 32;
		if (!(chunk.findBlock((short)(pos-512)) instanceof Solid)) //w-
			sides += 64;
		if (!(chunk.findBlock((short)(pos+512)) instanceof Solid)) //w+
			sides += 128;
	}
}
