package majoras.tasteroids;

/**
 * @(#)Text1.java
 *
 *
 * @author 
 * @version 1.00 2018/1/14
 */

import java.awt.event.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JPanel
{
	private JButton quit;
	private JButton play;
	private JLabel title;
	JFrame jf;
	

    public MainMenu(JFrame j) 
    {
    	jf = j;
    	this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
       	setFocusable(true);
		setBackground(Color.WHITE);
		
		quit = new JButton("Quit");
		play = new JButton("Play");
		title = new JLabel("Asteroids");
		
		quit.setAlignmentX(j.CENTER_ALIGNMENT);
		quit.setAlignmentY(j.CENTER_ALIGNMENT);
				
		play.setAlignmentX(j.CENTER_ALIGNMENT);
		play.setAlignmentY(j.BOTTOM_ALIGNMENT);
		
		title.setAlignmentX(j.CENTER_ALIGNMENT);
		title.setAlignmentY(j.TOP_ALIGNMENT);
		
		play.addActionListener(new action());
		quit.addActionListener(new action());
		add(title);
		add(play);
		add(quit);		
    }
    
    public class action implements ActionListener
    {
	    @Override
		 public void actionPerformed(ActionEvent ae)
		 {
		  	jf.dispose();
		 }
		
		@Override
		 public void actionPerformed(ActionEvent ae)
		 {
		  	Board b = new Board();
		  	MainMenu.dispose();
		 }
		
    }
}