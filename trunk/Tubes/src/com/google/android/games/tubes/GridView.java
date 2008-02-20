/* 
 * Copyright (C) 2007 Google Inc.
 *
 * Author: Wei-Hwa Huang
 */

package com.google.android.games.tubes;

import java.util.Map;
import java.util.Random;
import java.util.HashSet;

import android.content.Context;
import android.content.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

/**
 * GridView: implementation of a simple game of Series of Tubes
 * 
 * 
 */
public class GridView extends TileView {

    /**
     * Current mode of application: READY to run, RUNNING, or you have already
     * lost. static final ints are used instead of an enum for performance
     * reasons.
     */
    private int mMode = READY;
    public static final int PAUSE = 0;
    public static final int READY = 1;
    public static final int RUNNING = 2;
    public static final int GAMEOVER = 3;

    /**
     * Cursor Location.
     */
    private Coordinate cursor;
    
    private static final int NORTH = 1;
    private static final int SOUTH = 2;
    private static final int EAST = 3;
    private static final int WEST = 4;

    /**
     * mMoveDelay: number of
     * milliseconds between refreshes.
     */
    private long mMoveDelay = 200;
    /**
     * mLastMove: tracks the absolute time when the snake last moved, and is used
     * to determine if a move should be made based on mMoveDelay.
     */
    private long mLastMove;
    
    /**
     * mStatusText: text shows to the user in some run states
     */
    private TextView mStatusText;
    
    /**
     * A two-dimensional array of integers in which the number represents the
     * type of pipe at that location
     */
    private int[][] mPipeType;
    
    /**
     * A two-dimensional array that tells us whether that pipe is happy
     */
    private boolean[][] mPipeHappy;
    
    /**
     * The "starting pipe", which is always happy.
     */
    private Coordinate mStarter;
    
    /**
     * Count of number of Happy nodes.
     */
    private int mHappyCount;
    
    /**
     * Whether there is horizontal wrapping.
     */
    private boolean mWrapHoriz;
    
    /**
     * Whether there is vertical wrapping.
     */
    private boolean mWrapVert;
    
    /**
     * Cursor image.
     */
    private static final int CURSOR_IMAGE = 16;
    
    /**
     * Offset to make network happy.
     */
    private static final int HAPPY_OFFSET = 16;

    /**
     * Everyone needs a little randomness in their life
     */
    private static final Random RNG = new Random();

    private RefreshHandler mRedrawHandler = new RefreshHandler();

