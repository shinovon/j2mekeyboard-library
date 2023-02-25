package cc.nnproject.keyboard;

class KeyboardThread extends Thread {
	
	private static final int holdTime = 500;
	private static final int repeatTime = 100;
	private static final int keyHoldTime = 500;
	private static final int keyRepeatTime = 100;
	
	private Keyboard keyboard;
	
	public KeyboardThread(Keyboard keyboard) {
		super("Keyboard thread");
		this.keyboard = keyboard;
	}
	
	public void run() {
		try {
			int touchTicks = 0;
			int keyTicks = 0;
			while(keyboard.visible) {
				if(keyboard.pressed) {
					if(touchTicks > 10) {
						if(System.currentTimeMillis() - keyboard.pt >= holdTime) {
							keyboard._repeatPress(keyboard.px, keyboard.py);
							Thread.sleep(repeatTime);
							keyboard._requestRepaint();
							continue;
						}
					} else {
						touchTicks++;
					}
					if(keyboard.dragged) keyboard._requestRepaint();
				} else if(keyboard.keyPressed) {
					if(keyTicks > 10 && !keyboard.hasRepeatEvents) {
						if(System.currentTimeMillis() - keyboard.kt >= keyHoldTime) {
							keyboard._repeatKey();
							Thread.sleep(keyRepeatTime);
							keyboard._requestRepaint();
							continue;
						}
					} else {
						keyTicks++;
					}
				} else {
					touchTicks = 0;
					synchronized(keyboard.pressLock) {
						keyboard.pressLock.wait();
					}
				}
				if(keyboard.keyRepeatTicks > 0) {
					keyboard.keyRepeatTicks--;
					if(keyboard.keyRepeatTicks == 0) {
						keyboard._flushKeyBuffer();
					}
				}
				Thread.sleep(50);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
