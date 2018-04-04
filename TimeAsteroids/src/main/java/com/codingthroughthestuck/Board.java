package com.codingthroughthestuck;

//CURRENT ISSUES: window resizing changes the positions of everything because we're using mod to display things... I'm not sure if I care. No. I care, but I'm not going to let the user dynamically resize the window.
//					"continuous" tickspeed changes breaks things - possibly due to tickacceleration during spawn/collide frames?
//					collision "detection" says all collisions are at MAXINT
//					Collisions cause nullpointerexceptions at line 169 - finding the activeEntity incorrectly or something
//					entity does not yet exist: x0: 0.0, y0: 0.0, t0: 0.0 @Trajectory@getLocAt when travelling before 0s
//Current goal: Collision Detection
/*
Options:
-Collision detection
-Player movement & Controls
-Properly-sized artwork

 */

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Point;
import java.util.*;

public class Board extends Application
{
	private boolean firstTime = true;
	private boolean playerDidThings = false;
	private boolean nextEventFlag = false;
	private boolean backQuoteFlag = false;
	private int currentTime = 0; //worldtime in ms; 0 is start of game, - is past, + is future
	private long startTime = 0; //in ns
	private long lastClockTime = 0; //last time the loop updated itself in ns;
	private long deltaT; //time, in ns, since the last frame
	private int numLives = 1;
	private Canvas canvas;
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
		canvas = new Canvas(800,600);
		root.getChildren().add(canvas);

		GraphicsContext gc = canvas.getGraphicsContext2D();

		stage.show();

