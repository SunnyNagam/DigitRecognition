package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Button {
	int x, y, width, height, len, dis;
	String text;
	Rectangle hitbox = null;
	public Button(int x, int y, String text){
		this.x = x;
		this.y = y;
		this.width = 130;
		this.height = 60;
		this.text = text;
		len = text.length()*4;
		dis = (width)/2;
		hitbox = new Rectangle(x,y,width,height);
	}
	
	public void draw(Graphics2D g){
		g.setColor(Color.black);
		g.drawRect(x,y,width,height);
		g.drawString(text, x+dis-len, y+((height-5)/2)+5);
	}
}
