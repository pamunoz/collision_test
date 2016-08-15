package com.pfariasmunoz.libgdx.collisiontest;

/**
 * Created by Pablo Farias on 15-08-16.
 */
public class MyCollisionWorld extends BulletObjects {
    public static final MyCollisionWorld instance = new MyCollisionWorld();

    private MyCollisionWorld() {
        super();
    }

    @Override
    public void init() {
        super.init();
    }
}