		//change canvas size with screen size; Currently more annoying than useful. New decision: list of resolutions/sizes for player, rather than dynamic resizing
//		ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) ->
//		{
//			canvas.setHeight(stage.getHeight());
//			canvas.setWidth(stage.getWidth());
//		};
//		stage.widthProperty().addListener(stageSizeListener);
//		stage.heightProperty().addListener(stageSizeListener);

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
		junk.makeIntangible();
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
					if(firstTime)
					{
						firstTime = false;
						firstTimeSetup();
					}
					updateTimeline();
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
							Entity activeEntity = entities.get(currentNextEvent.getEntityKey()); //THIS SOLUTION IS SO MUCH BETTER - STORING THE KEYS AS SPAWN POINTS. Guaranteed to be unique - no two things can spawn in the same place at the same time - and also well compartmentalized and understood between classes
							if (activeEntity.getTickSpeed() > 0) //entity travels forward through time: normal
							{
								switch (type)
								{
									case 'S': //player ship spawn
									case 's': //spawn
									{
										activeEntities.put(currentNextEvent.getEntityKey(), activeEntity);
										break;
									}
									case 'C': //player ship collide
									case 'c': //collision
									{
										activeEntities.remove(currentNextEvent.getEntityKey());
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
										activeEntities.remove(currentNextEvent.getEntityKey());
										break;
									}
									case 'c': //collision
									{
										activeEntities.put(currentNextEvent.getEntityKey(), activeEntity);
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
							Entity activeEntity = entities.get(currentNextEvent.getEntityKey()); //THIS SOLUTION IS SO MUCH BETTER - STORING THE KEYS AS SPAWN POINTS. Guaranteed to be unique - no two things can spawn in the same place at the same time - and also well compartmentalized and understood between classes
							if (activeEntity.getTickSpeed() < 0) //entity travels backward through time: apparently normal
							{
								switch (type)
								{
									case 'C': //player ship going backwards in time
									case 's': //spawn
									{
										activeEntities.put(currentNextEvent.getEntityKey(), activeEntity);
										break;
									}
									case 'S':
									case 'c': //collision
									{
										activeEntities.remove(currentNextEvent.getEntityKey());
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
										activeEntities.remove(currentNextEvent.getEntityKey());
										break;
									}
									case 'c': //collision
									{
										activeEntities.put(currentNextEvent.getEntityKey(), activeEntity);
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
					if(keys.contains("SHIFT"))
					{
						playerShip.setTickSpeed(-0.5);
					}
				}
				if(keys.contains("E")) //increase tickspeed
				{
					//playerShip.setTickSpeed(playerShip.getTickSpeed()+0.1);
					playerShip.setTickSpeed(1);
					if(keys.contains("SHIFT"))
					{
						playerShip.setTickSpeed(0.5);
					}
				}
				if(keys.contains("W"))
				{
					playerShip.setTickSpeed(0);
				}
				if(keys.contains("BACK_QUOTE"))
				{
					backQuoteFlag = true;
				}
				if(!keys.contains("BACK_QUOTE") && backQuoteFlag) //THERE IS A BETTER WAY TO DETECT KEY PRESSES THAN THIS AND YOU SHOULD FEEL BAD THAT YOU'VE RESORTED TO THIS INSTEAD
				{
					// ` to display debug
					backQuoteFlag = false;
					dispDebugScreen();
				}
			}
		}.start();
	}
	private void dispClock(GraphicsContext gc)
	{
		int cs = (int)Math.floor(currentTime)/10;
		int sec = (int)Math.floor(currentTime)/1000;
		int min = (int)Math.floor(sec/60);
		int hour = (int)Math.floor(min/60);

		String centiSeconds = cs+"";
		if(centiSeconds.length()>2)
		{
			if(cs>0)
			{
				centiSeconds = centiSeconds.substring(centiSeconds.length()-2);
			}
			else if(cs<0)
			{
				centiSeconds = centiSeconds.substring(centiSeconds.length()-3);
			}
		}

		String seconds = sec%60+"";
		if(seconds.length()==1)
			seconds = "0"+seconds;
		else if(sec<0 && seconds.length()==2)
			seconds = "0"+seconds.substring(1);

		String hours = hour+"";
		String minutes = min%60+"";

		if(cs<0)
		{
			centiSeconds = centiSeconds.substring(1);
		}
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
		gc.fillText("Time: "+hours+":"+minutes+":"+seconds+"."+centiSeconds, gc.getCanvas().getWidth()/2-75, 25);
		gc.strokeText("Time: "+hours+":"+minutes+":"+seconds+"."+centiSeconds, gc.getCanvas().getWidth()/2-75, 25);

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
	private void dispDebugScreen()
	{
		Stage stage2 = new Stage();
		stage2.setX(0);
		//stage2.initModality(Modality.NONE); //A poor (read: failed) attempt to make it not autofocus to this window upon creation
		stage2.setTitle("Debug");
		Group root = new Group();
		Scene scene = new Scene(root);
		stage2.setScene(scene);
		Canvas canvas = new Canvas(350,600);
		root.getChildren().add(canvas);

		GraphicsContext gc = canvas.getGraphicsContext2D();

		stage2.show();
		new AnimationTimer()
		{
			@Override
			public void handle(long currentNanoTime)
			{
				gc.setFill(Color.WHITE);
				gc.fillRect(0,0,canvas.getHeight(),canvas.getWidth());
				int h = (int)gc.getFont().getSize()+3; //text height
				gc.setFill(Color.BLACK);
				gc.fillText("Entities Size: "+entities.size(),0,0);
				gc.fillText("Timeline Size: "+timeline.size(),0,h);

				int i = 0;
				for(AstEvent e : currentNextEvents)
				{
					switch(e.getType())
					{
						case 'C':
						case 'c':
							gc.setFill(Color.RED);
							break;
						case 'S':
						case 's':
							gc.setFill(Color.BLUE);
							break;
						default:
							gc.setFill(Color.BLACK);
					}
					gc.fillText("Current Active Event Time: " + e.getTime(), 0, (2+2*i) * h);
					gc.fillText("\" Entity Key: " + e.getEntityKey(), 0, (3+2*i) * h);
					i++;
				}
				i *= 2;
				gc.setFill(Color.BLACK);
				gc.fillText("Active Entities Size: "+activeEntities.size(),0,(4+i)*h);
				int[] iWrapper = {i};
				activeEntities.forEach((k,v) ->
				{
					gc.fillText("Active Entity Key: " + k, 0, (5+iWrapper[0]) * h);
					iWrapper[0] = iWrapper[0]+1;
				});
			}
		}.start();

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
	private LinkedList<AstEvent> nextEarliestEvent() //returns Least Upper Bound, chronologically, to current time from timeline. Returns the last event(s) at t = MAXINT otherwise
	{
		ListIterator<AstEvent> lit = timeline.listIterator(0);
		AstEvent e = lit.next();

		//navigate to next earliest event
		while(e.getTime() < currentTime && lit.hasNext() && !(e.getTime() > currentTime-(int)(deltaT/Math.pow(10,6))))
		{
			e = lit.next();
		}
		//check to see if any other events happen at this particular time; if so, add them to retVal
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
	private LinkedList<AstEvent> lastLatestEvent() //returns Greatest Lower Bound, chronologically, to current time from timeline. Returns the first event(s) at t = -MAXINT otherwise
	{
		ListIterator<AstEvent> lit = timeline.listIterator(timeline.size());
		AstEvent e = lit.previous();

		//navigate to next latest event
		while(e.getTime() > currentTime && lit.hasPrevious() && !(e.getTime() < currentTime+(int)(deltaT/Math.pow(10,6))))
		{
			e = lit.previous();
		}
		//check to see if any other events happen at this particular time; if so, add them to retVal
		LinkedList<AstEvent> retVal = new LinkedList<>();
		retVal.addLast(e);
		int time = e.getTime();
		while(lit.hasPrevious() && time == e.getTime())
		{
			if(e!=retVal.getLast())
			{
				retVal.addLast(e);
			}
			e = lit.previous();
		}

		return retVal;
	}

	private void updateTimeline() //collision detection/generation
	{
		//for each entity in entities, check for collisions against all others, setCollision(earliest collision)
		/*
		linkedList<int[]> updatedCollisions

		for each thing in entities
			if(!thing.isIntangible())
			time earliestCollision
			collisionKey1 = thing
			collisionKey2
			for each otherThing in entities & not in alreadychecked
				if thing and otherThing collide [findCollisionTime] and collision is earlier than earliestCollision
					collision = earliestCollision
					collisionKey2 = otherThing
			add earliestCollisionTime, collisionKey1, collisionKey2 to a linkedList of arrays (updatedCollisions): [time; x1, y1, t1; x2, y2, t2]
			add key to linkedlist "alreadychecked"
		 */
		LinkedList<Integer[]> updatedCollisions = new LinkedList<>();
		LinkedList<Point3D> alreadyChecked = new LinkedList<>();
		entities.forEach((k1,v1)->
		{
			if(!v1.isIntangible() && entities.size()>2) //must have at least one thing to collide with; entities always contains the junk entity, though
			{
				LinkedList<Integer> earliestCollision = new LinkedList<>(); //this is just acting as a wrapper class
				LinkedList<Point3D> collisionKeys = new LinkedList<>(); //same: k1 is first, k2 is last
				earliestCollision.add(Integer.MAX_VALUE);
				collisionKeys.addFirst(k1);
				collisionKeys.addLast(new Point3D(0,0,Integer.MIN_VALUE)); //second colliding entity defaults to junk, initialization entity

				//actual math bit starts here
				entities.forEach((k2,v2)->
				{
					//if we haven't compared this pair of entities and we're not comparing the same entity to itself - this is a power set of (entities x entities)
					if(!alreadyChecked.contains(k2) && !v2.equals(v1))
					{
						//find the collision time between these two entities; if they don't collide, collision time will be maxint
						int colTime = findCollisionTime(v1,v2);
						int temp = earliestCollision.getFirst();

						if((colTime < temp) && colTime != Integer.MAX_VALUE)
						{
							//if the found collision time is less than the running lowest time for these two objects, pass it and the colliding object's key out of this loop
							earliestCollision.removeFirst();
							earliestCollision.addFirst(colTime);
							collisionKeys.removeLast();
							collisionKeys.addLast(k2);
						}
					}
				});

				//grab the keys of the objects that collide earliest, as found from the previous loop
				Point3D key1 = collisionKeys.removeFirst();
				Point3D key2 = collisionKeys.removeLast();
				//create an int array containing the time of the collision, and the entity keys of collision_object 1 and collision_object 2, assuming a collision was found(ie colTime != maxint
				if(earliestCollision.getFirst()!=Integer.MAX_VALUE)
				{
					Integer[] temp = {earliestCollision.remove(), (int)key1.getX(),(int)key1.getY(),(int)key1.getZ() , (int)key2.getX(),(int)key2.getY(),(int)key2.getZ()};
					updatedCollisions.add(temp); //[time; x1, y1, t1; x2, y2, t2]
				}
			}
			//ignore key 1 in future iterations, thus ensuring this is a power set (the set of all possible subsets) of entities x entities (euclidian product of entities)
			alreadyChecked.add(k1);
		});


		/*
		for each thing in updatedCollisions
			resetCollision(entities.get(x1,y1,t1),time)
			resetCollision(entities.get(x2,y2,t2),time)
		 */
		//remove collisions whose entities have already collided ///////////////////THIS IS WHERE I LEFT OFF 4-3-18
		/*
			sort updatedCollisions by time aka updatedCollisions.get()[0]
			in order, for each int[] i : updatedCollisions
			{
				if thing[i] contains an entity on the temporary linkedList //just wrote a method for doing this or nearly this
					remove thing[i] from updatedCollisions
				add entity thing[i1]'s entitykey to a temporary linkedlist
				add entity thing[i2]'s entitykey to a temporary linkedlist
			}
		*/
		LinkedList<Integer[]> collisions = new LinkedList<>();
		sortUpdatedCollisionsByTime(updatedCollisions,0,updatedCollisions.size()-1);
		//LinkedList<Integer[]> updatedCollisionsCopy = new LinkedList<>(updatedCollisions);
		for(Integer[] i : updatedCollisions)
		{
			for(Integer[] j : collisions)
			{
				if(uCContainsEntityKey(i,j[0],j[1],j[2]))
				{
					updatedCollisions.remove(i);
				}
			}
			collisions.add(new Integer[] {i[1],i[2],i[3]});
			collisions.add(new Integer[] {i[4],i[5],i[6]});
		}

		//then delete the collision from entities and put the new one in
		for(Integer[] i : updatedCollisions)
		{
			resetCollision(entities.get(new Point3D(i[1],i[2],i[3])), i[0]);
			resetCollision(entities.get(new Point3D(i[4],i[5],i[6])), i[0]);
		}
		//also, do the same process for entities travelling backward in time and backward in time relative to the player tickspeed (might be taken care of in findCollisionTime. I pray !!!!!!!!!!

		//finally, sanity-check the timeline. Each time should have an even number of collisions
		/* //NOT CURRENTLY WORKING
		HashMap<Integer, Integer> unPairedCollisions = new HashMap<>(); //key = time of event, value = timeline index
		int tempTime;
		int sameTimeCount = 1;
		ListIterator<AstEvent> lit = timeline.listIterator();

		//increase count while list iterator.next.time = lit.prev.get time
		while(lit.hasNext())
		{
			AstEvent e = lit.next();
			if (e.getType() == 'c')
			{
				tempTime = e.getTime();
				//find duplicate collision events
				while (lit.hasPrevious() && lit.hasNext() && lit.previous().getTime() == tempTime)
				{
					lit.next(); //return to original next index
					sameTimeCount++;
					lit.next(); //advance
				}
				if (sameTimeCount % 2 != 0) //if num of duplicates is odd, get rid of... the last one added? and replace with maxInt collision
				{
					resetCollision(entities.get(timeline.get(lit.nextIndex() - 1).getEntityKey()), Integer.MAX_VALUE);
				}
			}
		}*/
	}
	 //quicksorts updated collisions by time; blatantly stolen from https://www.geeksforgeeks.org/quick-sort/ and loosely adapted for my purposes
		/* This function takes last element as pivot,
       places the pivot element at its correct
       position in sorted array, and places all
       smaller (smaller than pivot) to left of
       pivot and all greater elements to right
       of pivot */
		private int partition(LinkedList<Integer[]> updatedCollisions, int low, int high)
		{
			int pivot = updatedCollisions.get(high)[0];
			int i = (low-1); // index of smaller element
			for (int j=low; j<high; j++)
			{
				// If current element is smaller than or
				// equal to pivot
				if (updatedCollisions.get(j)[0] <= pivot)
				{
					i++;

					// swap arr[i] and arr[j]
					int temp = updatedCollisions.get(i)[0];
					updatedCollisions.get(i)[0] = updatedCollisions.get(j)[0];
					updatedCollisions.get(j)[0] = temp;
				}
			}

			// swap arr[i+1] and arr[high] (or pivot)
			int temp = updatedCollisions.get(i+1)[0];
			updatedCollisions.get(i+1)[0] = updatedCollisions.get(high)[0];
			updatedCollisions.get(high)[0] = temp;

			return i+1;
		}
		/* The main function that implements QuickSort()
      arr[] --> Array to be sorted,
      low  --> Starting index,
      high  --> Ending index */
		private void sortUpdatedCollisionsByTime(LinkedList<Integer[]> arr, int low, int high)
		{
			if (low < high)
			{
            /* pi is partitioning index, arr[pi] is
              now at right place */
				int pi = partition(arr, low, high);

				// Recursively sort elements before
				// partition and after partition
				sortUpdatedCollisionsByTime(arr, low, pi-1);
				sortUpdatedCollisionsByTime(arr, pi+1, high);
			}
		}
	private boolean uCContainsEntityKey(Integer[] updatedCollisions, int x, int y, int t)
	{
		if((updatedCollisions[1] == x && updatedCollisions[2] == y && updatedCollisions[3] == t) || (updatedCollisions[4] == x && updatedCollisions[5] == y && updatedCollisions[6] == t))
			return true;
		else
			return false;
	}
	private int findCollisionTime(Entity e1, Entity e2) //returns MAXINT if no collision between e1,e2
	{
		double r1 = e1.getSprite().getWidth()/2; //radii of hitboxes: hitboxes are circles
		double r2 = e2.getSprite().getWidth()/2;
		int retVal = Integer.MAX_VALUE; //no collision means collision @ infinity, but everything is discrete, so there's an upper bound on infinity
		/*
		if x distance is decreasing and y distance is decreasing //relative to tickspeeds!
			//find set of times T1 such that Math.abs(x1 - x2) <=(r1+r2) //that the x coordinates "would" collide
			//find the earliest time t2 such that Math.abs(y1-y2) <=(r1+r2) and t2 is contained in T1 //that the y coordinates "would" collide //"earliest" relative to tickspeeds!
			//if t1 is nearly equal to t2 (within 1 unit),
				retVal = t
		if retVal < t.spawn1 || retVal < t.spawn2
			retVal = MAXINT
		*/
		double dx1 = e1.getTrajectory().getmX();
		double dy1 = e1.getTrajectory().getmY();
		double dx2 = e2.getTrajectory().getmX();
		double dy2 = e2.getTrajectory().getmY();
		double x01 = e1.getSpawn().getLoc().getX();
		double y01 = e1.getSpawn().getLoc().getY();
		double x02 = e2.getSpawn().getLoc().getX();
		double y02 = e2.getSpawn().getLoc().getY();


		//if there is an intersection: either y slopes not equal and [x slopes not equal or (x slopes are equal and start positions are the same)] or vice versa
		//HANDLE M == 0!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		if((dy1!=dy2 && (dx1!=dx2 || x01==x02)) || (dx1!=dx2 && (dy1!=dy2 || y01==y02)))
		{
			//find range of x intersection
			double temp1 = (r1+r2+x02-x01)/(dx1-dx2); //first x intercept
			double temp2 = (x02-x01-r1-r2)/(dx1-dx2); //second x intercept
			double tXInt0 = Math.min(temp1,temp2); //earlier x intercept
			double tXInt1 = Math.max(temp1,temp2); //later x intercept

			//find range of y intersection
			temp1 = (r1+r2+y02-y01)/(dy1-dy2); //first y intercept
			temp2 = (y02-y01-r1-r2)/(dy1-dy2); //second y intercept
			double tYInt0 = Math.min(temp1,temp2); //earlier y intercept
			double tYInt1 = Math.max(temp1,temp2); //later y intercept

			//FIND PERIOD OF INTERSECTIONS
			//FIND FIRST TIME WHEN INTERSECTION HAPPENS AND ENTITIES EXIST

			//adjust intersects so that they are within the scope of both entities' existences: does not account for entities travelling backward in time yet!!!!!

///////////.//get a list of all unique interception points - there should be xShortsPerLong of them - and add n*period time to each until the earliest one after both asteroids have spawned. That's your most relevant x intercept.
			//if(not in range of entities' lifespans) <<CODE THIS!~!!
				double xPeriod1 = canvas.getWidth()/dx1;
				double xPeriod2 = canvas.getWidth()/dx2;
				double shortXPeriod = Math.min(xPeriod1,xPeriod2);
				double longXPeriod = Math.max(xPeriod1,xPeriod2);
				double yPeriod1 = canvas.getHeight()/dy1;
				double yPeriod2 = canvas.getHeight()/dy2;
				double shortYPeriod = Math.min(yPeriod1,yPeriod2);
				double longYPeriod = Math.max(yPeriod1,yPeriod2);

			int latestSpawn = Math.max(e1.getSpawn().getTime(), e2.getSpawn().getTime());

				//unique intersections of short line along length of long line
			if((dx1 ==0 || dx2 ==0)&&(dy1 == 0 || dy2 == 0)) //vertical & horizontal
			{
				//FIGURE OUT HOW TO HANDLE THIS!!!!!!!!!!!!!!
				return Integer.MAX_VALUE;
			}
			else if(dx1==0 || dx2 ==0) //one vertical
			{
				//initial intersection time + period of nonzero line
				//aka shortxperiod
				int j = 1;
				while (tXInt0 < (latestSpawn + shortXPeriod))
				{
					tXInt0 = tXInt0 + j * shortXPeriod;
					j++;
				}
				return (int) tXInt0;

			}
			else if(dy1 == 0 || dy2 ==0) //horizontal
			{
				//initial intersection time + period of nonzero
				//return retVal such that retVal < (latest spawn + shorterPeriod) AND retVal contained in {intersection range}+n*shorterPeriod
				int j = 1;
				while (tYInt0 < (latestSpawn + shortYPeriod))
				{
					tYInt0 = tYInt0 + j * shortYPeriod;
					j++;
				}
				return (int) tYInt0;
			}
			else
			{
				//number of shorter-period line segments per longer-period line segment
				int xShortsPerLong = (int) Math.ceil(longXPeriod/shortXPeriod);
				int yShortsPerLong = (int) Math.ceil(longYPeriod/shortYPeriod);
				double[] uniqueXInt0s = new double[xShortsPerLong];
				double[] uniqueYInt0s = new double[yShortsPerLong];

				//first intersection already found
				uniqueXInt0s[0] = tXInt0;
				uniqueYInt0s[0] = tYInt0;

				//find other intersections
				for (int i = 1; i < xShortsPerLong; i++)
				{
					uniqueXInt0s[i] = (shortXPeriod * i * (dx1 - dx2) + x02 - x01) / (dx1 - dx2);
				}
				for (int i = 1; i < yShortsPerLong; i++)
				{
					uniqueYInt0s[i] = (shortYPeriod * i * (dy1 - dy2) + y02 - y01) / (dy1 - dy2);
				}

				//find earliest valid intersection; assumes both entities are travelling forward in time
				int j = 0;
				//travel by large period
				while (tXInt0 < latestSpawn)
				{
					int count = 0;
					//travel by short period
					while (tXInt0 < latestSpawn && count < uniqueXInt0s.length)
					{
						tXInt0 = uniqueXInt0s[count] + j * longXPeriod;
						count++;
					}
					j++;
				}
				//travel by large period
				while (tYInt0 < latestSpawn)
				{
					int count = 0;
					//travel by short period
					while (tYInt0 < latestSpawn && count < uniqueYInt0s.length)
					{
						tYInt0 = uniqueYInt0s[count] + j * longYPeriod;
						count++;
					}
					j++;
				}
			}

			//tX1Int0 += i*period1, i++ until tXInt0 is after t01 and t02; note: use the shortest period possible
			//No, wait.
			//for k = numSmallPeriods/numLargerPeriods (both contained in Int), should have k different intercepts
			/*
			int latestSpawn = Math.max(e1.getSpawn().getTime(), e2.getSpawn().getTime());

			if(tXInt0 < latestSpawn && latestSpawn < tXInt1) //intersect is partially within both entities' existence
			{
				tXInt0 = latestSpawn;
			}
			else if (tXInt1 < latestSpawn) //intersect is before both entities exist
			{
				tXInt0 = Integer.MAX_VALUE;
			}
			if(tYInt0 < latestSpawn && latestSpawn < tYInt1) //intersect is partially within both entities' existence
			{
				tYInt0 = latestSpawn;
			}
			else if (tYInt1 < latestSpawn) //intersect is before both entities exist
			{
				tYInt0 = Integer.MAX_VALUE;
			}*/

			//now find earliest time y2 such that y intersects within the x intersect range AND is within the entities' existence range
			if(tYInt0>tXInt0)
			{
				retVal = (int)tYInt0;
			}
			else
			{
				retVal = (int) tXInt0;
			}
		}


		if(retVal < e1.getSpawn().getTime() || retVal < e2.getSpawn().getTime()) //THIS NEEDS TO BE MODIFIED TO WORK FOR - AND + TICKSPEEDS OF BOTH ENTITIES: SPAWN IS NOT ALWAYS IN THE FUTURE
		{
			retVal = Integer.MAX_VALUE;
		}
		return retVal;
	}
	private int earliestSetIntersection(int x0, int x1, int y0, int y1) //returns earliest point if sets intersect and MAXINT if they don't
	{
		if(x1>y0)
			return x1;
		else if(y1 > x0)
			return x0;
		else if (x0 == y0 || y1 == x0)
			return x0;
		else if (x1 == y0)
			return x1;
		else //no intersection
			return Integer.MAX_VALUE;
	}
	private void resetCollision(Entity e, int newCollisionTime)
	{
		//calculate new collision time
		//double x = e.getTrajectory().getLocAt(newCollisionTime,canvas).getX();
		//double y = e.getTrajectory().getLocAt(newCollisionTime,canvas).getY();
		//Point3D newCollide = new Point3D(x,y,newCollisionTime);

		//remove old collision from timeline
		timeline.remove(e.getCollide()); //remove the old collision

		//update collision in entities
		if(e.getSpawn().getType()=='S')
		{
			entities.get(e.getSpawn().getXYT()).setCollide('C',newCollisionTime, canvas);
		}
		else
		{
			entities.get(e.getSpawn().getXYT()).setCollide('c',newCollisionTime, canvas);
		}
		//add new collision to timeline
		timeline.add(entities.get(e.getSpawn().getXYT()).getCollide());

		//re-sort timeline
		Collections.sort(timeline);
		Collections.reverse(timeline);
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
		//entities.put(e.getCollide().getXYT(),e); Why was I adding entities at their collisions? This was wrong.
 		timeline.add(e.getSpawn());
		timeline.add(e.getCollide());
		Collections.sort(timeline);
		Collections.reverse(timeline);
	}
}