package majoras.asteroids;

//Game logic, keylistenening, etc. also "merged" with the main game window atm.

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.*;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.util.*;

public class Board extends JPanel implements ActionListener, ComponentListener
{
	private Timer timer;
	private Ship ship;
	private final int DELAY = 10;
	private boolean debug = false;
	public static LinkedList<Bullet> bullets = new LinkedList<Bullet>();
	public static HashMap<Integer,Asteroid> spaceRocks = new HashMap<Integer,Asteroid>();
	private static int[][] validSpaces; //array of whether or not pixel is occupied
	private static double ctrX = 0.0; //ship-centers
	private static double ctrY = 0.0;
	public static HashMap<Integer,Entity> entRegister = new HashMap<Integer,Entity>(); //used in collision detection; a map of entities on the board & keys
	private boolean restartFlag = false; //used to detect a restart so that we don't try to detect collisions against an empty validSpaces
	
	public Board()
	{
		addKeyListener(new TAdapter());
		setFocusable(true);
		setBackground(Color.WHITE);
		
		ship = new Ship(this,400.0-38,400.0-40);
		validSpaces = new int[1200][1200];
		timer = new Timer(DELAY, this);
		timer.start();
	}
	
	public int[][] getValidSpaces()
	{
		return validSpaces;
	}
	public static void resetValidSpaces()
	{
		validSpaces = new int[validSpaces.length][validSpaces[0].length];
	}
	public static void setValidSpaces(int vS[][])
	{
		validSpaces = new int[vS.length][vS[0].length];
		for(int i=0; i<validSpaces.length; i++)
		{
		  for(int j=0; j<validSpaces[i].length; j++)
		  {
		    validSpaces[i][j]=vS[i][j];
		  }
		}
	}
	public int askKey() //assigns an unused key to an entity
	{
		Set keys = entRegister.keySet();
		int count = 1;
		
		if(!keys.isEmpty())
		{
			while (count <= keys.size() && keys.contains(count))
			{
				count++;
			}
		}
		
		return count;
	}
	public int askSRocksKey()
	{
		Set keys = spaceRocks.keySet();
		int count = 1;
		
		if(!keys.isEmpty())
		{
			while (count <= keys.size() && keys.contains(count))
			{
				count++;
			}
		}
		
		return count;
	}
	
