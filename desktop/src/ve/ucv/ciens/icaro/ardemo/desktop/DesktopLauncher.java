package ve.ucv.ciens.icaro.ardemo.desktop;

import ve.ucv.ciens.icaro.ardemo.EviDemo;
import ve.ucv.ciens.icaro.ardemo.ProjectConstants;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/**
 * This is the main class of the applicaton. It is in charge of creating the {@link CVProcessor} instance
 * used throught the rest of the application and launching the {@link EviDemo} class.
 * 
 * @author Miguel Angel Astor Romero.
 */
public class DesktopLauncher{
	private static final String TAG = "NXTAR_ANDROID_MAIN";
	private static final String CLASS_NAME = DesktopLauncher.class.getSimpleName();

	public static void main (String[] arg) {
		if(!CVProcessor.isOcvOn()){
			throw new RuntimeException(TAG + " : " + CLASS_NAME + ": OpenCV failed to load.");
		}

		if(arg.length != 3) {
			System.err.println("Usage: " + arg[0] + " <markers video file> <calibration video file>");
			System.exit(1);
		}

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = ProjectConstants.W;
		config.height = ProjectConstants.H;
		new LwjglApplication(new EviDemo(new CVProcessor(arg)), config);
	}

}
