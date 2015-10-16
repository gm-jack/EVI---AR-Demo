package ve.ucv.ciens.icaro.ardemo;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

class Monkey {
	private Model         model;
	public  ModelInstance instance;
	public  Vector3       position;
	public  Matrix3       rotation;
	public  Vector3       scaling;
	private boolean       visible;

	public Monkey() {
		this.position = new Vector3();
		this.rotation = new Matrix3();
		this.scaling  = new Vector3(1.0f, 1.0f, 1.0f);
		this.visible  = false;
		this.instance = null;
		this.model    = null;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
		this.instance = new ModelInstance(this.model);
	}

	public void applyWorldTransform(){
		Matrix4 translationMatrix = new Matrix4().setToTranslation(0.0f, 0.0f, 0.0f);;
		Matrix4 rotationMatrix    = new Matrix4().idt();;
		Matrix4 scalingMatrix     = new Matrix4().setToScaling(0.0f, 0.0f, 0.0f);;

		translationMatrix.setToTranslation(this.position);

		rotationMatrix.val[Matrix4.M00] = this.rotation.val[0];
		rotationMatrix.val[Matrix4.M10] = this.rotation.val[1];
		rotationMatrix.val[Matrix4.M20] = this.rotation.val[2];
		rotationMatrix.val[Matrix4.M30] = 0;

		rotationMatrix.val[Matrix4.M01] = this.rotation.val[3];
		rotationMatrix.val[Matrix4.M11] = this.rotation.val[4];
		rotationMatrix.val[Matrix4.M21] = this.rotation.val[5];
		rotationMatrix.val[Matrix4.M31] = 0;

		rotationMatrix.val[Matrix4.M02] = this.rotation.val[6];
		rotationMatrix.val[Matrix4.M12] = this.rotation.val[7];
		rotationMatrix.val[Matrix4.M22] = this.rotation.val[8];
		rotationMatrix.val[Matrix4.M32] = 0;

		rotationMatrix.val[Matrix4.M03] = 0;
		rotationMatrix.val[Matrix4.M13] = 0;
		rotationMatrix.val[Matrix4.M23] = 0;
		rotationMatrix.val[Matrix4.M33] = 1;

		scalingMatrix.setToScaling(this.scaling);

		instance.transform.idt().mul(translationMatrix).mul(rotationMatrix).mul(scalingMatrix);
		instance.calculateTransforms();
	}
}
