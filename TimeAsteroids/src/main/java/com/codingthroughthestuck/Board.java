package com.codingthroughthestuck;

//CURRENT ISSUES: Not yet known.
//Current goal: Window resizing;
/*
Options:
-Collision detection
-Player movement & Controls
-Properly-sized artwork

 */

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.awt.Point;
import java.util.*;

public class Board extends Application
{
	private boolean firstTime = true;
	private boolean playerDidThings = false;
	private boolean nextEventFlag = false;
	private int currentTime = 0; //worldtime in ms; 0 is start of game, - is past, + is future
	private long startTime = 0; //in ns
	private long lastClockTime = 0; //last time the loop updated itself in ns;
	private long deltaT; //time, in ns, since the last frame
	private int numLives = 1;
	private Entity playerShip;
	private LinkedList<AstEvent> timeline = new LinkedList<>(); //re-sort this any time you add something to it!!!!!!
	private LinkedList<AstEvent> currentNextEvents = new LinkedList<>();  //the next events in the timeline, relative to tickspeed; contains a list of all events happening at the same millisecond
	private HashMap<Point3D,Entity> entities = new HashMap<>(); //where the point is the spawnpoint; master list of all entities correlated to when they spawn
	private HashMap<Point3D,Entity> activeEntities = new HashMap<>(); //list of only those entities spawned but not yet collided during the current time
	private final String GAMEPATH = "/graphics/game/"; //really need to make a final static version of this somewhere - not here. Maybe in topmost level GUI? Also, have a variable for other graphics and sound resource directories
	private final String HUDPATH = "/graphics/hud/";

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

		//setup key listening array
		ArrayList<String> keys = new ArrayList<String>();

		scene.setOnKeyPressed(e ->
				{
					String code = e.getCode().toString();

					// only add once... prevent duplicates
					if (!keys.contains(code))
						keys.add(code);

				});

		scene.setOnKeyReleased(
				e ->
				{
					String code = e.getCode().toString();
					keys.remove(code);
				});

		//setup timeline
		startTime = System.nanoTime();
		Entity junk = new Entity(); //adds a spawn at -MAXINT and a collision at MAXINT; this is necessary for the timeline to work - I don't remember why, though, so figure it out
		register(junk);

		//setup initial entities
		Image shipSprite = new Image(GAMEPATH+"ship.png");
		playerShip = new Entity(shipSprite,new Trajectory(0.5,0,1,new Point3D(canvas.getWidth()/2-shipSprite.getWidth()/2,canvas.getHeight()/2-shipSprite.getHeight()/2,-1000))); //I was using this for testing, so maybe make it properly later
		playerShip.getSpawn().setType('S');
		playerShip.getCollide().setType('C');
		register(playerShip);
		//Add an asteroid for testing purposes
		Asteroid ast = new Asteroid((short)2,new Trajectory(0,0.1,1,new Point3D(50,50,1000)));
		register(ast);
		ast = new Asteroid((short)2,new Trajectory(0.2,0.1,1,new Point3D(50,50,3000)));
		register(ast);

		//find the next event
		setCurrentNextEvents();

