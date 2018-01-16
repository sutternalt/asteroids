package majoras.asteroids;

import java.awt.Image;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;

public class Ship extends Entity
{
	private final double ACCEL = 1.0;
	private final double ANGACL = Math.PI/8/13; //angular acceleration; adjust to ms delay speed
	private double newSpd=0;
	private double newHdg=0;
	
	public Ship(Board b)
	{
		super(b,"ship",40.0,50.0);
	}
	public Ship(Board b, double x, double y)
	{
		super(b, "ship",x,y);
	}
	public String toString()
	{
		return "entity";
	}
	
	public void collide(Board b)
    {
    	System.out.println("Ship collide");
    	super.getBoard().entRegister.remove(super.entRKey);
    	super.getBoard().restart();
    } 
	
	public double getNewSpd()
	{
		return newSpd;
	}
	public double getNewHdg()
	{
		return newHdg;
	}
	
    public void left()//rotate left
    {
    	super.setDO(super.getDO()-ANGACL);
    }   
	public void right()//rotate right
	{
		super.setDO(super.getDO()+ANGACL);
	}
	public void forward()//forward
	{
		accelerate(ACCEL,super.getOrn());
    	super.setVel(newSpd,newHdg);
	}
	public void reverse()//backward
	{
		accelerate((0-ACCEL),super.getOrn());
     	super.setVel(newSpd,newHdg);
	}
    private void accelerate(double accl, double orn)
    {
    	double hdg = super.getHdg();
		double spd = super.getSpd();
		
		//Polar vector addition
		//Also, apparently atan2 is a math function that just... exists.
		//So now I know that, I guess.
		newHdg = hdg + Math.atan2(accl*Math.sin(orn-hdg),spd+accl*Math.cos(orn-hdg));
		newSpd = Math.sqrt(Math.pow(spd,2)+Math.pow(accl,2)+2*spd*accl*Math.cos(orn-hdg));
		if(Math.abs(newSpd-0)<0.001)
		{
			newSpd = 0.0;
		}
		if(Math.abs(newHdg-0)<0.001)
		{
			newHdg = 0.0;
		}
    }
}