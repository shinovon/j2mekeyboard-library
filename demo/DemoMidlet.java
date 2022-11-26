import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public class DemoMidlet extends MIDlet {

	public DemoMidlet() {
	}

	protected void destroyApp(boolean b) {
	}

	protected void pauseApp() {
	}

	protected void startApp() {
		DemoCanvas c = new DemoCanvas();
		Display.getDisplay(this).setCurrent(c);
	}

}
