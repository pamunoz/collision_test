package com.pfariasmunoz.libgdx.collisiontest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.utils.Array;

public class MyCollisionTest extends ApplicationAdapter {
	PerspectiveCamera cam;
	ModelBatch modelBatch;
	Array<Model> models;
	ModelInstance sphereInstance;
	ModelInstance groundInstance;
	Environment environment;
	ModelBuilder modelBuilder;

    MyCollisionWorld worldInstance;
    btRigidBody groundBody;
    MyContactListener collisionListener;
    Sprite box, cone, cylinder, sphere, raypick, tick;
    ClosestRayResultCallback rayTestCB;
    Vector3 rayFrom = new Vector3();
    Vector3 rayTo = new Vector3();

    BitmapFont font;
    OrthographicCamera guiCam;
    SpriteBatch batch;

    // Bullet
    private btDefaultCollisionConfiguration collisionConfiguration;
    private btCollisionDispatcher dispatcher;
    private btDbvtBroadphase broadphase;
    private btSequentialImpulseConstraintSolver solver;
    private btDiscreteDynamicsWorld world;
    private Array<btCollisionShape> shapes = new Array<btCollisionShape>();
    private Array<btRigidBodyConstructionInfo> bodyInfos = new Array<btRigidBodyConstructionInfo>();
    private Array<btRigidBody> bodies = new Array<btRigidBody>();
    private btDefaultMotionState sphereMotionState;

	@Override
	public void create () {
        worldInstance = MyCollisionWorld.instance;
        worldInstance.init();
        groundBody = worldInstance.create_ground();

        int w = -10;
        for (int i = 0; i < 10; i++) {
            worldInstance.create_box(new Vector3(w += 2, 1.5f, 10), true);
        }

        rayTestCB = new ClosestRayResultCallback(Vector3.Zero, Vector3.Z);
        font = new BitmapFont();
        guiCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        guiCam.position.set(guiCam.viewportWidth / 2f, guiCam.viewportHeight / 2f, 0);
        guiCam.update();
        batch = new SpriteBatch();

        float wt = Gdx.graphics.getWidth() / 5f;
        float dt = .1f * wt;
        box = new Sprite(new Texture("cube.png"));
        box.setPosition(0, 0);

        cone = new Sprite(new Texture("cone.png"));
        cone.setPosition(wt + dt, 0);

        sphere = new Sprite(new Texture("sphere.png"));
        sphere.setPosition(2 * wt + dt, 0);

        cylinder = new Sprite(new Texture("cylinder.png"));
        cylinder.setPosition(3 * wt + dt, 0);

        raypick = new Sprite(new Texture("ray.png"));
        raypick.setPosition(4 * wt + dt, 0);

        tick = new Sprite(new Texture("mark.png"));
        enableButton(sphere);

        collisionListener = new MyContactListener();
        Gdx.input.setInputProcessor(adapter);

	}

	@Override
	public void render () {

		MyContactListener contactListener = new MyContactListener();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        world.stepSimulation(Gdx.graphics.getDeltaTime(), 5);

        sphereMotionState.getWorldTransform(sphereInstance.transform);

		modelBatch.begin(cam);
		modelBatch.render(groundInstance, environment);
		modelBatch.render(sphereInstance, environment);
		modelBatch.end();

//		// Adding some rigid bodies
//		modelBuilder.begin();
//		MeshPartBuilder mpb = modelBuilder.part("parts", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.ColorPacked, new Material(ColorAttribute.createDiffuse(Color.WHITE)));
//		mpb.setColor(1f, 1f, 1f, 1f);
//		mpb.box(0, 0, 0, 40, 1, 40);
//		Model model = modelBuilder.end();
//		groundInstance = new ModelInstance(model);
//
//		btCollisionShape groundShape = new btBoxShape(new Vector3(20, 1 / 2f, 20));
//		btRigidBodyConstructionInfo bodyInfo = new btRigidBodyConstructionInfo(0, null, groundShape, Vector3.Zero);
//		btRigidBody body = new btRigidBody(bodyInfo);
//		world.addRigidBody(body);

	}
	
	@Override
	public void dispose () {
		modelBatch.dispose();
		for (Model model : models) model.dispose();
		for (btRigidBody body : bodies) body.dispose();
        sphereMotionState.dispose();
        for (btCollisionShape shape : shapes) shape.dispose();
        for (btRigidBodyConstructionInfo info : bodyInfos) info.dispose();
        world.dispose();
        collisionConfiguration.dispose();
        dispatcher.dispose();
        broadphase.dispose();
        solver.dispose();
        Gdx.app.log(this.getClass().getName(), "Disposed");
    }
}

