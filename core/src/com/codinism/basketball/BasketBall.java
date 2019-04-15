package com.codinism.basketball;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class BasketBall extends ApplicationAdapter implements InputProcessor {

    SpriteBatch batch;
    Texture img;

    public static float PIXEL_PER_METER = 100;

    private World world;
    private OrthographicCamera camera;
    private Sprite backSprite;
    private Sprite ballSprite;
    private Sprite basketEmpty;
    private Sprite goalPostSprite;
    private Box2DDebugRenderer debugRender;
    private Body ballBody;
    private Body ground;
    private Body leftBody;
    private Body rightBody;
    private boolean isDown;
    private Vector3 startVector;
    private Fixture groundFix;
    private boolean groundScale;

    private float convertToWorld(float px) {
        return px / 100;
    }

    private float convertToBox(float meter) {
        return meter * 100;
    }

    @Override

    public void create() {
        batch = new SpriteBatch();
        world = new World(new Vector2(0, -10f), true);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, convertToWorld(Gdx.graphics.getWidth()), convertToWorld(Gdx.graphics.getHeight()));
        debugRender = new Box2DDebugRenderer();

        img = new Texture(Gdx.files.internal("single basket.png"));
        backSprite = new Sprite(img, 720, 1280);
        ballSprite = new Sprite(new Texture(Gdx.files.internal("ball.png")), 141, 158);
        basketEmpty = new Sprite(new Texture(Gdx.files.internal("0.png")), 82, 177);

        goalPostSprite = new Sprite(new Texture(Gdx.files.internal("goalpost.png")), 147, 128);


        createBall();


        // First we create a body definition
        BodyDef bodyDef = new BodyDef();

// Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
// Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();

        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(convertToWorld(Gdx.graphics.getWidth() - 320), convertToWorld(Gdx.graphics.getHeight() - 250));
        leftBody = world.createBody(bodyDef);
        circle.setRadius(convertToWorld(5 / 2));
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.3f; // Make it bounce a little bit
        fixtureDef.isSensor = true;
        leftBody.createFixture(fixtureDef);

        bodyDef.position.set(convertToWorld(Gdx.graphics.getWidth() - 320 + 100), convertToWorld(Gdx.graphics.getHeight() - 250));
        rightBody = world.createBody(bodyDef);
        circle.setRadius(convertToWorld(5 / 2));
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.3f; // Make it bounce a little bit
        rightBody.createFixture(fixtureDef);


        // Create our body definition
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.type = BodyDef.BodyType.KinematicBody;
// Set its world position
        groundBodyDef.position.set(new Vector2(0, 0));

// Create a body from the defintion and add it to the world
        Body groundBody = world.createBody(groundBodyDef);

// Create a polygon shape
        PolygonShape groundBox = new PolygonShape();
// Set the polygon shape as a box which is twice the size of our view port and 20 high
// (setAsBox takes half-width and half-height as arguments)
        groundBox.setAsBox(camera.viewportWidth, convertToWorld(49));
// Create a fixture from our polygon shape and add it to our ground body
        groundFix = groundBody.createFixture(groundBox, 0.0f);

        Gdx.input.setInputProcessor(this);

    }

    private void createBall() {
        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
// We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
// Set our body's starting position in the world
        bodyDef.position.set(convertToWorld(Gdx.graphics.getWidth() / 2f), convertToWorld(Gdx.graphics.getHeight() / 2f));

// Create our body in the world using our body definition
        ballBody = world.createBody(bodyDef);

// Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(convertToWorld(79));

// Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit
        ballBody.createFixture(fixtureDef);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(backSprite, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(basketEmpty, 40, 400, basketEmpty.getWidth(), basketEmpty.getHeight());


        if (ballBody.getPosition().y > leftBody.getPosition().y) {
            leftBody.getFixtureList().get(0).setSensor(false);
            rightBody.getFixtureList().get(0).setSensor(false);
        } else {
            leftBody.getFixtureList().get(0).setSensor(true);
            rightBody.getFixtureList().get(0).setSensor(true);
        }

        float r = ballBody.getFixtureList().get(0).getShape().getRadius();

//        ballSprite.setOrigin(ballSprite.getWidth() / 2, ballSprite.getHeight() / 2);
        batch.draw(ballSprite, convertToBox(ballBody.getPosition().x - r),
                convertToBox(ballBody.getPosition().y - r),
                convertToBox(r) * 2, convertToBox(r) * 2);

        batch.end();

        if (groundScale) {
            PolygonShape ps = (PolygonShape) groundFix.getShape();
            if (ballBody.getPosition().y < 3.8) {
                ps.setAsBox(camera.viewportWidth, ballBody.getPosition().y + 0.1f);
            }
        }

        if (ballBody.getPosition().x > convertToWorld(Gdx.graphics.getWidth()) || ballBody.getPosition().x < convertToWorld(20)) {
            ballBody.getWorld().destroyBody(ballBody);
            createBall();
            groundScale = false;
            PolygonShape ps = (PolygonShape) groundFix.getShape();
            ps.setAsBox(camera.viewportWidth, convertToWorld(49));
        }


        debugRender.render(world, camera.combined);
        world.step(1 / 60f, 6, 2);
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Gdx.app.log("touchDown", "x: " + screenX + " Y: " + screenY);

        isDown = true;
        startVector = new Vector3(screenX, screenY, 0);
        camera.unproject(startVector);
        Gdx.app.log("IN World", "x: " + startVector.x + " Y: " + startVector.y);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Gdx.app.log("tochUp", "x: " + screenX + " Y: " + screenY);
        Vector3 touchPos = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPos);
        Gdx.app.log("IN World", "x: " + touchPos.x + " Y: " + touchPos.y);
        if (isDown) {

            float speed = 5f;

            Vector2 endVector = new Vector2(touchPos.x, touchPos.y);
            Vector2 startVector2 = new Vector2(startVector.x, startVector.y);

            float angle = endVector.angleRad(startVector2);
            Gdx.app.log("ANgle", "" + angle);

            Vector2 initialVelocity = new Vector2(startVector2.x - endVector.x, startVector2.y - endVector.y * 10);
            initialVelocity.rotate(angle);
            ballBody.setLinearVelocity(initialVelocity);
            ballBody.getFixtureList().get(0).getShape().setRadius(convertToWorld(35));

            groundScale = true;

        } else {
            isDown = false;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
