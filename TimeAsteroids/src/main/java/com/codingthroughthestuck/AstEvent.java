package com.codingthroughthestuck;

import javafx.geometry.Point3D;

import java.awt.*;

public class AstEvent implements Comparable<AstEvent>
{
	private int time; //time in ms
	private Point location; //xy location of event in pixels from upper left corner
	private char type; //type of event: s = spawn, c = collision, p = player action

	public AstEvent() //only included for completeness: a spawn at 0,0,0
	{
		time = 0;
		location = new Point(0,0);
		type = 's';
	}
	public AstEvent(char y)
	{
		type = y;
		switch(y)
		{
			case 's':
			{
				time = Integer.MIN_VALUE;
				location = new Point(0,0);
				break;
			}
			case 'c':
			{
				time = Integer.MAX_VALUE;
				location = new Point(0,0);
				break;
			}
			default:
			case 'p':
			{
				time = Integer.MAX_VALUE;
				location = new Point(0,0);
				break;
			}
		}
	}
	public AstEvent(char y, int t, Point l)
	{
		time = t;
		location = l;
		type = y;
	}

	public char getType()
	{
		return type;
	}

	public int getTime()
	{
		return time;
	}

	public Point getLoc()
	{
		return location;
	}
	public Point3D getXYT()
	{
		return new Point3D(location.getX(),location.getY(),time);
	}

	@Override
	public int compareTo(AstEvent o) //natural order: time of event
	{
		if(o.getTime()>this.time)
		{
			return 1;
		}
		else if(o.getTime()<this.time)
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}
	@Override
	public boolean equals(Object o)
	{
		AstEvent a = (AstEvent)o;
		if(a.getLoc() == this.getLoc() && a.getTime() == this.time && a.getType() == this.getType())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
