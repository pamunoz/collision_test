package com.pfariasmunoz.libgdx.collisiontest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
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
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
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
	Environment environment;

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
		Gdx.gl.glClearColor(.2f, .2f, .2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        float delta = Gdx.graphics.getDeltaTime();
        worldInstance.update(delta);

        for(UserData data : UserData.data) {
            if(!data.isVisible(cam)) {
                worldInstance.remove(data.getBody());
            }
        }
        modelBatch.begin(cam);
        for(UserData data : UserData.data) {
            modelBatch.render(data.getInstance(), environment);
        }
        modelBatch.end();

        batch.setProjectionMatrix(guiCam.combined);
        batch.begin();
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, Gdx.graphics.getHeight());
        box.draw(batch);
        cone.draw(batch);
        cylinder.draw(batch);
        sphere.draw(batch);
        raypick.draw(batch);
        tick.draw(batch);
        batch.end();
	}
	
	@Override
	public void dispose () {
		for (UserData data : UserData.data) {
            data.dispose();
        }
        worldInstance.dispose();
        modelBatch.dispose();

        box.getTexture().dispose();
        cone.getTexture().dispose();
        cylinder.getTexture().dispose();
        raypick.getTexture().dispose();
        sphere.getTexture().dispose();
        Gdx.app.log(this.getClass().getName(), "Disposed.");
    }

    public void enableButton(Sprite sp) {
        tick.setPosition(sp.getX(), sp.getY());
    }

    // Input adapter and ray testing
    private final InputAdapter adapter = new InputAdapter() {
        private Items item = Items.SPHERE;
        private final Vector3 temp = new Vector3();

        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            guiCam.unproject(temp.set(screenX, screenY, 0));
            if(box.getBoundingRectangle().contains(temp.x, temp.y)) {
                enableButton(box);
                item = Items.BOX;
                return true;
            } else if (cone.getBoundingRectangle().contains(temp.x, temp.y)) {
                enableButton(cone);
                item = Items.CONE;
                return true;
            } else if (sphere.getBoundingRectangle().contains(temp.x, temp.y)) {
                enableButton(sphere);
                item = Items.SPHERE;
                return true;
            } else if (cylinder.getBoundingRectangle().contains(temp.x, temp.y)) {
                enableButton(cylinder);
                item = Items.CYLINDER;
                return true;
            } else if (raypick.getBoundingRectangle().contains(temp.x, temp.y)) {
                enableButton(raypick);
                item = Items.RAY_PICKING;
                return true;
            }

            Ray ray = cam.getPickRay(screenX, screenY);
            Vector3 position = ray.origin.cpy();
            btRigidBody body;
            switch (item) {
                default:
                case BOX:
                    body = worldInstance.create_box(position, false);
                    break;
                case CONE:
                    body = worldInstance.create_cone(position, false);
                    break;
                case CYLINDER:
                    body = worldInstance.create_cylinder(position, false);
                    break;
                case SPHERE:
                    body = worldInstance.create_sphere(position, false);
                    break;
                case RAY_PICKING:
                    rayFrom.set(ray.origin);
                    rayTo.set(ray.direction).scl(50f).add(rayFrom); // 50 meters max
                    rayTestCB.setCollisionObject(null);
                    rayTestCB.setClosestHitFraction(1f);
                    worldInstance.getWorld().rayTest(rayFrom, rayTo, rayTestCB);

                    if(rayTestCB.hasHit()) {
                        final btCollisionObject obj = rayTestCB.getCollisionObject();
                        body = (btRigidBody) (obj);
                        if (body != groundBody) worldInstance.remove(body);
                    }
                    return true;
            }

            body.applyCentralImpulse(ray.direction.scl(20));

            return true;
        }
    };
}

