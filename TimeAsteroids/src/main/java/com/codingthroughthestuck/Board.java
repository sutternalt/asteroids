package com.codingthroughthestuck;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

public class Board extends Application
{
	private boolean firstTime = true;
	private boolean playerDidThings = false;
	private int currentTime = 0; //worldtime in ms; 0 is start of game, - is past, + is future
	private long startTime = 0; //in ns
	private long lastTime = 0; //last time the loop updated itself in ns;
	private int numLives = 1;
	private Entity playerShip;
	private LinkedList<AstEvent> timeline = new LinkedList<>(); //re-sort this any time you add something to it!!!!!!
	private AstEvent currentNextEvent = new AstEvent();  //the next event in the timeline, relative to tickspeed
	private HashMap<Point3D,Entity> entities = new HashMap<>(); //where the point is the spawnpoint; master list of all entities correlated to when they spawn
	private HashMap<Point3D,Entity> activeEntities = new HashMap<>(); //list of only those entities spawned but not yet collided during the current time
	private final String GAMEPATH = "/graphics/game/"; //really need to make a final static version of this somewhere - not here. Maybe in topmost level GUI? Also, have a variable for other graphics and sound resource directories

	public static void main(String args[])
	{
		launch(args);
	}
	public void start(Stage stage)
	{
		stage.setTitle("Time Asteroids");
		Group root = new Group();
		Scene scene = new Scene(root);
		stage.setScene(scene);
		Canvas canvas = new Canvas(800,800);
		root.getChildren().add(canvas);

		GraphicsContext gc = canvas.getGraphicsContext2D();

		stage.show();

		//setup timeline
		startTime = System.nanoTime();
		timeline.addFirst(new AstEvent('s')); //adds a spawn at -MAXINT
		timeline.addLast(new AstEvent('c')); //and a collision at MAXINT, so the timeline is properly set up

		//setup initial entities
		Image shipSprite = new Image(GAMEPATH+"ship.png");
		playerShip = new Entity(shipSprite,new Trajectory(1,0,1,new Point3D(canvas.getWidth()/2-shipSprite.getWidth()/2,canvas.getHeight()/2-shipSprite.getHeight()/2,0))); //I was using this for testing, so maybe make it properly later
		register(playerShip);
		//Add an asteroid for testing purposes
		Asteroid ast = new Asteroid((short)2,new Trajectory(0,0.1,1,new Point3D(50,50,5)));
		register(ast);

		//find the next event
		setCurrentNextEvent();

		new AnimationTimer()
		{
			@Override
			public void handle(long currentNanoTime)
			{
				//reset background
				gc.setFill(Color.WHITE);
				gc.fillRect(0,0,canvas.getWidth(),canvas.getHeight());

				//advance time
				long deltaT;
				if(lastTime !=0)
				{
					deltaT = lastTime - currentNanoTime; //time since the last time this loop ran in ns
				}
				else
				{
					deltaT = 0;
				}
				currentTime = (int)((playerShip.getTickSpeed()*currentNanoTime - startTime)/Math.pow(10,6)); //in ms; NOTE: THIS MEANS THAT PLAYERS CANNOT GO MORE THAN 24 DAYS INTO THE PAST/FUTURE.
				setCurrentNextEvent();

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
					if((currentNextEvent.getTime()-deltaT) < currentTime) //if it's time for the current next-event to trigger (ie, current next-event has happened within the last frame)
					{
						//trigger it
						char type = currentNextEvent.getType();
						Entity activeEntity = entities.get(currentNextEvent.getXYT()); //THIS SOLUTION IS SO MUCH BETTER - STORING THE KEYS AS SPAWN POINTS. Guaranteed to be unique - no two things can spawn in the same place at the same time - and also well compartmentalized and understood between classes
						switch(type)
						{
							case 's': //spawn
							{
								activeEntities.put(currentNextEvent.getXYT(),activeEntity);
								break;
							}
							case 'c': //collision
							{
								activeEntities.remove(currentNextEvent.getXYT());
								break;
							}
							default: //player action
							{
								//do your player action; this will only trigger for either the ghost ship of the active player ship or old ships that previously died; these ships will not be collisionable !!!!!!
							}
						}
						//set the next event
						setCurrentNextEvent();
					}
					else if(currentTime < (currentNextEvent.getTime()+deltaT)/2) //negative timeflow; we don't do events during paused time.
					{
						//trigger it
						char type = currentNextEvent.getType();
						Entity activeEntity = entities.get(currentNextEvent.getXYT()); //THIS SOLUTION IS SO MUCH BETTER - STORING THE KEYS AS SPAWN POINTS. Guaranteed to be unique - no two things can spawn in the same place at the same time - and also well compartmentalized and understood between classes
						switch(type)
						{
							case 's': //spawn
							{
								activeEntities.put(currentNextEvent.getXYT(),activeEntity);
								break;
							}
							case 'c': //collision
							{
								activeEntities.remove(currentNextEvent.getXYT());
								break;
							}
							default: //player action
							{
								//do your player action; this will only trigger for either the ghost ship of the active player ship or old ships that previously died; these ships will not be collisionable. !!!!!!!
								//Normal player actions are handled when keys are pressed.
							}
						}
						//set the next event
						setCurrentNextEvent();
					}
				}

				//check for endCondition
				if(numLives == 0)
				{
					//quit!!!!!!!
				}

				//draw all active entities
				activeEntities.forEach((k,v)->
				{
					Point loc = v.getTrajectory().getLocAt(currentTime);
					gc.drawImage(v.getSprite(),loc.getX(),loc.getY());
				});

				//update lastTime
				lastTime = currentNanoTime;
			}
		}.start();
	}
	private void setCurrentNextEvent() //finds the next event in the timeline relative to tickspeed and current time
	{
		if(playerShip.getTickSpeed() > 0) //positive timeflow
		{
			currentNextEvent = nextEarliestEvent();
		}
		else if(playerShip.getTickSpeed() < 0) //negative timeflow
		{
			currentNextEvent = lastLatestEvent();
		}
		else //paused, so just return the last event; we'll never get to it anyway
		{
			currentNextEvent = timeline.getLast();
		}
	}
	private AstEvent nextEarliestEvent() //returns Least Upper Bound, chronologically, to current time from timeline. Returns the last event at t = MAXINT otherwise
	{
		ListIterator<AstEvent> lit = timeline.listIterator(0);
		AstEvent retVal = lit.next();

		while(retVal.getTime() < currentTime && lit.hasNext())
		{
			retVal = lit.next();
		}
		return retVal;
	}
	private AstEvent lastLatestEvent() //returns Greatest Lower Bound, chronologically, to current time from timeline. Returns the first event at t = -MAXINT otherwise
	{
		ListIterator<AstEvent> lit = timeline.listIterator(timeline.size());
		AstEvent retVal = lit.previous();

		while(retVal.getTime() > currentTime && lit.hasPrevious())
		{
			retVal = lit.previous();
		}
		return retVal;
	}

	private void updateTimeline() //collision detection
	{
		//for each entity in entities, check for collisions against all others, setCollision(earliest collision) !!!!!!!
	}
	private void firstTimeSetup()
	{
		//for each entity in entities, if getspawn < current time < getCollide, putInto ActiveEntities
		entities.forEach((k,v) ->
		{
			//check to see if current time is between spawn and collide (for positive tickspeed entities) or between collide and spawn (for negative tickspeed entities)
			if((v.getSpawn().getTime() <= currentTime && currentTime <= v.getCollide().getTime()) || (v.getCollide().getTime() <= currentTime && currentTime <= v.getSpawn().getTime()))
			{
				activeEntities.put(k,v);
			}
		});
	}
	private void register(Entity e) //registers an entity onto timeline and entities
	{
		entities.put(e.getSpawn().getXYT(),e);
		timeline.add(e.getSpawn());
		timeline.add(e.getCollide());
		Collections.sort(timeline);
	}
}