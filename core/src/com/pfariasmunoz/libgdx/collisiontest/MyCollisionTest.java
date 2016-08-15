package com.pfariasmunoz.libgdx.collisiontest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;

public class MyCollisionTest extends ApplicationAdapter {
	PerspectiveCamera cam;
	ModelBatch modelBatch;
	Array<Model> models;
	ModelInstance sphereInstance;
	ModelInstance groundInstance;
	Environment environment;
	ModelBuilder modelBuilder;
	SpriteBatch batch;
	Texture img;
	
	@Override
	public void create () {
		modelBatch = new ModelBatch();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0, 10, -20);
		cam.lookAt(0, 0, 0);
		cam.update();

		models = new Array<Model>();

		modelBuilder = new ModelBuilder();
		// creating a ground model using box shape
		float groundWidth = 40;
		modelBuilder.begin();
		MeshPartBuilder mpb = modelBuilder.part("parts", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.ColorUnpacked, new Material(ColorAttribute.createDiffuse(Color.WHITE)));
		mpb.setColor(1f, 1f, 1f, 1f);
		mpb.box(0, 0, 0, groundWidth, 1, groundWidth);
		Model model = modelBuilder.end();
		models.add(model);
		groundInstance = new ModelInstance(model);

		// create a sphere model
		float radius = 2f;
		final Model sphereModel = modelBuilder.createSphere(radius, radius, radius, 20, 20, new Material(ColorAttribute.createDiffuse(Color.RED), ColorAttribute.createSpecular(Color.GRAY), FloatAttribute.createShininess(64f)), Usage.Position | Usage.Normal);
		models.add(sphereModel);
		sphereInstance = new ModelInstance(sphereModel);
		sphereInstance.transform.trn(0, 10, 0);
	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		modelBatch.render(groundInstance, environment);
		modelBatch.render(sphereInstance, environment);
		modelBatch.end();
	}
	
	@Override
	public void dispose () {
		modelBatch.dispose();
		for (Model model : models) {
			model.dispose();
		}
	}
}
