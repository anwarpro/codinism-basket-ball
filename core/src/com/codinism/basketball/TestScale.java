package com.codinism.basketball;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver.Resolution;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.codinism.basketball.water.Pair;
import com.codinism.basketball.water.Water;

public class TestScale extends ApplicationAdapter implements GestureDetector.GestureListener, ContactListener {
    private static final float BALL_RADIOS = 0.5f;
    private static final float GROUND_Y = 0.5f;
    private static final float RIM_RADIOS = 0.02f;
    private static final float UPPER_GROUND_Y = 3f;
    final float VIRTUAL_HEIGHT = 8f;

    OrthographicCamera cam;
    SpriteBatch batch;
    Texture texture;

    ResolutionFileResolver fileResolver; // +++

    float gravity = -9.81f; // +++ earths gravity is around 9.81 m/s^2 downwards

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
    private boolean shoot;
    private boolean win;
    private boolean lose;

    private Sound dropSound1;
    private Sound shootSound;
    private Sound croowedSound;
    private Sprite spriteBall;
    private Sprite spriteFloor;
    private Sprite spriteWall;
    private Sprite spriteTopMonitor;
    private Sprite spriteSideMonitor;
    private Sprite spriteBasketRim;
    private Water water;
    private Sprite spriteBasketNet;
    private Sprite spriteBasketBack;

    public static final int STEEL = 0;
    public static final int WOOD = 1;
    public static final int RUBBER = 2;
    public static final int STONE = 3;

    private int ballShooted = 0;
    private int ballRemain = 3;
    private int ballStored = 0;
    private int round = 1;

    private Body basketSensor;
    private Sprite[] spriteBallJar;

