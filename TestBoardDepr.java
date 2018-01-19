/**
 * @(#)Text1.java
 *		GOAL: Efficiently find the intersection points of an arbitrary number of lines by seeing if a point is 
 *			contained in a hashset. This method is only more efficient for more than 1599 entities.
 *			O(n*w) where n = num entities and w = max(screenWidth,screenHeight). Checking if each entity will
 *			collide with each other entity, however, is O((n^2-n)/2): entity 1 checks against n-1 entities, 
 *			entity 2 checks against n-2 entities, and entity n checks against 0 entities. This is a triangle of
 *			height n and width n-1.
 *		GOAL2: Find the intersection points of objects moving along said lines at arbitrary speeds
 *		*Display this graphically
 * @author 
 * @version 1.00 2018/1/17
 */
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
//import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;

public class TestBoard extends JPanel
{
	HashMap<Integer,Line> lines = new HashMap<Integer,Line>();
	int x,y;
	
    public TestBoard() 
    {
    	setFocusable(true);
		setBackground(Color.WHITE);
		makeLines();
    }    
    
    @Override
    public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			//Graphics2D g2d = (Graphics2D) g; 
			
			makeLines();
			
			Set keys = lines.keySet();
		
			if(!keys.isEmpty())
			{
				int count = 0;	
				while (count <= keys.size() && keys.contains(count))
				{
					Line lin = lines.get(count);
					int xEnd, yEnd,x,y = 0;
					x = (int)lin.getX0();
					y = (int)lin.getY0();
					xEnd = x + (int)(Math.sqrt(2)*800*Math.cos(lin.getHdg()));
					yEnd = y + (int)(Math.sqrt(2)*800*Math.sin(lin.getHdg()));
					
					//g.setColor(new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255)));
					g.setColor(Color.BLACK);
					g.drawLine(x,y, xEnd, yEnd);
							
					count++;
				}
			}
			for(Point p : getCollisions())
			{
				g.setColor(Color.RED);
				int x = (int)p.getX();
				int y = (int)p.getY();
				g.drawOval(x-2,y-2,4,4);
				System.out.println(x+","+y);
			}
		}

	private LinkedList<Point> getCollisions()
	{
		LinkedList<Point> retVal = new LinkedList<Point>();
		HashSet<Point> points = new HashSet<Point>(8000);
		
		Set keys = lines.keySet();
		if(!keys.isEmpty())
		{
			int count = 0;	
			while (count <= keys.size() && keys.contains(count))
			{
				Line lin = lines.get(count);
				int x0 = (int)lin.getX0();
				int y0 = (int)lin.getY0();
				int x, y;
				
				for(int i = 1; i<800; i++) //i+=lin.getSpd()?
				{
					x = x0 + (int)(Math.sqrt(2)*i*Math.cos(lin.getHdg()));
					y = y0 + (int)(Math.sqrt(2)*i*Math.sin(lin.getHdg()));
					Point p = new Point(x,y);
					if(x>=0 && y >=0 && !points.add(p))
					{
						retVal.add(p);
					}
				}
				
				count++;
			}
		}
		return retVal;
	}
	
	private void makeLines()
	{
		int n = (int)(Math.random()*30)+5;
				
		for(int i=0; i<2*n; i++)
		{
			Line lin = new Line();
			Line linP = new Line(lin.getX0()+(int)(((double)lin.getSize()/2)*(Math.cos(lin.getHdg()+Math.PI/2))),lin.getY0()+(int)(((double)lin.getSize()/2)*(Math.sin(lin.getHdg()+Math.PI/2))),lin.getHdg(),lin.getSpd());
			Line linN = new Line(lin.getX0()-(int)(((double)lin.getSize()/2)*(Math.cos(lin.getHdg()+Math.PI/2))),lin.getY0()-(int)(((double)lin.getSize()/2)*(Math.sin(lin.getHdg()+Math.PI/2))),lin.getHdg(),lin.getSpd());
			lines.put(i,linP);
			i++;
			lines.put(i,linN);
		}
		System.out.println("Lines Size:"+lines.size());
	}	

	private class Line
	{
		private double y0, x0, hdg;
		private int spd;
		private int sz=3;
		
		public Line()
		{
			setXY();
			hdg = Math.random()*Math.PI*2;
			spd = (int)(Math.random()*4+1.0);
			sz = (int)(Math.random()*2+1.0);
		//	System.out.println(hdg+","+spd+"; ");
		}
		public Line(double x0, double y0, double hdg, int spd)
		{
			this.y0 = y0;
			this.x0 = x0;
			this.hdg = hdg;
			this.spd = spd;
		}
		public int getSize()
		{
			return sz*50;
		}
		public Rectangle getHitBox()
		{
			return new Rectangle((int)x0,(int)y0,sz*50,sz*50);
		}
		public double getX0()
		{
			return x0;
		}
		public double getY0()
		{
			return y0;
		}
		public double getHdg()
		{
			return hdg;	
		}
		public int getSpd()
		{
			return spd;
		}
		
		private boolean setXY() ///get random edge location///
	    {
	    	double hgt = getHeight();
	    	double wdt = getWidth();
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
	}
}