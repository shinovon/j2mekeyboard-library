package cc.nnproject.keyboard;

public interface KeyboardListener {
	
	/**
	 * Эвент нажатия на клавишу, действие можно отменить возвратив false
	 */
	public boolean appendChar(char c);
	
	/**
	 * Эвент нажатия на бэкспейс
	 */
	public void charRemoved();
	
	/**
	 * Эвент изменения языка
	 */
	public void langChanged();

	/**
	 * Эвент нажатия на OK при включенном многострочном режиме,
	 * <p>
	 * <i>Перед ним всегда отправляется эвент</i> <code>onChar('\n')</code>
	 * </p>
	 */
	public void newLine();
	
	/**
	 * Эвент нажатия на OK при выключенном многострочном режиме
	 */
	public void done();
	
	/**
	 * Запрос перерисовки, лучше всего это делать асинхронно
	 */
	public void requestRepaint();

}
