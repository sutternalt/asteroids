package com.codingthroughthestuck;

import javafx.scene.image.Image;

import java.util.Random;

public class Asteroid extends Entity
{
	short size; //0 = small, 1 = medium, 2 = large

	public Asteroid()
	{
		super();
		Random rand = new Random();
		size = (short)rand.nextInt(3);
		initialize();
		super.setSprite(getImg());
	}
	public Asteroid(short size)
	{
		super();
		this.size = size;
		initialize();
		super.setSprite(getImg());
	}
	public Asteroid(short size, AstEvent spawn, Trajectory trajectory)
	{
		super();
		this.size = size;
		initialize();
		super.setSprite(getImg());
		super.setSpawn(spawn);
		super.setTrajectory(trajectory);
	}
	private void initialize()
	{
		super.setOrientation(Math.random()*Math.PI*2);
		super.setSpin(Math.random()*10);
	}
	private Image getImg()
	{
		switch (size)
		{
			case 0:
			{
				return new Image("C:\\Users\\Andrew\\IdeaProjects\\TimeAsteroids\\src\\main\\resources\\graphics\\game\\ast_sm.png");
			}
			case 1:
			{
				return new Image("C:\\Users\\Andrew\\IdeaProjects\\TimeAsteroids\\src\\main\\resources\\graphics\\game\\ast_md.png");
			}
			case 2:
			{
				return new Image("C:\\Users\\Andrew\\IdeaProjects\\TimeAsteroids\\src\\main\\resources\\graphics\\game\\ast_lg.png");
			}
			default:
			{
				return new Image("C:\\Users\\Andrew\\IdeaProjects\\TimeAsteroids\\src\\main\\resources\\graphics\\game\\bullet.png");
			}
		}
	}

	public void setSize(short size) throws Exception
	{
		if(size <= 2)
			this.size = size;
		else
			throw new Exception("Asteroid size out of bounds!");
	}

	public short getSize()
	{
		return size;
	}
	public double getRadius()
	{
		switch(size)
		{
			case 0:
			{
				return 25.0;
			}
			case 1:
			{
				return 50.0;
			}
			default:
			{
				return 100.0;
			}
		}
	}
}
