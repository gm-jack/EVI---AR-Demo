package ve.ucv.ciens.icaro.ardemo;

public abstract class ProjectConstants{
	public static final int     SERVICE_DISCOVERY_PORT     = 9988;
	public static final int     VIDEO_STREAMING_PORT       = 9989;
	public static final int     MOTOR_CONTROL_PORT         = 9990;
	public static final int     SENSOR_REPORT_PORT         = 9991;
	public static final int     APP_CONTROL_PORT           = 9992;
	public static final String  MULTICAST_ADDRESS          = "230.0.0.1";

	public static final int     EXIT_SUCCESS               = 0;
	public static final int     EXIT_FAILURE               = 1;

	public static final boolean DEBUG                      = false;

	public static final int[]   POWERS_OF_2                = {64, 128, 256, 512, 1024, 2048};
	public static final float   MAX_ABS_ROLL               = 60.0f;

	public static final String  FONT_CHARS                 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890:,";

	public static final int     MAXIMUM_NUMBER_OF_MARKERS  = 5;
	public static final int     CALIBRATION_PATTERN_POINTS = 54;
	public static final int     CALIBRATION_SAMPLES        = 10;

	public static final int   W    = 640;
	public static final int   H    = 360;
	public static final float NEAR = 0.01f;
	public static final float FAR  = 10.0f;
	public static final int   CODE = 213;
}
