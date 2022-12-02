package cc.nnproject.keyboard;

class KeyboardThread extends Thread {
	
	private static final int holdTime = 500;
	private static final int repeatTime = 100;
	
	private Keyboard keyboard;
	
	public KeyboardThread(Keyboard keyboard) {
		super("Keyboard thread");
		this.keyboard = keyboard;
	}
	
	public void run() {
		try {
			int count = 0;
			while(keyboard.visible) {
				if(keyboard.pressed) {
					if(count > 10) {
						if(System.currentTimeMillis() - keyboard.pt >= holdTime) {
							keyboard.repeatPress(keyboard.px, keyboard.py);
							Thread.sleep(repeatTime);
							keyboard.requestRepaint();
							continue;
						}
					} else {
						count++;
					}
					if(keyboard.dragged) keyboard.requestRepaint();
				} else {
					count = 0;
					synchronized(keyboard.pressLock) {
						keyboard.pressLock.wait();
					}
				}
				Thread.sleep(50);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
