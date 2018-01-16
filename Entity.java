package majoras.asteroids;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.util.Arrays;

public abstract class Entity //something on the screen that moves, collides, and wraps from one part of the screen to the other
{
	private double x; //location
	private double y;
	private double spd; //polar speed
	private double hdg; //polar heading; CW positive values only
	private double orn; //orientation in rad; CW positive values only
	private double dO; //change in orientation
	protected Image image; //associated image
	private boolean dead = false;
	protected int entRKey = 0; //the key for this entity from Board's EntRegister
	private Board bd;
	
	public Entity(Board b) //defaults to black sqaure
	{
		ImageIcon ii = new ImageIcon("square.png");

		x = 40;
		y = 60;
		spd = 0;
		hdg = 0;
		orn = 0;
		dO = 0;
		image = ii.getImage();
		
		entRKey = b.askKey(); //register on the board's entityRegister; this is used for collision detection
		b.entRegister.put(entRKey,this);
//		System.out.println(b.entRegister.getObjects()+"\n");
		bd = b;
	}
	public Entity(Board b, String iconName)
	{
		this(b);
		
		ImageIcon ii = new ImageIcon(iconName+".png");

		image = ii.getImage();
	}
	public Entity(Board b, String iconName, double x, double y)
	{
		this(b, iconName);
		
		this.x = x;
		this.y = y;
	}
	public Entity(Board b, String iconName, double x, double y, double spd, double hdg)
	{
		this(b, iconName,x,y);
		
		this.spd = spd;
		this.hdg = hdg;
		orn = getHdg();
	}
	public String toString()
	{
		return "entity";
	}
	
	public abstract void collide(Board b); //what happens when you've collided
	
	public void setBoard(Board b)
	{
		bd=b;
	}
	public Board getBoard()
	{
		return bd;
	}
    public double getX() 
    {
        return x;
    }
    public double getY() 
    {
        return y;
    }
    public double getHdg() //CW positive values only
    {
    	return hdg;
    }
    public double getSpd()
    {
    	return spd;
    }
    public double getDx() //discrete step in catesian pixels
    {
    	double dx = spd*Math.cos(hdg);
    	return dx; 
    }
    public double getDy()
    {
		double dy = spd*Math.sin(hdg);
    	return dy; 
    }
    public double getOrn()
    {
    	return orn;
    }
    public double getDO()
    {
    	return dO;
    }
    public Image getImage() 
    {
        return image;
    }
   	public boolean isDead()
	{
		return dead;			
	} 
		
	public void setDead(boolean k)
	{
		dead = k;
	}    
    public void setX(double x) 
    {
        this.x = x;
    }
    public void setY(double y) 
    {
        this.y = y;
    }
    public void setHdg(double hdg) //heading in rad, relative to CURRENT SPEED; CW positive values only
    {
    	this.hdg=hdg;
    	hdg = angleXForm(hdg);
    }
    public void setSpd(double spd) //relative to CURRENT HEADING
    {
    	this.spd=spd;
    }
    public void setVel(double spd, double hdg)
    {
    	this.spd=spd; 
    	this.hdg=hdg;
    	hdg = angleXForm(hdg);	
    }
    public void setDO(double dOIn)
    {
    	dO = dOIn;
    	dO = angleXForm(dO);
    }
    public void setOrn(double orient)
    {
    	orn = orient;
    	orn = angleXForm(orn);
    }
        
