package com.google.android.games.tubes;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.Menu.Item;
import android.view.Window;

public class PlayTubes extends Activity {
	
	private GridView mGridView;
	private static String ICICLE_KEY = "tubes-grid-view";
    public static final int INSERT_ID = Menu.FIRST;
    
    private Item mOptions;
    private Item mNewGame;
    
    private GameState mGameState;
    
    /** Called when the activity is first created.
     * Turns off the title bar, sets up the content views,
     * and fires up the GridView.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // No Title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.grid_layout);

        mGridView = (GridView) findViewById(R.id.grid);

        if (icicle == null) {
            // We were just launched -- set up a new game
        	mGameState = new GameState();
        	new NewGameDialog(this, mGameState, mGridView).show();
        } else {
            // We are being restored
            Bundle map = icicle.getBundle(ICICLE_KEY);
            if (map != null) {
            	mGridView.restoreState(map);
            	mGameState = mGridView.getGameState();
            } else {
            	mGridView.setMode(GridView.PAUSE);
            }
        }
                
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        mOptions = menu.add(0, INSERT_ID, "New Game...");
        mNewGame = menu.add(1, INSERT_ID, "Options...");
        return result;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Pause the game along with the activity
        mGridView.setMode(GridView.PAUSE);
    }

    @Override
    public void onFreeze(Bundle outState) {
        //Store the game state
        outState.putBundle(ICICLE_KEY, mGridView.saveState());
    }

	@Override
	public boolean onOptionsItemSelected(Item item) {
		if (item == mOptions) {
   		 	new NewGameDialog(this, mGameState, mGridView).show();		
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

    
}