package com.google.android.games.tubes;

public class GameSettings {
	private int mGridHeight;
	private int mGridWidth;
	private long mPuzzleID;  // used as seed for Random number generator
	
	public GameSettings() {
		mGridHeight = 7;
		mGridWidth = 7;
		mPuzzleID = 1;
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

	public long getPuzzleID() {
		return mPuzzleID;
	}

	public void setPuzzleID(long puzzleID) {
		mPuzzleID = puzzleID;
	}
}