   	public void move(Board b) 
	{	
	//	if(getX()>0 && getY()>0)//Super lazy solution
		//{
			//erase previous position from validSpaces
			int[][] vS=b.getValidSpaces();
			for(int i = (int)getX(); i<(int)(getX()+image.getWidth(null)); i++)
	        {
	        	for(int j = (int)getY(); j<(int)(getY()+image.getHeight(null)); j++)
	        	{
	        		vS[i+199][j+199] = 0;
	        	}
	        }
	        b.setValidSpaces(vS);
	        int[] collisions = new int[10];//create collisions array
	        int count = 0;
	        //create dummy position variables
	        boolean onXEdge = false;
	        boolean onYEdge = false;
	        int w = b.getWidth();
			int h = b.getHeight();
			double tx=0;
			double ty=0;
			if(getX() > (double)w)
	        {
	        	onXEdge = true;
	        	tx=0.0;
	        }
	        else if(getX() < 0.0)
	        {
	        	onXEdge = true;
	        	tx=(double)w;
	        }
	        else if(getY() > (double)h)
	        {
	        	onYEdge = true;
	        	ty=0.0;
	        }
	        else if(getY() < 0.0)
	        {
	        	onYEdge = true;
	        	ty=(double)h;
	        }
	        else
	        {
				tx = x + getDx();
		        ty = y + getDy();
		        orn += getDO();
		        orn = angleXForm(orn);
	        }
	        
			//check if new position is free
			for(int i = (int)tx; i<(int)(tx+image.getWidth(null)); i++)
	        {
	        	for(int j = (int)ty; j<(int)(ty+image.getHeight(null)); j++)
	        	{
	        		if(vS[i+199][j+199] != 0)
	        		{
	        			if(Arrays.binarySearch(collisions,vS[i+199][j+199])<0)//add to list of collided objects if not already there
	        			{
	        				collisions[count] = vS[i+199][j+199];
	        				if(count <9)
	        				{
	        					count++;
	        				}
	        				Arrays.sort(collisions,0,collisions.length);
	        			}
	        		}
	        	}
	        }					
	        //if any collisions, collide return validSpaces with erased entity
	        if(collisions[0] != 0)
	        {
	        	collide(b);
	        	int i=0;
	        	while(collisions[i]!=0 && i<=9)
	        	{
	        		Board.entRegister.get(collisions[i]).collide(b);
	        		i++;
	        	}
	        }
	        else
	        {
				//if valid, move, update and return validSpaces with new position
				if(onXEdge)
		        {
		        	setX(tx);
		        }
		        else if (onYEdge)
		        {
		        	setY(ty);
		        }
		        else
		        {
					x += getDx();
			        y += getDy();
		        }
		     	//if(tx>0 && ty >0)//this is the lazy way out
				//{
			        for(int i = (int)getX(); i<(int)(getX()+image.getWidth(null)); i++)
			        {
			        	for(int j = (int)getY(); j<(int)(getY()+image.getHeight(null)); j++)
			        	{
			        		vS[i+199][j+199] = entRKey;
			        	}
			        }
				//}
	        }
	        Board.setValidSpaces(vS);
		//}
		//else
		/*{
			boolean onEdge = false;
	        int w = b.getWidth();
			int h = b.getHeight();
			double tx=0;
			double ty=0;
			if(getX() > (double)w)
	        {
	        	onEdge = true;
	        	tx=0.0;
	        }
	        else if(getX() < 0.0)
	        {
	        	onEdge = true;
	        	tx=(double)w;
	        }
	        else if(getY() > (double)h)
	        {
	        	onEdge = true;
	        	ty=0.0;
	        }
	        else if(getY() < 0.0)
	        {
	        	onEdge = true;
	        	ty=(double)h;
	        }
	        else
	        {
				tx = x + getDx();
		        ty = y + getDy();
		        orn += getDO();
		        orn = angleXForm(orn);
	        }
	        
			if(onEdge)
	        {
	        	setX(tx);
	        	setY(ty);
	        }
	        else
	        {
				x += getDx();
		        y += getDy();
	        }
		}*/
    }
    
    private double angleXForm(double angleIn) //turns arbitrary angles to be between 0:2PI; CW positive only!
    {
     	double angle = (angleIn)%(Math.PI*2); 
		if(angle<0)
		{
			angle = 2*Math.PI+angle;
		}
		else if(Math.abs(angle-0.0) < 0.001)
		{
			angle = 0.0;
		}
		return angle;
    }
}