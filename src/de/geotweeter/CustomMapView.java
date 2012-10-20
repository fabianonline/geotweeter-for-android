package de.geotweeter;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.maps.MapView;

public class CustomMapView extends MapView {
	// private Context context;
	public CustomMapView(Context context, String apiKey) {

		super(context, apiKey);
		// this.context = context;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		try {
			return super.dispatchTouchEvent(ev);

		} catch (Exception ex) {

			//     Log.e("CustomMapView", "Caught the exception");
			int zoomLevel = getZoomLevel();
			getController().setZoom(zoomLevel-1);
			super.setVisibility(View.GONE);
			super.setVisibility(View.VISIBLE);
		}

		return true;
	}

	@Override
	public void draw(Canvas canvas) {
		try {
			super.draw(canvas);
		} catch (Exception e) {

			//     Log.e("CustomMapView", "Caught the exception");
			int zoomLevel = getZoomLevel();
			getController().setZoom(zoomLevel-1);
			super.setVisibility(View.GONE);
			super.setVisibility(View.VISIBLE);
		}
	}

	public CustomMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// this.context = context;
	}

	public CustomMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// this.context = context;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		System.gc();
		return super.onTouchEvent(ev);
	}

}