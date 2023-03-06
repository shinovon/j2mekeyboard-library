package cc.nnproject.keyboard;

public interface KeyboardListener {
	
	/**
	 * Эвент нажатия на клавишу, действие можно отменить возвратив false
	 */
	public boolean appendChar(char c);
	
	/**
	 * Эвент нажатия на бэкспейс, действие можно отменить возвратив false
	 */
	public boolean removeChar();
	
	/**
	 * Эвент изменения языка
	 */
	public void langChanged();

	/**
	 * Эвент обновления текста
	 */
	public void textUpdated();
	
	/**
	 * Эвент нажатия на OK при выключенном многострочном режиме
	 */
	public void done();
	
	/**
	 * Запрос перерисовки, рекомендуется это делать асинхронно
	 */
	public void requestRepaint();

	public void cancel();

	public void requestCaretRepaint();

}
