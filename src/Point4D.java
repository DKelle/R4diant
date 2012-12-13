
public class Point4D
{
	double x = 0, y = 0, z = 0, w = 0;
	
	public Point4D() {}
	public Point4D(double a, double b, double c, double d)
	{
		x = a; y = b; z = c; w = d;
	}
	public Point4D(int a, int b, int c, int d)
	{
		x = (double)a; y = (double)b; z = (double)c; w = (double)d;
	}
	
	public double[] angles(Point4D p)
	{
		if (p != null)
		{
			//angles:
			// xz, xzy, xzyw
			// Math.atan2(p.z-z, p.x-x)
			// Math.atan2(p.y-y, Math.sqrt( Math.pow(p.x-x, 2) + Math.pow(p.z-z, 2) ))
			// Math.atan2(p.w-w, Math.sqrt( Math.pow(p.x-x, 2) + Math.pow(p.y-y, 2) + Math.pow(p.z-z, 2) ))
			double[] a = 
					{ 
						Math.atan2(p.z-z, p.x-x), 
						Math.atan2(p.y-y, Math.sqrt( Math.pow(p.x-x, 2) + Math.pow(p.z-z, 2) )), 
						Math.atan2(p.w-w, Math.sqrt( Math.pow(p.x-x, 2) + Math.pow(p.y-y, 2) + Math.pow(p.z-z, 2) ))
					};
			return a;
		}	
		double[] alt = {0.0, 0.0, 0.0};
		return alt;
	}
	
	public double[] angles()
	{
		//angles:
		// xz, xzy, xzyw
		// Math.atan2(p.z-z, p.x-x)
		// Math.atan2(p.y-y, Math.sqrt( Math.pow(p.x-x, 2) + Math.pow(p.z-z, 2) ))
		// Math.atan2(p.w-w, Math.sqrt( Math.pow(p.x-x, 2) + Math.pow(p.y-y, 2) + Math.pow(p.z-z, 2) ))
		double[] a = 
				{ 
					Math.atan2(x, -z), 
					Math.atan2(y, Math.sqrt( x*x + z*z )), 
					Math.atan2(w, Math.sqrt( x*x + y*y + z*z ))
				};
		return a;
	}
	
	public Point4D negate()
	{
		return new Point4D(-x, -y, -z, -w);
	}
	
	public Point4D direction(Point4D p)
	{
		if (p != null)
		{
			//slope: (x2-x1, y2-y1, z2-z1, w2-w1)
			Point4D m = new Point4D( p.x-x, p.y-y, p.z-z, p.w-w );
			return m;
		}	
		return this;
	}
	
	public double dist(Point4D p)
	{
		if (p != null)
		{
			//distance: sqrt( (x2-x1)^2 + (y2-y1)^2 + (z2-z1)^2 + (w2-w1)^2 )
			double d = Math.sqrt( Math.pow(p.x-x,2) + Math.pow(p.y-y,2) + Math.pow(p.z-z,2) + Math.pow(p.w-w,2) );
			return d;
		}	
		double alt = -1.0;
		return alt;
	}
	
	public double dist()
	{
		double d = Math.sqrt( x*x + y*y + z*z + w*w );
		return d;
	}
	
	public void set(Point4D p)
	{
		if (p != null)
		{
			x = p.x;
			y = p.y;
			z = p.z;
			w = p.w;
		}
	}
	
	public Point4D add(Point4D p)
	{
		Point4D res = new Point4D(this.x, this.y, this.z, this.w);
		if (p != null)
		{
			res.x += p.x;
			res.y += p.y;
			res.z += p.z;
			res.w += p.w;
		}	
		return res;
	}
	
	public Point4D dot(Point4D p)
	{
		Point4D res = new Point4D(this.x, this.y, this.z, this.w);
		if (p != null)
		{
			res.x *= p.x;
			res.y *= p.y;
			res.z *= p.z;
			res.w *= p.w;
		}	
		return res;
	}
	
	public Point4D div(Point4D p)
	{
		Point4D res = new Point4D(this.x, this.y, this.z, this.w);
		if (p != null)
		{
			res.x /= p.x;
			res.y /= p.y;
			res.z /= p.z;
			res.w /= p.w;
		}	
		return res;
	}
	
	public Point4D pow(double amt)
	{
		Point4D res = new Point4D(this.x, this.y, this.z, this.w);
		res.x = Math.pow(res.x, amt);
		res.y = Math.pow(res.y, amt);
		res.z = Math.pow(res.z, amt);
		res.w = Math.pow(res.w, amt);
		return res;
	}
	
	public String toString()
	{	
		double tx = (x * 100)/100;
		double ty = (y * 100)/100;
		double tz = (z * 100)/100;
		double tw = (w * 100)/100;
		
		return "( "+tx+", "+ty+", "+tz+", "+tw+" )";
	}
}