		new AnimationTimer()
		{
			@Override
			public void handle(long currentNanoTime)
			{
				//reset background
				gc.setFill(Color.WHITE);
				gc.fillRect(0,0,canvas.getWidth(),canvas.getHeight());

				//advance time
				if(lastClockTime !=0)
				{
					deltaT = Math.abs(lastClockTime - currentNanoTime); //time since the last time this loop ran in ns
				}
				else
				{
					deltaT = 0;
				}
				//update lastClockTime
				lastClockTime = currentNanoTime;
				//ctime = old time + change in time = old time + ms since last time * tickspeed
				currentTime = currentTime + (int)((playerShip.getTickSpeed()*deltaT)/Math.pow(10,6)); //in ms; NOTE: THIS MEANS THAT PLAYERS CANNOT GO MORE THAN 24 DAYS INTO THE PAST/FUTURE.
				setCurrentNextEvents();

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
				if(nextEventFlag)
				{
					setCurrentNextEvents();
					nextEventFlag = false;
				}

				//trigger scheduled events
				for(AstEvent currentNextEvent : currentNextEvents)
				{
					if (playerShip.getTickSpeed() > 0) //positive timeflow
					{
						if ((currentNextEvent.getTime() <= currentTime) && (currentNextEvent.getTime() >= (currentTime - deltaT))) //if it's time for the current next-event to trigger (ie, current next-event has happened within the last frame)
						{
							//trigger it: get the entity from the next event, figure out the type of event, and then perform the appropriate actions
							char type = currentNextEvent.getType();
							Entity activeEntity = entities.get(currentNextEvent.getXYT()); //THIS SOLUTION IS SO MUCH BETTER - STORING THE KEYS AS SPAWN POINTS. Guaranteed to be unique - no two things can spawn in the same place at the same time - and also well compartmentalized and understood between classes
							if (activeEntity.getTickSpeed() > 0) //entity travels forward through time: normal
							{
								switch (type)
								{
									case 'S': //player ship spawn
									case 's': //spawn
									{
										activeEntities.put(currentNextEvent.getXYT(), activeEntity);
										break;
									}
									case 'C': //player ship collide
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
							} else if (activeEntity.getTickSpeed() < 0) //entity travels backward through time: reversed
							{
								switch (type)
								{
									case 's': //spawn
									{
										activeEntities.remove(currentNextEvent.getXYT());
										break;
									}
									case 'c': //collision
									{
										activeEntities.put(currentNextEvent.getXYT(), activeEntity);
										break;
									}
									default: //player action
									{
										//do your player action; this will only trigger for either the ghost ship of the active player ship or old ships that previously died; these ships will not be collisionable !!!!!!
									}
								}
							}
							//set the next event
							nextEventFlag = true;
						}
					}
					else if (playerShip.getTickSpeed() < 0)//negative timeflow; we don't do events during paused time.
					{
						if ((currentNextEvent.getTime() >= currentTime) && (currentNextEvent.getTime() <= (currentTime + deltaT))) //if it's time for the current next-event to trigger (ie, current next-event has happened within the last frame)
						{
							//trigger it
							char type = currentNextEvent.getType();
							Entity activeEntity = entities.get(currentNextEvent.getXYT()); //THIS SOLUTION IS SO MUCH BETTER - STORING THE KEYS AS SPAWN POINTS. Guaranteed to be unique - no two things can spawn in the same place at the same time - and also well compartmentalized and understood between classes
							if (activeEntity.getTickSpeed() < 0) //entity travels backward through time: apparently normal
							{
								switch (type)
								{
									case 'C': //player ship going backwards in time
									case 's': //spawn
									{
										activeEntities.put(currentNextEvent.getXYT(), activeEntity);
										break;
									}
									case 'S':
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
							} else if (activeEntity.getTickSpeed() > 0) //entity travels forward through time: apparently reversed
							{
								switch (type)
								{
									case 's': //spawn
									{
										activeEntities.remove(currentNextEvent.getXYT());
										break;
									}
									case 'c': //collision
									{
										activeEntities.put(currentNextEvent.getXYT(), activeEntity);
										break;
									}
									default: //player action
									{
										//do your player action; this will only trigger for either the ghost ship of the active player ship or old ships that previously died; these ships will not be collisionable. !!!!!!!
										//Normal player actions are handled when keys are pressed.
									}
								}
							}
							//set the next event
							nextEventFlag = true;
						}
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
					Point loc = v.getTrajectory().getLocAt(currentTime,canvas);
					gc.drawImage(v.getSprite(),loc.getX(),loc.getY());
				});

				//draw the HUD
				dispTempometer(gc);
				dispClock(gc);
				dispTimeline(gc);


				//Debugging Lines
// 				gc.setFill(Color.BLACK);
//				gc.fillText("Active Entities: "+activeEntities.size(),200,200);
//				gc.fillText("Entities: "+entities.size(),200,225);

				//Helpful for knowing which key does what
//				keys.forEach(s ->
//				{
//					System.out.println(s);
//				});

				//handle key presses
				if(keys.contains("Q")) //decrease tickspeed
				{
					//playerShip.setTickSpeed(playerShip.getTickSpeed()-0.1);
					playerShip.setTickSpeed(-1);
				}
				if(keys.contains("E")) //increase tickspeed
				{
					playerShip.setTickSpeed(playerShip.getTickSpeed()+0.1);
					playerShip.setTickSpeed(1);
				}
				if(keys.contains("W"))
				{
					playerShip.setTickSpeed(0);
				}
			}
		}.start();
	}
	private void dispClock(GraphicsContext gc)
	{
		int sec = (int)Math.floor(currentTime)/1000;
		int min = (int)Math.floor(sec/60);
		int hour = (int)Math.floor(min/60);
		String seconds = sec%60+"";
		if(seconds.length()==1)
			seconds = "0"+seconds;
		else if(sec<0 && seconds.length()==2)
			seconds = "0"+seconds.substring(1);
		String hours = hour+"";
		String minutes = min%60+"";

		if(min<0)
		{
			minutes = minutes.substring(1);
		}
		if(hour<0)
		{
			hours = hours.substring(1);
		}
		if(sec<0)
		{
			hours = "-" + hour;
		}

		if(sec >0)
			gc.setFill(Color.LIMEGREEN);
		else if(sec == 0)
			gc.setFill(Color.ORANGE);
		else
			gc.setFill(Color.RED);
		gc.fillText("Time: "+hours+":"+minutes+":"+seconds, gc.getCanvas().getWidth()/2-75, 25);
		gc.strokeText("Time: "+hours+":"+minutes+":"+seconds, gc.getCanvas().getWidth()/2-75, 25);

	}
	private void dispTempometer(GraphicsContext gc)
	{
		String tickText = playerShip.getTickSpeed()+"";

		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1);
		Font theFont = Font.font("Bookman Old Style", FontWeight.BOLD, 20);
		gc.setFont(theFont);
		if(playerShip.getTickSpeed()>0)
			gc.setFill(Color.LIMEGREEN);
		else if(playerShip.getTickSpeed() == 0)
			gc.setFill(Color.ORANGE);
		else
			gc.setFill(Color.RED);
		gc.fillText("Tickspeed: "+tickText+" s/s", gc.getCanvas().getWidth()/2-75, 50);
		gc.strokeText("Tickspeed: "+tickText+" s/s", gc.getCanvas().getWidth()/2-75, 50);
	}
	private void dispTimeline(GraphicsContext gc)
	{
		Image tL = new Image(HUDPATH+"Timeline.png");
		Image img;
		gc.drawImage(tL,gc.getCanvas().getWidth()/2-(tL.getWidth()/2),gc.getCanvas().getHeight()-tL.getHeight());
		double xTime = currentTime/1000*(int)tL.getWidth()/(60*4); //relative location of present
		double xOffset = gc.getCanvas().getWidth()/2 - xTime; //center of timeline image, x
		double yOffset = gc.getCanvas().getHeight()-(tL.getHeight()/2);//center of timeline image, y

		for(AstEvent e : timeline)
		{
			int xRel = e.getTime()/1000*(int)tL.getWidth()/(60*4); //pixels from center of timeline: time*s/ms*px/s
			switch(e.getType())
			{
				case 'S':
				case 's':
					img = new Image(HUDPATH+"Spawn.png");
					gc.drawImage(img,xRel+xOffset-img.getWidth()/2,yOffset-img.getHeight()/2);
					break;
				case 'C':
					img = new Image(HUDPATH+"Death.png");
					gc.drawImage(img,xRel+xOffset-img.getWidth()/2,yOffset-img.getHeight()/2);
					break;
				case 'c':
					img = new Image(HUDPATH+"Collide.png");
					gc.drawImage(img,xRel+xOffset-img.getWidth()/2,yOffset-img.getHeight()/2);
					break;
				default:
					break;
			}
		}
	}
	private void setCurrentNextEvents() //finds the next event in the timeline relative to tickspeed and current time
	{
		currentNextEvents.clear();
		if(playerShip.getTickSpeed() > 0) //positive timeflow
		{
			currentNextEvents = nextEarliestEvent();
		}
		else if(playerShip.getTickSpeed() < 0) //negative timeflow
		{
			currentNextEvents = lastLatestEvent();
		}
		else //paused, so just return the last event; we'll never get to it anyway
		{
			currentNextEvents.add(timeline.getLast());
		}
	}
	private LinkedList<AstEvent> nextEarliestEvent() //returns Least Upper Bound, chronologically, to current time from timeline. Returns the last event at t = MAXINT otherwise
	{
		ListIterator<AstEvent> lit = timeline.listIterator(0);
		AstEvent e = lit.next();

		while(e.getTime() < currentTime && lit.hasNext() && !(e.getTime() > currentTime-(int)(deltaT/Math.pow(10,6))))
		{
			e = lit.next();
		}
		LinkedList<AstEvent> retVal = new LinkedList<>();
		retVal.addLast(e);
		int time = e.getTime();
		while(lit.hasNext() && time == e.getTime())
		{
			if(e!=retVal.getLast())
			{
				retVal.addLast(e);
			}
			e = lit.next();
		}

		return retVal;
	}
	private LinkedList<AstEvent> lastLatestEvent() //returns Greatest Lower Bound, chronologically, to current time from timeline. Returns the first event at t = -MAXINT otherwise
	{
		ListIterator<AstEvent> lit = timeline.listIterator(timeline.size());
		AstEvent e = lit.previous();

		while(e.getTime() > currentTime && lit.hasPrevious() && !(e.getTime() < currentTime+(int)(deltaT/Math.pow(10,6))))
		{
			e = lit.previous();
		}
		LinkedList<AstEvent> retVal = new LinkedList<>();
		retVal.addLast(e);
		int time = e.getTime();
		while(lit.hasNext() && time == e.getTime())
		{
			if(e!=retVal.getLast())
			{
				retVal.addLast(e);
			}
			e = lit.next();
		}

		return retVal;
	}

