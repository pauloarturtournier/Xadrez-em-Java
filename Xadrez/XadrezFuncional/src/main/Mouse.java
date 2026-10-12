package main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Mouse extends MouseAdapter{
	
	public int x, y;
	public boolean pressed;
	
	@Override
	public void mousePressed(MouseEvent e) {// caso contrario essa da verdadeira
		pressed = true;
	}
	@Override
	public void mouseReleased(MouseEvent e) {//se o player soltar o botao do mouse essa função da falsa
		pressed = false;
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		x = e.getX();
		y = e.getY();
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		x = e.getX();
		y = e.getY();
	}
}
