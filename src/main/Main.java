package main;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.JPanel;

import network.Neuralnet;

@SuppressWarnings("serial")
public class Main extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener{
	//dimensions
	public static final int WIDTH = 1300;
	public static final int HEIGHT = 650;
	public static final int SCALE = 1;


	//game thread
	private Thread thread;
	private boolean running; 
	private int FPS = 15;
	private long targetTime = 1000/FPS; 

	//image
	private BufferedImage image;
	private Graphics2D g;

	// program variables
	static int bSize = 25; 
	int numHid = 45;
	int drawSize = 80;
	int thickness = 2;
	boolean[][] board = new boolean[drawSize][drawSize];
	double[][] out = new double[bSize][bSize];
	int pixelWid = (HEIGHT/(drawSize+1));
	int comppixelWid = (HEIGHT/(bSize+1));
	Button[] buttons = new Button[7];
	boolean erasemode = false, toggrid = false, compvis = false;
	int leftBound = 0, rightBound = 0, topBound = 0, bottomBound = 0;

	// data loader network variables
	static double[][] datainps;
	static double[][] dataexps;
	static int datasiz=0;
	static String datafile = "digits123";

	//	neural network variables
	static Neuralnet coybert; 
	double lastout[] = new double[10];
	static ArrayList<double[]> inputs = new ArrayList<double[]>();
	static ArrayList<double[]> expec = new ArrayList<double[]>();
	
	private void init(){							// initalizes game states
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) image.getGraphics();
		running = true;

