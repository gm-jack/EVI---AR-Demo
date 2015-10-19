package ve.ucv.ciens.icaro.ardemo;

import ve.ucv.ciens.icaro.ardemo.ImageProcessor.CalibrationData;
import ve.ucv.ciens.icaro.ardemo.ImageProcessor.MarkerData;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;

/**
 * This is the core class of the demo. It handles all resource loading and rendering.
 * 
 * @author Miguel Angel Astor Romero
 */
public class EviDemo extends ApplicationAdapter {
	private static final String TAG        = "EVI DEMO - CORE";
	private static final String CLASS_NAME = EviDemo.class.getSimpleName();

	private ImageProcessor          cvProc;
	private Texture                 tex;
	private Pixmap                  frame;
	private SpriteBatch             batch;
	private ModelBatch              mBatch;
	private MarkerData              data;
	private CalibrationData         calib;
	private AssetManager            manager;
	private boolean                 doneLoading;
	private CustomPerspectiveCamera camera;
	private Monkey                  monkey;

	private float[][] calibrationSamples;
	private int lastSampleTaken;

	public EviDemo(ImageProcessor proc) {
		super();
		tex                = null;
		cvProc             = proc;
		frame              = null;
		doneLoading        = false;
		lastSampleTaken    = 0;
		calibrationSamples = new float[ProjectConstants.CALIBRATION_SAMPLES][];
		monkey             = new Monkey();

		for(int i = 0; i < calibrationSamples.length; i++){
			calibrationSamples[i] = new float[ProjectConstants.CALIBRATION_PATTERN_POINTS * 2];
		}
	}

	@Override
	public void create () {
		batch   = new SpriteBatch();
		mBatch  = new ModelBatch();

		manager = new AssetManager();
		manager.load("monkey.g3db", Model.class);

		camera      = new CustomPerspectiveCamera(67, ProjectConstants.W, ProjectConstants.H);
		camera.near = ProjectConstants.NEAR;
		camera.far  = ProjectConstants.FAR;
		camera.translate(0.0f, 0.0f, 0.0f);
		camera.lookAt(0.0f, 0.0f, -1.0f);
		camera.update();
	}

	@Override
	public void dispose() {
		if(tex != null) tex.dispose();
		manager.dispose();
		batch.dispose();
		mBatch.dispose();
	}

	@Override
	public void render () {
		float focalPointX, focalPointY, cameraCenterX, cameraCenterY;

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		if(doneLoading) {
			/*
			 * When all resources are loaded we can start the rendering.
			 */

			if(cvProc.isCameraCalibrated()) {
				data = cvProc.findMarkersInFrame();

				if(data != null) {

					for(int i = 0; i < ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS; i++) {
						if(data.markerCodes[i] == ProjectConstants.CODE) {
							monkey.position.set(data.translationVectors[i]);
							monkey.rotation.set(data.rotationMatrices[i]);
							monkey.applyWorldTransform();
							monkey.setVisible(true);
						}
					}

					focalPointX   = cvProc.getFocalPointX();
					focalPointY   = cvProc.getFocalPointY();
					cameraCenterX = cvProc.getCameraCenterX();
					cameraCenterY = cvProc.getCameraCenterY();
					camera.setCustomARProjectionMatrix(
							focalPointX,
							focalPointY,
							cameraCenterX,
							cameraCenterY,
							ProjectConstants.NEAR,
							ProjectConstants.FAR,
							ProjectConstants.W,
							ProjectConstants.H
							);
					camera.update(camera.projection);

					frame = new Pixmap(data.outFrame, 0, data.outFrame.length);
					tex = new Texture(frame);

					batch.begin(); {
						batch.draw(tex, 0, 0);
					} batch.end();

					if(monkey.isVisible()) {
						mBatch.begin(camera); {
							mBatch.render(monkey.instance);
						} mBatch.end();
						monkey.setVisible(false);
					}

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

		} else {
			/*
			 * Keep loading the models until the AssetsManager finishes.
			 */
			doneLoading = manager.update();
			if(doneLoading)
				monkey.setModel(manager.get("monkey.g3db", Model.class));
		}
	}
}
