package ve.ucv.ciens.icaro.ardemo.desktop;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;

import ve.ucv.ciens.icaro.ardemo.ImageProcessor;
import ve.ucv.ciens.icaro.ardemo.ProjectConstants;

/**
 * This class implements the glue methods needed to use the OpenCV native methods. All
 * image processing is done through this class.
 * 
 * @author Miguel Angel Astor Romero.
 */
public class CVProcessor implements ImageProcessor {
	private static final String TAG = "NXTAR_ANDROID_MAIN";
	private static final String CLASS_NAME = CVProcessor.class.getSimpleName();

	/**
	 * Indicates if the external native libraries were loaded successfully.
	 */
	private static boolean ocvOn = false;

	/*
	 * These two matrices represent the camera parameters calculated during camera calibration.
	 * Both parameters are needed to render the virtual objects correctly.
	 */
	private Mat cameraMatrix;
	private Mat distortionCoeffs;

	/*
	 * These objects are used to capture data from the video files.
	 */
	private VideoCapture markerCap;
	private VideoCapture calibCap;

	/**
	 * Indicates if the camera calibration procedure completed sucessfully.
	 */
	private boolean cameraCalibrated;

	/*
	 * This block is executed when this class is loaded by the Java VM. It attempts to load
	 * the external native libraries. 
	 */
	static{
		try{
			System.loadLibrary("opencv_java248");
			System.loadLibrary("evi_10");
			ocvOn = true;
		}catch(UnsatisfiedLinkError e){
			e.printStackTrace();
			ocvOn = false;
		}
	}

	private native void getMarkerCodesAndLocations(
			long inMat,
			long outMat,
			int[] codes,
			long camMat,
			long distMat,
			float[] translations,
			float[] rotations
			);

	private native boolean findCalibrationPattern(
			long inMat,
			long outMat,
			float[] points
			);

	private native double calibrateCameraParameters(
			long camMat,
			long distMat,
			long frame,
			float[] calibrationPoints
			);

	public static boolean isOcvOn() {
		return ocvOn;
	}

	public CVProcessor(String[] arg) {
		cameraCalibrated = false;
		cameraMatrix = new Mat();
		distortionCoeffs = new Mat();

		markerCap = new VideoCapture(arg[1]);
		calibCap = new VideoCapture(arg[2]);
	}

	@Override
	public MarkerData findMarkersInFrame() {
		if(ocvOn){
			if(cameraCalibrated){
				int[] codes = new int[ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS];
				float[] translations = new float[ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS * 3];
				float[] rotations = new float[ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS * 9];
				MarkerData data;
				boolean success;
				Mat inImg = new Mat();
				Mat outImg = new Mat();

				// Fill the codes array with -1 to indicate markers that were not found;
				for(int i : codes)
					codes[i] = -1;

				success = markerCap.grab();

				if(!success){
					Gdx.app.debug(TAG, CLASS_NAME + ".findMarkersInFrame(): Failed to fetch a frame.");
					markerCap.set(1, 1);
					return null;
				}

				markerCap.retrieve(inImg);

				Imgproc.resize(inImg, inImg, new Size(), 0.5, 0.5, Imgproc.INTER_AREA);

				// Find the markers in the input image.
				getMarkerCodesAndLocations(
						inImg.getNativeObjAddr(),
						outImg.getNativeObjAddr(),
						codes,
						cameraMatrix.getNativeObjAddr(),
						distortionCoeffs.getNativeObjAddr(),
						translations,
						rotations);

				// Encode the output image as a JPEG image.
				MatOfByte buf = new MatOfByte();
				Highgui.imencode(".jpg", outImg, buf);

				// Create and fill the output data structure.
				data = new MarkerData();
				data.outFrame = buf.toArray();
				data.markerCodes = codes;
				data.rotationMatrices = new Matrix3[ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS];
				data.translationVectors = new Vector3[ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS];

				for(int i = 0, p = 0; i < ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS; i++, p += 3){
					data.translationVectors[i] = new Vector3(translations[p], translations[p + 1], translations[p + 2]);
				}

				for(int k = 0; k < ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS; k++){
					data.rotationMatrices[k] = new Matrix3();
					for(int row = 0; row < 3; row++){
						for(int col = 0; col < 3; col++){
							data.rotationMatrices[k].val[col + (row * 3)] = rotations[col + (row * 3) + (9 * k)];
						}
					}
				}

				return data;
			}else{
				Gdx.app.debug(TAG, CLASS_NAME + ".findMarkersInFrame(): The camera has not been calibrated.");
				return null;
			}
		}else{
			Gdx.app.debug(TAG, CLASS_NAME + ".findMarkersInFrame(): OpenCV is not initialized.");
			return null;
		}
	}

