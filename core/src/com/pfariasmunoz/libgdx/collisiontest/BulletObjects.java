package com.pfariasmunoz.libgdx.collisiontest;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;

/**
 * Created by Pablo Farias on 15-08-16.
 */
public class BulletObjects extends BulletWorld {
    private static final Vector3 temp = new Vector3();
    private static final Vector3 localInertia = new Vector3(1, 1, 1);
    private btCollisionShape boxShape, coneShape, sphereShape, cylinderShape, groundShape;
    private Model boxModel, coneModel, sphereModel, cylinderModel, groundModel;

    protected BulletObjects() {
        super();
    }

    @Override
    public void init() {
        super.init();
        final ModelBuilder builder = new ModelBuilder();
        float width, height, radius;

        // ground
        width = 20;
        builder.begin();
        MeshPartBuilder mpb = builder.part("parts", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.ColorPacked, new Material(ColorAttribute.createDiffuse(Color.WHITE)));
        mpb.setColor(1f, 1f, 1f, 1f);
        mpb.box(0, 0, 0, 2 * width, 1, 2 * width);
        groundModel = builder.end();
        groundShape = new btBoxShape(new Vector3(width, 1 / 2f, width));

        // box
        width = 2f;
        boxModel = builder.createBox(width, width, width, new Material(ColorAttribute.createDiffuse(Color.GREEN)), Usage.Position | Usage.Normal);
        boxShape = new btBoxShape(new Vector3(width, width, width).scl(.5f));

        // cone
        width = 1.5f;
        height = 2f;
        coneModel = builder.createCone(width, height, width, 20, new Material(ColorAttribute.createDiffuse(Color.LIGHT_GRAY)), Usage.Position | Usage.Normal);
        coneShape = new btConeShape(width / 2f, height);

        // sphere
        radius = 2f;
        sphereModel = builder.createSphere(radius, radius, radius, 20, 20, new Material(ColorAttribute.createDiffuse(Color.ORANGE)), Usage.Position | Usage.Normal);
        sphereShape = new btSphereShape(radius / 2f);

        // cylinder
        width = 2f;
        height = 2.5f;
        cylinderModel = builder.createCylinder(width, height, width, 20, new Material(ColorAttribute.createDiffuse(Color.RED)), Usage.Position | Usage.Normal);
        cylinderShape = new btCylinderShape(new Vector3(width, height, width).scl(.5f));
    }

    @Override
    public void dispose() {
        super.dispose();
        boxModel.dispose();
        coneModel.dispose();
        sphereModel.dispose();
        cylinderModel.dispose();
        groundModel.dispose();
        boxShape.dispose();
        coneShape.dispose();
        sphereShape.dispose();
        cylinderShape.dispose();
        groundShape.dispose();
    }

    public btRigidBody create_box(Vector3 position, boolean isStatic) {
        return createRigidBody(boxModel, boxShape, position, isStatic);
    }

    public btRigidBody create_cone(Vector3 position, boolean isStatic) {
        return createRigidBody(coneModel, coneShape, position, isStatic);
    }

    public btRigidBody create_sphere(Vector3 position, boolean isStatic) {
        return createRigidBody(sphereModel, sphereShape, position, isStatic);
    }

    public btRigidBody create_cylinder(Vector3 position, boolean isStatic) {
        return createRigidBody(cylinderModel, cylinderShape, position, isStatic);
    }

    public btRigidBody create_ground() {
        return createRigidBody(groundModel, groundShape, Vector3.Zero, true);
    }

    private btRigidBody createRigidBody(Model model, btCollisionShape collisionShape, Vector3 position, boolean isStatic) {
        if(isStatic) return createStaticRigidBody(model, collisionShape, position);

        final ModelInstance instance = new ModelInstance(model);
        final btMotionState motionState = new MyMotionState(instance);

        motionState.setWorldTransform(instance.transform.trn(position).rotate(Vector3.Z, MathUtils.random(360)));
        final btRigidBodyConstructionInfo bodyInfo = new btRigidBodyConstructionInfo(1, motionState, collisionShape, localInertia);
        final btRigidBody body = new btRigidBody(bodyInfo);

        body.userData = new UserData(instance, motionState, bodyInfo, body);
        world.addRigidBody(body);
        return body;
    }

    private btRigidBody createStaticRigidBody(Model model, btCollisionShape collisionShape, Vector3 position) {
        final ModelInstance instance = new ModelInstance(model);
        instance.transform.trn(position);
        final btRigidBodyConstructionInfo bodyInfo = new btRigidBodyConstructionInfo(0, null, collisionShape, Vector3.Zero);
        final btRigidBody body = new btRigidBody(bodyInfo);

        body.translate(instance.transform.getTranslation(temp));
        body.userData = new UserData(instance, null, bodyInfo, body);
        return body;
    }
}
