package com.codinism.basketball.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.codinism.basketball.BasketBall;
import com.codinism.basketball.TestScale;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.height = BasketBall.V_HEIGHT;
        config.width = BasketBall.V_WIDTH;
        new LwjglApplication(new TestScale(), config);
    }
}