	private boolean areManyNonZeroes(int[][] arr, int iLo, int iHi, int jLo, int jHi)//USED FOR FINDCOLLISIONS arr.partitionFrom[(iS.length-2):(iS.length-1)][(0):(arr.length[0]-1)].contains(numberOfNonZeroNumbers)>1)//does this partition contain more than one nonzero number?
	{
		int nZero = 0;
		int i = iLo;
		int j = jLo;
		while(i<iHi)
		{
			j = jLo;
			while(j<jHi) //for the length of the partition
			{
				if(arr[i][j]!=0) //provided arr[ij]!=0
				{
					if (nZero==0) //if nZero isn't filled, fill it
					{
						nZero=arr[i][j];
					}
					else if(nZero!=arr[i][j]) //otherwise, provided arr[ij]!=nZero, return true
					{
						return true;
					}
				}
				j++;
			}
			i++;
		}
		return false;
	}	
	private void checkForCollision(int[][] arr,LinkedList<Integer> cols, int loc, int x, int y) 
	{
		// array you care about, location in array (edge or not, see below), i/j coord in array
		//location:	7,8,9
				//	4,5,6
				//	1,2,3
		int wst = 1;
		int est = 1;
		int nrt = 1;
		int sth = 1; 
		if((loc - 1)%3 == 0)
		{
			wst = 0;
		}
		if(loc <= 3)
		{
			sth = 0;
		}
		if(loc % 3 == 0)
		{
			est = 0;
		}
		if(loc >= 7)
		{
			nrt = 0;
		}
		
		if(!cols.contains(arr[x][y]) && arr[x][y]!=0) //if this square isn't in collisions && isn't 0
		{
			for(int m = -nrt; m <= sth; m++) //check surrounding squares
			{
				for(int n = -wst; n <= est; n++) //m=i=eastwest, n=j=northsouth ///WHAT THE FUCK THIS BREAKS IF N<STH BUT WORKS IF N<=STH
				{
					if(m!=0 && n !=0) //if we're not looking at the center square
					{
						if(arr[x+m][y+n]!=0)//if surrounding square isn't 0
						{
							if(arr[x+m][y+n]!=arr[x][y])//if square isn't the same number as a surrounding square
							{
								if(!cols.contains(arr[x][y]))
								{
									cols.add(arr[x][y]); //add square to collisions
								}
								if(!cols.contains(arr[x+m][y+n])) //if surrounding square isn't in collisions
								{
									cols.add(arr[x+m][y+n]); //add surrounding square to collisions
								}
							}
						}
					}
				}
			}
		}
	}//USED FOR FINDCOLLISIONS
	private void traverseArray(LinkedList<Integer> cols, int arr[][])
	{
		traverseArray(cols,arr,0,arr.length,0,arr[0].length);
	}//USED FOR FINDCOLLISIONS
	private void traverseArray(LinkedList<Integer> collisions, int arr[][], int iLo, int iHi, int jLo, int jHi)
	{
		for(int i = iLo; i<iHi; i++)
		{
			for(int j = jLo; j<jHi; j++)
			{
				if(i != iLo && i != (iHi-1) && j != jLo && j != (jHi-1))//interior
				{
					checkForCollision(arr,collisions,5,i,j);
				}
				else if(i == iLo)//north edge
				{
					if(j!=jLo && j != (jHi-1)) //edge
					{	
						checkForCollision(arr,collisions,8,i,j);					
					}
					if(j == jLo)//northwest corner
					{
						checkForCollision(arr,collisions,7,i,j);
					}
					else if(j == (jHi-1))//northeast corner
					{
						checkForCollision(arr,collisions,9,i,j);
					}
				}
				else if(i == (iHi-1))//south edge
				{
					if(j!=jLo && j != (jHi-1)) //edge
					{
						checkForCollision(arr,collisions,2,i,j);							
					}
					if(j == jLo)//southwest corner
					{
						checkForCollision(arr,collisions,1,i,j);
					}
					else if(j == (jHi-1))//southeast corner
					{
						checkForCollision(arr,collisions,3,i,j);
					}
				}
				else if(j ==jLo)//west edge
				{
					if(i!=iLo && i != (iHi-1)) //edge
					{	
						checkForCollision(arr,collisions,4,i,j);						
					}
					if(i == iLo)//northwest corner
					{
						checkForCollision(arr,collisions,7,i,j);
					}
					else if(i == (iHi-1))//southwest corner
					{
						checkForCollision(arr,collisions,1,i,j);
					}
				}
				else if(j == (jHi-1))//east edge
				{
					if(i!=iLo && i != (iHi-1)) //edge
					{					
						checkForCollision(arr,collisions,6,i,j);		
					}
					if(i == iLo)//northeast corner
					{
						checkForCollision(arr,collisions,8,i,j);
					}
					else if(i == (iHi-1))//southeast corner
					{
						checkForCollision(arr,collisions,3,i,j);
					}
				}
			}
		}	
	}//USED FOR FINDCOLLISIONS
	public LinkedList<Integer> findCollisions(int arr[][])
	{
		//searches an array for collisions; if the array is over size 100, it partitions the array into 16 parts and 2 optional remainders,
		//tests to see if each partition even has two nonzero objects in it in the first place,
		//and then checks valid partitions for collisions. Then it checks the lines along each partition.
		LinkedList<Integer> collisions = new LinkedList<Integer>();
		
		if(arr.length<=200 || arr[0].length <= 200) //no partitioning
		{
			//traverse array
			{
				traverseArray(collisions,arr);
			}
		}
		else //partition
		{
			int nPartI=1;
			int nPartJ=1;
			//get min/max xy values for each partition
			if(arr.length>100) //rows
			{
				nPartI = 4;
			}
			else//cols
			{
				nPartJ = 4;
			}
			int[] iS = new int[nPartI+2];//interpartition numbers: ex: 203: [0,50,100,150,200,203] ex: 200: [0,50,100,150,200,0]
			int[] jS = new int[nPartJ+2];
			
			for(int i = 0; i < iS.length-1; i++)
			{
				iS[i] = i*(int)Math.floor(arr.length/nPartI);
			}
			if(iS[iS.length-2]-arr.length!=0)
			{
				iS[iS.length-1] = arr.length;
			}
			for(int j = 0; j < jS.length-1; j++)
			{
				jS[j] = j*(int)Math.floor(arr[0].length/nPartJ);
			}
			if(jS[jS.length-2]-arr[0].length!=0)
			{
				jS[jS.length-1] = arr[0].length;
			}
		
			//for num.partitions
			for(int i = 0; i<iS.length-1; i++) //iterate the standard partitions
			{
				for(int j = 0; j<jS.length-1; j++)
				{	
					if(i!=0 && j!=0)//ignore the first index
					{
						if(areManyNonZeroes(arr,iS[i-1],iS[i],jS[j-1],jS[j]))//does this partition contain more than one nonzero number?
						{
							traverseArray(collisions,arr,iS[i-1],iS[i],jS[j-1],jS[j]);//if so, check for cols
						}
					}					
				}
			}
			
			if(iS[iS.length-1]!=0) //if i Remainder exists
			{
				if(areManyNonZeroes(arr,iS[iS.length-2],iS[iS.length-1],0,arr[0].length-1)) //arr.partitionFrom[(iS.length-2):(iS.length-1)][(0):(arr.length[0]-1)].contains(numberOfNonZeroNumbers)>1)//does this partition contain more than one nonzero number?
				{
					traverseArray(collisions,arr,iS[iS.length-2],iS[iS.length-1],0,arr[0].length-1);//if so, check for cols
				}
			}
			if(jS[jS.length-1]!=0) //if j Remainder exists
			{
				if(areManyNonZeroes(arr,0,arr.length-1,jS[jS.length-2],jS[jS.length-1])) //arr.partitionFrom[(0):(arr.length-1)][(jS.length-2):(jS.length-1].contains(numberOfNonZeroNumbers)>1)//does this partition contain more than one nonzero number?
				{
					traverseArray(collisions,arr,0,arr.length-1,jS[jS.length-2],jS[jS.length-1]);//checkforcols//if so, check for cols
				}
			}
			
			
			//for interpartition lines
			//if interpartition line isn't on array edge
			//check for cols
			//Ignoring this line of code because I just want the game to work again, and this chunk of code... doesn't
			//This is safe to comment out (theoretically) for two reasons:
			//1) the screen resolution should be large enough that 16 lines of failed collision detection won't be super noticeable
			//		(side note: there should be some math equation about error and significant digits that could back this up, and furthermore,
			//		 tell us what number, not arbitrarily-selected 100, would be safe to ignore this code for)
			//2) this chunk of code will take up more time that we just don't have
			/*
			for(int i:iS) //for each line of i's
			{
				for(int j =0; j<arr[0].length; j++) //the whole freaking line
				{
					if(i!=0 && i!=arr.length-1 && j!=0 && j!=arr[0].length-1) //excepting the edge
					{
						checkForCollision(arr,collisions,5,i,j); //checkFor Col's
					}
					else if(i==0)
					{
						if(j!=0 && j!=arr[0].length-1)//N
						{
							checkForCollision(arr,collisions,8,i,j);
						}
						//NW
						else if(j==0)
						{
							checkForCollision(arr,collisions,7,i,j);
						}
						else if(j==arr[0].length-1)//NE
						{
							checkForCollision(arr,collisions,9,i,j);
						}
					}
					else if(i==arr.length-1)
					{
						if(j!=0 && j!=arr[0].length-1)//S
						{
							checkForCollision(arr,collisions,2,i,j);
						}
						else if(j==0) //SW
						{
							checkForCollision(arr,collisions,1,i,j);
						}
						else if(j==arr[0].length-1)//SE
						{
							checkForCollision(arr,collisions,3,i,j);
						}
					}
					//E
					else if(j==arr[0].length-1)
					{
						checkForCollision(arr,collisions,6,i,j);
					}
					//W
					else if(j==0)
					{
						checkForCollision(arr,collisions,4,i,j);
					}
				}
			}
			//same again for the j's:
			*/
		}		
		return collisions;
	}
	
