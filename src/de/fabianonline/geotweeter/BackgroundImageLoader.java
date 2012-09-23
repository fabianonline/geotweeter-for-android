package de.fabianonline.geotweeter;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

/*
 * Vorlage: https://github.com/thest1/LazyList/blob/master/src/com/fedorvlasov/lazylist/ImageLoader.java
 */
public class BackgroundImageLoader {
	
	private Map<String, Bitmap> bitmap_cache = Collections.synchronizedMap(new WeakHashMap<String, Bitmap>());
	private Map<ImageView, String> image_views = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	static ExecutorService executor_service = null;
	final int loading_image_id = R.drawable.ic_launcher;
	
	public BackgroundImageLoader(Context applicationContext) {
		if (executor_service == null) {
			executor_service = Executors.newFixedThreadPool(5);
		}
	}

	
	public void displayImage(String url, ImageView image_view) {
		image_views.put(image_view, url);
		Bitmap bitmap = bitmap_cache.get(url);
		
		if (bitmap != null) {
			image_view.setImageBitmap(bitmap);
		} else {
			image_view.setImageResource(loading_image_id);
			/* Queue Image to download */
			executor_service.submit(new ImageLoader(url, image_view));
		}
		
	}
	
	public class ImageLoader implements Runnable {
		String url;
		ImageView image_view;
		
		public ImageLoader(String url, ImageView image_view) {
			this.url = url;
			this.image_view = image_view;
		}

		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			if (bitmap_cache.containsKey(url)) {
				((Activity)image_view.getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						image_view.setImageBitmap(bitmap_cache.get(url));
					}
				});
				return;
			}
			Log.d("ImageLoader", "Loading "+url);
			if (imageViewReused(url, image_view)) return;
			try {
				final Bitmap bmp = new BitmapDrawable(BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream())).getBitmap();
				bitmap_cache.put(url, bmp);
				
				if (imageViewReused(url, image_view)) return;
				((Activity)image_view.getContext()).runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						if (bmp!=null) {
							image_view.setImageBitmap(bmp);
						}
					}
					
				});
			} catch (IOException e) { 
				e.printStackTrace(); 
			}
		}

	}

	public boolean imageViewReused(String url, ImageView image_view) {
		String old_url = image_views.get(image_view);
		if (old_url==null || !old_url.equals(url)) return true;
		return false;
	}

}
