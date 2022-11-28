import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import cc.nnproject.keyboard.*;

// демка, как примерно это будет выглядеть в реальной программе
public class DemoCanvas extends Canvas implements KeyboardListener, KeyboardConstants {
	Keyboard keyboard;
	
	public DemoCanvas() {
		setFullScreenMode(true);
		keyboard = Keyboard.getKeyboard(false, getWidth(), getHeight());
		
		// стилизация, не обязятельно
		keyboard.setBackgroundColor(0x000000);
		keyboard.setButtonColor(0x404040);
		keyboard.setButtonHoverColor(0x606060);
		keyboard.setButtonOutlineColor(0x131313);
		keyboard.setTextColor(0xCDCDCD);
		keyboard.setTextShadowColor(0x2E2E2E);
		keyboard.setButtons(true);
		keyboard.setTextShadows(false);
		
		// начать с большой буквы
		keyboard.setShifted(true);
		// выбрать языки
		keyboard.setLanguages(new String[] { "en", "ru" });
		// поставить русский язык
		keyboard.setLanguage("ru");
		
		keyboard.setListener(this);
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
		g.setFont(Font.getDefaultFont());
		g.drawString(keyboard.getText(), 10, 10, 0);
	}
	
	public void pointerPressed(int x, int y) {
		if(!keyboard.pointerPressed(x, y)) {
			// чето делать здесь...
		}
	}
	
	public void pointerReleased(int x, int y) {
		if(!keyboard.pointerReleased(x, y)) {
			// чето делать здесь...
			if(!keyboard.isVisible()) {
				keyboard.show();
				repaint();
			}
		}
	}
	
	public void pointerDragged(int x, int y) {
		if(!keyboard.pointerDragged(x, y)) {
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
	
	public void requestRepaint() {
		repaint();
	}

}
