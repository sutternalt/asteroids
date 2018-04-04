package com.codingthroughthestuck;

import javafx.geometry.Point3D;

import java.awt.*;

public class AstEvent implements Comparable<AstEvent>
{
	private int time; //time in ms
	private Point location; //xy location of event in pixels from upper left corner
	private char type; //type of event: s = spawn, c = collision, p = player action
	private Point3D entityKey; //associated entity's spawn time/loc; defaults to junk entity

	public AstEvent() //only included for completeness: a spawn at 0,0,0
	{
		time = 0;
		location = new Point(0,0);
		type = 's';
		entityKey = new Point3D(0,0,Integer.MIN_VALUE);
	}
	public AstEvent(char y, Point3D entityKey) //type of event: s = spawn, c = collide, p = player action; S = active playerShip spawn, C = active PlayerShip collide <= these are necessary b/c relative tickspeed of player ship is just the tickspeed of the player ship
	{
		this.entityKey = entityKey;
		type = y;
		switch(y)
		{
			case 'S':
			case 's':
			{
				time = Integer.MIN_VALUE;
				location = new Point(0,0);
				break;
			}
			case 'C':
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
	public AstEvent(char y, Point l, int t, Point3D entityKey)
	{
		this.entityKey = entityKey;
		time = t;
		location = l;
		type = y;
	}
	public AstEvent(AstEvent e) //deep copy
	{
		time = e.getTime();
		type = e.getType();
		location = new Point((int)e.getLoc().getX(),(int)e.getLoc().getY());
		entityKey = new Point3D(e.getEntityKey().getX(),e.getEntityKey().getY(),e.getEntityKey().getZ());
	}

	public char getType()
	{
		return type;
	}

	public int getTime()
	{
		return time;
	}
	public Point3D getEntityKey()
	{
		return entityKey;
	}

	public Point getLoc()
	{
		return location;
	}
	public Point3D getXYT()
	{
		return new Point3D(location.getX(),location.getY(),time);
	}
	public void setType(char c)
	{
		type = c;
	}
	public void setEntityKey(Point3D entityKey)
	{
		this.entityKey = entityKey;
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
		else //times are equal
		{
			if(this.type != o.getType())
			{
				switch(this.type)
				{
					case 'C':
					case 'c':
						return -1;
					default:
						return 1;
				}
			}
			else
			{
				return 0;
			}
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
