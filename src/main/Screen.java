package main;
import javax.swing.JFrame;
public class Screen {
	public static void main(String[] args){
		
		JFrame window = new JFrame("NEURAL NETWORK PATTERN RECOGNITION");				// jframe widow
		
		window.setContentPane(new Main());						// content of window
		
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		window.setResizable(false);								// un-expandable
		window.pack();
		window.setVisible(true);				
	}
}