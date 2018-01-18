/**
 * @(#)Text1.java
 *		GOAL: Efficiently find the intersection points of an arbitrary number of lines
 *				Display this graphically
 *		GOAL2: Find the intersection points of objects moving along said lines at arbitrary speeds
 * @author 
 * @version 1.00 2018/1/17
 */
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
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
			Graphics2D g2d = (Graphics2D) g; 
			
			makeLines();
			
			Set keys = lines.keySet();
		
			if(!keys.isEmpty())
			{
				int count = 0;	
				while (count < keys.size() && keys.contains(count))
				{
					Line lin = lines.get(count);
					int xEnd, yEnd,x,y = 0;
					x = (int)lin.getX0();
					y = (int)lin.getY0();
					xEnd = x + (int)(Math.sqrt(2)*800*Math.cos(lin.getHdg()));
					yEnd = y + (int)(Math.sqrt(2)*800*Math.sin(lin.getHdg()));
					
					g.setColor(Color.BLACK);
					g.drawLine(x,y, xEnd, yEnd);
							
					count++;
				}
			}
		}

	private void makeLines()
	{
		int n = (int)(Math.random()*30)+5;
		
		for(int i=0; i<n; i++)
		{
			lines.put(i,new Line());
		}
	}	

	private class Line
	{
		private double y0, x0, hdg;
		private int spd;
		
		public Line()
		{
			setXY();
			hdg = Math.random()*Math.PI*2;
			spd = (int)(Math.random()*4+1.0);
		//	System.out.println(hdg+","+spd+"; ");
		}
		public Line(double y0, double x0, double hdg, int spd)
		{
			this.y0 = y0;
			this.x0 = x0;
			this.hdg = hdg;
			this.spd = spd;
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
		public double getSpd()
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