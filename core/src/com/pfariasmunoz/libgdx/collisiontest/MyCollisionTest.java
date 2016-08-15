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

        // Initiating Bullet Physics
        Bullet.init();

        // setting up the world
        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();
        world = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        world.setGravity(new Vector3(0, -9.81f, 1f));

        // creating ground body
        btCollisionShape groundshape = new btBoxShape(new Vector3(20, 1 / 2f, 20));
        shapes.add(groundshape);
        btRigidBodyConstructionInfo bodyInfo = new btRigidBodyConstructionInfo(0, null, groundshape, Vector3.Zero);
        this.bodyInfos.add(bodyInfo);
        btRigidBody body = new btRigidBody(bodyInfo);
        bodies.add(body);

        world.addRigidBody(body);

        // creating sphere body
        sphereMotionState = new btDefaultMotionState(sphereInstance.transform);
        sphereMotionState.setWorldTransform(sphereInstance.transform);
        final btCollisionShape sphereShape = new btSphereShape(1f);
        shapes.add(sphereShape);

        bodyInfo = new btRigidBodyConstructionInfo(1, sphereMotionState, sphereShape, new Vector3(1, 1, 1));
        this.bodyInfos.add(bodyInfo);

        body = new btRigidBody(bodyInfo);
        bodies.add(body);
        world.addRigidBody(body);
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

