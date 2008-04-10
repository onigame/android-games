/* 
 * Copyright (C) 2007 Google Inc.
 *
 * Author: Wei-Hwa Huang
 */

package com.google.android.games.tubes;

import java.util.Map;
import java.util.Random;
import java.util.HashSet;
import java.util.ArrayList;

import com.google.android.games.tubes.GameSettings;

import com.google.android.games.tubes.SingleTileView.RotationCompletedListener;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * GridView: implementation of a simple game of Series of Tubes
 * 
 * 
 */
public class GridView extends TileView {

	private GameSettings mGameState;
	
	private View mTopView;
	private Activity mMaster;
	
	private int mMoveCount;
	
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
    private Coordinate mCursor;
       
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
     * Cursor image and computer image indices.
     */
    private static final int CURSOR_IMAGE = 16;
    private static final int COMPUTER_IMAGE = 32;
    
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
    }
    
    /**
     * Constructs a GridView based on inflation from XML
     * 
     * @param context
     * @param attrs
     * @param inflateParams
     */
    public GridView(Context context, AttributeSet attrs, Map<Integer, Integer> inflateParams) {
        super(context, attrs, inflateParams);
        initGridView();
   }

    private void initGridView() {
        setFocusable(true);

        Resources r = this.getContext().getResources();
        
        resetTiles(33);
        loadTile(COMPUTER_IMAGE, r.getDrawable(R.drawable.computer));
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
    
    public void initNewGame(GameSettings gs) {
    	clearTiles();
    	mMoveCount = 0;
    	mGameState = gs;
		initializeGrid(mGameState.getWidth(),mGameState.getHeight());
    	mCursor = new Coordinate(0,0);
    	resetPuzzleData(gs.getPuzzleID());
    	String s = "Puzzle ID: " + new Integer(gs.getPuzzleID()).toString();
    	TextView t = (TextView) mTopView.findViewById(R.id.puzzle_id);
    	t.setText(s);
    	setMode(RUNNING);
        update();    		    	
    }
    
    public void updateMoveCount() {
      String s = "Moves: " + new Integer(mMoveCount).toString();
      TextView t = (TextView) mTopView.findViewById(R.id.move_count);
      t.setText(s);      
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
    
    public DialogInterface.OnClickListener mStartNewGameListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface arg0, int arg1) {
		    mGameState.nextPuzzle();
            initNewGame(mGameState);
		}
    };

    public DialogInterface.OnClickListener mUnPauseListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface arg0, int arg1) {
            setMode(RUNNING);
            update();    		
		}
    };

    /*
     * handles key events in the game.
     * 
     * @see android.view.View#onKeyDown(int, android.os.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
           	rotatePipeAtCursor();
           	return (true);
        }
        
    	if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
    		if (mMode != RUNNING) return (true);
            this.mCursor.move(NORTH);
            return (true);
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
    		if (mMode != RUNNING) return (true);
            this.mCursor.move(SOUTH);
            return (true);
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
    		if (mMode != RUNNING) return (true);
            this.mCursor.move(WEST);
            return (true);
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
    		if (mMode != RUNNING) return (true);
        	this.mCursor.move(EAST);
            return (true);
        }

        return super.onKeyDown(keyCode, msg);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Coordinate location = new Coordinate(getXindex((int)event.getX()), getYindex((int)event.getY()));
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            this.mCursor = location;
            return (true);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            this.mCursor = location;
            return (true);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            rotatePipeAtCursor();
            return (true);           
        }
        return super.onTouchEvent(event);
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
            update();
            return;
        }

        if (newMode == PAUSE) {
            final Builder b = new Builder(this.getContext());
            b.setMessage("Game Paused");
            b.setNegativeButton("Ready", mUnPauseListener);
            b.show();
        }
        if (newMode == READY) {
        	final Builder b = new Builder(this.getContext());
            b.setMessage("Ready for a New Game?");
            b.setCancelable(false);
            b.setPositiveButton("Ready", mStartNewGameListener);
            b.show();
        }
        if (newMode == GAMEOVER) {
            Log.e("33", "FTesting ");
        	final Builder b = new Builder(this.getContext());
            Log.e("33", "FTesting ");
        	Intent i = new Intent(Intent.INSERT_ACTION);
        	i.setClass(this.getContext(), ScoresActivity.class);
        	Log.e("33", "FTesting ");
        	mMaster.startSubActivity(i, 1);
            Log.e("33", "FTesting ");
            b.setMessage("Congratulations!  Ready for a New Game?");
            b.setCancelable(false);
            b.setPositiveButton("Ready", mStartNewGameListener);
            b.show();
        }

    }

    /**
     * Handles the basic update loop, checking to see if we are in the running
     * state, determining if a move should be made, updating the Grid's location.
     */
    public void update() {
        if (mMode == RUNNING) {
            long now = System.currentTimeMillis();

            if (now - mLastMove > mMoveDelay) {
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
    			setBackgroundTile(NO_TILE, x, y);
    			setTile(mPipeType[x][y] + (mPipeHappy[x][y] ? HAPPY_OFFSET : 0), x, y);
    		}
    	}
		setBackgroundTile(CURSOR_IMAGE, mCursor.x, mCursor.y);
		setOverlayTile(COMPUTER_IMAGE, mStarter.x, mStarter.y);
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
    private void resetPuzzleData(long seed) {
        RNG.setSeed(seed);
    	int xx = mXTileCount;
    	int yy = mYTileCount;
    	int aa = xx * yy;
    	
    	
    	mPipeType = new int[xx][yy];
    	
    	// Using Kruskal's algorithm to create the tree.
    	ArrayList<HashSet<Integer>> edges = new ArrayList<HashSet<Integer>>();
    	int temp[] = new int[aa];
    	for (int t = 0; t < aa; t++) {
    		edges.add(new HashSet<Integer>());
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
    			edges.get(node1).add(node2);
    			edges.get(node2).add(node1);
    			num_trees--;
    			temp[val1] = val2;
    		}
    	}
    	    	
    	// END of Kruskal's algorithm
    	for (int x = 0; x < xx; x++) {
    		for (int y = 0; y < yy; y++) {
    			int pos = y * xx + x;
    			mPipeType[x][y] = 0;
    			if (edges.get(pos).contains(pos - 1)) mPipeType[x][y] += 8;
    			if (edges.get(pos).contains(pos + xx)) mPipeType[x][y] += 4;
    			if (edges.get(pos).contains(pos + 1)) mPipeType[x][y] += 2;
    			if (edges.get(pos).contains(pos - xx)) mPipeType[x][y] += 1;
    			if (edges.get(pos).contains(pos + xx - 1)) mPipeType[x][y] += 8;
    			if (edges.get(pos).contains(pos + xx - aa)) mPipeType[x][y] += 4;
    			if (edges.get(pos).contains(pos - xx + 1)) mPipeType[x][y] += 2;
    			if (edges.get(pos).contains(pos - xx - aa)) mPipeType[x][y] += 1;
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
    
	protected class Assigner implements RotationCompletedListener {
		private int mX, mY;
		Assigner(int x, int y) {
			mX = x;
			mY = y;
		}
		synchronized public void rotationCompleted(int numRotations) {
			int value = mPipeType[mX][mY];
			for (int i=0; i<numRotations; i++)
				value = rotate(value);
			mPipeType[mX][mY] = value;
			setTile(value, mX, mY);
           	calculateHappiness();
			updateGrid();
           	if (mHappyCount == mXTileCount * mYTileCount) {
           		setMode(GAMEOVER);
           	}
	    }
	}
    
    /**
     * Rotates the object at the current cursor location.
     */
    private void rotatePipeAtCursor() {
        mMoveCount++;
        updateMoveCount();
    	Assigner r = new Assigner(mCursor.x, mCursor.y);
    	rotateTile(mCursor.x, mCursor.y, r);
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

	public GameSettings getGameState() {
		return mGameState;
	}

	public void setGameState(GameSettings gameState) {
		mGameState = gameState;
	}

  public View getTopView() {
    return mTopView;
  }

  public void setTopView(View topView) {
    mTopView = topView;
  }

  public Activity getMaster() {
    return mMaster;
  }

  public void setMaster(Activity master) {
    mMaster = master;
  }    
    
    
}
