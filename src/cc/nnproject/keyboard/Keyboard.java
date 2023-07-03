package cc.nnproject.keyboard;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import cc.nnproject.json.AbstractJSON;
import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public final class Keyboard implements KeyboardConstants {
	
	private int[][][] layouts;
	
	private String[] supportedLanguages;
	private int[] supportedLanguagesIdx;
	
	private String[] langs;
	private int[] langsIdx;
	
	private int[] specs;
	
	private int[] layoutTypes;

	private int[][][] widths;
	private int[][][] positions;
	private int[][] offsets;

	private int keyStartY;
	private int keyEndY;
	private int keyMarginY;
	private int keyHeight;
	
	private int keyboardHeight;
	private int Y;
	
	private int currentLayout;
	private int lang;
	private int spec;
	
	boolean visible;
	private boolean keepShifted;
	private boolean shifted;
	
	private KeyboardListener listener;
	
	private String text = "";
	
	private int size;
	
	private int keyboardType;
	private boolean multiLine;
	
	boolean pressed;
	boolean dragged;
	int px;
	int py;
	long pt;
	
	private int screenWidth;
	private int screenHeight;
	
	private char[][][] keyLayouts;
	int lastKey;
	int keyRepeatTicks = -1;
	boolean keyPressed;
	long kt;
	private int physicalKeyboard;
	private char[] keyVars;
	private int keyVarIdx;
	private char keyBuffer;
	private int currentPhysicalLayout;
	private boolean keyWasRepeated;

	private Thread repeatThread;
	Object pressLock = new Object();

	private int keyTextY;
	
	// стиль
	private int bgColor = DEFAULT_BACKGROUND_COLOR;
	private int keyTextColor = DEFAULT_KEY_TEXT_COLOR;
	private int keyTextShadowColor = DEFAULT_KEY_TEXT_SHADOW_COLOR;
	private int keyButtonColor = DEFAULT_BUTTON_COLOR;
	private int keyButtonHoverColor = DEFAULT_BUTTON_HOVER_COLOR;
	private int keyButtonOutlineColor = DEFAULT_BUTTON_OUTLINE_COLOR;
	private int caretColor = DEFAULT_CARET_COLOR;
	private boolean drawButtons = DEFAULT_BUTTONS;
	private boolean drawShadows = DEFAULT_TEXT_SHADOWS;
	private boolean roundButtons = DEFAULT_ROUND_BUTTONS;
	private int keyButtonPadding = DEFAULT_BUTTON_PADDING;
	private int textColor = DEFAULT_TEXT_COLOR;
	private int textHintColor = DEFAULT_TEXT_HINT_COLOR;
	
	private Font font = Font.getFont(0, 0, 0);
	private int fontHeight = font.getHeight();
	private String layoutPackRes;
	private boolean hasQwertyLayouts;
	private Font textFont = Font.getFont(0, 0, 8);
	private int textFontHeight = textFont.getHeight();

	boolean hasRepeatEvents;
	boolean hasPointerEvents;

	private Canvas canvas;
	
	// текстбокс
	protected int textBoxX;
	protected int textBoxY;
	private int textBoxWidth;
	private int textBoxHeight;
	private int removedTextWidth;
	private String textHint = "";
	public int caretPosition;
	public boolean caretFlash;
	private boolean textBoxShown;
	protected boolean textBoxPressed;
	protected int startPosition = -1;
	protected int endPosition = -1;
	protected int caretRow;
	protected int caretX;
	protected int caretCol;
	protected int startX;
	protected int startRow;
	protected int endX;
	protected int endRow;
	protected int endCol;
	protected int startCol;

	private String[] abc;
	
	private Keyboard(Canvas canvas, int keyboardType, boolean multiLine, int screenWidth, int screenHeight, String layoutPackRes) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.multiLine = multiLine;
		this.layoutPackRes = layoutPackRes == null ? DEFAULT_LAYOUT_PACK : layoutPackRes;
		this.keyboardType = keyboardType;
		if(canvas != null) {
			this.hasRepeatEvents = canvas.hasRepeatEvents();
			this.hasPointerEvents = canvas.hasPointerEvents();
			this.canvas = canvas;
		}
		
		// Physical keyboard checks
		String sysKeyboardType = System.getProperty("com.nokia.keyboard.type");
		if(sysKeyboardType == null) {
			physicalKeyboard = PHYSICAL_KEYBOARD_PHONE_KEYPAD;
			// Symbian 9.x check
			if(System.getProperty("com.symbian.midp.serversocket.support") != null ||
					System.getProperty("com.symbian.default.to.suite.icon") != null ||
					(System.getProperty("microedition.platform") != null &&
					System.getProperty("microedition.platform").indexOf("version=3.2") != -1)) {
				if(screenWidth > screenHeight) {
					physicalKeyboard = PHYSICAL_KEYBOARD_QWERTY;
				} else {
					physicalKeyboard = PHYSICAL_KEYBOARD_PHONE_KEYPAD;
				}
			}
		} else if(sysKeyboardType.equalsIgnoreCase("None")) {
			physicalKeyboard = PHYSICAL_KEYBOARD_NONE;
		} else if(sysKeyboardType.equalsIgnoreCase("PhoneKeypad")) {
			physicalKeyboard = PHYSICAL_KEYBOARD_PHONE_KEYPAD;
		} else {
			physicalKeyboard = PHYSICAL_KEYBOARD_QWERTY;
		}
		
		parseLayoutPack();
		layout();
	}

	/**
	 * Инициализация с дефолтными настройками
	 */
	public static Keyboard getKeyboard(Canvas canvas) {
		return new Keyboard(canvas, KEYBOARD_DEFAULT, false, 0, 0, null);
	}
	
	public static Keyboard getKeyboard(Canvas canvas, int keyboardType) {
		return new Keyboard(canvas, keyboardType, false, 0, 0, null);
	}

	/**
	 * Инициализация с дефолтной раскладкой
	 * @param multiLine
	 * @param screenWidth
	 * @param screenHeight
	 */
	public static Keyboard getKeyboard(Canvas canvas, boolean multiLine, int screenWidth, int screenHeight) {
		return new Keyboard(canvas, KEYBOARD_DEFAULT, multiLine, screenWidth, screenHeight, null);
	}
	
	/**
	 * Инициализация клавы
	 * @param keyboardType
	 * @param multiLine
	 * @param screenWidth
	 * @param screenHeight
	 */
	public static Keyboard getKeyboard(Canvas canvas, int keyboardType, boolean multiLine, int screenWidth, int screenHeight) {
		return new Keyboard(canvas, keyboardType, multiLine, screenWidth, screenHeight, null);
	}

	/**
	 * Инициализация клавы с кастомным паком раскладок
	 * @param keyboardType
	 * @param multiLine
	 * @param screenWidth
	 * @param screenHeight
	 * @param layoutPackRes
	 */
	public static Keyboard getKeyboard(Canvas canvas, int keyboardType, boolean multiLine, int screenWidth, int screenHeight, String layoutPackRes) {
		return new Keyboard(canvas, keyboardType, multiLine, screenWidth, screenHeight, layoutPackRes);
	}
	
	private void parseLayoutPack() {
		try {
			JSONObject json = (JSONObject) readJSONRes(layoutPackRes);
			String m;
			switch(keyboardType) {
			case KEYBOARD_URL:
				m = "url";
				break;
			case KEYBOARD_NUMERIC:
				m = "numeric";
				break;
			case KEYBOARD_DECIMAL:
				m = "decimal";
				break;
			case KEYBOARD_PHONE_NUMBER:
				m = "phone_number";
				break;
			case KEYBOARD_DEFAULT:
			default:
				m = "default";
				break;
			}
			json = json.getObject("keyboards");
			if(!json.has(m)) {
				throw new RuntimeException("Layout pack " + layoutPackRes + " does not have " + m + " keyboard!");
			}
			if(json.getObject(m).has("base")) {
				json = json.getObject(json.getObject(m).getString("base"));
			} else {
				json = json.getObject(m);
			}
			JSONArray arr = json.getArray("supported_languages");
			int i = arr.size();
			supportedLanguages = new String[i];
			supportedLanguagesIdx = new int[i];
			if(hasQwertyLayouts = i != 0) {
				arr.copyInto(supportedLanguages, 0, i);
			}
			arr = json.getArray("layouts");
			i = arr.size();
			layouts = new int[i][4][];
			keyLayouts = new char[i][10][];
			layoutTypes = new int[i];
			abc = new String[i];
			Enumeration e = arr.elements();
			i = 0;
			Vector specialsVector = new Vector();
			while(e.hasMoreElements()) {
				JSONObject j = (JSONObject) e.nextElement();
				String type = j.getNullableString("type");
				layoutTypes[i] = type == null ? 0 : type.equals("special") ? 1 : 0;
				if(type != null) {
					if(type.equals("qwerty")) {
						String lng = j.getNullableString("lang");
						if(lng != null) {
							for(int k = 0; k < supportedLanguages.length; k++) {
								if(supportedLanguages[k].equals(lng)) {
									supportedLanguagesIdx[k] = i;
									break;
								}
							}
						}
					} if(type.equals("special")) {
						specialsVector.addElement(new Integer(i));
					}
				}
				abc[i] = j.getNullableString("abc");
				int[][] l = new int[4][];
				char[][] o = new char[10][];
				JSONArray a = (JSONArray) readJSONRes(j.getString("res"));
				JSONArray t = a.getArray(0);
				for(int k = 0; k < 4; k++) {
					JSONArray b = t.getArray(k);
					int n = b.size();
					l[k] = new int[b.size()];
					for(int p = 0; p < n; p++) {
						try {
							l[k][p] = b.getInt(p);
						} catch (Exception e2) {
							l[k][p] = b.getString(p).charAt(0);
						}
					}
				}
				if(a.size() > 1) {
					JSONObject s = a.getObject(1);
					for(int k = 1; k < 10; k++) {
						JSONArray b = s.getArray(Integer.toString(k));
						int n = b.size();
						o[k] = new char[b.size()];
						for(int p = 0; p < n; p++) {
							try {
								o[k][p] = (char) b.getInt(p);
							} catch (Exception e2) {
								o[k][p] = b.getString(p).charAt(0);
							}
						}
					}
					keyLayouts[i] = o;
				}
				layouts[i] = l;
				i++;
			}
			int l = specialsVector.size();
			specs = new int[l];
			for(i = 0; i < l; i++) {
				specs[i] = ((Integer)specialsVector.elementAt(i)).intValue();
			}
			setLanguages(new String[0]);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.toString());
		}
	}
	
	private static AbstractJSON readJSONRes(String res) throws IOException {
		InputStream is = "".getClass().getResourceAsStream(KEYBOARD_LAYOUTS_DIR + res);
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		byte[] buf = new byte[128];
		int i;
		while((i = is.read(buf)) != -1) {
			o.write(buf, 0, i);
		}
		is.close();
		String s = new String(o.toByteArray(), "UTF-8"); 
		o.close();
		if(s.charAt(0) == '{')
			return JSON.getObject(s);
		else if(s.charAt(0) == '[')
			return JSON.getArray(s);
		return null;
	}
	
	private void layout() {
		if(screenWidth == 0 || screenHeight == 0) {
			return; 
		}
		keyStartY = 2;
		keyEndY = 2;
		int h = screenHeight / 10;
		if(screenHeight == 640) {
			h = 58;
		}
		keyHeight = h;
		//keyMarginY = 2;
		int w1 = screenWidth / 10;
		widths = new int[layouts.length][4][];
		positions = new int[layouts.length][4][];
		offsets = new int[layouts.length][4];
		for(int l = 0; l < layouts.length; l++) {
			double dw = (double) screenWidth / (double)layouts[l][0].length;
			int w = (int) dw;
			int fz = layouts[l][2].length-2;
			int fw = ((int) (screenWidth - dw * fz)) >> 1;
			for(int row = 0; row < 4; row++) {
				if(row == 3 && layouts[l][3].length < layouts[l][2].length) {
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
					case KEY_SHIFT:
					case KEY_BACKSPACE:
					case KEY_MODE:
					case KEY_RETURN:
						kw = fw;
						break;
					case KEY_SPACE:
						kw *= 3;
						break;
					}
					widths[l][row][col] = kw;
					positions[l][row][col] = x;
					x+=kw;
				}
				offsets[l][row] = (screenWidth - x) >> 1;
			}
		}
		keyboardHeight = keyStartY + keyEndY + (keyHeight + keyMarginY) * 4;
		keyTextY = ((keyHeight - fontHeight) >> 1) + 1;
	}

	/**
	 * Установка листенера клавиатуры
	 * @param listener
	 */
	public void setListener(KeyboardListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Введенный текст
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Длина введенного текста
	 */
	public int getLength() {
		return text.length();
	}
	
	public int getCaretPosition() {
		return caretPosition;
	}
	
	/**
	 * Установить текст
	 * @param s
	 */
	public void setText(String s) {
		if(size > 0 && s.length() > size) {
			s = s.substring(0, size);
		}
		caretPosition = s.length();
		text = s;
	}
	
	/**
	 * Добавить текст
	 * @param s
	 */
	public void appendText(String s) {
		if(size > 0 && text.length() >= size) return;
		if(size > 0 && text.length()+s.length() >= size) {
			text += s;
			text = text.substring(0, size);
			caretPosition = text.length();
			return;
		}
		caretPosition += s.length();
		text += s;
	}
	
	/**
	 * Вставить текст
	 * @param s
	 * @param index
	 */
	public void insertText(String s, int index) {
		if(size > 0 && text.length() >= size) return;
		text = text.substring(0, index) + s + text.substring(index);
		caretPosition += s.length();
	}
	
	/**
	 * Убрать символ из текста
	 * @param index
	 */
	public void removeChar(int index) {
		text = text.substring(0, index) + text.substring(index + 1);

		caretPosition --;
	}
	
	/**
	 * Убрать кусок текста
	 * @param start
	 * @param end
	 */
	public void remove(int start, int end) {
		text = text.substring(0, start) + text.substring(end);
		caretPosition -= end - start;
		if(caretPosition < 0) caretPosition = 0;
	}

	/**
	 * Очистка текста
	 */
	public void clear() {
		text = "";
		caretPosition = 0;
	}
	
	/**
	 * Шифт.
	 */
	public void setShifted(boolean shifted) {
		this.shifted = true;
		this.keepShifted = false;
	}
	
	/**
	 * Выбрать язык клавиатуры
	 * <p><i>Если в текущей клавиатуре нет QWERTY раскладок, вызов будет проигнорирован</i></p>
	 * @param language
	 */
	public void setLanguage(String language) {
		if(!hasQwertyLayouts) {
			return;
		}
		for(int i = 0; i < langs.length; i++) {
			if(langs[i].equalsIgnoreCase(language)) {
				lang = i;
				currentLayout = langsIdx[i];
				break;
			}
		}
	}
	
	/**
	 * Сброс ввода
	 * <p>Почти то же, что и очистка но будет возвращать язык и раскладку на значения по умолчанию</p>
	 */
	public void reset() {
		text = "";
		shifted = false;
		keepShifted = false;
		if(hasQwertyLayouts) {
			currentLayout = langsIdx[lang = 0];
		}
	}
	
	/**
	 * @return Высота виртуальной клавиатуры
	 */
	public int getHeight() {
		return keyboardHeight;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	/**
	 * Показать клавиатуру
	 */
	public void show() {
		if(visible) {
			return;
		}
		visible = true;
		try {
			repeatThread = new KeyboardThread(this);
			repeatThread.start();
		} catch (Exception e) {
		}
	}
	
	/**
	 * Скрыть клавиатуру
	 * <p><i>Не забудьте вызвать это при скрытии экрана с клавиатурой</i></p>
	 */
	public void hide() {
		visible = false;
		try {
			repeatThread.interrupt();
		} catch (Exception e) {
		}
	}
	
	/**
	 * Отрисовка виртуальной клавиатуры
	 * @param g
	 * @param screenWidth
	 * @param screenHeight
	 * @return Сколько высоты экрана забрало
	 */
	public int paint(Graphics g, int screenWidth, int screenHeight) {
		if(!visible || !hasPointerEvents) return 0;
		// если размеры экрана изменились, то сделать релэйаут
		if(this.screenWidth == 0 || screenWidth != this.screenWidth || screenHeight != this.screenHeight) {
			this.screenWidth = screenWidth;
			this.screenHeight = screenHeight;
			layout();
		}
		g.translate(0, Y = screenHeight - keyboardHeight);
		g.setFont(font);
		g.setColor(bgColor);
		g.fillRect(0, 0, screenWidth, keyboardHeight);
		int y = keyStartY;
		int l = currentLayout;
		for(int row = 0; row < layouts[l].length; row++) {
			int x = offsets[l][row];
			for(int i = 0; i < layouts[l][row].length; i++) {
				x += drawKey(g, row, i, x, y, l);
			}
			y += keyHeight + keyMarginY;
		}
		g.translate(0, -Y);
		return keyboardHeight;
	}
	
	public void drawTextBox(Graphics g, int x, int y, int width, int height) {
		textBoxShown = true;
		this.textBoxX = x;
		this.textBoxY = y;
		this.textBoxWidth = width;
		this.textBoxHeight = height;
		g.setFont(textFont);
		int textY = multiLine ? y : y + (height - textFont.getHeight()) >> 1;
		String s = "";
		boolean hint = text.length() == 0;
		if(!hint) {
			g.setColor(textColor);
			s = text;
		} else {
			g.setColor(textHintColor);
			s = textHint;
		}
		int th   = textFont.getHeight() + 2;
		if(multiLine && !hint) {
			String[] arr = getTextArray(s, textFont, width - 4);
			int yo = 0;
			while(th * arr.length > height + yo) {
				yo += th;
			}
			int ty = -yo;
			for(int i = 0; i < arr.length; i++) {
				if(ty >= 0) {
					g.drawString(arr[i], x + 2, ty + textY, 0);
				}
				ty += th;
			}
			int cx = x + 2 + caretX;
			int cy = textY + (th * caretRow);
			if(endPosition != -1) {
				int strow = startRow;
				int endrow = endRow;
				int stx = startX;
				int endx = endX;
				int stcol = startCol;
				int endcol = endCol;
				if(startPosition > endPosition) {
					strow = endRow;
					endrow = startRow;
					stx = endX;
					endx = startX;
					stcol = endCol;
					endcol = startCol;
				}
				for(int i = strow; i <= endrow; i++) {
					int lx = 0;
					int substart = 0;
					int subend = arr[i].length();
					if(i == strow) {
						lx = stx;
						substart = stcol;
					}
					int lw = -1;
					if(i == endrow) {
						lw = endx - (i == strow ? stx : 0);
						subend = endcol;
					}
					String ls = arr[i].substring(substart, subend);
					if(lw < 0) {
						lw = textFont.stringWidth(ls);
					}
					int ly = textY - yo + (th * i);
					g.setColor(caretColor);
					g.fillRect(x + 2 + lx, ly, lw, th);
					g.setColor(~caretColor);
					g.drawString(ls, x + 2 + lx, ly, 0);
				}
			}
			drawCaret(g, cx, cy);
		} else {
			if(!hint) {
				int ww = 0;
				while(textFont.stringWidth(s) >= width - 4) {
					ww += textFont.charWidth(s.charAt(0));
					s = s.substring(1);
				}
				removedTextWidth = ww;
			} else {
				removedTextWidth = 0;
			}
			g.drawString(s, x + 2, textY, 0);
			if(endPosition != -1) {
				int start = Math.min(startPosition, endPosition);
				int end = Math.max(startPosition, endPosition);
				int startW = textFont.stringWidth(text.substring(0, start)) - removedTextWidth;
				String selected = text.substring(start, end);
				int selectedW = textFont.stringWidth(selected) - removedTextWidth;
				g.setColor(caretColor);
				g.fillRect(x + 2 + startW, textY, selectedW, th);
				g.setColor(~caretColor);
				g.drawString(selected, x + 2 + startW, textY, 0);
			}
			s = text;
			if(s.length() > 0 && caretPosition != s.length()) {
				s = s.substring(0, caretPosition);
			}
			drawCaret(g, x + textFont.stringWidth(s) + 2, textY);
		}
	}
	
	protected void setCaretPosition(int x, int y) {
		x -= 2;
		if(multiLine) {
			String[] arr = getTextArray(text, textFont, textBoxWidth - 4);
			int textHeight = textFont.getHeight() + 2;
			int line = y / textHeight;
			if(arr != null && line >= 0 && line < arr.length) {
				int lineLength = arr[line].length();
				int i = 0;
				int j = 0;
				caretCol = 0;

				int k;
				for (k = x; caretCol <= lineLength; ++caretCol) {
					j = i;
					if ((i = textFont.substringWidth(arr[line], 0, caretCol)) >= k) {
						break;
					}
				}
				int l = (i - j) / 2;
				int m;
				if (k >= j + l) {
					m = i;
				} else {
					--caretCol;
					m = j;
				}
				caretX = m;
				caretRow = line;
				caretCol = Math.min(Math.max(0, caretCol), lineLength);
				int n = caretCol;
				while (true) {
					caretPosition = n;
					if (line <= 0) {
						return;
					}
					--line;
					n = caretPosition + arr[line].length();
				}
			}
			return;
		}
		int xx = x + removedTextWidth;
		int i = 0;
		for(i = text.length(); i > 0; i--) {
			if(textFont.stringWidth(text.substring(0, i)) < xx) {
				break;
			}
		}
		setCaretPostion(i);
	}
	
	private void drawCaret(Graphics g, int caretX, int caretY) {
		if(keyBuffer != 0) {
			char c = keyBuffer;
			if(shifted) c = Character.toUpperCase(c);
			int w = textFont.charWidth(c);
			g.setColor(caretColor);
			g.fillRect(caretX, caretY, w, textFontHeight);
			g.setColor(~caretColor);
			g.drawChar(c, caretX, caretY, 0);
			/*
			if(keyVars != null) {
				w = textFont.charsWidth(keyVars, 0, keyVars.length)+1;
				g.setColor(~caretColor);
				g.fillRect(caretX, caretY-textFontHeight, w, textFontHeight);
				g.setColor(caretColor);
				g.drawRect(caretX, caretY-textFontHeight, w, textFontHeight);
				g.setColor(caretColor);
				g.drawChars(keyVars, 0, keyVars.length, caretX+1, caretY-textFontHeight, 0);
			}
			*/
		}
		if(caretFlash) {
			g.setColor(caretColor);
			g.drawLine(caretX, caretY, caretX, caretY + textFontHeight);
		}
	}
	
	public void drawOverlay(Graphics g) {
		if(physicalKeyboard == PHYSICAL_KEYBOARD_PHONE_KEYPAD && keyboardType == KEYBOARD_DEFAULT && !hasPointerEvents) {
			String s = abc[currentPhysicalLayout];
			int w = textFont.stringWidth(s.toUpperCase());
			g.setColor(0xaaaaaa);
			g.fillRect(0, 0, w, textFontHeight);
			g.setColor(0);
			g.drawString(shifted ? keepShifted ? s.toUpperCase() : Character.toUpperCase(s.charAt(0)) + s.substring(1) : s, 0, 0, 0);
		}
	}

	private void drawKeyButton(Graphics g, int x, int y, int w) {
		if(!drawButtons) return;
		int h = keyHeight;
		g.setColor(pressed && px > x && px < x + w && py-Y > y && py-Y < y+h ? keyButtonHoverColor : keyButtonColor);
		x += keyButtonPadding;
		y += keyButtonPadding;
		w -= keyButtonPadding*2;
		h -= keyButtonPadding*2;
		g.fillRect(x, y, w, h);
		g.setColor(keyButtonOutlineColor);
		// если паддинг = 0, рисовать границы
		if(keyButtonPadding == 0) {
			g.drawRect(x, y, w, h);
		} else if(roundButtons) {
			g.drawLine(x, y, x, y);
			g.drawLine(x+w-1, y, x+w-1, y);
			g.drawLine(x, y+h-1, x, y+h-1);
			g.drawLine(x+w-1, y+h-1, x+w-1, y+h-1);
		} 
	}

	private int drawKey(Graphics g, int row, int column, int x, int y, int l) {
		int key = layouts[l][row][column];
		int w = widths[l][row][column];
		if(key == KEY_UNDEFINED) return w;
		drawKeyButton(g, x, y, w);
		String s = null;
		char c = 0;
		boolean b = false;
		switch(key) {
		case KEY_SHIFT:
			b = true;
			// в спец.символах это должно быть табами
			// если ширина кнопки такая же как у обычных клавиш, то отображать ^ вместо шифта
			// и вообще надо приделать картинки
			s = layoutTypes[currentLayout] == 1 ? (spec+1)+"/2" : w <= widths[l][0][0] ? "^" : "shift";
			break;
		case KEY_BACKSPACE:
			b = true;
			s = "<-";
			break;
		case KEY_LANG:
			b = true;
			s = langs[lang];
			break;
		case KEY_MODE:
			b = true;
			s = layoutTypes[currentLayout] == 0 ? "!#1" : abc[currentLayout];
			break;
		case KEY_RETURN:
			b = true;
			s = multiLine ? "->" : "OK";
			break;
		case KEY_SPACE:
			b = true;
			s = "space";
			break;
		case KEY_EMPTY:
			// если 0, то клавиша пустая 
			return w;
		default:
			c = (char) key;
			break;
		}
		y += keyTextY;
		if(b) {
			x += (w - font.stringWidth(s)) >> 1;
			if(drawShadows) {
				g.setColor(keyTextShadowColor);
				g.drawString(s, x+1, y+1, 0);
				g.drawString(s, x+1, y-1, 0);
				g.drawString(s, x-1, y+1, 0);
				g.drawString(s, x-1, y-1, 0);
			}
			g.setColor(keyTextColor);
			g.drawString(s, x, y, 0);
		} else if(key != 0) {
			if(shifted && l < langs.length)
				c = Character.toUpperCase(c);
			x += (w - font.charWidth(c)) >> 1;
			if(drawShadows) {
				g.setColor(keyTextShadowColor);
				g.drawChar(c, x+1, y+1, 0);
				g.drawChar(c, x+1, y-1, 0);
				g.drawChar(c, x-1, y+1, 0);
				g.drawChar(c, x-1, y-1, 0);
			}
			g.setColor(keyTextColor);
			g.drawChar(c, x, y, 0);
		}
		return w;
	}
	
	/**
	 * Обработка нажатия пальца
	 * @return true если клава забрала эвент
	 */
	public boolean pointerPressed(int x, int y) {
		if(y >= Y && visible && hasPointerEvents) {
			pressed = true;
			pt = System.currentTimeMillis();
			px = x;
			py = y;
			synchronized(pressLock) {
				pressLock.notify();
			}
			if(drawButtons) _requestRepaint();
			return true;
		}
		if(textBoxShown && visible && x > textBoxX && y > textBoxY && x < textBoxX + textBoxWidth && y < textBoxY + textBoxHeight) {
			pressed = true;
			pt = System.currentTimeMillis();
			px = x;
			py = y;
			textBoxPressed = true;
			setCaretPosition(x - textBoxX, y - textBoxY);
			endPosition = -1;
			startPosition = caretPosition;
			startX = caretX;
			startRow = caretRow;
			startCol = caretCol;
			_requestTextBoxRepaint();
			return true;
		}
		return false;
	}
	
	public boolean keyPressed(int key) {
		switch(key) {
		case -3:
		case -4:
			keyPressed = true;
			moveCaret(key == -3 ? -1 : 1);
			_keyPressed(key);
			return true;
		case -1:
		case -2:
		case -6:
			return false;
		case -7:
		case -5:
		default:
			keyPressed = true;
			handleKey(key, false);
			_keyPressed(key);
			return true;
		}
	}
	
	private void moveCaret(int i) {
		caretFlash = true;
		caretPosition += i;
		if(caretPosition < 0) {
			caretPosition = 0;
			caretCol = 0;
			caretX = 0;
			caretRow = 0;
		} else if(caretPosition > text.length()) {
			caretPosition = text.length();
		} else if(i == -1) {
			char c = text.charAt(caretPosition);
			if(c == '\n') {
				caretRow--;
				String s = getTextArray(text, textFont, textBoxWidth-4)[caretRow];
				caretCol = s.length();
				caretX = textFont.stringWidth(s);
			} else {
				caretCol--;
				caretX -= textFont.charWidth(c);
			}
		} else {
			char c = text.charAt(caretPosition-1);
			if(c == '\n') {
				caretX = 0;
				caretCol = 0;
				caretRow++;
			} else {
				caretCol ++;
				caretX += textFont.charWidth(c);
			}
		}
		_requestTextBoxRepaint();
	}

	public boolean keyRepeated(int key) {
		if(keyPressed) {
			if(key == -3 || key == -4) {
				keyPressed = true;
				moveCaret(key == -3 ? -1 : 1);
				_keyPressed(key);
				return true;
			}
			handleKey(key, true);
			return true;
		}
		return false;
	}

	public boolean keyReleased(int key) {
		if(keyPressed) {
			keyWasRepeated = false;
			keyPressed = false;
			return true;
		}
		return false;
	}
	
	private void _keyPressed(int key) {
		lastKey = key;
		kt = System.currentTimeMillis();
		synchronized(pressLock) {
			pressLock.notify();
		}
	}
	
	void _repeatKey() {
		handleKey(lastKey, true);
	}
	
	void _flushKeyBuffer() {
		if(keyBuffer != 0) {
			type(keyBuffer);
		}
		keyRepeatTicks = keyVarIdx = keyBuffer = 0;
	}
	
	private void handleKey(int key, boolean repeated) {
		if(physicalKeyboard == PHYSICAL_KEYBOARD_PHONE_KEYPAD) {
			if(repeated) {
				if(key >= '1' && key <= '9') {
					if(!keyWasRepeated || keyboardType == KEYBOARD_PHONE_NUMBER || keyboardType == KEYBOARD_NUMERIC || keyboardType == KEYBOARD_DECIMAL) {
						keyBuffer = (char) key;
						_flushKeyBuffer();
					}
				} else if(key == -7 || key == -8 || key == '\b') {
					backspace();
				} else if(key == '0') {
					if(!keyWasRepeated || keyboardType == KEYBOARD_NUMERIC || keyboardType == KEYBOARD_DECIMAL) {
						keyBuffer = keyboardType == KEYBOARD_PHONE_NUMBER ? '+' : '0';
						_flushKeyBuffer();
					}
				} else {
					if(canvas != null) {
						String keyName = canvas.getKeyName(key);
						if(keyName.length() == 1) {
							type(keyName.charAt(0));
						}
					} else {
						type((char) key);
					}
				}
				keyWasRepeated = true;
			} else {
				if(key >= '1' && key <= '9') {
					switch(keyboardType) {
					case KEYBOARD_DEFAULT:
						if(keyRepeatTicks > 0 && lastKey == key) {
							if(keyVarIdx++ == keyVars.length-1) {
								keyVarIdx = 0;
							}
							if(keyVars == null) return;
							keyBuffer = (char) keyVars[keyVarIdx];
						} else {
							_flushKeyBuffer();
							keyVars = keyLayouts[currentPhysicalLayout][key-'0'];
							if(keyVars == null) return;
							keyBuffer = (char) keyVars[keyVarIdx];
						}
						break;
					case KEYBOARD_PHONE_NUMBER:
					case KEYBOARD_NUMERIC:
					case KEYBOARD_DECIMAL:
						keyBuffer = (char) key;
						_flushKeyBuffer();
					}
					_requestRepaint();
					keyRepeatTicks = KEY_REPEAT_TICKS;
				} else if(key == '0') {
					keyVars = null;
					switch(keyboardType) {
					case KEYBOARD_DEFAULT:
						if(keyRepeatTicks > 0 && lastKey == key) {
							if(keyVarIdx++ == 2) {
								keyVarIdx = 0;
								keyBuffer = ' ';
							} else if(keyVarIdx == 1) {
								keyBuffer = '0';
							} else {
								keyBuffer = '\n';
							}
						} else {
							_flushKeyBuffer();
							keyBuffer = ' ';
						}
					case KEYBOARD_PHONE_NUMBER:
						_flushKeyBuffer();
						keyBuffer = '0';
						break;
					case KEYBOARD_NUMERIC:
					case KEYBOARD_DECIMAL:
						keyBuffer = (char) key;
						_flushKeyBuffer();
					}
					_requestRepaint();
					keyRepeatTicks = KEY_REPEAT_TICKS;
				} else if(key == '#'){
					switch(keyboardType) {
					case KEYBOARD_DEFAULT:
						shiftKey();
						break;
					case KEYBOARD_PHONE_NUMBER:
						_flushKeyBuffer();
						type('#');
						break;
					case KEYBOARD_NUMERIC:
						break;
					case KEYBOARD_DECIMAL:
						_flushKeyBuffer();
						type('-');
						break;
					}
				} else if(key == '*'){
					switch(keyboardType) {
					case KEYBOARD_DEFAULT:
						langKey();
						break;
					case KEYBOARD_NUMERIC:
						break;
					case KEYBOARD_PHONE_NUMBER:
						_flushKeyBuffer();
						type('*');
						break;
					case KEYBOARD_DECIMAL:
						_flushKeyBuffer();
						type('.');
						break;
					}
				} else if(key == -7 || key == -8 || key == '\b') {
					_flushKeyBuffer();
					if(key == -7 && text.length() == 0) { // убирать фокус если нет текста
						cancel();
					} else {
						backspace();
					}
				} else {
					_flushKeyBuffer();
					switch(key) {
					case 13:
					case 80:
					case -5:
						enter();
						return;
					case ' ':
						space();
						return;
					default:
						if(canvas != null) {
							String keyName = canvas.getKeyName(key);
							if(keyName.length() == 1) {
								type(keyName.charAt(0));
							}
						} else {
							type((char) key);
						}
					}
				}
			}
		} else {
			switch(key) {
			case '\b':
				backspace();
				return;
			case -5:
			case 13:
			case 80:
				enter();
				return;
			case ' ':
				space();
				return;
			default:
				if(canvas != null) {
					String keyName = canvas.getKeyName(key);
					if(keyName.length() == 1) {
						type(keyName.charAt(0));
					}
				} else {
					type((char) key);
				}
			}
		}
	}
	
	/**
	 * Обработка отпускания пальца
	 * @return true если клава забрала эвент
	 */
	public boolean pointerReleased(int x, int y) {
		if(pressed) {
			if(textBoxPressed) {
				setCaretPosition(x - textBoxX, y - textBoxY);
				if(dragged) {
					endPosition = caretPosition;
					endX = caretX;
					endRow = caretRow;
					endCol = caretCol;
				}
			} else {
				handleTap(x, y-Y, false);
			}
			pressed = false;
			dragged = false;
			return true;
		}
		return false;
	}

	/**
	 * Обработка перемещения пальца
	 * @return true если клава забрала эвент
	 */
	public boolean pointerDragged(int x, int y) {
		if(pressed) {
			// filter
			if(py == x && py == y) return true;
			px = x;
			py = y;
			dragged = true;
			return true;
		}
		return false;
	}

	void _repeatPress(int x, int y) {
		handleTap(x, y-Y, true);
	}
	
	private void handleTap(int x, int y, boolean repeated) {
		int row = div(y - keyStartY, keyHeight + keyMarginY);
		if(row == 4) row = 3;
		if(repeated && row != 2) return;
		if(row >= 0 && row <= 3) {
			if(x < 0 || x > screenWidth) return;
			int l = currentLayout;
			int kx = offsets[l][row];
			for(int col = 0; col < layouts[l][row].length; col++) {
				int w = widths[l][row][col];
				if(x > kx && x < kx+w) {
					int key = layouts[l][row][col];
					switch(key) {
					case KEY_SHIFT:
						if(!repeated) shiftKey();
						break;
					case KEY_BACKSPACE:
						backspace();
						break;
					case KEY_LANG:
						if(!repeated) langKey();
						break;
					case KEY_MODE:
						if(!repeated) modeKey();
						break;
					case KEY_RETURN:
						if(!repeated) enter();
						break;
					case KEY_SPACE:
						if(!repeated) space();
						break;
					case KEY_EMPTY:
					case KEY_UNDEFINED:
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
		if(layoutTypes[currentLayout] == 0) {
			currentLayout = specs[spec = 0];
		} else if(layoutTypes[currentLayout] == 1) {
			currentLayout = langsIdx[lang];
		}
		_requestRepaint();
	}
	
	private void langKey() {
		shifted = false;
		int l = langs.length;
		lang++;
		if(lang >= l) {
			lang = 0;
		}
		currentPhysicalLayout = langsIdx[lang];
		currentLayout = langsIdx[lang];
		if(listener != null) listener.langChanged();
		_requestRepaint();
	}

	// деление без остатка
	private int div(int i, int j) {
		double d = i;
		d /= j;
		return (int)(d - d % 1);
	}

	private void enter() {
		// если мультилайн мод, добавить \n, иначе послать эвент
		if(multiLine) {
			type('\n');
		} else {
			if(listener != null) listener.done();
		}
		textUpdated();
	}

	private void shiftKey() {
		if(layoutTypes[currentLayout] == 1) {
			keepShifted = false;
			shifted = false;
			spec++;
			if(spec > 1) spec = 0;
			currentLayout = specs[spec];
		} else if(shifted && !keepShifted) {
			keepShifted = true;
		} else {
			keepShifted = false;
			shifted = !shifted;
		}
		_requestRepaint();
	}

	private void textUpdated() {
		if(caretPosition > text.length()) caretPosition = text.length();
		if(listener != null) listener.textUpdated();
		_requestRepaint();
	}

	private void type(char c) {
		if(size > 0 && text.length() >= size) return;
		if(shifted) {
			c = Character.toUpperCase(c);
			if(!keepShifted) shifted = false;
		}
		if(listener != null && !listener.appendChar(c)) return;
		if(multiLine) {
			if(c == '\n') {
				caretX = 0;
				caretCol = 0;
				caretRow++;
			} else {
				caretCol ++;
				caretX += textFont.charWidth(c);
			}
		}
		if(caretPosition != text.length()) {
			String s = caretPosition == 0 ? "" : text.substring(0, caretPosition);
			s += c;
			s += text.substring(caretPosition);
			if(s.length() > size && size > 0) s = s.substring(0, size);
			text = s;
		} else {
			text += c;
		}
		caretPosition ++;
		textUpdated();
	}
	
	private void space() {
		type(' ');
	}
	
	private void backspace() {
		if(keyBuffer != 0) {
			keyBuffer = 0;
			_flushKeyBuffer();
			_requestRepaint();
			return;
		}
		if(listener != null && !listener.removeChar()) {
			_requestRepaint();
			return;
		}
		if(text.length() > 0) {
			if(endPosition != -1) {
				int start = Math.min(startPosition, endPosition);
				remove(start, Math.max(startPosition, endPosition));
				endPosition = -1;
				caretPosition = start;
				textUpdated();
				return;
			}
			if(caretPosition == text.length()) {
				if(multiLine) {
					char c = text.charAt(text.length() - 1);
					if(c == '\n') {
						caretRow--;
						String s = getTextArray(text, textFont, textBoxWidth-4)[caretRow];
						caretCol = s.length();
						caretX = textFont.stringWidth(s);
					} else {
						caretCol--;
						caretX -= textFont.charWidth(c);
					}
				}
				text = text.substring(0, text.length() - 1);
				caretPosition --;
			} else if(caretPosition > 0) {
				if(multiLine) {
					char c = text.charAt(caretPosition - 1);
					if(c == '\n') {
						caretRow--;
						String s = getTextArray(text, textFont, textBoxWidth-4)[caretRow];
						caretCol = s.length();
						caretX = textFont.stringWidth(s);
					} else {
						caretCol--;
						caretX -= textFont.charWidth(c);
					}
				}
				text = text.substring(0, caretPosition - 1) + text.substring(caretPosition);
				caretPosition --;
			}
			if(caretPosition == 0 && (keyboardType != PHYSICAL_KEYBOARD_QWERTY || hasPointerEvents)) {
				shifted = true;
			}
		}
		caretFlash = true;
		textUpdated();
	}
	
	private void cancel() {
		if(listener != null) listener.cancel();
		hide();
	}
	
	// стиль
	
	/**
	 * Задать цвет фона
	 * @param color
	 */
	public void setBackgroundColor(int color) {	
		this.bgColor = color;
	}
	
	/**
	 * Задать цвет кнопки
	 * @param color
	 */
	public void setButtonColor(int color) {	
		this.keyButtonColor = color;
	}
	
	/**
	 * Задать цвет нажатой кнопки
	 * @param color
	 */
	public void setButtonHoverColor(int color) {	
		this.keyButtonHoverColor = color;
	}
	
	/**
	 * Задать цвет обводки кнопок
	 * @param color
	 */
	public void setButtonOutlineColor(int color) {	
		this.keyButtonOutlineColor = color;
	}

	/**
	 * Задать цвет текста
	 * @param color
	 */
	public void setKeyTextColor(int color) {	
		this.keyTextColor = color;
	}
	
	/**
	 * Задать цвет обводки текста
	 * @param color
	 */
	public void setKeyTextShadowColor(int color) {	
		this.keyTextShadowColor = color;
	}

	/**
	 * Задать цвет мигающей полоски
	 * @param color
	 */
	public void setCaretColor(int color) {
		this.caretColor = color;
	}

	/**
	 * Задать цвет текста в текст боксе
	 * @param color
	 */
	public void setTextColor(int color) {
		this.textColor = color;
	}

	/**
	 * Задать цвет подсказки в текст боксе
	 * @param color
	 */
	public void setTextHintColor(int color) {
		this.textHintColor = color;
	}
	
	/**
	 * Включить отображение кнопок
	 * @param enabled
	 */
	public void setButtons(boolean enabled) {
		this.drawButtons = enabled;
	}
	
	/**
	 * Включить обводку текста
	 * @param enabled
	 */
	public void setTextShadows(boolean enabled) {
		this.drawShadows = enabled;
	}
	
	/**
	 * Включить "закругление" кнопок
	 * @param enabled
	 */
	public void setRoundButtons(boolean enabled) {
		this.roundButtons = enabled;
	}
	
	/**
	 * Изменить паддинг кнопок
	 * @param padding
	 */
	public void setButtonPadding(int padding) {
		this.keyButtonPadding = padding;
	}
	
	/**
	 * Изменить шрифт
	 * @param font
	 */
	public void setKeyFont(Font font) {
		this.font = font;
		this.fontHeight = font.getHeight();
		this.keyTextY = ((keyHeight - fontHeight) >> 1) + 1;
	}
	
	/**
	 * Изменить шрифт набираемого текста
	 * @param font
	 */
	public void setTextFont(Font font) {
		this.textFont = font;
		this.textFontHeight = font.getHeight();
	}
	
	public void setTextHint(String textHint) {
		this.textHint = textHint;
	}
	
	/**
	 * <p>Изменить доступные языки</p>
	 * @param languages Можно пустой массив чтобы выбрать все доступные языки,<br>
	 * но возможно тогда юзверю придется много раз нажимать на кнопку языка чтобы найти нужный
	 */
	public void setLanguages(String[] languages) {
		if(languages.length == 0 || !hasQwertyLayouts) {
			langs = supportedLanguages;
			langsIdx = supportedLanguagesIdx;
		} else {
			Vector v = new Vector();
			for(int i = 0; i < languages.length; i++) {
				for(int j = 0; j < supportedLanguages.length; j++) {
					if(languages[i].equalsIgnoreCase(supportedLanguages[j])) {
						v.addElement(new Integer(j));
						break;
					}
				}
			}
			int l = v.size();
			if(l < languages.length) {
				// предупреждение в логи о том что некоторые языки не были добавлены
				System.out.println("Some selected languages are not supported by current layout pack and skipped!");
			}
			langs = new String[l];
			langsIdx = new int[l];
			for(int i = 0; i < l; i++) {
				int k = ((Integer)v.elementAt(i)).intValue();
				langs[i] = supportedLanguages[k];
				langsIdx[i] = supportedLanguagesIdx[k];
			}
		}
		if(hasQwertyLayouts) {
			currentLayout = langsIdx[0];
		}
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	void _requestRepaint() {
		if(listener != null) listener.requestRepaint();
	}
	
	void _requestTextBoxRepaint() {
		if(listener != null) listener.requestTextBoxRepaint();
	}
	
	public static String[] getSupportedLanguages() {
		return getSupportedLanguages(DEFAULT_LAYOUT_PACK);
	}
	
	public static String[] getSupportedLanguages(String layoutPackRes) {
		try {
			JSONObject json = (JSONObject) readJSONRes(layoutPackRes);
			JSONArray arr = json.getArray("languages");
			String[] res = new String[arr.size()];
			Enumeration e = arr.elements();
			int i = 0;
			while(e.hasMoreElements()) {
				JSONObject o = (JSONObject) e.nextElement();
				String lng = (String) o.keys().nextElement();
				res[i++] = o.getString(lng) + " [" + lng + "]";
			}
			return res;
		} catch (IOException e) {
			throw new RuntimeException(e.toString());
		}
	}

	public void setCaretPostion(int i) {
		caretFlash = true;
		caretPosition = i;
		if(caretPosition < 0) caretPosition = 0;
		if(caretPosition > text.length()) caretPosition = text.length();
	}
	
	public static String[] getTextArray(String s, Font font, int maxWidth) {
    	if(s == null) return new String[0];
       if(maxWidth > 0) {
          boolean var4 = s.indexOf('\n') != -1;
          if(font.stringWidth(s) <= maxWidth) {
             return var4?split(s, '\n'):new String[]{s};
          } else {
             Vector list = new Vector();
             if(!var4) {
                splitToWidth(s, font, maxWidth, list);
             } else {
                char[] var7 = s.toCharArray();
                int var8 = 0;

                for(int var9 = 0; var9 < var7.length; ++var9) {
                   if(var7[var9] == 10 || var9 == var7.length - 1) {
                      String var11 = var9 == var7.length - 1?new String(var7, var8, var9 + 1 - var8):new String(var7, var8, var9 - var8);
                      if(font.stringWidth(var11) <= maxWidth) {
                         list.addElement(var11);
                      } else {
                         splitToWidth(var11, font, maxWidth, list);
                      }

                      var8 = var9 + 1;
                   }
                }
             }

 			String[] r = new String[list.size()];
 			list.copyInto(r);
 			return r;
          }
       } else {
          return new String[]{s};
       }
    }
	
    private static void splitToWidth(String s, Font font, int maxWidth, Vector list) {
        char[] arr = s.toCharArray();
        int k = 0;
        int i = 0;
        int w = 0;

        while(true) {
           while(i < arr.length) {
              if((w += font.charWidth(arr[i])) > maxWidth) {
                 int j = i;

                 while(arr[j] != ' ') {
                    --j;
                    if(j < k) {
                       j = i;
                       break;
                    }
                 }

                 list.addElement(new String(arr, k, j - k));
                 k = arr[j] != ' ' && arr[j] != '\n' ?j:j + 1;
                 w = 0;
                 i = k;
              } else {
                 ++i;
              }
           }

           list.addElement(new String(arr, k, i - k));
           return;
        }
     }
	
	private static String[] split(String s, char c) {
		char[] arr = s.toCharArray();
		int i = 0;
		Vector list = null;

		for (int j = 0; j < arr.length; ++j) {
			if (arr[j] == c) {
				if (list == null) {
					list = new Vector();
				}

				list.addElement(new String(arr, i, j - i));
				i = j + 1;
			}
		}

		if (list == null) {
			return new String[] { s };
		} else {
			if (i < arr.length) {
				list.addElement(new String(arr, i, arr.length - i));
			}
			String[] r = new String[list.size()];
			list.copyInto(r);
			return r;
		}
	}

}
