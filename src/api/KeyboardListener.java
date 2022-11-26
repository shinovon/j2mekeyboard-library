package api;

public interface KeyboardListener {
	
	// false если надо отменить
	public boolean appendChar(char c);
	
	public void charRemoved();
	
	public void langChanged();
	
	// режим с много строками
	public void newLine();
	
	// режим с одной строкой
	public void done();
	
	public void requestRepaint();

}
