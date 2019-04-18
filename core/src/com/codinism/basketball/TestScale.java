package com.codinism.basketball;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver.Resolution;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TestScale extends ApplicationAdapter {
    final float VIRTUAL_HEIGHT = 4f;

    OrthographicCamera cam;
    SpriteBatch batch;
    Texture texture;

    ResolutionFileResolver fileResolver; // +++

    float y; // +++
    float gravity = -9.81f; // +++ earths gravity is around 9.81 m/s^2 downwards
    float velocity; // +++
    float jumpHeight = 1f; // +++ jump 1 meter every time

    public void create() {
        fileResolver = new ResolutionFileResolver(new InternalFileHandleResolver(), new Resolution(800, 480, "480"), // +++
                new Resolution(1280, 720, "720"), new Resolution(1920, 1080, "1080")); // +++
        batch = new SpriteBatch();
        texture = new Texture(fileResolver.resolve("badlogic.jpg")); // +++
        cam = new OrthographicCamera();
    }

    public void resize(int width, int height) { // +++
        cam.setToOrtho(false, VIRTUAL_HEIGHT * width / (float) height, VIRTUAL_HEIGHT); // +++
        batch.setProjectionMatrix(cam.combined); // +++
    } // +++

    public void render() {
        if (Gdx.input.justTouched()) // +++
            y += jumpHeight; // +++

        float delta = Math.min(1 / 10f, Gdx.graphics.getDeltaTime()); // +++
        velocity += gravity * delta; // +++
        y += velocity * delta; // +++
        if (y <= 0) // +++
            y = velocity = 0; // +++

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(texture, 0, y, 1.8f, 1.8f); // +++
        batch.end();

    /*    If a Sprite is used instead of a Texture the correct size has to be applied to the sprite.
                To keep the origin in the center of the sprite it has to be adjusted too.*/
    
        //        sprite.setSize(1.8f, 1.8f);
        //        sprite.setOriginCenter();
    }

    public void dispose() {
        texture.dispose();
        batch.dispose();
    }
}
