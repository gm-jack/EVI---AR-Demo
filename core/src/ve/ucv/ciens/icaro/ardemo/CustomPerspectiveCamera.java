package ve.ucv.ciens.icaro.ardemo;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class CustomPerspectiveCamera extends PerspectiveCamera{
	private final Vector3 tmp = new Vector3();

	public CustomPerspectiveCamera(float fieldOfView, float viewportWidth, float viewportHeight){
		super(fieldOfView, viewportWidth, viewportHeight);
		update();
	}

	public void update(Matrix4 customProjection){
		this.update(customProjection, true);
	}

	public void update(Matrix4 customProjection, boolean updateFrustum){
		projection.set(customProjection);
		view.setToLookAt(position, tmp.set(position).add(direction), up);
		combined.set(projection).mul(view);

		if(updateFrustum){
			invProjectionView.set(combined).inv();
			frustum.update(invProjectionView);
		}
	}

	public void setCustomARProjectionMatrix(final float focalPointX, final float focalPointY, final float cameraCenterX, final float cameraCenterY, final float near, final float far, final float w, final float h){
		final float FAR_PLUS_NEAR = far + near;
		final float FAR_LESS_NEAR = far - near;

		projection.val[Matrix4.M00] = -2.0f * focalPointX / w;
		projection.val[Matrix4.M10] = 0.0f;
		projection.val[Matrix4.M20] = 0.0f;
		projection.val[Matrix4.M30] = 0.0f;

		projection.val[Matrix4.M01] = 0.0f;
		projection.val[Matrix4.M11] = 2.0f * focalPointY / h;
		projection.val[Matrix4.M21] = 0.0f;
		projection.val[Matrix4.M31] = 0.0f;

		projection.val[Matrix4.M02] = 2.0f * cameraCenterX / w - 1.0f;
		projection.val[Matrix4.M12] = 2.0f * cameraCenterY / h - 1.0f;
		projection.val[Matrix4.M22] = -FAR_PLUS_NEAR / FAR_LESS_NEAR;
		projection.val[Matrix4.M32] = -1.0f;

		projection.val[Matrix4.M03] = 0.0f;
		projection.val[Matrix4.M13] = 0.0f;
		projection.val[Matrix4.M23] = -2.0f * far * near / FAR_LESS_NEAR;
		projection.val[Matrix4.M33] = 0.0f;
	}
}
