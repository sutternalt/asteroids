package com.codingthroughthestuck;
import java.awt.*;

public class Testing
{
	/*public static void main(String[] args)
	{


	private static class Path
	{
		private  double y0, x0, hdg;
		private  int spd;
		private  int sz=3;
		private  int spawnTime; //in ms
		private  double tickspeed;

		public Path()
		{
			setXY();
			hdg = Math.random()*Math.PI*2;
			spd = (int)(Math.random()*4+1.0);
			sz = (int)(Math.random()*2+1.0);
			spawnTime = 0;
			tickspeed = 1.0;
			//	System.out.println(hdg+","+spd+"; ");
		}
		public Path(double x0, double y0, double hdg, int spd)
		{
			this.y0 = y0;
			this.x0 = x0;
			this.hdg = hdg;
			this.spd = spd;
			spawnTime = 0;
			tickspeed =1.0;
		}
		public  double getTickspeed()
		{
			return tickspeed;
		}
		public  int getSize()
		{
			return sz*50;
		}
		public Rectangle getHitBox()
		{
			return new Rectangle((int)x0,(int)y0,sz*50,sz*50);
		}
		public  double getX0()
		{
			return x0;
		}
		public  double getY0()
		{
			return y0;
		}
		public  double getHdg()
		{
			return hdg;
		}
		public  int getSpd()
		{
			return spd;
		}
		public  int getSpawnTime() //returns the spawntime in ms
		{
			return spawnTime;
		}
		public  double[] get3Point()//returns 3point of original spawn
		{
			double[] retVal = new double[3];
			retVal[0] = x0;
			retVal[1] = y0;
			retVal[2] = (double)spawnTime;
			return retVal;
		}
		public  double[] get3Point(int t) //where t is time in ms
		{
			double[] retVal = new double[3];
			retVal[0] = x0+cleanUp(Math.cos(hdg),3)*t*spd;
			retVal[1] = y0+cleanUp(Math.sin(hdg),3)*t*spd;
			retVal[2] = (double)spawnTime+t;
			return retVal;
		}

		private  boolean setXY() ///get random edge location///
		{
			double hgt = 800;
			double wdt = 800;
			if(Math.round(Math.random()) == 0) //select x to be constrained
			{
				y0 = Math.random()*hgt;
				if(Math.round(Math.random()) == 0)// x = 0
				{
					x0=0.0;
				}
				else//x = screenwidth
				{
					x0=wdt;
				}
			}
			else //or y to be constrained
			{
				x0 = Math.random()*hgt;
				if(Math.round(Math.random()) == 0)//y = 0
				{
					y0=0.0;
				}
				else//y = screenheight
				{
					y0=wdt;
				}
			}
			return true;
		}
	}*/
}
