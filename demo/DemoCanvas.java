import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import cc.nnproject.keyboard.*;

// демка, как примерно это будет выглядеть в реальной программе
public class DemoCanvas extends Canvas implements KeyboardListener, KeyboardConstants {
	Keyboard keyboard;
	private Font font;
	
	public DemoCanvas() {
		setFullScreenMode(true);
		keyboard = Keyboard.getKeyboard(this, true, getWidth(), getHeight());
		font = Font.getDefaultFont();
		keyboard.setTextFont(font);
		keyboard.setListener(this);
		keyboard.setTextHint("Что-то...");
		keyboard.setTextColor(0);
		keyboard.setTextHintColor(0x7F7F7F);
		keyboard.setCaretColor(0);
		keyboard.show();
	}
	
	protected void paint(Graphics g) {
		int screenWidth = getWidth();
		int screenHeight = getHeight();
		int height = screenHeight;
		if(keyboard.isVisible()) {
			height -= keyboard.paint(g, screenWidth, screenHeight);
			g.clipRect(0, 0, screenWidth, height);
		}
		// чето делать здесь...
		g.setColor(-1);
		g.fillRect(0, 0, screenWidth, height);
		g.setColor(0);
		keyboard.drawTextBox(g, 0, 0, screenWidth, height);
	}
	
	public void pointerPressed(int x, int y) {
		if(!keyboard.isVisible() || !keyboard.pointerPressed(x, y)) {
			// чето делать здесь...
		}
	}
	
	public void pointerReleased(int x, int y) {
		if(!keyboard.isVisible() || !keyboard.pointerReleased(x, y)) {
			// чето делать здесь...
			keyboard.show();
			repaint();
		}
	}
	
	public void pointerDragged(int x, int y) {
		if(!keyboard.isVisible() || !keyboard.pointerDragged(x, y)) {
			// чето делать здесь...
		}
	}
	
	public void keyPressed(int key) {
		if(!keyboard.isVisible() || !keyboard.keyPressed(key)) {
			// чето делать здесь...
			keyboard.show();
			repaint();
		}
	}
	
	public void keyReleased(int key) {
		if(!keyboard.isVisible() || !keyboard.keyReleased(key)) {
			// чето делать здесь...
		}
	}
	
	public void keyRepeated(int key) {
		if(!keyboard.isVisible() || !keyboard.keyRepeated(key)) {
			// чето делать здесь...
		}
	}

	public boolean appendChar(char c) {
		return true;
	}

	public void charRemoved() {
	}

	public void langChanged() {
	}

	public void newLine() {
	}

	public void done() {
		keyboard.hide();
		// чето делать здесь...
	}

	public void cancel() {
		repaint();
	}
	
	public void requestRepaint() {
		repaint();
	}

	public boolean removeChar() {
		return true;
	}

	public void textUpdated() {
	}

	public void requestTextBoxRepaint() {
		repaint();
	}

}
