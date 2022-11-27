package api;

public interface KeyboardConstants {

	// режимы клавы
	
	public static final int MODE_DEFAULT = 0;
	// урл
	public static final int MODE_URL = 1;
	// только цифры
	public static final int MODE_NUMERIC = 2;
	// 
	public static final int MODE_DECIMAL = 3;
	// номер телефона
	public static final int MODE_PHONE_NUMBER = 4;

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
	
	// языки
	
	public static final int LANG_EN = 0;
	public static final int LANG_RU = 1;
	
}
