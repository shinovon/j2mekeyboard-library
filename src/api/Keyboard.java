package api;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class Keyboard implements KeyboardConstants {
	
	private static final char BSL = '\\';
	private static final char QOT = '\'';

	private static final int SHIFT = 1;
	private static final int BACKSPACE = 2;
	private static final int SMALL_SHIFT = 3;
	private static final int SMALL_BACKSPACE = 4;
	private static final int LANG = 5;
	private static final int MODE = 6;
	private static final int RETURN = 7;
	private static final int CANCEL = 8;
	private static final int SPACE = ' ';
	
	private static final int[][][] layouts = {/*en*/ {
			{'q','w','e','r','t','y','u','i','o','p'},
			{'a','s','d','f','g','h','j','k','l'},
			{SHIFT,'z','x','c','v','b','n','m',BACKSPACE},
			{MODE,LANG,',',SPACE,'/','.',RETURN}
		}, /*ru*/ {
			{'й','ц','у','к','е','н','г','ш','щ','з','х'}, 
			{'ф','ы','в','а','п','р','о','л','д','ж','э'}, 
			{SMALL_SHIFT,'я','ч','с','м','и','т','ь','б','ю',SMALL_BACKSPACE},
			{MODE,LANG,',',SPACE,'/','.',RETURN}
		}, /*spec 1*/ {
			{'1','2','3','4','5','6','7','8','9','0'},
			{'@','#','$','%','&','*','-','+','(',')'},
			{SHIFT,'!','"',QOT,':',';',BSL,'?',BACKSPACE},
			{MODE,LANG,',',SPACE,'/','.',RETURN}
		}, /*spec 2*/ {
			{'1','2','3','4','5','6','7','8','9','0'},
			{'~','`','|','^','[',']','_','=','{','}'},
			{SHIFT,'<' , '>' , 0 , 0 , 0 , 0 , 0,BACKSPACE},
			{MODE,LANG,',',SPACE,'/','.',RETURN}
		},
	};
	
	private static final String[] langs = {
			"en",
			"ru"
	};
	
	private static final int[] layoutsMode = {
			0,
			0,
			1,
			1,
	};

	private int[][][] widths;
	private int[][][] positions;
	private int[][] offsets;

	private static int keyStartY;
	private static int keyEndY;
	private static int keyMarginY;
	private static int keyHeight;
	
	private int keyboardHeight;
	private int Y;
	
	private int currentLayout;
	private int lang;
	private int spec;
	
	private boolean visible;
	private boolean keepShifted;
	private boolean shifted;
	
	private KeyboardListener listener;
	
	private String text = "";
	
	private int mode; // TODO
	private boolean multiLine;
	
	private boolean pressed;
	private int px;
	private int py;
	private long pt;
	
	private int screenWidth;
	private int screenHeight;

	private Thread repeatThread;
	private Object pressLock = new Object();
	private Font font;
	private int holdTime = 500;
	private int repeatTime = 100;

	private int keyTextY;
	private int fontHeight;
	
	// стиль
	private int bgColor = DEFAULT_BACKGROUND_COLOR;
	private int textColor = DEFAULT_TEXT_COLOR;
	private int textShadowColor = DEFAULT_TEXT_SHADOW_COLOR;
	private int keyButtonColor = DEFAULT_BUTTON_COLOR;
	private int keyButtonHoverColor = DEFAULT_BUTTON_HOVER_COLOR;
	private int keyButtonOutlineColor = DEFAULT_BUTTON_OUTLINE_COLOR;
	private boolean drawButtons = DEFAULT_BUTTONS;
	private boolean drawShadows = DEFAULT_TEXT_SHADOWS;
	
	private Keyboard(int mode, boolean multiLine, int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.multiLine = multiLine;
		repeatThread = new Thread("Key Repeat Thread") {
			public void run() {
				try {
					int count = 0;
					while(true) {
						if(pressed) {
							if(count > 10) {
								if(System.currentTimeMillis() - pt >= holdTime) {
									repeatPress(px, py);
									Thread.sleep(repeatTime);
									requestRepaint();
									continue;
								}
							} else {
								count++;
							}
							requestRepaint();
						} else {
							count = 0;
							synchronized(pressLock) {
								pressLock.wait();
							}
						}
						Thread.sleep(50);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		repeatThread.start();
		font = Font.getFont(0, 0, 0);
		fontHeight = font.getHeight();
		layout();
	}

	// режимы пока не придумал
	public static Keyboard initialize(int mode, boolean multiLine, int screenWidth, int screenHeight) {
		return new Keyboard(mode, multiLine, screenWidth, screenHeight);
	}
	
	private boolean layout() {
		keyStartY = 2;
		keyEndY = 2;
		int h = (int) (0.1D * screenHeight);
		if(screenHeight == 320) {
			h = 32;
		}
		if(screenHeight == 400) {
			h = 40;
		}
		if(screenHeight == 640) {
			h = 58;
		}
		keyHeight = h;
		//keyMarginY = 2;
		int w1 = (int) (screenWidth/10D);
		widths = new int[layouts.length][4][];
		positions = new int[layouts.length][4][];
		offsets = new int[layouts.length][4];
		for(int l = 0; l < layouts.length; l++) {
			double dw = (double) screenWidth / (double)layouts[l][0].length;
			int w = (int) dw;
			int fz = layouts[l][2].length-2;
			int fw = ((int) (screenWidth - dw * fz)) >> 1;
			for(int row = 0; row < 4; row++) {
				if(row == 3) {
					w = w1;
					fw = ((int) (screenWidth - w * 7)) >> 1;
				}
				int x = 0;
				int c1 = layouts[l][row].length;
				widths[l][row] = new int[c1];
				positions[l][row] = new int[c1];
				for(int col = 0; col < c1; col++) {
					int key = layouts[l][row][col];
					int kw = w;
					switch(key) {
					case SHIFT:
						kw = fw;
						break;
					case BACKSPACE:
						kw = fw;
						break;
					case SMALL_SHIFT:
						kw = w;
						break;
					case SMALL_BACKSPACE:
						kw = w;
						break;
					case LANG:
						kw = w;
						break;
					case MODE:
						kw = fw;
						break;
					case RETURN:
						kw = fw;
						break;
					case CANCEL:
						break;
					case SPACE:
						kw *= 3;
						break;
					case 0:
						kw = w;
						break;
					default:
						kw = w;
						break;
					}
					widths[l][row][col] = kw;
					positions[l][row][col] = x;
					x+=kw;
				}
				offsets[l][row] = (screenWidth - x) >> 1;
			}
		}
		keyboardHeight = keyStartY + keyEndY + (keyHeight+keyMarginY)*4;
		keyTextY = ((keyHeight - fontHeight) >> 1) + 1;
		return true;
	}

	public void setListener(KeyboardListener listener) {
		this.listener = listener;
	}
	
	// текст
	
	public String getText() {
		return text;
	}
	
	public int getLength() {
		return text.length();
	}
	
	public void setText(String s) {
		text = s;
	}
	
	public void appendText(String s) {
		text += s;
	}
	
	public void removeChar(int index) {
		text = text.substring(0, index) + text.substring(index+1);
	}
	
	public void remove(int start, int end) {
		text = text.substring(0, start) + text.substring(end+1);
	}
	
	public void clear() {
		text = "";
	}
	
	public void setShifted(boolean shifted) {
		shifted = true;
	}
	
	public void setLanguage(int language) {
		lang = language;
	}
	
	// почти то же что и очистка но будет возвращать язык и раскладку на дефолтные
	public void reset() {
		text = "";
		shifted = false;
		keepShifted = false;
		lang = LANG_EN;
	}
	
	public int getHeight() {
		return keyboardHeight;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void show() {
		visible = true;
	}
	
	public void hide() {
		visible = false;
	}
	
	// возвращает сколько высоты экрана забрало
	public int paint(Graphics g, int screenWidth, int screenHeight) {
		if(!visible) return 0;
		if(this.screenWidth == 0 || screenWidth != this.screenWidth || screenHeight != this.screenHeight) {
			this.screenWidth = screenWidth;
			this.screenHeight = screenHeight;
			layout();
		}
		Y = screenHeight - keyboardHeight;
		g.translate(0, Y);
		g.setFont(font);
		g.setColor(bgColor);
		g.fillRect(0, 0, screenWidth, keyboardHeight);
		
		int y = keyStartY;
		int mode = currentLayout;
		for(int row = 0; row < layouts[mode].length; row++) {
			int x = offsets[mode][row];
			for(int i = 0; i < layouts[mode][row].length; i++) {
				x += drawKey(g, row, i, x, y, mode);
			}
			y += keyHeight + keyMarginY;
		}
		g.translate(0, -Y);
		return keyboardHeight;
	}

	private void drawKeyButton(Graphics g, int x, int y, int w) {
		if(!drawButtons) return;
		int h = keyHeight;
		g.setColor(pressed && px > x && px < x + w && py-Y > y && py-Y < y+h ? keyButtonHoverColor : keyButtonColor);
		//x += 1;
		//y += 1;
		//w -= 2;
		//h -= 2;
		g.fillRect(x, y, w, h);
		g.setColor(keyButtonOutlineColor);
		g.drawRect(x, y, w, h);
	/*
		g.drawLine(x+1, y+1, x+1, y+1);
		g.drawLine(x+w-2, y+1, x+w-2, y+1);
		g.drawLine(x+1, y+h-1, x+1, y+h-1);
		g.drawLine(x+w-2, y+h-1, x+w-2, y+h-1);
	*/
	}

	private int drawKey(Graphics g, int row, int column, int x, int y, int mode) {
		int w = widths[mode][row][column];
		drawKeyButton(g, x, y, w);
		int key = layouts[mode][row][column];
		String s = null;
		char c = 0;
		boolean b = false;
		switch(key) {
		case SHIFT:
			b = true;
			s = layoutsMode[currentLayout] == 1 ? (spec+1)+"/2" : "shift";
			break;
		case BACKSPACE:
			b = true;
			s = "<-";
			break;
		case SMALL_SHIFT:
			c = '^';
			break;
		case SMALL_BACKSPACE:
			b = true;
			s = "<-";
			break;
		case LANG:
			b = true;
			s = langs[lang];
			break;
		case MODE:
			b = true;
			s = layoutsMode[currentLayout] == 0 ? "!#1" : "ABC";
			break;
		case RETURN:
			b = true;
			s = "OK";
			break;
		case CANCEL:
			b = true;
			s = "cancel";
			break;
		case SPACE:
			b = true;
			s = "space";
			break;
		case 0:
			break;
		default:
			c = (char) key;
			break;
		}
		y += keyTextY;
		if(b) {
			x += (w - font.stringWidth(s)) >> 1;
			if(drawShadows) {
				g.setColor(textShadowColor);
				g.drawString(s, x+1, y+1, 0);
				g.drawString(s, x+1, y-1, 0);
				g.drawString(s, x-1, y+1, 0);
				g.drawString(s, x-1, y-1, 0);
			}
			g.setColor(textColor);
			g.drawString(s, x, y, 0);
		} else if(key != 0) {
			if(shifted && mode < langs.length)
				c = Character.toUpperCase(c);
			x += (w - font.charWidth(c)) >> 1;
			if(drawShadows) {
				g.setColor(textShadowColor);
				g.drawChar(c, x+1, y+1, 0);
				g.drawChar(c, x+1, y-1, 0);
				g.drawChar(c, x-1, y+1, 0);
				g.drawChar(c, x-1, y-1, 0);
			}
			g.setColor(textColor);
			g.drawChar(c, x, y, 0);
		}
		return widths[mode][row][column];
	}
	
	// true если забрать, false если отдать
	
	public boolean pointerPressed(int x, int y) {
		if(y >= Y && visible) {
			pressed = true;
			pt = System.currentTimeMillis();
			px = x;
			py = y;
			synchronized(pressLock) {
				pressLock.notify();
			}
			requestRepaint();
			return true;
		}
		return false;
	}
	
	public boolean pointerReleased(int x, int y) {
		if(pressed) {
			handleTap(x, y-Y, false);
			pressed = false;
			requestRepaint();
			return true;
		}
		return false;
	}
	
	public boolean pointerDragged(int x, int y) {
		if(pressed) {
			// filter
			if(py == x && py == y) return true;
			px = x;
			py = y;
			return true;
		}
		return false;
	}

	protected void repeatPress(int x, int y) {
		handleTap(x, y-Y, true);
		requestRepaint();
	}
	
	private void handleTap(int x, int y, boolean repeated) {
		int row = div(y - 4, keyHeight+keyMarginY);
		if(row == 4) row = 3;
		if(repeated && row != 2) return;
		if(row >= 0 && row <= 3) {
			if(x < 0 || x > screenWidth) return;
			int mode = currentLayout;
			int kx = offsets[mode][row];
			for(int col = 0; col < layouts[mode][row].length; col++) {
				int w = widths[mode][row][col];
				if(x > kx && x < kx+w) {
					int key = layouts[mode][row][col];
					switch(key) {
					case SHIFT:
						if(!repeated) shiftKey();
						break;
					case BACKSPACE:
						backspace();
						break;
					case SMALL_SHIFT:
						if(!repeated) shiftKey();
						break;
					case SMALL_BACKSPACE:
						backspace();
						break;
					case LANG:
						if(!repeated) langKey();
						break;
					case MODE:
						if(!repeated) modeKey();
						break;
					case RETURN:
						if(!repeated) enter();
						break;
					case CANCEL:
						//if(!repeated) cancel();
						break;
					case SPACE:
						if(!repeated) space();
						break;
					case 0:
						break;
					default:
						if(!repeated) type((char) key);
						break;
					}
					break;
				}
				kx += w;
			}
		}
	}
	
	private void modeKey() {
		shifted = false;
		if(layoutsMode[currentLayout] == 0) {
			currentLayout = langs.length;
			spec = 0;
		} else if(layoutsMode[currentLayout] == 1) {
			currentLayout = lang;
		}
		requestRepaint();
	}
	
	private void langKey() {
		shifted = false;
		int l = langs.length;
		lang++;
		if(lang >= l) {
			lang = 0;
		}
		currentLayout = lang;
		requestRepaint();
	}
	
	private int div(int i, int j) {
		double d = i;
		d /= j;
		return (int)(d - d % 1);
	}

	private void enter() {
		if(multiLine) {
			type('\n');
		} else {
			text = "";
			requestRepaint();
		}
	}

	private void shiftKey() {
		if(layoutsMode[currentLayout] == 1) {
			keepShifted = false;
			shifted = false;
			spec++;
			if(spec > 1) spec = 0;
			currentLayout = spec+langs.length;
		} else if(shifted && !keepShifted) {
			keepShifted = true;
		} else {
			keepShifted = false;
			shifted = !shifted;
		}
	}

	private void typed() {
		requestRepaint();
	}

	private void type(char c) {
		if(shifted) {
			c = Character.toUpperCase(c);
			if(!keepShifted) shifted = false;
		}
		if(listener != null && !listener.appendChar(c)) return;
		text += c;
		typed();
	}
	
	private void space() {
		if(listener != null && !listener.appendChar(' ')) return;
		text += " ";
		typed();
	}
	
	private void backspace() {
		if(text.length() > 0) {
			text = text.substring(0, text.length() - 1);
		}
		if(listener != null) listener.charRemoved();
		requestRepaint();
	}
	
	// стиль
	
	public void setBackgroundColor(int color) {	
		this.bgColor = color;
	}
	
	public void setButtonColor(int color) {	
		this.keyButtonColor = color;
	}
	
	public void setButtonHoverColor(int color) {	
		this.keyButtonHoverColor = color;
	}
	
	public void setButtonOutlineColor(int color) {	
		this.keyButtonOutlineColor = color;
	}
	
	public void setTextColor(int color) {	
		this.textColor = color;
	}
	
	public void setTextShadowColor(int color) {	
		this.textShadowColor = color;
	}
	
	public void setButtons(boolean enabled) {
		this.drawButtons = enabled;
	}
	
	public void setTextShadows(boolean enabled) {
		this.drawShadows = enabled;
	}
	
	private void requestRepaint() {
		if(listener != null) listener.requestRepaint();
	}

}
