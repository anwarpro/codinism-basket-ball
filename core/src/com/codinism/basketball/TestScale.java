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
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.LinkedList;

public class TestScale extends ApplicationAdapter implements GestureDetector.GestureListener, ContactListener {
    private static final float BALL_RADIOS = 0.6f;
    private static final float GROUND_Y = 0.5f;
    private static final float RIM_RADIOS = 0.02f;
    private static final float UPPER_GROUND_Y = 3f;
    final float VIRTUAL_HEIGHT = 8f;

    OrthographicCamera cam;
    SpriteBatch batch;
    Texture texture;

    ResolutionFileResolver fileResolver; // +++

    float y; // +++
    float gravity = -9.81f; // +++ earths gravity is around 9.81 m/s^2 downwards
    float velocity; // +++
    float jumpHeight = 1f; // +++ jump 1 meter every time

    private World world;
    private Box2DDebugRenderer debugRender;
    private Body ballBody;
    private Body leftBody;
    private Body rightBody;
    private Fixture groundFix;
    private Fixture groundFixTop;
    private Body groundBody;
    private boolean topOfBasket;
    private boolean updatedGround;
    private Vector3 point;
    private boolean wasTouched;
    private Vector3 point2;

    private void createBall() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(cam.viewportWidth / 2f, 4f);

        ballBody = world.createBody(bodyDef);

        // Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(BALL_RADIOS);

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit
        ballBody.createFixture(fixtureDef);
    }

    private void createFloor() {
        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(cam.viewportWidth / 2f - BALL_RADIOS, 6f);

        leftBody = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        FixtureDef fixtureDef = new FixtureDef();
        circle.setRadius(RIM_RADIOS);
        fixtureDef.shape = circle;
        fixtureDef.density = 1f;
        fixtureDef.friction = 1f;
        fixtureDef.restitution = 0.2f; // Make it bounce a little bit
        fixtureDef.isSensor = true;
        leftBody.createFixture(fixtureDef);

        bodyDef.position.set(leftBody.getPosition().x + 2 * BALL_RADIOS, leftBody.getPosition().y);
        rightBody = world.createBody(bodyDef);
        rightBody.createFixture(fixtureDef);


        // Create our body definition
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.type = BodyDef.BodyType.KinematicBody;
        groundBodyDef.position.set(new Vector2(2, 0));
        groundBody = world.createBody(groundBodyDef);

        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(cam.viewportWidth, 1f);
        groundFix = groundBody.createFixture(groundBox, 0.0f);

        Body groundBodyTop = world.createBody(groundBodyDef);
        groundBox.setAsBox(cam.viewportWidth, UPPER_GROUND_Y);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.density = 0;
        fixtureDef1.isSensor = true;
        fixtureDef1.shape = groundBox;
        groundFixTop = groundBodyTop.createFixture(fixtureDef1);
    }

    public void create() {

        fileResolver = new ResolutionFileResolver(new InternalFileHandleResolver(), new Resolution(800, 480, "480"), // +++
                new Resolution(1280, 720, "720"), new Resolution(1920, 1080, "1080")); // +++
        batch = new SpriteBatch();
        texture = new Texture(fileResolver.resolve("badlogic.jpg")); // +++
        cam = new OrthographicCamera();
        cam.setToOrtho(false, VIRTUAL_HEIGHT * Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight(), VIRTUAL_HEIGHT); // +++

        world = new World(new Vector2(0, gravity), true);
        debugRender = new Box2DDebugRenderer();

        createBall();
        createFloor();

        Gdx.input.setInputProcessor(new GestureDetector(this));
        world.setContactListener(this);
    }

    public void resize(int width, int height) {
        cam.setToOrtho(false, VIRTUAL_HEIGHT * width / (float) height, VIRTUAL_HEIGHT); // +++
        batch.setProjectionMatrix(cam.combined);
    }

    public void render() {
        cam.update();

        float delta = Math.min(1 / 10f, Gdx.graphics.getDeltaTime());

        if (topOfBasket) {
            if (ballBody.getLinearVelocity().y == 0) {
                topOfBasket = false;

                ballBody.getWorld().destroyBody(ballBody);

                groundFixTop.setSensor(true);

                leftBody.getFixtureList().get(0).setSensor(true);
                rightBody.getFixtureList().get(0).setSensor(true);

                createBall();

                updatedGround = false;
            } else {
//                batch.draw(goalPostSprite, 0, 0, V_WIDTH, V_HEIGHT);
                groundFixTop.setSensor(false);
            }
        }

        if (ballBody.getPosition().x < 0 || ballBody.getPosition().x > cam.viewportWidth) {
            topOfBasket = false;

            ballBody.getWorld().destroyBody(ballBody);

            groundFixTop.setSensor(true);

            leftBody.getFixtureList().get(0).setSensor(true);
            rightBody.getFixtureList().get(0).setSensor(true);

            createBall();

            updatedGround = false;
        }


        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
//        batch.draw(texture, 0, y, 1.8f, 1.8f); // +++
        batch.end();

    /*    If a Sprite is used instead of a Texture the correct size has to be applied to the sprite.
                To keep the origin in the center of the sprite it has to be adjusted too.*/

        //        sprite.setSize(1.8f, 1.8f);
        //        sprite.setOriginCenter();

        debugRender.render(world, cam.combined);
        world.step(1 / 60f, 6, 2);
    }

    public void dispose() {
        texture.dispose();
        batch.dispose();
        world.dispose();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        point = new Vector3();
        point.set(x, y, 0); // Translate to world coordinates.
        cam.unproject(point);
        wasTouched = ballBody.getFixtureList().first().testPoint(point.x, point.y);
        return true;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            if (velocityX > 0) {

            } else {

            }
        } else {
            if (velocityY > 0) {

            } else {
                if (wasTouched) {
                    if (ballBody.getLinearVelocity().y == 0) {

//                    shootSound.play();
                        Vector2 initialVelocity = new Vector2(8, 8);
                        initialVelocity.rotate(45 + 1);
                        Gdx.app.log("Velocity", "" + initialVelocity.x + ", " + initialVelocity.y);
//                        ballBody.setLinearDamping(1 - 0.98f);
                        ballBody.setLinearVelocity(initialVelocity.x, initialVelocity.y);
                        ballBody.getFixtureList().get(0).getShape().setRadius(0.4f);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        point2 = new Vector3();
        point2.set(x, y, 0);
        cam.unproject(point2);
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }

    @Override
    public void beginContact(Contact contact) {

    }

    @Override
    public void endContact(Contact contact) {
        Fixture A = contact.getFixtureA();
        Fixture B = contact.getFixtureB();

        if (A.getBody().getType() == BodyDef.BodyType.StaticBody) {
            leftBody.getFixtureList().get(0).setSensor(false);
            rightBody.getFixtureList().get(0).setSensor(false);
            topOfBasket = true;
        } else if (B.getBody().getType() == BodyDef.BodyType.StaticBody) {
            leftBody.getFixtureList().get(0).setSensor(false);
            rightBody.getFixtureList().get(0).setSensor(false);
            topOfBasket = true;
        }

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