    class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            GridView.this.update();
            GridView.this.invalidate();
        }

        public void sleep(long delayMillis) {
        	this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    };
    
    /**
     * Constructs a GridView based on inflation from XML
     * 
     * @param context
     * @param attrs
     * @param inflateParams
     */
    public GridView(Context context, AttributeSet attrs, Map inflateParams) {
        super(context, attrs, inflateParams);
        initGridView();
   }

    public GridView(Context context, AttributeSet attrs, Map inflateParams,
    		int defStyle) {
    	super(context, attrs, inflateParams, defStyle);
    	initGridView();
    }

    private void initGridView() {
        setFocusable(true);

        Resources r = this.getContext().getResources();
        
        resetTiles(32);
        loadTile(1, r.getDrawable(R.drawable.pipe1));
        loadTile(2, r.getDrawable(R.drawable.pipe2));
        loadTile(3, r.getDrawable(R.drawable.pipe3));
        loadTile(4, r.getDrawable(R.drawable.pipe4));
        loadTile(5, r.getDrawable(R.drawable.pipe5));
        loadTile(6, r.getDrawable(R.drawable.pipe6));
        loadTile(7, r.getDrawable(R.drawable.pipe7));
        loadTile(8, r.getDrawable(R.drawable.pipe8));
        loadTile(9, r.getDrawable(R.drawable.pipe9));
        loadTile(10, r.getDrawable(R.drawable.pipe10));
        loadTile(11, r.getDrawable(R.drawable.pipe11));
        loadTile(12, r.getDrawable(R.drawable.pipe12));
        loadTile(13, r.getDrawable(R.drawable.pipe13));
        loadTile(14, r.getDrawable(R.drawable.pipe14));
        loadTile(15, r.getDrawable(R.drawable.pipe15));
        loadTile(CURSOR_IMAGE, r.getDrawable(R.drawable.redstar));
        loadTile(HAPPY_OFFSET + 1, r.getDrawable(R.drawable.pipeg1));
        loadTile(HAPPY_OFFSET + 2, r.getDrawable(R.drawable.pipeg2));
        loadTile(HAPPY_OFFSET + 3, r.getDrawable(R.drawable.pipeg3));
        loadTile(HAPPY_OFFSET + 4, r.getDrawable(R.drawable.pipeg4));
        loadTile(HAPPY_OFFSET + 5, r.getDrawable(R.drawable.pipeg5));
        loadTile(HAPPY_OFFSET + 6, r.getDrawable(R.drawable.pipeg6));
        loadTile(HAPPY_OFFSET + 7, r.getDrawable(R.drawable.pipeg7));
        loadTile(HAPPY_OFFSET + 8, r.getDrawable(R.drawable.pipeg8));
        loadTile(HAPPY_OFFSET + 9, r.getDrawable(R.drawable.pipeg9));
        loadTile(HAPPY_OFFSET + 10, r.getDrawable(R.drawable.pipeg10));
        loadTile(HAPPY_OFFSET + 11, r.getDrawable(R.drawable.pipeg11));
        loadTile(HAPPY_OFFSET + 12, r.getDrawable(R.drawable.pipeg12));
        loadTile(HAPPY_OFFSET + 13, r.getDrawable(R.drawable.pipeg13));
        loadTile(HAPPY_OFFSET + 14, r.getDrawable(R.drawable.pipeg14));
        loadTile(HAPPY_OFFSET + 15, r.getDrawable(R.drawable.pipeg15));
       
    }
    
    private void initNewGame() {
        this.cursor = new Coordinate(0,0);
        resetPuzzleData();
    }

    /**
     * Save game state so that the user does not GAMEOVER anything
     * if the game process is killed while we are in the 
     * background.
     * 
     * @return a Bundle with this view's state
     */
    public Bundle saveState() {
        Bundle map = new Bundle();
        return map;
    }

    /**
     * Restore game state if our process is being relaunched
     * 
     * @param icicle a Bundle containing the game state
     */
    public void restoreState(Bundle icicle) {
        setMode(PAUSE);
    }

    /*
     * handles key events in the game.
     * 
     * @see android.view.View#onKeyDown(int, android.os.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (mMode == READY | mMode == GAMEOVER) {
                /*
                 * At the beginning of the game, or the end of a previous one,
                 * we should start a new game.
                 */
                initNewGame();
                setMode(RUNNING);
                update();
                return (true);
            }

            if (mMode == PAUSE) {
                /*
                 * If the game is merely paused, we should just continue where
                 * we left off.
                 */
                setMode(RUNNING);
                update();
                return (true);
            }
            
            if (mMode == RUNNING) {
            	rotatePipeAtCursor();
            	calculateHappiness();
            	if (mHappyCount == mXTileCount * mYTileCount) {
                    clearTiles();
                    updateGrid();
            		setMode(GAMEOVER);
            	}
            	return (true);
            }
        }
        
    	if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            this.cursor.move(NORTH);
            return (true);
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            this.cursor.move(SOUTH);
            return (true);
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            this.cursor.move(WEST);
            return (true);
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
        	this.cursor.move(EAST);
            return (true);
        }

        return super.onKeyDown(keyCode, msg);
    }

    /**
     * Sets the TextView that will be used to give information (such as "Game
     * Over" to the user.
     * 
     * @param newView
     */
    public void setTextView(TextView newView) {
        mStatusText = newView;
    }

    /**
     * Updates the current mode of the application (RUNNING or PAUSED or the like)
     * as well as sets the visibility of textview for notification
     * 
     * @param newMode
     */
    public void setMode(int newMode) {
        int oldMode = mMode;
        mMode = newMode;

        if (newMode == RUNNING & oldMode != RUNNING) {
            mStatusText.setVisibility(View.INVISIBLE);
            update();
            return;
        }

        Resources res = getContext().getResources();
        CharSequence str = "";
        if (newMode == PAUSE) {
            str = res.getText(R.string.mode_pause);
        }
        if (newMode == READY) {
            str = res.getText(R.string.mode_ready);
        }
        if (newMode == GAMEOVER) {
            str = res.getString(R.string.mode_gameover_prefix) + "???"
                  + res.getString(R.string.mode_gameover_suffix);
        }

        mStatusText.setText(str);
        mStatusText.setVisibility(View.VISIBLE);
    }

    /**
     * Handles the basic update loop, checking to see if we are in the running
     * state, determining if a move should be made, updating the Grid's location.
     */
    public void update() {
        if (mMode == RUNNING) {
            long now = System.currentTimeMillis();

            if (now - mLastMove > mMoveDelay) {
                clearTiles();
                updateGrid();
                mLastMove = now;
            }
            mRedrawHandler.sleep(mMoveDelay);
        }

    }

    /**
     * Update the view of the grid.
     * 
     */
    private void updateGrid() {
    	for (int x = 0; x < mXTileCount; x++) {
    		for (int y = 0; y < mYTileCount; y++) {
    			setTile(mPipeType[x][y] + (mPipeHappy[x][y] ? HAPPY_OFFSET : 0), x, y);
    		}
    	}
		setBackgroundTile(16, cursor.x, cursor.y);
    }

    /**
     * Simple class containing two integer values and a comparison function.
     * Cribbed from SnakeView.java
     * 
     */
    private class Coordinate {
        public int x;
        public int y;

        public Coordinate(int newX, int newY) {
            x = newX;
            y = newY;
        }
        
        public void move(int direction) {
        	if (direction == GridView.NORTH) {
        		y--;
        		if (y < 0) y += mWrapVert ? mYTileCount : 1;
        	} else if (direction == GridView.SOUTH) {
        		y++;
        		if (y >= mYTileCount) y -= mWrapVert ? mYTileCount : 1;
        	} else if (direction == GridView.EAST) {
        		x++;
        		if (x >= mXTileCount) x -= mWrapHoriz ? mXTileCount : 1;
        	} else if (direction == GridView.WEST) {
        		x--;
        		if (x < 0) x += mWrapHoriz ? mXTileCount : 1;
        	}
        }

        public boolean equals(Coordinate other) {
            if (x == other.x && y == other.y) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Coordinate: [" + x + "," + y + "]";
        }
    }

    /**
     * Creates a puzzle.
     * 
     */
    private void resetPuzzleData() {
    	int xx = mXTileCount;
    	int yy = mYTileCount;
    	int aa = xx * yy;
    	
    	mPipeType = new int[xx][yy];
    	
    	// Using Kruskal's algorithm to create the tree.
    	HashSet edges[] = new HashSet[aa];
    	int temp[] = new int[aa];
    	for (int t = 0; t < aa; t++) {
    		edges[t] = new HashSet<Integer>();
    		temp[t] = t;
    	}
    	int num_trees = aa;
    	while (num_trees > 1) {
    		int node1, node2;
    		if (RNG.nextBoolean()) {
    			// make a vertical connection
    			if (mWrapVert) {
    				node1 = RNG.nextInt(yy) * xx + RNG.nextInt(xx);
    				node2 = node1 + xx;
    				if (node2 >= aa) node2 -= aa;
    			} else {
    				node1 = RNG.nextInt(yy - 1) * xx + RNG.nextInt(xx);
    				node2 = node1 + xx;
    			}
    		} else {
    			if (mWrapHoriz) {
    				node1 = RNG.nextInt(yy) * xx + RNG.nextInt(xx);
    				node2 = node1 + 1;
    				if (node2 % xx == 0) node2 -= xx;
    			} else {
    				node1 = RNG.nextInt(yy) * xx + RNG.nextInt(xx - 1);
    				node2 = node1 + 1;    				
    			}
    		}
    		int val1 = node1; while (val1 != temp[val1]) val1 = temp[val1];
    		int val2 = node2; while (val2 != temp[val2]) val2 = temp[val2];
    		if (val1 != val2) {
    			edges[node1].add(node2);
    			edges[node2].add(node1);
    			num_trees--;
    			temp[val1] = val2;
    		}
    	}
    	// END of Kruskal's algorithm
    	for (int x = 0; x < xx; x++) {
    		for (int y = 0; y < yy; y++) {
    			int pos = y * xx + x;
    			mPipeType[x][y] = 0;
    			if (edges[pos].contains(pos - 1)) mPipeType[x][y] += 8;
    			if (edges[pos].contains(pos + xx)) mPipeType[x][y] += 4;
    			if (edges[pos].contains(pos + 1)) mPipeType[x][y] += 2;
    			if (edges[pos].contains(pos - xx)) mPipeType[x][y] += 1;
    			if (edges[pos].contains(pos + xx - 1)) mPipeType[x][y] += 8;
    			if (edges[pos].contains(pos + xx - aa)) mPipeType[x][y] += 4;
    			if (edges[pos].contains(pos - xx + 1)) mPipeType[x][y] += 2;
    			if (edges[pos].contains(pos - xx - aa)) mPipeType[x][y] += 1;
    			int spin = RNG.nextInt(4);
    			for (int j=0; j<spin; ++j) {
    				mPipeType[x][y] = rotate(mPipeType[x][y]);
    			}
    		}
    	}    	
    	
    	mStarter = new Coordinate(RNG.nextInt(xx), RNG.nextInt(yy));
    	calculateHappiness();
    }
    
    /**
     * Rotates the given value.
     * 
     * @param value
     */
    private int rotate(int value) {
    	int new_value = value * 2;
    	return ((new_value < 16) ? new_value : (new_value - 15));
    }
    
    /**
     * Rotates the object at the current cursor location.
     */
    private void rotatePipeAtCursor() {
    	mPipeType[cursor.x][cursor.y] = rotate(mPipeType[cursor.x][cursor.y]);
    }
    
    private void testWest(int x1, int y1, int x2, int y2) {
    	if (!(mPipeHappy[x2][y2])
    		&& mPipeType[x1][y1] >= 8
    		&& mPipeType[x2][y2] % 4 >= 2)
    			recurseHappiness(x2, y2);
    }
    private void testEast(int x1, int y1, int x2, int y2) {
    	if (!(mPipeHappy[x2][y2])
    		&& mPipeType[x2][y2] >= 8
    		&& mPipeType[x1][y1] % 4 >= 2)
    			recurseHappiness(x2, y2);
    }
    private void testNorth(int x1, int y1, int x2, int y2) {
    	if (!(mPipeHappy[x2][y2])
    		&& mPipeType[x1][y1] % 2 == 1
    		&& mPipeType[x2][y2] % 8 >= 4)
    			recurseHappiness(x2, y2);
    }
    private void testSouth(int x1, int y1, int x2, int y2) {
    	if (!(mPipeHappy[x2][y2])
       		&& mPipeType[x2][y2] % 2 == 1
      		&& mPipeType[x1][y1] % 8 >= 4)
    			recurseHappiness(x2, y2);
    }

    /**
     * Recursive routine for calculating happiness.
     * 
     * @param x
     * @param y
     */
    private void recurseHappiness(int x, int y) {
    	mPipeHappy[x][y] = true;
    	mHappyCount++;
    	if (x != 0) {
    		testWest(x, y, x-1, y);
    	} else if (mWrapHoriz) {
    		testWest(x, y, mXTileCount-1, y);
    	}
    	if (x != mXTileCount-1) {
    		testEast(x, y, x+1, y);
    	} else if (mWrapHoriz) {
    		testEast(x, y, 0, y);
    	}
        if (y != 0) {
            this.testNorth(x, y, x, y-1);
        } else if (mWrapVert) {
            this.testNorth(x, y, x, mYTileCount-1);
        }
        if (y != mYTileCount-1) {
            this.testSouth(x, y, x, y+1);
        } else if (mWrapVert) {
            this.testSouth(x, y, x, 0);
        }
    }
    
    /**
     * Calculates which nodes are happy.
     */
    private void calculateHappiness() {
    	mHappyCount = 0;
    	mPipeHappy = new boolean[mXTileCount][mYTileCount];
    	recurseHappiness(mStarter.x, mStarter.y);
    }    
}
