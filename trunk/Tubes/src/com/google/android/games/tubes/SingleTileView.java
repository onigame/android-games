package com.google.android.games.tubes;

/* 
 * Copyright (C) 2008 Google Inc.
 * Author: Wei-Hwa Huang
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.LinearLayout.LayoutParams;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation.AnimationListener;

/**
 * SingleTileView: a View-variant designed for handling arrays of "icons" or other
 * drawables.
 * 
 */
public class SingleTileView extends View {

    /**
     * Parameters controlling the size of the tiles and their range within view.
     * Width/Height are in pixels, and Drawables will be scaled to fit to these
     * dimensions. X/Y Tile Counts are the number of tiles that will be drawn.
     */
	
	private Bitmap mBackground;
	private Bitmap mForeground;
	private Bitmap mOverlay;
	
	private RotationListener mRotationListener;
    private RotateAnimation mRotate;
    private AnimationSet mTotalAnimation;
	private int mTimesToRotate;
	private final static int mRotationTime = 500;
	private RotationCompletedListener mRotationCompletedListener;

    public int mTileSize;

    private final Paint mPaint = new Paint();

    public SingleTileView(Context context, int tilesize) {
        super(context);
        mTileSize = tilesize;
        mBackground = null;
        mForeground = null;
        mOverlay = null;
        mTimesToRotate = 0;
        setLayoutParams(new LayoutParams(mTileSize, mTileSize));
    }
    
    public void setBackground(Bitmap b) {
    	mBackground = b;
    }
    
    public void setForeground(Bitmap b) {
    	mForeground = b;
    }
    
    public void setOverlay(Bitmap b) {
    	mOverlay = b;
    }
    
    @Override 
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBackground != null) {
           	Rect r = new Rect(0, 0, mTileSize, mTileSize);
           	canvas.drawBitmap(mBackground, null, r, mPaint);
        }
        if (mForeground != null) {
           	Rect r = new Rect(0, 0, mTileSize, mTileSize);
           	canvas.drawBitmap(mForeground, null, r, mPaint);
        }
        if (mOverlay != null) {
           	Rect r = new Rect(0, 0, mTileSize, mTileSize);
           	canvas.drawBitmap(mOverlay, null, r, mPaint);
        }
    }

    synchronized public void rotateClockwise(RotationCompletedListener r) {
		mRotationCompletedListener = r;
		mTimesToRotate++;
    	if (mTimesToRotate != 1) {
    		mTotalAnimation.addAnimation(mRotate);
    		mRotate.setDuration(mRotationTime * mTimesToRotate);
    	} else {
    		mRotate = new RotateAnimation(
    				0, 
    				90, 
    				RotateAnimation.RELATIVE_TO_SELF, 0.5f, 
    				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
    		mRotate.setDuration(mRotationTime * mTimesToRotate);
    		mRotate.setFillAfter(false);
    		mRotate.setInterpolator(new LinearInterpolator());
    		mRotate.setRepeatMode(Animation.NO_REPEAT);
    		mRotationListener = new RotationListener();
    		mRotate.setAnimationListener(mRotationListener);
    		mTotalAnimation = new AnimationSet(false);
    		mTotalAnimation.addAnimation(mRotate);
    		startAnimation(mTotalAnimation);
    	}
    }
    
	protected interface RotationCompletedListener {
		abstract public void rotationCompleted(int numRotations);
	}
	
        
    private class RotationListener implements AnimationListener {
    	public RotationListener() {
    	}
    	
		synchronized public void onAnimationEnd() {
			int temp = mTimesToRotate;
			mTimesToRotate = 0;
			mRotationCompletedListener.rotationCompleted(temp);
		}

		public void onAnimationRepeat() {
		}

		public void onAnimationStart() throws RuntimeException {
		}
    	
    }

}