    private static FixtureDef makeFixture(int material, Shape shape) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        switch (material) {
            case 0:
                fixtureDef.density = 1f;
                fixtureDef.friction = 0.3f;
                fixtureDef.restitution = 0.1f;
                break;
            case 1:
                fixtureDef.density = 0.5f;
                fixtureDef.friction = 0.7f;
                fixtureDef.restitution = 0.3f;
                break;
            case 2:
                fixtureDef.density = 1f;
                fixtureDef.friction = 0f;
                fixtureDef.restitution = 1f;
                break;
            case 3:
                fixtureDef.density = 1f;
                fixtureDef.friction = 0.9f;
                fixtureDef.restitution = 0.01f;
            default:
                fixtureDef.density = 7f;
                fixtureDef.friction = 0.5f;
                fixtureDef.restitution = 0.3f;
        }
        return fixtureDef;
    }

    private void makeChainShape(World world) {

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(leftBody.getPosition().x, leftBody.getPosition().y);
        bodyDef.angle = 0;

        Body body = world.createBody(bodyDef);

        EdgeShape es = new EdgeShape();
        es.set(leftBody.getPosition().x, leftBody.getPosition().y,
                leftBody.getPosition().x, 2f);

        body.setTransform(leftBody.getPosition().x, leftBody.getPosition().y,
                MathUtils.atan2(2f - leftBody.getPosition().y,
                        leftBody.getPosition().x + 0.25f - leftBody.getPosition().x));

        FixtureDef fixtureDef = makeFixture(RUBBER, es);

        body.createFixture(fixtureDef);

    }

    private void createBall() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(cam.viewportWidth / 2f, 3f);

        ballBody = world.createBody(bodyDef);

        // Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(BALL_RADIOS);

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.000001f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.5f; // Make it bounce a little bit
        ballBody.createFixture(fixtureDef);

        shoot = false;
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
        fixtureDef.density = 0f;
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
        groundBodyDef.position.set(new Vector2(0, 0));
        groundBody = world.createBody(groundBodyDef);

        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(cam.viewportWidth, 0.4f);

        groundFix = groundBody.createFixture(makeFixture(WOOD, groundBox));

        Body groundBodyTop = world.createBody(groundBodyDef);
        groundBox.setAsBox(cam.viewportWidth, UPPER_GROUND_Y);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.density = 0;
        fixtureDef1.isSensor = true;
        fixtureDef1.shape = groundBox;
        groundFixTop = groundBodyTop.createFixture(fixtureDef1);

        //create basket sensor

        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(leftBody.getPosition().x + BALL_RADIOS, leftBody.getPosition().y - 0.45f);
        basketSensor = world.createBody(bodyDef);

        groundBox.setAsBox(BALL_RADIOS,
                0.4f);
        fixtureDef1.shape = groundBox;
        basketSensor.createFixture(fixtureDef1);
        basketSensor.setUserData("BASKET");

        // Create our body definition
        BodyDef basketSensorBodyDef = new BodyDef();
        basketSensorBodyDef.type = BodyDef.BodyType.StaticBody;
        basketSensorBodyDef.position.set(new Vector2(0, leftBody.getPosition().y));

        Body sensor = world.createBody(basketSensorBodyDef);
        sensor.setUserData("basketline");
        PolygonShape groundBoxS = new PolygonShape();
        groundBoxS.setAsBox(cam.viewportWidth, 0.01f);

        FixtureDef fixtureDef2 = new FixtureDef();
        fixtureDef2.density = 0;
        fixtureDef2.isSensor = true;
        fixtureDef2.shape = groundBoxS;
        sensor.createFixture(fixtureDef2);
    }

    private void loadAsset() {
        texture = new Texture(fileResolver.resolve("ball.png")); // +++
        spriteBall = new Sprite(texture);

        spriteBasketRim = new Sprite(new Texture(fileResolver.resolve("new/busket-transprans.png")));

        spriteFloor = new Sprite(new Texture(fileResolver.resolve("new/flor.png")));
        spriteWall = new Sprite(new Texture(fileResolver.resolve("new/wall.png")));
        spriteTopMonitor = new Sprite(new Texture(fileResolver.resolve("new/monitor.png")));
        spriteSideMonitor = new Sprite(new Texture(fileResolver.resolve("new/monitor2.png")));
        spriteBasketNet = new Sprite(new Texture(fileResolver.resolve("new/basket-net.png")));
        spriteBasketBack = new Sprite(new Texture(fileResolver.resolve("new/basket-background.png")));

        spriteBallJar = new Sprite[4];

        for (int i = 0; i < 4; i++) {
            spriteBallJar[i] = new Sprite(new Texture(fileResolver.resolve("basketjar/" + i + ".png")));
        }

    }

    public void create() {

        fileResolver = new ResolutionFileResolver(new InternalFileHandleResolver(), new Resolution(800, 480, "480"), // +++
                new Resolution(1280, 720, "720"), new Resolution(1920, 1080, "1080")); // +++
        batch = new SpriteBatch();

        loadAsset();

        cam = new OrthographicCamera();
        cam.setToOrtho(false, VIRTUAL_HEIGHT * Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight(), VIRTUAL_HEIGHT); // +++

        world = new World(new Vector2(0, gravity), true);
        debugRender = new Box2DDebugRenderer();

//        water = new Water();

        createBall();
        createFloor();

        makeChainShape(world);

        soundLoad();

        Gdx.input.setInputProcessor(new GestureDetector(this));
        world.setContactListener(this);

//        water.createBody(world, leftBody.getPosition().x + 0.55f, leftBody.getPosition().y - 0.25f,
//                rightBody.getPosition().x - leftBody.getPosition().x, 0.5f); //world, x, y, width, height
//        water.setDebugMode(true);
    }

    private void soundLoad() {
        dropSound1 = Gdx.audio.newSound(Gdx.files.internal("audio/drop.ogg"));
        shootSound = Gdx.audio.newSound(Gdx.files.internal("audio/shoot.ogg"));
        croowedSound = Gdx.audio.newSound(Gdx.files.internal("audio/croowed.mp3"));
    }

    public void resize(int width, int height) {
        cam.setToOrtho(false, VIRTUAL_HEIGHT * width / (float) height, VIRTUAL_HEIGHT); // +++
        batch.setProjectionMatrix(cam.combined);
        cam.update();
    }

    public void render() {
        cam.update();

        if (shoot && win && ballBody.getPosition().y < leftBody.getPosition().y) {
            if (win) {
                croowedSound.play();
                win = false;
            }
        }

        if (shoot && ballBody.getLinearVelocity().y == 0 ||
                (ballBody.getPosition().x + BALL_RADIOS < 0 || ballBody.getPosition().x - BALL_RADIOS > cam.viewportWidth)) {
            if (ballRemain - ballShooted > 0) {
                resetGame();
            } else {
                if (ballStored > 0) {

                    ballRemain = ballStored;
                    ballShooted = 0;
                    ballStored = 0;
                    round++;

                    resetGame();
                }
            }
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float r = ballBody.getFixtureList().get(0).getShape().getRadius();
        batch.begin();

        batch.draw(spriteFloor, 0, 0, cam.viewportWidth, UPPER_GROUND_Y + 0.5f);
        batch.draw(spriteWall, 0, UPPER_GROUND_Y + 0.5f,
                cam.viewportWidth, 8 - (UPPER_GROUND_Y + 0.5f));

        batch.draw(spriteTopMonitor, 0.5f, 8 - 1.3f,
                cam.viewportWidth - (2 * 0.5f), 0.8f);

        batch.draw(spriteSideMonitor, 0.2f, leftBody.getPosition().y - 0.125f,
                0.5f, 0.25f);

        spriteBasketBack.setSize((rightBody.getPosition().x - leftBody.getPosition().x) + 2 * 0.5f, 1.5f);
        spriteBasketBack.setOriginCenter();
        batch.draw(spriteBasketBack, leftBody.getPosition().x - 0.5f, 5f,
                (rightBody.getPosition().x - leftBody.getPosition().x) + 2 * 0.5f, 1.5f);

        spriteBasketNet.setSize(rightBody.getPosition().x - leftBody.getPosition().x, 0.8f);
        spriteBasketNet.setOriginCenter();
//        batch.draw(spriteBasketNet, leftBody.getPosition().x - RIM_RADIOS, leftBody.getPosition().y - RIM_RADIOS - 0.8f,
//                rightBody.getPosition().x - leftBody.getPosition().x, 0.8f);

        spriteBasketRim.setSize(rightBody.getPosition().x - leftBody.getPosition().x, 2 * BALL_RADIOS);
        spriteBasketRim.setOriginCenter();
        batch.draw(spriteBasketRim, leftBody.getPosition().x, leftBody.getPosition().y + 3 * RIM_RADIOS - 2 * BALL_RADIOS,
                rightBody.getPosition().x - leftBody.getPosition().x, 2 * BALL_RADIOS);

        spriteBall.setSize(2 * r, 2 * r);
        spriteBall.setOriginCenter();
        batch.draw(spriteBall, ballBody.getPosition().x - r, ballBody.getPosition().y - r,
                r * 2, 2 * r);

        if (round > 1) {
            if (ballStored > 0) {
                if (ballStored > 3) {
                    batch.draw(spriteBallJar[3], cam.viewportWidth - 2f, 3f, 1f, 2f);
                } else {
                    batch.draw(spriteBallJar[ballStored], cam.viewportWidth - 2f, 3f, 1f, 2f);
                }
            } else {
                batch.draw(spriteBallJar[0], cam.viewportWidth - 2f, 3f, 1f, 2f);
            }
        } else {
            if (ballStored > 0) {
                if (ballStored > 3) {
                    batch.draw(spriteBallJar[3], cam.viewportWidth - 2f, 3f, 1f, 2f);
                } else {
                    batch.draw(spriteBallJar[ballStored], cam.viewportWidth - 2f, 3f, 1f, 2f);
                }
            }
        }

        if (topOfBasket) {
            spriteBasketRim.setOriginCenter();
            batch.draw(spriteBasketRim, leftBody.getPosition().x, leftBody.getPosition().y + (3 * RIM_RADIOS) - 2 * BALL_RADIOS,
                    rightBody.getPosition().x - leftBody.getPosition().x, 2 * BALL_RADIOS);
        }

        batch.end();

        debugRender.render(world, cam.combined);
        world.step(1 / 60f, 6, 2);

    }

    private void resetGame() {
        topOfBasket = false;
        win = false;
        lose = false;
        updatedGround = false;

        groundFixTop.setSensor(true);
        ballBody.getWorld().destroyBody(ballBody);

        leftBody.getFixtureList().get(0).setSensor(true);
        rightBody.getFixtureList().get(0).setSensor(true);

        createBall();

    }

    public void dispose() {

        croowedSound.dispose();
        dropSound1.dispose();
        shootSound.dispose();

        texture.dispose();
        batch.dispose();
        spriteBasketBack.getTexture().dispose();
        spriteBasketBack.getTexture().dispose();
        spriteBall.getTexture().dispose();
//        water.dispose();
        world.dispose();
        debugRender.dispose();
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

        float angle = -(float) Math.toDegrees(Math.atan2(velocityY, velocityX));

        if (angle > 90) {
            angle = 90 + ((angle - 90) / 5);
        } else if (angle < 90) {
            angle = 90 - ((90 - angle) / 5);
        }

        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            if (velocityX > 0) {

            } else {

            }
        } else {
            if (velocityY > 0) {

            } else {
                if (wasTouched) {
                    if (ballBody.getLinearVelocity().y == 0) {

                        shoot = true;
                        ballShooted++;

                        float speed = new Vector2(ballBody.getPosition().x, ballBody.getPosition().y)
                                .dst(new Vector2(point2.x, point2.y));

                        shootSound.play();

                        Vector2 initialVelocity = new Vector2(Math.min(8f, Math.max(7.5f, speed * 2)),
                                Math.min(8f, Math.max(7.5f, speed * 2)));
                        initialVelocity.rotate(angle - 45);

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
        Fixture A = contact.getFixtureA();
        Fixture B = contact.getFixtureB();

        if (A.getBody().getUserData() == "BASKET") {
            if (topOfBasket) {
                win = true;
                ballStored++;
            }

        } else if (B.getBody().getUserData() == "BASKET") {
            if (topOfBasket) {
                win = true;
                ballStored++;
            }

        } else if (!A.isSensor() || !B.isSensor()) {
            dropSound1.play();
        }

    }

    @Override
    public void endContact(Contact contact) {
        Fixture A = contact.getFixtureA();
        Fixture B = contact.getFixtureB();

        if (A.getBody().getUserData() == "basketline") {
            if (!topOfBasket) {
                leftBody.getFixtureList().get(0).setSensor(false);
                rightBody.getFixtureList().get(0).setSensor(false);
                topOfBasket = true;
            } else {
                groundFixTop.setSensor(false);
            }
        } else if (B.getBody().getUserData() == "basketline") {
            if (!topOfBasket) {
                leftBody.getFixtureList().get(0).setSensor(false);
                rightBody.getFixtureList().get(0).setSensor(false);
                topOfBasket = true;
            } else {
                groundFixTop.setSensor(false);
            }
        }

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
