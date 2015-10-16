package ve.ucv.ciens.icaro.ardemo.desktop;

import ve.ucv.ciens.icaro.ardemo.EviDemo;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher{
	private static final String TAG = "NXTAR_ANDROID_MAIN";
	private static final String CLASS_NAME = DesktopLauncher.class.getSimpleName();

	public static void main (String[] arg) {
		if(!CVProcessor.isOcvOn()){
			throw new RuntimeException(TAG + " : " + CLASS_NAME + ": OpenCV failed to load.");
		}

		if(arg.length != 2) {
			System.err.println("Usage: EVI07 <markers video file> <calibration video file>");
			System.exit(1);
		}

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 640;
		config.height = 360;
		new LwjglApplication(new EviDemo(new CVProcessor(arg)), config);
	}

}
