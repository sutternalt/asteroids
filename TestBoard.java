/**
 * @(#)Text1.java
 *		GOAL: Efficiently find the intersection points of an arbitrary number of lines by checking if each entity will
 *			collide with each other entity, excepting entities that have already been checked.
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
//import javax.vecmath.Point3d;

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
					
					g.setColor(new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255)));
					//g.setColor(Color.BLACK);
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
			while (count <= keys.size() && keys.contains(count)) //for each line in lines
			{
				Line lin = lines.get(count);
				int x1 = (int)lin.getX0();
				int y1 = (int)lin.getY0();
				int x2 = x1 + (int)(Math.sqrt(2)*100*Math.cos(lin.getHdg()));
				int y2 = y1 + (int)(Math.sqrt(2)*100*Math.sin(lin.getHdg())); //x1,y1,x2,y2 are four points on lin
				int spd1 = lin.getSpd();
				for(int i = count; keys.contains(i); i++) //for every other unchecked line in lines
				{
					Line lin2 = lines.get(i);
					int spd2 = lin2.getSpd();
					
					if(areCoplanar(lin,lin2)) //if in the same plane in xyt-space: aka if scalar triple product of four 3points == 0
					{
						//transform lines to two lines from xyt space to two lines on coplanar plane space
						//construct basis matrix: {N,lin1,lin1 X N}
						double[] v1,v2,v3;
						v1 = getCoplanarNormal(lin,lin2);
						v2 = new double[] {x2-x1,y2-y1,100}; //this does not account for actual speed yet, and just assumes that you've gone x2-x1 in 100ms
						double norm = Math.sqrt(Math.pow(v2[0],2)+Math.pow(v2[1],2)+Math.pow(v2[2],2));
						v2 = new double[] {v2[0]/norm,v2[1]/norm,v2[2]/norm};
						v3 = new double[] {v1[1]*v2[2]-v1[2]*v2[2],v1[2]*v2[0]-v1[0]*v2[2],v1[0]*v2[1]-v1[1]*v2[0]}; //v1 X v2
						norm = Math.sqrt(Math.pow(v3[0],2)+Math.pow(v3[1],2)+Math.pow(v3[2],2));
						v3 = new double[] {v3[0]/norm,v3[1]/norm,v3[2]/norm};
																	
						double[][] coplanarBasis = {v1,v2,v3};
						
						
						//getCoPlane from (p2-p1) X (p3-p1) <- vector orthoganal to coplanar plane
						
						//start looking for collisions
						int x3 = (int)lin2.getX0();
						int y3 = (int)lin2.getY0();
						int x4 = x3 + (int)(Math.sqrt(2)*100*Math.cos(lin2.getHdg()));
						int y4 = y3 + (int)(Math.sqrt(2)*100*Math.sin(lin2.getHdg())); //x3,y3,x4,y4 are four points on lin2
						
						int y; //collision point
						int x;
						double num;
						double den;
						//This is complicated things to do with determinants; see https://en.wikipedia.org/wiki/Line-line_intersection
						den = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
						if (den!=0) //when den. == 0, lines are parallel or coincident
						{
							num = (x1*y2-y1*x2)*(x3-x4) - (x1-x2)*(x3*y4-y3*x4);
							x = (int)(num/den);
							
							num = (x1*y2-y1*x2)*(y3-y4) - (y1-y2)*(x3*y4-y3*x4);
							y = (int)(num/den);
							
							if((x>=0 && x <=800) && (y>=0 && y<=800)) //check if point on coincident plane is outside xy space bounds
							{
								//transform point from point on coincident plane to xyt point
								
								//add xyt point to retVal
								retVal.add(new Point(x,y));					
							}
						}
						else //how to deal with coincident case
						{
							
						}
					}
				}				
				count++;
			}
		}
		return retVal;
	}
	
	private double[] getCoplanarNormal(Line lin1, Line lin2)
	{
		double[] p1 = lin1.get3Point(); //xyt points
		double[] p2 = lin1.get3Point(100);
		double[] p3 = lin2.get3Point();
		double[] p4 = lin2.get3Point(100);
		
		//scalar triple product == 0 between four 3points => coplanar
		double[] p21 = {p2[0]-p1[0],p2[1]-p1[1],p2[2]-p1[2]};
		double[] p31 = {p3[0]-p1[0],p3[1]-p1[1],p3[2]-p1[2]};
		double[] p41 = {p4[0]-p1[0],p4[1]-p1[1],p4[2]-p1[2]};
		
		//{(x2-x1)X(x4-x1)} = (p21 X p41)
		double[] cross = {(p21[1]*p41[2]-p21[2]*p41[1]), (p21[2]*p41[0]-p21[0]*p41[2]), (p21[0]*p41[1]-p21[1]*p41[0])};
		double crossNorm = Math.sqrt(Math.pow(cross[0],2)+Math.pow(cross[1],2)+Math.pow(cross[2],2));
		for(int i=0; i<3; i++)
		{
			cross[i] = cross[i]/crossNorm;
		}
		return cross;
	}
	private boolean areCoplanar(Line lin1, Line lin2)
	{
		double[] p1 = lin1.get3Point(); //xyt points
		double[] p2 = lin1.get3Point(100);
		double[] p3 = lin2.get3Point();
		double[] p4 = lin2.get3Point(100);
		
		//scalar triple product == 0 between four 3points => coplanar
		double[] p21 = {p2[0]-p1[0],p2[1]-p1[1],p2[2]-p1[2]};
		double[] p31 = {p3[0]-p1[0],p3[1]-p1[1],p3[2]-p1[2]};
		double[] p41 = {p4[0]-p1[0],p4[1]-p1[1],p4[2]-p1[2]};
		
		//{(x2-x1)X(x4-x1)}*(x3-x1) = (p21 X p41)*p31
		double stp = (p21[1]*p41[2]-p21[2]*p41[1])*p31[0] + (p21[2]*p41[0]-p21[0]*p41[2])*p31[1] + (p21[0]*p41[1]-p21[1]*p41[0])*p31[2];
		
		if(stp == 0)
		{
			return true;
		}
		else
		{
			return false;
		}		
	} 
	
	private boolean intersectPossible(Line lin1, Line lin2) //tests to see if intersections are possible based on x0y0 and hdg; filters out coincident case
	{
		double hdg1 = lin1.getHdg();
		double hdg2 = lin2.getHdg();
		double hdgAbs;
		
		//find absolute hdg of line between x0y0's of each Line
		if(lin1.getX0()!=lin2.getX0() && lin1.getY0()!=lin2.getX0()) //not n*PI/2
		{
			double x0,y0,x1,y1; //reference points for line between x0y0's of each Line
			if(lin1.getX0()<lin2.getY0() && lin1.getY0()<lin2.getY0()) //if lin1 is above and left of lin2
			{
				x0=lin1.getX0();
				y0=lin1.getY0();
				x1=lin2.getX0();
				y1=lin2.getY0();
			}
			else
			{
				x1=lin1.getX0();
				y1=lin1.getY0();
				x0=lin2.getX0();
				y0=lin2.getY0();
			}
			
			hdgAbs = Math.atan((y1-y0)/(x1-x0));
			
			//Account for atan only giving you an answer in quadrant I or -quad. I (aka quad. IV)
			if(hdgAbs < 3*Math.PI/2 && hdgAbs > Math.PI/2) //quad 2 or 3
			{
				hdgAbs += Math.PI;
			}			
			else if(hdgAbs < 0) //quad "4"
			{
				hdgAbs += 2*Math.PI;
			}			
		}
		else if(lin1.getX0()==lin2.getX0())
		{
			if(lin1.getY0()<lin2.getY0())
			{
				hdgAbs = Math.PI/2;
			}
			else
			{
				hdgAbs = Math.PI*3/2;
			}
		}
		else
		{
			if(lin1.getX0()<lin2.getX0())
			{
				hdgAbs = 0;
			}
			else
			{
				hdgAbs = Math.PI;
			}
		}
		
		//adjust 0-PI axis to be along the line between x0y0's of each Line
		hdg1 -= hdgAbs;
		hdg2 -= hdgAbs;
		
		//return true if lin1.hdg is in Q1 and lin2.hdg is in Q2, or lin1 in Q4 and lin2 in Q3; lin1
		//guaranteed to be above and left of lin2, or in adjusted coord, left of lin2
		if(((hdg1<Math.PI/2 && hdg1>=0)&&(hdg2<=Math.PI&&hdg2>Math.PI/2)) || (((hdg1<=Math.PI*2||hdg1==0.0) && hdg1>Math.PI*3/2)&&(hdg2<Math.PI*3/2&&hdg2>=Math.PI)))
		{
			return true;
		}
		else
		{
			return false;
		}
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
		private int spawnTime; //in ms
		private double tickspeed;
		
		public Line()
		{
			setXY();
			hdg = Math.random()*Math.PI*2;
			spd = (int)(Math.random()*4+1.0);
			sz = (int)(Math.random()*2+1.0);
			spawnTime = 0;
			tickspeed = 1.0;
		//	System.out.println(hdg+","+spd+"; ");
		}
		public Line(double x0, double y0, double hdg, int spd)
		{
			this.y0 = y0;
			this.x0 = x0;
			this.hdg = hdg;
			this.spd = spd;
			spawnTime = 0;
			tickspeed =1.0;
		}
		public double getTickspeed()
		{
			return tickspeed;
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
		public int getSpawnTime() //returns the spawntime in ms
		{
			return spawnTime;
		}
		public double[] get3Point()
		{
			double[] retVal = new double[3];
			retVal[0] = x0;
			retVal[1] = y0;
			retVal[2] = (double)spawnTime;
			return retVal;
		}
		public double[] get3Point(int t) //where t is time in ms
		{
			double[] retVal = new double[3];
			retVal[0] = x0+Math.cos(hdg)*t*spd;
			retVal[1] = y0+Math.sin(hdg)*t*spd;
			retVal[2] = (double)spawnTime+t;
			return retVal;
		}
		
		private boolean setXY() ///get random edge location///
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
	}
}