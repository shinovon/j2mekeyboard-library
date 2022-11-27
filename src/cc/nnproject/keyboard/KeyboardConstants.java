package cc.nnproject.keyboard;

public interface KeyboardConstants {

	// режимы клавы
	
	public static final int KEYBOARD_DEFAULT = 0;
	// урл
	public static final int KEYBOARD_URL = 1;
	// только цифры
	public static final int KEYBOARD_NUMERIC = 2;
	// 
	public static final int KEYBOARD_DECIMAL = 3;
	// номер телефона
	public static final int KEYBOARD_PHONE_NUMBER = 4;

	// цвета
	
	public static final int DEFAULT_BACKGROUND_COLOR = 0x000000;
	public static final int DEFAULT_BUTTON_COLOR = 0x404040;
	public static final int DEFAULT_BUTTON_HOVER_COLOR = 0x606060;
	public static final int DEFAULT_BUTTON_OUTLINE_COLOR = 0x131313;
	public static final int DEFAULT_TEXT_COLOR = 0xCDCDCD;
	public static final int DEFAULT_TEXT_SHADOW_COLOR = 0x2E2E2E;
	
	public static final boolean DEFAULT_BUTTONS = true;
	public static final boolean DEFAULT_TEXT_SHADOWS = true;
	public static final boolean DEFAULT_ROUND_BUTTONS = true;
	
	public static final int DEFAULT_BUTTON_PADDING = 2;
	
	public static final String KEYBOARD_LAYOUTS_DIR = "/keyboard_layouts/";
	public static final String DEFAULT_LAYOUT_PACK = "default_layout_pack.json";
	
}
