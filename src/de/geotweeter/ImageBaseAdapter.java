/**
 * 
 */
package de.geotweeter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

/**
 * @author Julian Kuerby
 *
 */
public class ImageBaseAdapter extends BaseAdapter {
	
	private static final String LOG = "ImageBaseAdapter";
	private List<String> items;
	private Context context;
	private Map<String, Drawable> bitmaps;
	
	private List<Boolean> markedForDelete;
	
	public ImageBaseAdapter(Context context) {
		this.context = context;
		items = new LinkedList<String>();
		markedForDelete = new LinkedList<Boolean>();
		bitmaps = new HashMap<String, Drawable>();
	}
	
	public void add(String path) {
		items.add(path);
		markedForDelete.add(false);
		notifyDataSetChanged();
		new ImageResizeTask().execute(path);
	}
	
	@Override
	public int getCount() {
		return items.size();
	}
	
	@Override
	public String getItem(int position) {
		if(0 <= position && position < getCount()) {
			return items.get(position);
		}
		return null;
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void markForDelete(int position) {
		markedForDelete.set(position, true);
	}
	
	public void unmarkForDelete(int position) {
		markedForDelete.set(position, false);
	}
	
	public void unmarkAll() {
		for(int i = 0; i < markedForDelete.size(); i++) {
			markedForDelete.set(i, false);
		}
	}
	
	public void deleteMarked() {
		for(int i = markedForDelete.size() - 1; i >= 0 ; i--) {
			if(markedForDelete.get(i)) {
				items.remove(i);
				markedForDelete.remove(i);
			}
		}
		notifyDataSetChanged();
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
		
		String path = items.get(position);
		imageView.setImageDrawable(bitmaps.get(path));
		convertView.findViewById(R.id.cross).setVisibility(markedForDelete.get(position)? View.VISIBLE: View.INVISIBLE);
		return convertView;
	}
	
	public List<String> getItems() {
		return items;
	}
	
	public void clear() {
		items.clear();
		markedForDelete.clear();
		notifyDataSetChanged();
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