	public void updateVS()
	{
		//for each entity in entRegister
		//get key, location, hitbox (image) size and orn
		//update validSpaces with above data if data not already present
	}
	public void spawnAsteroid(String sz, double x, double y)
	{
		Board b = Board.this;
		Asteroid ast = new Asteroid(b,sz,x,y);
	}
	
	@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g; 
				
			//draw debug
			if (debug)
			{
				g.drawString("NewSpd: "+ship.getNewSpd()+" NewHdg: "+ship.getNewHdg()+" dO: "+ship.getDO(),0,15);
				g.drawString("Spd: "+ship.getSpd()+" Hdg:"+ship.getHdg()+" Orn: "+ship.getOrn(),0,30);
				g.drawString("wd: "+super.getWidth()+" ht:"+super.getHeight(),0,45);
				for(int i=200; i<validSpaces.length-201; i++)
				{
					for(int j=200; j<validSpaces[i].length-201; j++)
					{
						if(validSpaces[i][j]!=0)
						{
							Random rand = new Random(validSpaces[i][j]);
							g.setColor(new Color(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255)));
							g.drawLine(i-200, j-200, i-200, j-200);
						}
					}
				}
			}
			
			//update validSpaces
			updateVS();
			//Detect collisions
			if(!restartFlag)
			{
				for (int i:findCollisions(validSpaces))
				{
					if(!entRegister.isEmpty())
					{
						if(entRegister.get(i)!=null)
						{
							entRegister.get(i).collide(this);
						}
					}
				}
			}
			else
			{ 
				restartFlag = false;
			}
			
			//draw ship
			ctrX = ship.getX()+ship.getImage().getWidth(this)/2;
			ctrY = ship.getY()+ship.getImage().getHeight(this)/2;
			g2d.rotate(ship.getOrn(),(int)Math.round(ctrX),(int)Math.round(ctrY)); 	
			g2d.drawImage(ship.getImage(), (int)Math.round(ship.getX()), (int)Math.round(ship.getY()), this);																		
			g2d.rotate(-ship.getOrn(),(int)Math.round(ctrX),(int)Math.round(ctrY));
			
			//draw bullets
			int i=0;
			for(Bullet ent:bullets)
	       	{
	       		if(ent != null)
	       		{
	       			if(!ent.isDead())
	       			{
		       			int entX = (int)Math.round(ent.getX());
		       			int entY = (int)Math.round(ent.getY());
		       			g2d.drawImage(ent.getImage(),entX,entY,this);
	       			}
	       			else
	       			{
	       				bullets.remove(i);
	       				ent.collide(this);
	       				break;
	       			}
	       		}
	       		i++;
	       	}
	       	//draw Asteroids
	       	Set keys = spaceRocks.keySet();
			int count = 1;
			
			if(!keys.isEmpty())
			{
				while (count <= keys.size() && keys.contains(count))
				{
					Asteroid ent = spaceRocks.get(count);
					if(ent != null)
		       		{
		       			if(!ent.isDead())
		       			{
			       			int entX = (int)Math.round(ent.getX());
			       			int entY = (int)Math.round(ent.getY());
			       			double aCtrX = ent.getX()+ent.getImage().getWidth(this)/2;
			       			double aCtrY = ent.getY()+ent.getImage().getHeight(this)/2;
			       			g2d.rotate(ent.getOrn(),(int)Math.round(aCtrX),(int)Math.round(aCtrY));
			       			g2d.drawImage(ent.getImage(),entX,entY,this);
			       			g2d.rotate(-ent.getOrn(),(int)Math.round(aCtrX),(int)Math.round(aCtrY));
		       			}
		       			else
		       			{
		       				spaceRocks.remove(count);
		       				break;
		       			}
		       		}
					count++;
				}
			}
		}
	@Override
		public void actionPerformed(ActionEvent e) //called every DELAY ms. move sprite, repaint board
		{
			Board b = this;
			
	       	ship.move(b);
	       	for(Entity ent:bullets)
	       	{
	       		if(ent != null)
	       		{
	       			ent.move(b);
	       		}
	       	}
	       	
	       	Set keys = spaceRocks.keySet();
			int count = 1;
			if(!keys.isEmpty())
			{
				while (count <= keys.size() && keys.contains(count))
				{
					Asteroid ent = spaceRocks.get(count);
					if(ent != null)
		       		{
	       				ent.move(b);
		       		}
		       		count++;
	       		}
	       	}
	        repaint();
		}
		
 	public void componentHidden(ComponentEvent e)
 	{
 	}
	public void componentMoved(ComponentEvent e)
	{
	}
	public void componentResized(ComponentEvent e)
	{
		int[][] temp = new int[validSpaces.length][validSpaces[1].length];
		for(int i = 0; i<validSpaces.length; i++)
		{
			for(int j = 0; j<validSpaces[i].length; j++)
			{
				temp[i][j] = validSpaces[i][j];
			}
		}
		validSpaces = new int[this.getHeight()+200][this.getWidth()+200];
		for(int i = 0; i<temp.length; i++)
		{
			for(int j = 0; j<temp[i].length; j++)
			{
				validSpaces[i][j] = temp[i][j];
			}
		}
	}
	
	public void componentShown(ComponentEvent e)	
	{
	}
	public void restart()
	{
		Board b = Board.this;
		double ctrX = ship.getImage().getWidth(null)/2;
		double ctrY = ship.getImage().getHeight(null)/2;
		ship = new Ship(b,b.getWidth()/2-ctrX,b.getHeight()/2-ctrY);
		spaceRocks.clear();
		bullets.clear();
		resetValidSpaces();
		entRegister.clear();
		restartFlag = true;
	}
	
	private class TAdapter extends KeyAdapter //listen for key events
	{
		private double x;
		private double y;
		@Override
			public void keyPressed(KeyEvent e)
			{
				int key = e.getKeyCode();

				if(key == KeyEvent.VK_F1)
				{
					debug = !debug;
				}
				else if(key == KeyEvent.VK_F2)//spawn asteroid
				{
					Board b = Board.this;
					new Asteroid(b,createSize(),x,y);
				}
				else if(key == KeyEvent.VK_R)//restart
				{
					restart();
				}
				
				else if(key == KeyEvent.VK_UP)
				{
					ship.forward();
				}
				else if(key == KeyEvent.VK_DOWN)
				{
					ship.reverse();
				}
				else if(key == KeyEvent.VK_LEFT)
				{
					ship.left();
				}
				else if(key == KeyEvent.VK_RIGHT)
				{
					ship.right();
				}
				else if(key == KeyEvent.VK_SPACE)//fire
				{
					Board b = Board.this;
					Bullet bullet = new Bullet(b, ctrX+35.0*Math.cos(ship.getOrn()),ctrY+35.0*Math.sin(ship.getOrn()),ship.getSpd()+3.0,ship.getOrn());//where 35 is shipLength/2 + bullet size
					bullets.add(bullet);
				}

			}
			
		private boolean setXY() ///get random edge location///
	    {
	    	double hgt = getHeight();
	    	double wdt = getWidth();
	    	if(Math.round(Math.random()) == 0) //select x to be constrained
	    	{
	    		y = (double)Math.random()*hgt;
	    		if(Math.round(Math.random()) == 0)// x = 0
		    	{
		    		x=0.0;
		    	}
		    	else//x = screenwidth
		    	{
		    		x=wdt;
		    	}
	    	}
	    	else //or y to be constrained
	    	{
	    		x = (double)Math.random()*hgt;
	    		if(Math.round(Math.random()) == 0)//y = 0
		    	{
		    		y=0.0;
		    	}
		    	else//y = screenheight
		    	{
		    		y=wdt;
		    	}			
	    	}
	    	return true;
	    }
	    private String createSize()//used for asteroid sizing
	    {
	    	String sz;
	    	int r = (int) Math.round(Math.random()*2);		
			switch(r)
			{
				case 0:
					sz = "ast_lg";
					break;
				case 1:
					sz = "ast_md";
					break;
				case 2:
					sz = "ast_sm";
					break;
				default:
					sz = "ast_lg";
					break;
			}
			return sz;
	    }
	}
}