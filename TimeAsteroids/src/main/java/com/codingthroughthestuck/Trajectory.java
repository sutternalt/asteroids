package com.codingthroughthestuck;

import javafx.geometry.Point3D;
import javafx.scene.canvas.Canvas;

import java.awt.Point;

public class Trajectory
{
	private double mX, mY, tickSpeed; //change in x and y respectively (y = mY*tickSpeed*t + y0) where tickSpeed is in worldSeconds/playerSecond
	private Point3D spawn; //spawn location (x0,y0,t0) where 0,0,0 is upper left at player spawn time

	public Trajectory()
	{
		mX = 0.0;
		mY = 0.0;
		spawn = new Point3D(0.0,0.0,0.0);
	}
	public Trajectory(double mX, double mY, double tickSpeed, Point3D spawn)
	{
		this.mX = mX;
		this.mY = mY;
		this.tickSpeed = tickSpeed;
		this.spawn = spawn;
	}
	public Trajectory(Point3D spawn, double hdg, double spd, double tickSpeed) //hdg in CW radians (down is PI/2, right is 0), absSpd = speed*tickspeed
	{
		this.spawn = spawn;
		mX = spd*Math.sin(hdg);
		mY = spd*Math.cos(hdg);
		this.tickSpeed = tickSpeed;
	}

	public double getHeading() //hdg in CW radians (down is PI/2, right is 0)
	{
		return Math.tan(mY/mX);
	}
	public double getSpeed()
	{
		return Math.sqrt(Math.pow(mX,2)+Math.pow(mY,2));
	}
	public Point getLocAt(int time, Canvas canvas) //returns the xy location at a given time in ms or -1,-1 if it doesn't exist - accounts for wraparound
	{
		if((time > spawn.getZ() && tickSpeed >=0) || (time < spawn.getZ() && tickSpeed < 0))
		{
			int x = ((int)Math.floor(mX*(time-spawn.getZ())*tickSpeed) + (int)spawn.getX())%(int)canvas.getWidth();
			int y = ((int)Math.floor(mY*(time-spawn.getZ())*tickSpeed) + (int)spawn.getY())%(int)canvas.getHeight();
			return new Point(x,y);
		}
		else
		{
			System.out.println("entity does not yet exist: x0: "+spawn.getZ()+", y0: "+spawn.getY()+", t0: "+spawn.getZ() +" @Trajectory@getLocAt");
			return new Point(-1,-1);
		}
	}
	public Point getRawLocAt(int time) //returns the xy location at a given time in ms or -1,-1 if it doesn't exist - does not account for wraparound
	{
		if(time > spawn.getZ())
		{
			int x = (int)Math.floor(mX*(time-spawn.getZ())*tickSpeed) + (int)spawn.getX();
			int y = (int)Math.floor(mY*(time-spawn.getZ())*tickSpeed) + (int)spawn.getY();
			return new Point(x,y);
		}
		else
		{
			return new Point(-1,-1);
		}
	}

	public void setHeading(double hdg)
	{
		double spd = getSpeed();
		mX = spd*Math.sin(hdg);
		mY = spd*Math.cos(hdg);
	}
	public void setSpeed(double spd)
	{
		double hdg = getHeading();
		mX = spd*Math.sin(hdg);
		mY = spd*Math.cos(hdg);
	}

	public double getmX()
	{
		return mX;
	}
	public double getmY()
	{
		return mY;
	}
	public double getTickSpeed() //in worldSeconds/playerSecond; if you want the trajectory to go backward through time, set it negative
	{
		return tickSpeed;
	}
	public Point3D getSpawn()
	{
		return spawn;
	}
	public double getX0()
	{
		return spawn.getX();
	}
	public double getY0()
	{
		return spawn.getY();
	}
	public double getT0()
	{
		return spawn.getZ();
	}
}
