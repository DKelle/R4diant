

public class Point4D implements Comparable
{
	/** The x, y, z, and w points */
	double x = 0, y = 0, z = 0, w = 0;
	
	/**
	 * Constructs a point with four dimensions at the origin
	 */
	public Point4D() {}
	
	/**
	 * Constructs a new point with four dimensions (double)
	 * @param a - the x coordinate
	 * @param b - the y coordinate
	 * @param c - the z coordinate
	 * @param d - the w coordinate
	 */
	public Point4D(double a, double b, double c, double d)
	{
		x = a; y = b; z = c; w = d;
	}
	
	/**
	 * Constructs a new point with four dimensions (intger)
	 * @param a - the x coordinate
	 * @param b - the y coordinate
	 * @param c - the z coordinate
	 * @param d - the w coordinate
	 */
	public Point4D(int a, int b, int c, int d)
	{
		x = (double)a; y = (double)b; z = (double)c; w = (double)d;
	}
	
	/**
	 * Gets the angles that this point would look to face the other point
	 * @param p - the other point
	 * @return the angles from this point to the other (yaw, pitch, wane)
	 */
	public double[] angles(Point4D p)
	{
		if (p != null)
		{
			//angles:
			// xz, xzy, xzyw (yaw, pitch, and wane)
			// Math.atan2(p.z-z, p.x-x)
			// Math.atan2(p.y-y, Math.sqrt( Math.pow(p.x-x, 2) + Math.pow(p.z-z, 2) ))
			// Math.atan2(p.w-w, Math.sqrt( Math.pow(p.x-x, 2) + Math.pow(p.y-y, 2) + Math.pow(p.z-z, 2) ))
			double[] a = 
					{ 
						Math.atan2(x-p.x, z-p.z), 
						Math.atan2(p.y-y, Math.sqrt( Math.pow(p.x-x, 2) + Math.pow(p.z-z, 2) )), 
						Math.atan2(p.w-w, Math.sqrt( Math.pow(p.x-x, 2) + Math.pow(p.y-y, 2) + Math.pow(p.z-z, 2) ))
					};
			//System.out.println("dir: "+a[0]);
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
	
	/**
	 * Gets the point that is this one reflected about the origin
	 * @return the reflection of this point about the origin
	 */
	public Point4D negate()
	{
		return new Point4D(-x, -y, -z, -w);
	}
	
	/**
	 * Gets the vector from this point to the other
	 * @param p - the other point
	 * @return a point that represents the vector
	 */
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
	
	/**
	 * Gets the distance, in four dimensions, to the other point
	 * @param p - the other point
	 * @return the distance
	 */
	public double dist(Point4D p)
	{
		if (p != null)
		{
			//distance: sqrt( (x2-x1)^2 + (y2-y1)^2 + (z2-z1)^2 + (w2-w1)^2 )
			double d = Math.sqrt( Math.pow(p.x-x,2) + Math.pow(p.y-y,2) + Math.pow(p.z-z,2) + Math.pow(p.w-w,2) );
			//System.out.println("dist: "+d);
			return d;
		}	
		double alt = -1.0;
		return alt;
	}
	
	/**
	 * Gets the distance from the origin to this point
	 * @return the magnitude of this point
	 */
	public double dist()
	{
		double d = Math.sqrt( x*x + y*y + z*z + w*w );
		return d;
	}
	
	/**
	 * Reset this point to a new point
	 * @param p - the new point
	 */
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
	
	/**
	 * Add a point to this one
	 * @param p - the other point
	 * @return the result of adding the two points
	 */
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
	
	/**
	 * Subtracts the other point from this point
	 * @param p - the other point
	 * @return the result of subtracting the two points
	 */
	public Point4D sub(Point4D p)
	{
		Point4D res = new Point4D(this.x, this.y, this.z, this.w);
		if (p != null)
		{
			res.x -= p.x;
			res.y -= p.y;
			res.z -= p.z;
			res.w -= p.w;
		}	
		return res;
	}
	
	/**
	 * Multiplies this point by another point
	 * @param p - the other point
	 * @return the result of multiplying the two points
	 */
	public Point4D mul(Point4D p)
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
	
	/**
	 * Divides this point by another point
	 * @param p - the other point
	 * @return the result of dividing the two points
	 */
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
	
	/**
	 * Raises this point to a power
	 * @param amt - the power to raise it to
	 * @return the result of the operation
	 */
	public Point4D pow(double amt)
	{
		Point4D res = new Point4D(this.x, this.y, this.z, this.w);
		res.x = Math.pow(res.x, amt);
		res.y = Math.pow(res.y, amt);
		res.z = Math.pow(res.z, amt);
		res.w = Math.pow(res.w, amt);
		return res;
	}
	
	/**
	 * Converts this point to a string (x, y, z, w)
	 */
	public String toString()
	{	
		double tx = (x * 100)/100;
		double ty = (y * 100)/100;
		double tz = (z * 100)/100;
		double tw = (w * 100)/100;
		
		return "( "+tx+", "+ty+", "+tz+", "+tw+" )";
	}
	
	/**
	 * Compares this point to another point based on xyzw position
	 */
	public int compareTo(Object o) 
	{
		if ( w == ((Point4D)o).w )
		{
			if ( z == ((Point4D)o).z )
			{
				if ( y == ((Point4D)o).y )
				{
					if ( x == ((Point4D)o).x )
					{
						return 0;
					}
					return Double.compare(x, ((Point4D)o).x);
				}
				return Double.compare(y, ((Point4D)o).y);
			}
			return Double.compare(z, ((Point4D)o).z);
		}
		return Double.compare(w, ((Point4D)o).w);
	}
	
	
}
