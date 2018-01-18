import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.BoxLayout;


public class Test extends JFrame
{
	public Test() 
    {
    	add(new TestBoard());

    	
    	setSize(800,800);
    	setResizable(true);
    	
    	setTitle("Test");
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
					Test t = new Test();
					t.setVisible(true);
				}
		});
	}
}