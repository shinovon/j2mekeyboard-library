package cc.nnproject.keyboard;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

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
	
	private static final int holdTime = 500;
	private static final int repeatTime = 100;

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
	
	private boolean visible;
	private boolean keepShifted;
	private boolean shifted;
	
	private KeyboardListener listener;
	
	private String text = "";
	
	private int keyboardType;
	private boolean multiLine;
	
	boolean pressed;
	boolean dragged;
	int px;
	int py;
	long pt;
	
	private int screenWidth;
	private int screenHeight;

	private Thread repeatThread;
	private Object pressLock = new Object();

	private int keyTextY;
	
	// стиль
	private int bgColor = DEFAULT_BACKGROUND_COLOR;
	private int textColor = DEFAULT_TEXT_COLOR;
	private int textShadowColor = DEFAULT_TEXT_SHADOW_COLOR;
	private int keyButtonColor = DEFAULT_BUTTON_COLOR;
	private int keyButtonHoverColor = DEFAULT_BUTTON_HOVER_COLOR;
	private int keyButtonOutlineColor = DEFAULT_BUTTON_OUTLINE_COLOR;
	private boolean drawButtons = DEFAULT_BUTTONS;
	private boolean drawShadows = DEFAULT_TEXT_SHADOWS;
	private boolean roundButtons = DEFAULT_ROUND_BUTTONS;
	private int keyButtonPadding = DEFAULT_BUTTON_PADDING;
	
	private Font font = Font.getFont(0, 0, 0);
	private int fontHeight = font.getHeight();
	private String layoutPackRes;
	private boolean hasQwertyLayouts;
	
	private Keyboard(int keyboardType, boolean multiLine, int screenWidth, int screenHeight, String layoutPackRes) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.multiLine = multiLine;
		this.layoutPackRes = layoutPackRes == null ? DEFAULT_LAYOUT_PACK : layoutPackRes;
		this.keyboardType = keyboardType;
		repeatThread = new Thread("Key Repeat Thread") {
			public void run() {
				try {
					int count = 0;
					while(visible) {
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
							if(dragged) requestRepaint();
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
		parseLayoutPack();
		layout();
	}

	/**
	 * Инициализация с дефолтными настройками
	 */
	public static Keyboard getKeyboard() {
		return new Keyboard(KEYBOARD_DEFAULT, false, 0, 0, null);
	}

	/**
	 * Инициализация с дефолтной раскладкой
	 * @param multiLine
	 * @param screenWidth
	 * @param screenHeight
	 */
	public static Keyboard getKeyboard(boolean multiLine, int screenWidth, int screenHeight) {
		return new Keyboard(KEYBOARD_DEFAULT, multiLine, screenWidth, screenHeight, null);
	}
	
	/**
	 * Инициализация клавы
	 * @param keyboardType
	 * @param multiLine
	 * @param screenWidth
	 * @param screenHeight
	 */
	public static Keyboard getKeyboard(int keyboardType, boolean multiLine, int screenWidth, int screenHeight) {
		return new Keyboard(keyboardType, multiLine, screenWidth, screenHeight, null);
	}

	/**
	 * Инициализация клавы с кастомным паком раскладок
	 * @param keyboardType
	 * @param multiLine
	 * @param screenWidth
	 * @param screenHeight
	 * @param layoutPackRes
	 */
	public static Keyboard getKeyboard(int keyboardType, boolean multiLine, int screenWidth, int screenHeight, String layoutPackRes) {
		return new Keyboard(keyboardType, multiLine, screenWidth, screenHeight, layoutPackRes);
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
			layoutTypes = new int[i];
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
				int[][] l = new int[4][];
				JSONArray a = (JSONArray) readJSONRes(j.getString("res"));
				for(int k = 0; k < 4; k++) {
					JSONArray b = a.getArray(k);
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
	
	private AbstractJSON readJSONRes(String res) throws IOException {
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
	
	/**
	 * Установить текст
	 * @param s
	 */
	public void setText(String s) {
		text = s;
	}
	
	/**
	 * Добавить текст
	 * @param s
	 */
	public void appendText(String s) {
		text += s;
	}
	
	/**
	 * Вставить текст
	 * @param s
	 * @param index
	 */
	public void insertText(String s, int index) {
		text = text.substring(0, index) + s + text.substring(index);
	}
	
	/**
	 * Убрать символ из текста
	 * @param index
	 */
	public void removeChar(int index) {
		text = text.substring(0, index) + text.substring(index + 1);
	}
	
	/**
	 * Убрать кусок текста
	 * @param start
	 * @param end
	 */
	public void remove(int start, int end) {
		text = text.substring(0, start) + text.substring(end + 1);
	}

	/**
	 * Очистка текста
	 */
	public void clear() {
		text = "";
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
		visible = true;
		repeatThread.start();
	}
	
	/**
	 * Скрыть клавиатуру
	 * <p><i>Не забудьте вызвать это при скрытии экрана с клавиатурой</i></p>
	 */
	public void hide() {
		visible = false;
	}
	
	/**
	 * Отрисовка виртуальной клавиатуры
	 * @param g
	 * @param screenWidth
	 * @param screenHeight
	 * @return Сколько высоты экрана забрало
	 */
	public int paint(Graphics g, int screenWidth, int screenHeight) {
		if(!visible) return 0;
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
			s = layoutTypes[currentLayout] == 0 ? "!#1" : "ABC";
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
				g.setColor(textShadowColor);
				g.drawString(s, x+1, y+1, 0);
				g.drawString(s, x+1, y-1, 0);
				g.drawString(s, x-1, y+1, 0);
				g.drawString(s, x-1, y-1, 0);
			}
			g.setColor(textColor);
			g.drawString(s, x, y, 0);
		} else if(key != 0) {
			if(shifted && l < langs.length)
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
		return w;
	}
	
	/**
	 * Обработка нажатия пальца
	 * @return true если клава забрала эвент
	 */
	public boolean pointerPressed(int x, int y) {
		if(y >= Y && visible) {
			pressed = true;
			pt = System.currentTimeMillis();
			px = x;
			py = y;
			synchronized(pressLock) {
				pressLock.notify();
			}
			if(drawButtons) requestRepaint();
			return true;
		}
		return false;
	}

	/**
	 * Обработка отпускания пальца
	 * @return true если клава забрала эвент
	 */
	public boolean pointerReleased(int x, int y) {
		if(pressed) {
			pressed = false;
			dragged = false;
			handleTap(x, y-Y, false);
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

	void repeatPress(int x, int y) {
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
		requestRepaint();
	}
	
	private void langKey() {
		shifted = false;
		int l = langs.length;
		lang++;
		if(lang >= l) {
			lang = 0;
		}
		currentLayout = langsIdx[lang];
		if(listener != null) listener.langChanged();
		requestRepaint();
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
		requestRepaint();
	}

	private void textUpdated() {
		if(listener != null) listener.textUpdated();
		requestRepaint();
	}

	private void type(char c) {
		if(shifted) {
			c = Character.toUpperCase(c);
			if(!keepShifted) shifted = false;
		}
		if(listener != null && !listener.appendChar(c)) return;
		text += c;
		textUpdated();
	}
	
	private void space() {
		if(listener != null && !listener.appendChar(' ')) return;
		text += " ";
		textUpdated();
	}
	
	private void backspace() {
		if(listener != null && !listener.removeChar()) return;
		if(text.length() > 0) {
			text = text.substring(0, text.length() - 1);
		}
		textUpdated();
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
	public void setTextColor(int color) {	
		this.textColor = color;
	}
	
	/**
	 * Задать цвет обводки текста
	 * @param color
	 */
	public void setTextShadowColor(int color) {	
		this.textShadowColor = color;
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
	public void setFont(Font font) {
		this.font = font;
		this.fontHeight = font.getHeight();
		this.keyTextY = ((keyHeight - fontHeight) >> 1) + 1;
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
	
	void requestRepaint() {
		if(listener != null) listener.requestRepaint();
	}

}
