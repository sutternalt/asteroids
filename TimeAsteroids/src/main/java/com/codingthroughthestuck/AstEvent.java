package com.codingthroughthestuck;

import java.awt.*;

public class AstEvent
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
	public AstEvent(char y) throws Exception
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
			case 'p':
			{
				time = Integer.MAX_VALUE;
				location = new Point(0,0);
				break;
			}
			default:
			{
				throw new Exception("IMPROPER USE OF TYPE: type can only be 'p', 's', or 'c'");
			}
		}
	}
	public AstEvent(int t, Point l, char y)
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
}
