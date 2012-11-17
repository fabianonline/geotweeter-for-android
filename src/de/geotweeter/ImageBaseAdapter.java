/**
 * 
 */
package de.geotweeter;

import java.util.HashMap;
import java.util.Map;

import de.geotweeter.apiconn.TwitpicApiAccess;

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
	
	@Override
	public int getCount() {
		return size;
	}
	
	@Override
	public String getItem(int position) {
		if (0 <= position && position < getCount()) {
			return items[getIndex(position)];
		}
		return null;
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void markForDelete(int position) {
		markedForDelete[getIndex(position)] = true;
	}
	
	public void unmarkForDelete(int position) {
		markedForDelete[getIndex(position)] = false;
	}
	
	public void unmarkAll() {
		for(int i = 0; i < markedForDelete.length; i++) {
			markedForDelete[i] = false;
		}
	}
	
	public void deleteMarked() {
		for (int i = 0; i < markedForDelete.length; i++) {
			if (markedForDelete[i]) {
				items[i] = null;
				markedForDelete[i] = false;
				size--;
			}
		}
//		notifyDataSetChanged();
	}
	
	public void delete(int position) {
		try {
			int index = getIndex(position);
			items[index] = null;
			markedForDelete[index] = false;
			size--;
		} catch(IllegalArgumentException e) {
		}
	}
	
	public String deletePlaceholder(String text) {
		for (int i = 0; i < markedForDelete.length; i++) {
			if (markedForDelete[i]) {
				String placeHolder = TwitpicApiAccess.getPlaceholder(i);
				text = text.replace(placeHolder, "");
			}
		}
		return text;
	}
	
	@Override
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
	
	public String[] getItems() {
		return items;
	}
	
	public void clear() {
		for (int i = 0; i < SIZE; i++) {
			items[i] = null;
		}
//		notifyDataSetChanged();
	}
	
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
