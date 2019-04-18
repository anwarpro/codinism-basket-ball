package com.codinism.basketball;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
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
import com.badlogic.gdx.utils.viewport.FitViewport;

public class BasketBall extends ApplicationAdapter implements GestureDetector.GestureListener, ContactListener {

    private SpriteBatch batch;
    private Texture img;

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
    private boolean topOfBasket;
    private Music shootSound;
    private Music dropSound;
    private Music croowedSound;
    private boolean updatedGround;
    private Fixture groundFixTop;
    private FitViewport viewPort;

    public static int V_WIDTH = 480;
    public static int V_HEIGHT = 640;

    private float BALL_RADIOS = 0.6f;
    private float RIM_RADIOS = 0.02f;
    private float GROUND_Y = 0.5f;
    private float UPPER_GROUND_Y = 2.5f;

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
        camera = new OrthographicCamera(V_WIDTH / 100f, V_HEIGHT / 100f);
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);

        debugRender = new Box2DDebugRenderer();

        img = new Texture(Gdx.files.internal("single basket.png"));
        backSprite = new Sprite(img, 720, 1280);
        ballSprite = new Sprite(new Texture(Gdx.files.internal("ball.png")), 141, 158);
        basketEmpty = new Sprite(new Texture(Gdx.files.internal("0.png")), 82, 177);

        goalPostSprite = new Sprite(new Texture(Gdx.files.internal("goalpost.png")), 720, 1280);


        //sound
        shootSound = Gdx.audio.newMusic(Gdx.files.internal("audio/shoot.mp3"));
        shootSound.setLooping(false);
        dropSound = Gdx.audio.newMusic(Gdx.files.internal("audio/drop.mp3"));
        dropSound.setLooping(false);
        croowedSound = Gdx.audio.newMusic(Gdx.files.internal("audio/croowed.mp3"));
        croowedSound.setLooping(false);

        createBall();


        // First we create a body definition
        BodyDef bodyDef = new BodyDef();

// Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
// Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();

        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(camera.viewportWidth / 2.5f, camera.viewportHeight / 1.4f);
        leftBody = world.createBody(bodyDef);
        circle.setRadius(RIM_RADIOS);
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.3f; // Make it bounce a little bit
        fixtureDef.isSensor = true;
        leftBody.createFixture(fixtureDef);

        bodyDef.position.set(leftBody.getPosition().x + 2 * 0.6f, leftBody.getPosition().y);
        rightBody = world.createBody(bodyDef);
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
        groundBox.setAsBox(camera.viewportWidth, GROUND_Y);
        // Create a fixture from our polygon shape and add it to our ground body
        groundFix = groundBody.createFixture(groundBox, 0.0f);

        Body groundBodyTop = world.createBody(groundBodyDef);
        groundBox.setAsBox(camera.viewportWidth, UPPER_GROUND_Y);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.density = 0;
        fixtureDef1.isSensor = true;
        fixtureDef1.shape = groundBox;

        groundFixTop = groundBodyTop.createFixture(fixtureDef1);

        Gdx.input.setInputProcessor(new GestureDetector(this));
        world.setContactListener(this);
    }

    private void createBall() {
        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
        // We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // Set our body's starting position in the world
        bodyDef.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f);

        // Create our body in the world using our body definition
        ballBody = world.createBody(bodyDef);

        // Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(0.6f);

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

        camera.update();

        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        batch.begin();
//        batch.draw(backSprite, 0, 0, V_WIDTH, V_HEIGHT);
//        batch.draw(basketEmpty, 10, 200, basketEmpty.getWidth(), basketEmpty.getHeight());


        if (ballBody.getPosition().y - convertToWorld(35) > leftBody.getPosition().y) {
            leftBody.getFixtureList().get(0).setSensor(false);
            rightBody.getFixtureList().get(0).setSensor(false);
            topOfBasket = true;
        } else {
            leftBody.getFixtureList().get(0).setSensor(true);
            rightBody.getFixtureList().get(0).setSensor(true);
        }

        float r = ballBody.getFixtureList().get(0).getShape().getRadius();

        //        ballSprite.setOrigin(ballSprite.getWidth() / 2, ballSprite.getHeight() / 2);
//        batch.draw(ballSprite, convertToBox(ballBody.getPosition().x - r),
//                convertToBox(ballBody.getPosition().y - r),
//                convertToBox(r) * 2, convertToBox(r) * 2);

        if (topOfBasket) {
            if (ballBody.getLinearVelocity().y == 0) {
                topOfBasket = false;
                ballBody.getWorld().destroyBody(ballBody);

                groundFixTop.setSensor(true);

                createBall();
                groundScale = false;
                updatedGround = false;
            } else {
//                batch.draw(goalPostSprite, 0, 0, V_WIDTH, V_HEIGHT);
                groundFixTop.setSensor(false);
            }
        }

        batch.end();

        debugRender.render(world, camera.combined);
        world.step(1 / 60f, 6, 2);
    }

    @Override
    public void dispose() {
        batch.dispose();
        dropSound.dispose();
        croowedSound.dispose();
        shootSound.dispose();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
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
                if (ballBody.getLinearVelocity().y == 0) {
//                    shootSound.play();
                    Vector3 vector3 = new Vector3(velocityX, velocityY, 0);
                    camera.unproject(vector3);
                    Gdx.app.log("Fling", "x: " + vector3.x + " y:  " + vector3.y);
                    float angle = ballBody.getPosition().angleRad(new Vector2(vector3.x, vector3.y));
                    Vector2 initialVelocity = new Vector2(6.4f, 6.4f);
                    initialVelocity.rotate(45 + angle);
                    ballBody.setLinearVelocity(initialVelocity);
                    ballBody.getFixtureList().get(0).getShape().setRadius(convertToWorld(35));
                }
            }
        }

        return false;
    }

    private float normalize(float value, float min, float max) {
        return (value - min) / (max - min);
    }


    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
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
        Fixture A = contact.getFixtureA();
        Fixture B = contact.getFixtureB();

        if (A.getBody().getType() == BodyDef.BodyType.KinematicBody) {
//            dropSound.play();
        } else if (B.getBody().getType() == BodyDef.BodyType.KinematicBody) {
//            dropSound.play();
        }

    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
