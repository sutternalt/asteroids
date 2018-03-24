package com.codingthroughthestuck;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.LinkedList;

public class Board extends Application
{
	boolean firstTime = false;
	boolean playerDidThings = false;
	int currentTime = 0; //worldtime in ms; 0 is start of game, - is past, + is future
	long startTime = 0; //in ns
	long lastTime = 0; //last time the loop updated itself in ns;
	int numLives = 1;
	Entity playerShip;
	LinkedList<AstEvent> timeline = new LinkedList<>();
	HashMap<Point3D,Entity> entities = new HashMap<>(); //where the point is the spawnpoint
	HashMap<Point3D,Entity> activeEntities = new HashMap<>();
	HashMap<Point3D,Entity> inactiveEntities = new HashMap<>();

	public static void main(String args[])
	{
		launch(args);
	}
	public void start(Stage stage)
	{
		stage.setTitle("Time Asteroids");
		stage.show();

		startTime = System.nanoTime();
		new AnimationTimer()
		{
			@Override
			public void handle(long currentNanoTime)
			{
				//advance time
				long deltaT = lastTime - currentNanoTime; //time since the last time this loop ran in ns
				currentTime = (int)((playerShip.getTickSpeed()*currentNanoTime - startTime)/Math.pow(10,6)); //in ms; NOTE: THIS MEANS THAT PLAYERS CANNOT GO MORE THAN 24 DAYS INTO THE PAST/FUTURE.

				//be annoyed the player did things, forcing us to recalculate everything
				if(playerDidThings || firstTime)
				{
					updateTimeline();
					if(firstTime)
					{
						firstTime = false;
						firstTimeSetup();
					}
				}
				if(playerShip.getTickSpeed()>0) //positive timeflow
				{
					if(timeline.)
				}

				//update lastTime
				lastTime = currentNanoTime;
			}
		}.start();
	}
	private void updateTimeline() //collision detection
	{
		//for each entity in entities, check for collisions against all others, setCollision(earliest collision)
	}
	private void firstTimeSetup()
	{
		//put all entities in entities into inactive entities //DO I EVEN NEED ENTITIES!?!?!?!?!?!?
		//for each entity in inactive entities, if getspawn < current time < getCollide, putInto ActiveEntities, remove from inactive entities
	}
}
