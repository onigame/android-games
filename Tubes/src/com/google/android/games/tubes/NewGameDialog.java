/**
 * 
 */
package com.google.android.games.tubes;

import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.games.tubes.R;

import java.util.Random;

/**
 * @author whuang
 *
 */
public class NewGameDialog extends Dialog {
	
	private EditText mHeight;
	private EditText mWidth;
	private EditText mPuzzleIdBox;
	private Button mCancelButton;
	private Button mOkButton;
	private GameSettings mGameState;
	private GridView mGridView;
	
	private View.OnClickListener mCancelListener = new View.OnClickListener() {
		public void onClick(View v) {
			cancel();
		}
	};
	
	private View.OnClickListener mOkListener = new View.OnClickListener() {
		private final static int MIN = 3;
		private final static int MAX = 20;
	    public DialogInterface.OnClickListener mDialogDismiss = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
			}
	    };
		public void onClick(View v) {
			try {
				String newHeightS = mHeight.getText().toString();
				Integer newHeightI = Integer.parseInt(newHeightS);
				if (!newHeightI.toString().equals(newHeightS))
					throw new NumberFormatException();
				String newWidthS = mWidth.getText().toString();
				Integer newWidthI = Integer.parseInt(newWidthS);
                if (!newWidthI.toString().equals(newWidthS))
                  throw new NumberFormatException();
				String newPuzzleIdS = mPuzzleIdBox.getText().toString();
				Integer newPuzzleIdI;
				if (newPuzzleIdS.equals("")) {
				  newPuzzleIdI = mGameState.getPuzzleID();
				} else {
	              newPuzzleIdI = Integer.parseInt(newPuzzleIdS);
				  if (!newPuzzleIdI.toString().equals(newPuzzleIdS)) {
                    throw new NumberFormatException();
				  }
				}
		
				if (newHeightI.intValue() < MIN)
					throw new IllegalArgumentException();
				if (newHeightI.intValue() > MAX)
					throw new IllegalArgumentException();
                if (newWidthI.intValue() < MIN)
                  throw new IllegalArgumentException();
                if (newWidthI.intValue() > MAX)
                  throw new IllegalArgumentException();
                if (newPuzzleIdI.intValue() < 0)
                  throw new IllegalArgumentException();
                if (newPuzzleIdI.intValue() >= GameSettings.MAX_PUZZLE_ID)
                  throw new IllegalArgumentException();
				mGameState.setHeight(newHeightI.intValue());
				mGameState.setWidth(newWidthI.intValue());
				mGameState.setPuzzleID(newPuzzleIdI.intValue());
				dismiss();
				mGridView.initNewGame(mGameState);
			} catch (NumberFormatException e) {
	        	final Builder b = new Builder(getContext());
	            b.setMessage("Values must be integers.");
	            b.setCancelable(true);
	            b.setNeutralButton("OK", mDialogDismiss);
	            b.show();
			} catch (IllegalArgumentException e) {
	        	final Builder b = new Builder(getContext());
	            b.setMessage("Height and Width must be in the range " + MIN + "-" + MAX
	                + ".  Puzzle ID must be in the range 0-" + GameSettings.MAX_PUZZLE_ID + ".");
	            b.setCancelable(true);
	            b.setNeutralButton("OK", mDialogDismiss);
	            b.show();
			}
		}
	};
	
	public NewGameDialog(Context context, GameSettings gamestate, GridView gridview) {
		super(context);
		mGameState = gamestate;
		mGridView = gridview;
        setContentView(R.layout.options_dialog_layout);
        setTitle("New Game");
		setCancelable(true);
		
		mHeight = (EditText) findViewById(R.id.height);
		mHeight.setText(new Integer(mGameState.getHeight()).toString());
		mWidth = (EditText) findViewById(R.id.width);
		mWidth.setText(new Integer(mGameState.getWidth()).toString());
		
		mGameState.setPuzzleID(new Random().nextInt(GameSettings.MAX_PUZZLE_ID));
		mPuzzleIdBox = (EditText) findViewById(R.id.puzzle_id_box);
		mPuzzleIdBox.setHint(new Long(mGameState.getPuzzleID()).toString());
		
		mCancelButton = (Button) findViewById(R.id.cancel);
		mCancelButton.setOnClickListener(mCancelListener);
		mOkButton = (Button) findViewById(R.id.ok);
		mOkButton.setOnClickListener(mOkListener);
		
		this.show();
	}
	
}
