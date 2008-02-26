/**
 * 
 */
package com.google.android.games.tubes;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * @author whuang
 *
 */
public class TubesOptionsDialog extends Dialog {
	
	private static class TubesOptionsView extends View {
		TubesOptionsView(Context c) {
			super(c);
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
			p.setColor(0xFFFF0000);
			canvas.drawText("foo", 0, 0, p);
		}
	}

	public TubesOptionsDialog(Context context) {
		super(context);
		setContentView(new TubesOptionsView(context));
		setTitle("Change Options");
		this.show();
	}

}
