package ve.ucv.ciens.icaro.ardemo;

import ve.ucv.ciens.icaro.ardemo.ImageProcessor.CalibrationData;
import ve.ucv.ciens.icaro.ardemo.ImageProcessor.MarkerData;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EviDemo extends ApplicationAdapter {
	private static final String TAG = "NXTAR_ANDROID_MAIN";
	private static final String CLASS_NAME = EviDemo.class.getSimpleName();

	private ImageProcessor cvProc;
	private Texture tex;
	private Pixmap frame;
	private SpriteBatch batch;
	private MarkerData data;
	private CalibrationData calib;

	private float[][] calibrationSamples;
	private int lastSampleTaken;

	public EviDemo(ImageProcessor proc) {
		super();
		tex = null;
		cvProc = proc;
		frame = null;

		lastSampleTaken = 0;

		calibrationSamples = new float[ProjectConstants.CALIBRATION_SAMPLES][];
		for(int i = 0; i < calibrationSamples.length; i++){
			calibrationSamples[i] = new float[ProjectConstants.CALIBRATION_PATTERN_POINTS * 2];
		}
	}

	@Override
	public void create () {
		batch = new SpriteBatch();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if(cvProc.isCameraCalibrated()) {
			data = cvProc.findMarkersInFrame();
			if(data != null) {
				frame = new Pixmap(data.outFrame, 0, data.outFrame.length);
				tex = new Texture(frame);

				batch.begin();
				batch.draw(tex, 0, 0);
				batch.end();

				frame.dispose();
				tex.dispose();
			}
		} else {
			calib = cvProc.findCalibrationPattern();

			if(calib != null){

				if(!cvProc.isCameraCalibrated() && calib.calibrationPoints != null){
					Gdx.app.log(TAG, CLASS_NAME + ".render(): Sample taken.");

					// Save the calibration points to the samples array.
					for(int i = 0; i < calib.calibrationPoints.length; i += 2){
						Gdx.app.log(TAG, CLASS_NAME + ".render(): Value " + Integer.toString(i) + " = (" + Float.toString(calib.calibrationPoints[i]) + ", " + Float.toString(calib.calibrationPoints[i + 1]) + ")");
						calibrationSamples[lastSampleTaken][i] = calib.calibrationPoints[i];
						calibrationSamples[lastSampleTaken][i + 1] = calib.calibrationPoints[i + 1];
					}

					// Move to the next sample.
					lastSampleTaken++;

					// If enough samples has been taken then calibrate the camera.
					if(lastSampleTaken == ProjectConstants.CALIBRATION_SAMPLES){
						Gdx.app.log(TAG, CLASS_NAME + "render(): Last sample taken.");

						cvProc.calibrateCamera(calibrationSamples);
					}
				}

				frame = new Pixmap(calib.outFrame, 0, calib.outFrame.length);
				tex = new Texture(frame);

				batch.begin();
				batch.draw(tex, 0, 0);
				batch.end();

				frame.dispose();
				tex.dispose();
			}
		}
	}
}