	@Override
	public CalibrationData findCalibrationPattern() {
		if(ocvOn){
			boolean found;
			float points[] = new float[ProjectConstants.CALIBRATION_PATTERN_POINTS * 2];
			boolean success;
			Mat inImg = new Mat(), outImg = new Mat();
			CalibrationData data = new CalibrationData();

			// Decode the input frame and convert it to an OpenCV Matrix.
			success = calibCap.grab();

			if(!success){
				calibCap.set(1, 1);
				return null;
			}

			calibCap.retrieve(inImg);

			Imgproc.resize(inImg, inImg, new Size(), 0.5, 0.5, Imgproc.INTER_AREA);

			// Attempt to find the calibration pattern in the input frame.
			found = findCalibrationPattern(inImg.getNativeObjAddr(), outImg.getNativeObjAddr(), points);

			// Encode the output image as a JPEG image.
			MatOfByte buf = new MatOfByte();
			Highgui.imencode(".jpg", outImg, buf);

			// Prepare the output data structure.
			data.outFrame = buf.toArray();
			data.calibrationPoints = found ? points : null;

			return data;

		}else{
			Gdx.app.debug(TAG, CLASS_NAME + ".findCalibrationPattern(): OpenCV is not initialized.");
			return null;
		}
	}

	@Override
	public boolean calibrateCamera(float[][] calibrationSamples) {
		if(ocvOn){
			float[] calibrationPoints = new float[ProjectConstants.CALIBRATION_PATTERN_POINTS * 2 * ProjectConstants.CALIBRATION_SAMPLES];
			int w = ProjectConstants.CALIBRATION_PATTERN_POINTS * 2;
			boolean success;
			Mat inImg = new Mat();

			// Save the calibration points on a one dimensional array for easier parameter passing
			// to the native code.
			for(int i = 0; i < ProjectConstants.CALIBRATION_SAMPLES; i++){
				for(int j = 0, p = 0; j < ProjectConstants.CALIBRATION_PATTERN_POINTS; j++, p += 2){
					calibrationPoints[p + (w * i)] = calibrationSamples[i][p];
					calibrationPoints[(p + 1) + (w * i)] = calibrationSamples[i][p + 1];
				}
			}

			// Decode the input image and convert it to an OpenCV matrix.
			success = calibCap.grab();

			if(!success){
				calibCap.set(1, 1);
				return false;
			}

			calibCap.retrieve(inImg);

			Imgproc.resize(inImg, inImg, new Size(), 0.5, 0.5, Imgproc.INTER_AREA);

			// Attempt to obtain the camera parameters.
			double error = calibrateCameraParameters(
					cameraMatrix.getNativeObjAddr(),
					distortionCoeffs.getNativeObjAddr(),
					inImg.getNativeObjAddr(),
					calibrationPoints);

			Gdx.app.log(TAG, CLASS_NAME + "calibrateCamera(): calibrateCameraParameters retured " + Double.toString(error));
			cameraCalibrated = true;
			return cameraCalibrated;

		}else{
			Gdx.app.debug(TAG, CLASS_NAME + ".calibrateCamera(): OpenCV is not ready or failed to load.");
			return false;
		}
	}

	@Override
	public boolean isCameraCalibrated() {
		return ocvOn && cameraCalibrated;
	}

	@Override
	public float getFocalPointX() {
		return ocvOn && cameraCalibrated ? (float)cameraMatrix.get(0, 0)[0] : 0.0f;
	}

	@Override
	public float getFocalPointY() {
		return ocvOn && cameraCalibrated ? (float)cameraMatrix.get(1, 1)[0] : 0.0f;
	}

	@Override
	public float getCameraCenterX() {
		return ocvOn && cameraCalibrated ? (float)cameraMatrix.get(0, 2)[0] : 0.0f;
	}
	@Override
	public float getCameraCenterY() {
		return ocvOn && cameraCalibrated ? (float)cameraMatrix.get(1, 2)[0] : 0.0f;
	}

}
