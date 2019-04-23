package com.codinism.basketball;

import com.badlogic.gdx.graphics.g2d.Sprite;

import aurelienribon.tweenengine.TweenAccessor;

public class SpriteAccessor implements TweenAccessor<Sprite> {
    public static final int TYPE_Y = 1;
    public static final int TYPE_X = 2;
    public static final int TYPE_XY = 3;

    @Override
    public int getValues(Sprite target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case TYPE_Y:
                returnValues[0] = target.getY();
            case TYPE_X:
                returnValues[0] = target.getX();
            case TYPE_XY:
                returnValues[0] = target.getX();
                returnValues[1] = target.getY();
                return 2;

            default:
                assert false;
                return -1;
        }
    }

    @Override
    public void setValues(Sprite target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case TYPE_Y:
                target.setY(newValues[0]);
                break;
            case TYPE_X:
                target.setX(newValues[0]);
                break;
            case TYPE_XY:
                target.setX(newValues[0]);
                target.setY(newValues[1]);
                break;
            default:
                assert false;
                break;
        }
    }
}
