/**
 * @(#)Bullet.java
 *
 *
 * @author 
 * @version 1.00 2018/1/10
 */
package majoras.asteroids;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.util.*;

public class Bullet extends Entity
{
	private Timer timr = new Timer();
	private final long DEATHDELAY = 2000; //death time in ms
	
    public Bullet(Board b, double x, double y) 
    {
    	super(b, "bullet", x, y);
    	TimerTask kill = new TimerTask()
    	{
    		@Override
    			public void run()
    			{
    				setDead(true);
    			}
    	};
    	timr.schedule(kill,DEATHDELAY);
    }
    public Bullet(Board b, double x, double y, double s, double h)
    {
    	super(b, "bullet",x,y,s,h);
    	TimerTask kill = new TimerTask()
    	{
    		@Override
    			public void run()
    			{
    				setDead(true);
    			}
    	};
    	timr.schedule(kill,DEATHDELAY);
    }
    public String toString()
	{
		return "bullet";
	} 
	public void collide(Board b)
    {
    	System.out.println("Bullet collide");
    	b.entRegister.remove(super.entRKey);
    	int[][] vS=b.getValidSpaces();
		for(int i = (int)getX(); i<(int)(getX()+super.image.getWidth(null)); i++)
	    {
	    	for(int j = (int)getY(); j<(int)(getY()+super.image.getHeight(null)); j++)
	    	{
	    		vS[i+199][j+199] = 0;
	    	}
	    }
    	setDead(true);
    }  
}