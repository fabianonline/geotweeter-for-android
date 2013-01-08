/**
 * 
 */
package de.geotweeter;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import de.geotweeter.apiconn.TwitpicApiAccess;

/**
 * @author Julian Kuerby
 *
 */
public class ImageBaseAdapter extends BaseAdapter {
	
	private static final String LOG = "ImageBaseAdapter";
	private static final int SIZE = 7;
	private String[] items;
	private Context context;
	private Map<String, Drawable> bitmaps;
	private boolean[] markedForDelete;
	private int size;
	
	public ImageBaseAdapter(Context context) {
		this.context = context;
		items = new String[SIZE];
		markedForDelete = new boolean[SIZE];
		bitmaps = new HashMap<String, Drawable>();
		size = 0;
	}
	
	/**
	 * Add an image to the adapter, when it is not full.
	 * @param path The path to the image
	 * @return The index, where the path was added. -1, if adapter was full
	 */
	public int add(String path) {
		if(size == SIZE) {
			return -1;
		}
		int i = 0;
		for (; i < items.length && items[i] != null; i++) {
		}
		items[i] = path;
		size++;
		new ImageResizeTask().execute(path);
		return i;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getCount() {
		return size;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getItem(int position) {
		if (0 <= position && position < getCount()) {
			return items[getIndex(position)];
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/**
	 * Mars an image for deletion
	 * @param position The position of the item within the adapter's data set that we want to mark.
	 */
	public void markForDelete(int position) {
		markedForDelete[getIndex(position)] = true;
	}
	

	
	/**
	 * Unmark an image for deletion
	 * @param position The position of the item within the adapter's data set that we want to unmark.
	 */
	public void unmarkForDelete(int position) {
		markedForDelete[getIndex(position)] = false;
	}
	
	/**
	 * Unmark all images
	 */
	public void unmarkAll() {
		for(int i = 0; i < markedForDelete.length; i++) {
			markedForDelete[i] = false;
		}
	}
	
	/**
	 * Delete all marked images
	 */
	public void deleteMarked() {
		for (int i = 0; i < markedForDelete.length; i++) {
			if (markedForDelete[i]) {
				deleteIndex(i);
			}
		}
//		notifyDataSetChanged();
	}
	
	/**
	 * Delete the image on the specific position
	 * @param position The position of the item we want to delete within the adapter's data set.
	 */
	public void delete(int position) {
		try {
			int index = getIndex(position);
			deleteIndex(index);
		} catch(IllegalArgumentException e) {
		}
	}
	
	/**
	 * Delete the item at the specific index
	 * @param index The index of the item we want to delete
	 */
	public void deleteIndex(int index) {
		items[index] = null;
		markedForDelete[index] = false;
		size--;
	}
	
	/**
	 * Delete the placeholders whose images are marked
	 * 
	 * @param text The text in which the placeholders should be deleted
	 * @return The text without the deleted placeholders
	 */
	public String deleteAllMarkedPlaceholder(String text) {
		for (int i = 0; i < markedForDelete.length; i++) {
			if (markedForDelete[i]) {
				String placeHolder = TwitpicApiAccess.getPlaceholder(i);
				text = text.replace(placeHolder, "");
			}
		}
		return text;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.image_list_element, null);
			int width = parent.getWidth() / 3;
			Log.d(LOG, "width: " + width);
			convertView.setLayoutParams(new GridView.LayoutParams(140, 140));
		}
		imageView = (ImageView) convertView.findViewById(R.id.img_view_image_list_element);
		
		int index = getIndex(position);
		String path = items[index];
		imageView.setImageDrawable(bitmaps.get(path));
		convertView.findViewById(R.id.cross).setVisibility(markedForDelete[index]? View.VISIBLE: View.INVISIBLE);
		return convertView;
	}
	
	/**
	 * Returns the adapter's item list
	 * 
	 * @return The adapter's item list
	 */
	public String[] getItems() {
		return items;
	}
	
	/**
	 * Clear the whole adapter
	 */
	public void clear() {
		for (int i = 0; i < SIZE; i++) {
			items[i] = null;
		}
		size = 0;
//		notifyDataSetChanged();
	}
	
	/**
	 * Compute the index of the item on the specific position
	 * 
	 * @param position The position of the item, whose index we want get
	 * @return The index of the item on the specific position
	 * @throws IllegalArgumentException
	 */
	private int getIndex(int position) throws IllegalArgumentException {
		if (0 <= position && position < getCount()) {
			for (int i = 0; i < items.length; i++) {
				if(items[i] != null) {
					if(position == 0) {
						return i;
					}
					position--;
				}
			}
		}
		throw new IllegalArgumentException("Position: " + position);
	}
	
	private class ImageResizeTask extends AsyncTask<String, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(String... params) {
			String path = params[0];
			if(! bitmaps.containsKey(path)) {
				bitmaps.put(path, context.getResources().getDrawable(R.drawable.loading_image));
				Bitmap bmp = Utils.resizeBitmap(path, 300);
				bitmaps.put(path, new BitmapDrawable(context.getResources(), bmp));
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				notifyDataSetChanged();
			}
		}
		
	}
}