	private void updateTimeline() //collision detection/generation
	{
		//for each entity in entities, check for collisions against all others, setCollision(earliest collision) !!!!!!!
		//then delete the collision from entities and put the new one in
		//also, do the same process for entities travelling backward in time and backward in time relative to the player tickspeed
	}
	private void firstTimeSetup()
	{
		//for each entity in entities, if getspawn < current time < getCollide, putInto ActiveEntities
		entities.forEach((k,v) ->
		{
			//check to see if current time is between spawn and collide (for positive tickspeed entities) or between collide and spawn (for negative tickspeed entities)
			if (v.getTickSpeed() > 0)
			{
				if (v.getSpawn().getTime() <= 0 && 0 <= v.getCollide().getTime())
				{
					activeEntities.put(k, v);
				}
			}
			else
			{
				if (v.getCollide().getTime() <= 0 && 0 <= v.getSpawn().getTime())
				{
					activeEntities.put(k, v);
				}
			}

		});
	}
	private void register(Entity e) //registers an entity onto timeline and entities
	{
		entities.put(e.getSpawn().getXYT(),e);
		entities.put(e.getCollide().getXYT(),e);
		timeline.add(e.getSpawn());
		timeline.add(e.getCollide());
		Collections.sort(timeline);
		Collections.reverse(timeline);
	}
}