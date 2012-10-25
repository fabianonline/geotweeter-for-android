/**
 * 
 */
package de.geotweeter;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
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
	
	private List<Boolean> markedForDelete;
	
	public ImageBaseAdapter(Context context, List<String> objects) {
		this.context = context;
		items = objects;
		markedForDelete = new LinkedList<Boolean>();
		for (int i = 0; i < objects.size(); i++) {
			markedForDelete.add(false);
		}
	}
	
	public void add(String path) {
		items.add(path);
		markedForDelete.add(false);
		notifyDataSetChanged();
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
//			imageView = new ImageView(parent.getContext());
//			imageView.setLayoutParams(new GridView.LayoutParams(105, 105));
//			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//			imageView.setPadding(8, 8, 8, 8);
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.image_list_element, null);
			int width = parent.getWidth() / 3;
			Log.d(LOG, "width: " + width);
			convertView.setLayoutParams(new GridView.LayoutParams(140, 140));
//		} else {
//			imageView = (ImageView) convertView;
		}
		imageView = (ImageView) convertView.findViewById(R.id.img_view_image_list_element);
		
		String path = items.get(position);
//		if(path != null && path != "") {
//		Drawable[] layers = { new BitmapDrawable(context.getResources(), Utils.resizeBitmap(path, 300)),
//		                      context.getResources().getDrawable(R.drawable.cross) };
//		LayerDrawable draw = new LayerDrawable(layers);
		imageView.setImageDrawable(new BitmapDrawable(context.getResources(), Utils.resizeBitmap(path, 300)));
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

}
