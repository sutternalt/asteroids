package majoras.asteroids;

/**
 * @(#)Asteroids.java
 *
 *
 * @author 
 * @version 1.00 2018/1/9
 */
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.BoxLayout;

public class Asteroids extends JFrame
{

    public Asteroids() 
    {
    	
    	add(new Board());
    	//add(new MainMenu(this));
    	
    	setSize(800,800);
    	setResizable(true);
    	
    	setTitle("Asteroids");
    	setLocationRelativeTo(null);
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    public static void main(String args[])
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
				public void run()
				{
					//MainMenu men = new MainMenu();
					//add(men);
					//men.setVisible(true);
					Asteroids ast = new Asteroids();
					ast.setVisible(true);
				}
		});
	}
}