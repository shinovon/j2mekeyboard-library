package cc.nnproject.keyboard;

public interface KeyboardListener {
	
	/**
	 * Эвент нажатия на клавишу, действие можно отменить возвратив false
	 */
	public boolean onKeyboardType(char c);
	
	/**
	 * Эвент нажатия на бэкспейс, действие можно отменить возвратив false
	 */
	public boolean onKeyboardBackspace();
	
	/**
	 * Эвент изменения языка
	 */
	public void onKeyboardLanguageChanged();

	/**
	 * Эвент обновления текста
	 */
	public void onKeyboardTextUpdated();
	
	/**
	 * Эвент нажатия на OK при выключенном многострочном режиме
	 */
	public void onKeyboardDone();
	public void onKeyboardCancel();
	
	/**
	 * Запрос перерисовки, рекомендуется это делать асинхронно
	 */
	public void onKeyboardRepaintRequested();

	public void onTextBoxRepaintRequested();

}
