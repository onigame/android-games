/* 
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.games.tubes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import java.util.Map;

import com.google.android.games.tubes.SingleTileView.RotationCompletedListener;

/**
 * TileView: a View-variant designed for handling arrays of "icons" or other
 * drawables.
 * 
 */
public class TileView extends ViewGroup {

    /**
     * Parameters controlling the size of the tiles and their range within view.
     * Width/Height are in pixels, and Drawables will be scaled to fit to these
     * dimensions. X/Y Tile Counts are the number of tiles that will be drawn.
     */

    protected int mTileSize;
    protected final static int NO_TILE = 0;

    protected static int mXTileCount;
    protected static int mYTileCount;


    /**
     * A hash that maps integer handles specified by the subclasser to the
     * drawable that will be used for that reference
     */
    private Bitmap[] mTileArray; 

    /**
     * A two-dimensional array of integers in which the number represents the
     * index of the tile that should be drawn at that locations
     */
    private SingleTileView[][] mChildrenViews;
    
    public TileView(Context context, AttributeSet attrs, Map<Integer, Integer> inflateParams) {
        super(context, attrs, inflateParams);
        mXTileCount = 1;
        mYTileCount = 1;
        mTileSize = 44;
        resetTiles(1);
        mChildrenViews = new SingleTileView[1][1];
        mChildrenViews[0][0] = new SingleTileView(getContext(), mTileSize);
    } 
    
    /**
     * Rests the internal array of Bitmaps used for drawing tiles, and
     * sets the maximum index of tiles to be inserted
     * 
     * @param tilecount
     */
    
    public void resetTiles(int tilecount) {
    	mTileArray = new Bitmap[tilecount];
    	mTileArray[0] = null;
    }

    /**
     * Sets up the View to have the right number of children
     */
    public void initializeGrid (int xCount, int yCount) {
        mXTileCount = xCount;
        mYTileCount = yCount;
        
    	int max_height = (int) Math.floor(this.getHeight() / mYTileCount);
        int max_width = (int) Math.floor(this.getWidth() / mXTileCount);
    	mTileSize = Math.max(1,Math.min(max_height, max_width));
        
        mChildrenViews = new SingleTileView[mXTileCount][mYTileCount];
    	for (int y=0; y<mYTileCount; ++y) {
            for (int x=0; x<mXTileCount; ++x) {
               	mChildrenViews[x][y] = new SingleTileView(getContext(), mTileSize);
               	this.addView(mChildrenViews[x][y]);
            }
        }    	
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	int max_height = (int) Math.floor(h / mYTileCount);
    	int max_width = (int) Math.floor(w / mXTileCount);
    	mTileSize = Math.max(1,Math.min(max_height, max_width));
    	       
    	for (int y=0; y<mYTileCount; ++y) {
             for (int x=0; x<mXTileCount; ++x) {
               	mChildrenViews[x][y].mTileSize = mTileSize;
            }
        }    	        
    }

    /**
     * Function to set the specified Drawable as the tile for a particular
     * integer key.
     * 
     * @param key
     * @param tile
     */
    public void loadTile(int key, Drawable tile) {
        Bitmap bitmap = Bitmap.createBitmap(mTileSize, mTileSize, true);
        Canvas canvas = new Canvas(bitmap);
        tile.setBounds(0, 0, mTileSize, mTileSize);
        tile.draw(canvas);
        
        mTileArray[key] = bitmap;
    }

    /**
     * Resets all tiles to 0 (empty)
     * 
     */
    public void clearTiles() {
        for (int x = 0; x < mXTileCount; x++) {
            for (int y = 0; y < mYTileCount; y++) {
				setTile(0, x, y);
                setBackgroundTile(0, x, y);
                setOverlayTile(0, x, y);
            }
        }
    }

    /**
     * Used to indicate that a particular tile (set with loadTile and referenced
     * by an integer) should be drawn at the given x/y coordinates during the
     * next invalidate/draw cycle.
     * 
     * @param tileindex
     * @param x
     * @param y
     * @throws AnimationProgressException 
     */
    public void setTile(int tileindex, int x, int y) {
    	this.mChildrenViews[x][y].setForeground(mTileArray[tileindex]);
    }
    
    /**
     * Used to indicate that a particular tile (set with loadTile and referenced
     * by an integer) should be drawn at the given x/y coordinates during the
     * next invalidate/draw cycle.
     * 
     * @param tileindex
     * @param x
     * @param y
     */
    public void setBackgroundTile(int tileindex, int x, int y) {
    	this.mChildrenViews[x][y].setBackground(mTileArray[tileindex]);
    }
    
    /**
     * Used to indicate that a particular tile (set with loadTile and referenced
     * by an integer) should be drawn at the given x/y coordinates during the
     * next invalidate/draw cycle.
     * 
     * @param tileindex
     * @param x
     * @param y
     */
    public void setOverlayTile(int tileindex, int x, int y) {
    	this.mChildrenViews[x][y].setOverlay(mTileArray[tileindex]);
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    	int height = bottom - top;
    	int width = right - left;
    	int startX = ((width - mXTileCount * mTileSize) / 2);
    	int startY = ((height - mYTileCount * mTileSize) / 2);
    	
    	for (int y=0; y<mYTileCount; ++y) {
            for (int x=0; x<mXTileCount; ++x) {
               	mChildrenViews[x][y].layout(
               		startX + mTileSize * x,
               		startY + mTileSize * y,
               		startX + mTileSize * (x+1),
               		startY + mTileSize * (y+1));
            }
        }    	
	}

	protected void rotateTile(int x, int y, RotationCompletedListener r) {
		this.mChildrenViews[x][y].rotateClockwise(r);
	}
}
