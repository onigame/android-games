package com.google.android.games.tubes;

public class GameSettings {
	private int mGridHeight;
	private int mGridWidth;
	private int mPuzzleID;  // used as seed for Random number generator
	
	public final static int MAX_PUZZLE_ID = 100000;
	
	public GameSettings() {
		mGridHeight = 5;
		mGridWidth = 5;
		mPuzzleID = 0;
	}

	public int getHeight() {
		return mGridHeight;
	}

	public void setHeight(int gridHeight) {
		mGridHeight = gridHeight;
	}

	public int getWidth() {
		return mGridWidth;
	}

	public void setWidth(int gridWidth) {
		mGridWidth = gridWidth;
	}

	public int getPuzzleID() {
		return mPuzzleID;
	}

	public void setPuzzleID(int puzzleID) {
		mPuzzleID = puzzleID;
	}

  public void nextPuzzle() {
    mPuzzleID++;
    if (mPuzzleID >= MAX_PUZZLE_ID) {
      mPuzzleID = 0;
    }
  }
}
