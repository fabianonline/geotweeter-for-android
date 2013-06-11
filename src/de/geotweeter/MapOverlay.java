package de.geotweeter;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 * Provides the location marker for geotagged tweets
 * 
 * @author Lutz Krumme (@el_emka)
 * 
 */
public class MapOverlay extends ItemizedOverlay<OverlayItem> {

	private List<OverlayItem> overlays = new ArrayList<OverlayItem>();

	/**
	 * Constructs the object
	 * 
	 * @param drawable
	 *            The location marker to be shown on the map
	 */
	public MapOverlay(Drawable drawable) {
		super(boundCenterBottom(drawable));
	}

	/**
	 * Creates the location marker
	 * 
	 * @param bitmap
	 *            The avatar to be included in the marker
	 * @return The complete marker
	 */
	@SuppressWarnings("deprecation")
	public static Drawable getLocationMarker(Bitmap bitmap) {
		PathShape locationFrameShape = new PathShape(Constants.LOCATION_MARKER,
				50, 55);
		ShapeDrawable locationShape = new ShapeDrawable(locationFrameShape);
		Drawable[] layers = { locationShape, new BitmapDrawable(bitmap) };
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

	/**
	 * Adds overlay item to the map
	 * 
	 * @param overlay
	 */
	public void addOverlay(OverlayItem overlay) {
		overlays.add(overlay);
		populate();
	}

}
