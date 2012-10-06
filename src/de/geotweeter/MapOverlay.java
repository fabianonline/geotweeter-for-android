package de.geotweeter;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MapOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
	
	public MapOverlay(Drawable drawable) {
		super(boundCenterBottom(drawable));
	}

	@SuppressWarnings("deprecation")
	public static Drawable getLocationMarker(Bitmap bitmap) {
		PathShape locationFrameShape = new PathShape(Constants.LOCATION_MARKER, 50, 55);
		ShapeDrawable locationShape = new ShapeDrawable(locationFrameShape);
		Drawable[] layers = {locationShape, new BitmapDrawable(bitmap)};
		LayerDrawable locationMarker = new LayerDrawable(layers);
		locationMarker.setLayerInset(1, 1, 1, 1, 6);
		
		return locationMarker;
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return overlays.get(i);
	}

	@Override
	public int size() {
		return overlays.size();
	}

	public void addOverlay(OverlayItem overlay) {
	    overlays.add(overlay);
	    populate();
	}
	
}