		for(int x=0; x<10; x++)
			lastout[x] = 0;
		coybert = new Neuralnet(new int[]{bSize*bSize, numHid, 10});
		buttons[0] = new Button(650,50,"Evaluate");
		buttons[1] = new Button(650,170,"Clear");
		buttons[2] = new Button(650,290,"Eraser off");
		buttons[3] = new Button(850,530,"Toggle Grid");
		buttons[4] = new Button(650,410,"Reset");
		buttons[5] = new Button(650,530,"Train");
		buttons[6] = new Button(850,410,"ComputerVis");
		coybert = new Neuralnet("braindata");
	}

	public void run(){								// runs game
		init();

		long start, elapsed, wait;					//Vars to keep track of game's run times

		while(running){
			start = System.nanoTime();
			
			update();
			draw();
			drawToScreen();

			elapsed = System.nanoTime() - start;
			wait = targetTime - elapsed/1000000;
			if(wait <0) wait = 5;
			try{
				Thread.sleep(wait);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			coybert.Evaluate(toInput(board));
			lastout = coybert.getOutput();
		}
	}

	private void update() {								// where code constantly runs
	}
	public void mousePressed(MouseEvent e) {
		// check for button presses
		for(int x=0; x<buttons.length; x++)
			if(buttons[x].hitbox.contains(e.getPoint())){
				if(buttons[x].text.equals("Evaluate")){
					coybert.Evaluate(toInput(board));
					lastout = coybert.getOutput();
				}
				else if(buttons[x].text.equals("Clear")){
					for(int y=0; y<board.length; y++)
						for(int z=0; z<board[y].length; z++)
							board[y][z] = false;
					leftBound = 0;
					rightBound = 0;
					topBound = 0;
					bottomBound = 0;
				}
				else if(buttons[x].text.equals("Toggle Grid")){
					toggrid = !toggrid;
				}
				else if(buttons[x].text.equals("Reset")){
					refresh();
					leftBound = 0;
					rightBound = 0;
					topBound = 0;
					bottomBound = 0;
					coybert = new Neuralnet("braindata");
				}
				else if(buttons[x].text.contains("Eraser")){
					erasemode = !erasemode;
					buttons[x].text = "Eraser "+(erasemode?"On":"Off");
				}
				else if(buttons[x].text.contains("Train")){
					Train();
				}
				else if(buttons[x].text.contains("ComputerVis")){
					compvis = !compvis;
				}
				return;
			}
		try{	// y is row  \\  x is column
			int row = (e.getY()-10)/pixelWid, col = (e.getX()-10)/pixelWid;
			for(int x=0; x<thickness; x++)
				for(int y=0; y<thickness; y++)
					board[row-1+x][col-1+y] = erasemode?false:true;
		}catch(Exception q){};
	}

	public void mouseDragged(MouseEvent e) {
		try{
			int row = (e.getY()-10)/pixelWid, col = (e.getX()-10)/pixelWid;
			for(int x=0; x<thickness; x++)
				for(int y=0; y<thickness; y++)
					board[row-1+x][col-1+y] = erasemode?false:true;
		}catch(Exception q){};
	}
	private void draw() {								// printing things
		g.setColor(Color.white);		// white background
		g.fillRect(0,0,WIDTH,HEIGHT);

		g.setColor(Color.gray);			/// draw grid and drawing board
		if(compvis){
			for(int x=0; x<out.length; x++)
				for(int y=0; y<out[x].length; y++){
					if(toggrid)
						g.drawRect(y*comppixelWid+10, x*comppixelWid+10, comppixelWid, comppixelWid);
					if(out[x][y]==1.00){
						g.setColor(Color.black);
						g.fillRect(y*comppixelWid+10, x*comppixelWid+10, comppixelWid, comppixelWid);
						g.setColor(Color.gray);
					}
				}
		}
		else
			for(int x=0; x<board.length; x++)
				for(int y=0; y<board[x].length; y++){
					if(toggrid)
						g.drawRect(y*pixelWid+10, x*pixelWid+10, pixelWid, pixelWid);
					if(board[x][y]){
						g.setColor(Color.black);
						g.fillRect(y*pixelWid+10, x*pixelWid+10, pixelWid, pixelWid);
						g.setColor(Color.gray);
					}
				}

		for(int x=0; x<buttons.length; x++)		// draw buttons
			buttons[x].draw(g);

		g.setColor(Color.blue);
		for(int x=0; x<lastout.length; x++){

		}
		g.setColor(Color.black);
		g.drawString("OUTPUT:",870,70);
		double max = 0, minx= 0;
		for(int x=0; x<10; x++){
			if(lastout[x]>max){
				max = lastout[x];
				minx = x;
			}
		}

		for(int x=0; x<10; x++){
			int prob =(int) (Math.round(lastout[x]));
			prob = prob>100?100:prob<0?0:prob;
			if(x==(int)minx)
				g.setColor(Color.green);
			else
				g.setColor(Color.black);
			g.drawString(String.valueOf(x), 870, 100+(30*x));
			g.drawString(String.valueOf(prob)+"%", 900, 100+(30*x));
			g.drawString("("+String.valueOf(lastout[x])+")", 960, 100+(30*x));
		}
		
		g.setColor(Color.blue);
		g.drawLine((leftBound*pixelWid)+10, 10, (leftBound*pixelWid)+10, 10+(drawSize*pixelWid));
		g.drawLine((rightBound*pixelWid)+10, 10, (rightBound*pixelWid)+10, 10+(drawSize*pixelWid));
		g.drawLine(10, 10+(topBound*pixelWid), 10+(drawSize*pixelWid), 10+(topBound*pixelWid));
		g.drawLine(10, 10+(bottomBound*pixelWid), 10+(drawSize*pixelWid), 10+(bottomBound*pixelWid));
	}

	public void refresh(){
		coybert = new Neuralnet(new int[]{bSize*bSize,(int)(numHid), 10});
		inputs.clear();
		expec.clear();
	}
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();

		for(int x=0; x<=9; x++)
			if(key == 48+x){
				double[] toadd = toInput(board);
				inputs.add(toadd);
				double expout[] = new double[10];
				for(int y=0; y<10; y++)
					expout[y] = 0;
				expout[x] = 100;
				expec.add(expout);
				break;
			}
		if(key == KeyEvent.VK_T){
			Train();
		}
		else if(key == KeyEvent.VK_E){
			coybert.Evaluate(toInput(board));
			lastout = coybert.getOutput();
		}
		else if(key == KeyEvent.VK_Q){
			thickness -= thickness>1?1:0;
		}
		else if(key == KeyEvent.VK_W){
			thickness += thickness<20?1:0;
		}
//		else if(key == KeyEvent.VK_S){				// loading labled training data
//			saveTrainData();
//		}
		else if(key == KeyEvent.VK_L){				// loading labled data for learning
			loadTrainData(datafile);
		}
//		else if(key == KeyEvent.VK_ENTER){			// saving neural network data and connections
//			coybert.Savenet("braindata");
//		}
		else if(key == KeyEvent.VK_END){			// loading neural network data and connections
			coybert = new Neuralnet("briandata");
		}
	}

	public void Train(){
		double err=1.1;
		int x = 0;
		System.out.println("Training...");
		double learn = 0.01;
		for(x=1; x<100000 && (err>0.0001 || x<3000); x++){
			int ind = rand(0,inputs.size()-1);
			err= coybert.Learn(inputs.get(ind),expec.get(ind), 0.01, 0.00);
			if(x%500==0)			// pathetic temporary attempt at decay rate of learning factor
				learn *= 0.98;
			if(x%5000 == 0)
				System.out.println(" err="+err+" learn: "+learn);
		}
		System.out.println("Done in "+x/inputs.size()+" epochs ("+x+"total tests)");
	}
	public static void Train(double[][] in, double[][] out){
		double err=1.1;
		int x = 0, iter = 150000;
		System.out.println("Training...");
		double learn = 0.01;
		for(x=1; x<iter && (err>0.000001 || x<3000); x++){
			int ind = rand(0,in.length-1);
			err= coybert.Learn(in[ind],out[ind], learn, 0.00);
			if(x%500==0 && learn >0.0006)			// pathetic temporary attempt at decay rate of learning factor
				learn *= 0.985;
			if(x%500 == 0)
				System.out.println(" err="+err+" learn: "+learn+"         Loading: "+((double)(x/(iter/100)))+"%");
		}
		System.out.println("Done in "+x/in.length+" epochs ("+x+"total tests)");
	}
	private void drawToScreen() {						// scales and draws game with formating
		Graphics g2 = getGraphics();
		g2.drawImage(image, 0, 0, WIDTH ,  HEIGHT , null);
		g2.dispose();
	}
	public static int rand(int d, int e){
		return (int) (d + (int)(Math.random()*((e-d)+1)));
	}
	public void keyTyped(KeyEvent e) {
	}
	public void keyReleased(KeyEvent e) {
	}
	public double[] toInput(boolean[][] in){	// crops around digits, resizes them to fit in 30x30 neural input grid
		double[][] orig = new double[in.length][in.length];
		for(int x=0; x<out.length; x++)
			for(int y=0; y<out[x].length; y++)
				out[x][y] = 0;

		for(int x=0; x<in.length; x++)					// creating original pixelmap array from grid boolean input
			for(int y=0; y<in[x].length; y++)			
				orig[x][y] = in[x][y]?1:0;

		leftBound = 0;						// walls around number to crop
		rightBound = 0;
		topBound = 0;
		bottomBound = 0;
		int width = 0, height = 0;
		double maxima = 0;

		for(int row = 0; row<orig.length; row++)
			for(int col = 0; col<orig[row].length; col++)
				if(in[row][col]){
					topBound = row;
					row = orig.length;
					break;
				}

		for(int row = orig.length-1; row>=0; row--)			// finding boundries
			for(int col = 0; col<orig[row].length; col++)
				if(in[row][col]){
					bottomBound = row;
					row = -1;
					break;
				}

		for(int col = 0; col<orig.length; col++)
			for(int row = 0; row<orig[col].length; row++)
				if(in[row][col]){
					leftBound = col;
					col = orig.length;
					break;
				}

		for(int col = orig.length-1; col>=0; col--)
			for(int row = 0; row<orig[col].length; row++)
				if(in[row][col]){
					rightBound = col;
					col = -1;
					break;
				}

		height = bottomBound - topBound +1;		// calculating height and width and thier maximum
		width = rightBound - leftBound +1;
		maxima = height>width?height:width;

		if(height>width){					// adding white space to sides to make a perfect square
			int pixtoadd = height-width;
			while(pixtoadd>0){
				width++;
				if(pixtoadd%2==0)
					leftBound--;
				else
					rightBound++;
				pixtoadd--;
			}
		}
		else if(width>height){
			int pixtoadd = width-height;
			while(pixtoadd>0){
				height++;
				if(pixtoadd%2==0)
					topBound--;
				else
					bottomBound++;
				pixtoadd--;
			}
		}

		double[][] old = new double[height][width];
		for(int x=0; x<height; x++)						// cropping picture and putting in into old
			for(int y=0; y<width; y++){
				try{
					old[x][y] = in[topBound+x][leftBound+y]?1:0;
				}catch(Exception e){
					old[x][y] = 0;
				}
			}

		//old now contains cropped image
		//old is unknown width and height, needs to be resized to fit into out ( bSize*bSize )
		
		if(bSize<maxima)
			for(int x=0; x<out.length; x++)				// resizing to make big image in out smaller
				for(int y=0; y<out[x].length; y++)
					out[x][y] = old[(int)((x*height)/bSize)][(int)((y*width)/bSize)];
		else{
			double fach = height/bSize, facw = width/bSize;
			for(int x=0; x<out.length; x++)  // resizing to make small image in out bigger   (NEEDS ALOT OF FIXING)
			        for(int y=0; y<out[x].length; y++){
			                for(int x1=0; x1<fach; x1++)
			                        for(int y1=0; y1<facw; y1++)
			                                out[x][y] += old[(int)((x*height)/bSize)+x1][(int)((y*width)/bSize)+y1];
			                out[x][y] = out[x][y]>0?1:0;
			                //out[x][y] = Math.round((out[x][y]/(fach*facw)));
			        }
		}
		//System.out.print((int)out[x][y]+", ");
		//System.out.println();
		
		double act[] = new double[bSize*bSize];			// copying resized image into one dimentional array for output
		for(int x=0; x<out.length; x++)
			for(int y=0; y<out[x].length; y++)
				act[(bSize*x)+y] = out[x][y];

		return act;		
	}

	public Main(){			//  constructor
		super();
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setFocusable(true);
		requestFocus();
	}

	public static void loadTrainData(String filePath){
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(new File(filePath)));
			datasiz = Integer.parseInt(reader.readLine());			// number of training samples

			double[][]datainps = new double[datasiz][bSize*bSize];			// dimentions
			double[][]dataexps = new double[datasiz][10];

			for(int x=0; x<datasiz; x++){						// copying over input data
				String[] tempinps = reader.readLine().split(",");
				//System.out.println(tempinps.length);
				for(int y=0; y<tempinps.length; y++)
					datainps[x][y] = Double.parseDouble(tempinps[y]);
			}

			for(int x=0; x<datasiz; x++){
				String[] tempouts = reader.readLine().split(",");
				for(int y=0; y<tempouts.length; y++)
					dataexps[x][y] = Double.parseDouble(tempouts[y]);
			}

			Train(datainps,dataexps);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(reader!=null)
					reader.close();
			}catch(Exception e){}
		}
	}

	public static void saveTrainData(){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(datafile));
			int samples = inputs.size();
			out.write(String.valueOf(samples));							// write number of training samples
			out.newLine();

			for(int x=0; x<inputs.size(); x++){					
				for(int y=0; y<inputs.get(x).length; y++)
					out.write(String.valueOf(inputs.get(x)[y])+",");		// writes input pixel map data in doubles ex) 1.00,0.00,1.00
				out.newLine();
			}

			for(int x=0; x<expec.size(); x++){					
				for(int y=0; y<expec.get(x).length; y++)
					out.write(String.valueOf(expec.get(x)[y])+",");		// writes expected output data in doubles ex) 0,0,0,100,0,0,0,0
				out.newLine();
			}
			out.close();
		} catch (Exception e) {e.printStackTrace();}
	}
	public void addNotify(){				// declares parent status and adds listeners
		super.addNotify();
		if(thread == null){
			thread = new Thread(this);
			addKeyListener(this);
			addMouseListener(this);
			addMouseMotionListener(this);
			thread.start();
		}
	}
	public void mouseClicked(MouseEvent e) {
	}
	public void mouseReleased(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mouseMoved(MouseEvent e) {
	}
}
