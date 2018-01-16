package majoras.asteroids;
/**
 * @(#)Asteroid.java
 *
 *
 * @author 
 * @version 1.00 2018/1/10
 */


public class Asteroid extends Entity 
{
    String size;
    Board bd;	
    private int sRocksKey;
    	
    public Asteroid(Board b, String sz,double x,double y) //spawns an asteroid with random heading and speed between 1 and 5
    {
		super(b,sz,x,y,(int)(Math.random()*4+1.0),(double)(Math.random()*Math.PI*2));
		bd = b;
		size = sz;	
		setDO(((double)Math.random()*7-4.0)*Math.PI/8/20);
		sRocksKey = b.askSRocksKey();
		b.spaceRocks.put(sRocksKey,this);
    }
    public void collide(Board b)
    {
    	System.out.println("Asteroid collide");
    	//kill asteroid: deRegister from entReg & spaceRocks
		b.entRegister.remove(entRKey);    	
    	b.spaceRocks.remove(sRocksKey);
    	//remove from validSpaces
    	int[][] vS=b.getValidSpaces();
		for(int i = (int)getX(); i<(int)(getX()+super.image.getWidth(null)); i++)
	    {
	    	for(int j = (int)getY(); j<(int)(getY()+super.image.getHeight(null)); j++)
	    	{
	    		vS[i+199][j+199] = 0;
	    	}
	    }
    	
    	if(size.equals("ast_lg"))
    	{
    		b.spawnAsteroid("ast_md",getX()+90,getY());
    		b.spawnAsteroid("ast_md",getX()+90,getY());
    	}
    	else if(size.equals("ast_md"))
    	{
    		b.spawnAsteroid("ast_sm",getX()+90,getY());
    		b.spawnAsteroid("ast_sm",getX()+90,getY());
    	}    	
    }     
}