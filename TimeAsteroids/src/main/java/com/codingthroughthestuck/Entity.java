package com.codingthroughthestuck;

//add get/set sound

import javafx.scene.image.Image;

import java.awt.*;

public class Entity
{
	private Trajectory trajectory;
	private AstEvent spawn, collide;
	Image sprite;
	//sound spawnNoise, collideNoise
	private double tickSpeed, spin, orientation; //where orientation is in radians, right is 0. down is pi/2 and spin is in radians per second

	public Entity()
	{
		trajectory = new Trajectory();
		spawn = new AstEvent();
		collide = new AstEvent();
		sprite = new Image("C:\\Users\\Andrew\\IdeaProjects\\TimeAsteroids\\graphics\\game\\bullet.png"); //I need to somehow reference a relative url to stuff in the same jar... assuming that this ends up being a jar
		tickSpeed = 1;
		spin = 0;
		orientation = 0;
	}
	public Entity(Image sp)
	{
		this();
		sprite = sp;
	}
	public Entity(Image sp, AstEvent spawn, Trajectory trajectory)
	{
		this(sp);
		this.spawn = spawn;
		this.trajectory = trajectory;
	}

	public void setSpawn(AstEvent spawn)
	{
		this.spawn = spawn;
	}
	public void setCollide(AstEvent collide)
	{
		this.collide = collide;
	}
	public void setSprite(Image sprite)
	{
		this.sprite = sprite;
	}
	public void setSpin(double spin)
	{
		this.spin = spin;
	}
	public void setOrientation(double orientation)
	{
		this.orientation = orientation;
	}
	public void setTrajectory(Trajectory trajectory)
	{
		this.trajectory = trajectory;
	}
	public void setTickSpeed(double tickSpeed)
	{
		this.tickSpeed = tickSpeed;
	}

	public double getTickSpeed()
	{
		return tickSpeed;
	}
	public AstEvent getCollide()
	{
		return collide;
	}
	public AstEvent getSpawn()
	{
		return spawn;
	}
	public double getSpin()
	{
		return spin;
	}
	public double getOrientation()
	{
		return orientation;
	}
	public Image getSprite()
	{
		return sprite;
	}
	public Trajectory getTrajectory()
	{
		return trajectory;
	}
}
