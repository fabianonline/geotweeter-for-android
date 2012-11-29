package de.geotweeter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.acra.ACRA;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import de.geotweeter.activities.TimelineActivity;
import de.geotweeter.exceptions.UnknownJSONObjectException;
import de.geotweeter.timelineelements.DirectMessage;
import de.geotweeter.timelineelements.FavoriteEvent;
import de.geotweeter.timelineelements.FollowEvent;
import de.geotweeter.timelineelements.ListMemberAddedEvent;
import de.geotweeter.timelineelements.ListMemberRemovedEvent;
import de.geotweeter.timelineelements.NotShownEvent;
import de.geotweeter.timelineelements.TimelineElement;
import de.geotweeter.timelineelements.Tweet;
import de.geotweeter.timelineelements.Url;

public class Utils {
	private static int mainSpinnerDisplays = 0;
	private static final String LOG = "Utils";
	private static Properties properties;
	
	public enum PictureService {
		NONE, TWITPIC, YFROG, YOUTUBE, IMGUR, INSTAGRAM, LOCKERZ, PLIXI, IMGLY, MOBYTO, VIMEO
	}
	
	public static int countChars(String str) {
		str = str.trim();
		int length = str.length();
		Pattern p = Pattern.compile("((https?)://[^\n\r ]+)");
		Matcher m = p.matcher(str);
		while(m.find()) {
			/* Original-Link-Länge abziehen und die gekürzten-20-Zeichen hinzuaddieren. */
			length = length - m.group(1).length() + 20;
			/* War es ein https-Link, packen wir noch ein Zeichen für den gekürzten https-Link dazu. */
			if (m.group(2).equalsIgnoreCase("https")) { 
				length++;
			}
		}
		
		return length;
	}
	
	public static TimelineElement jsonToNativeObject(String json) throws JSONException, UnknownJSONObjectException {
		JSONObject obj;
		
		try {
			obj = JSON.parseObject(json, Feature.DisableCircularReferenceDetect);
		} catch (JSONException ex) {
			ACRA.getErrorReporter().putCustomData("json", json);
			throw ex;
		} catch (RuntimeException ex) {
			ACRA.getErrorReporter().putCustomData("json", json);
			throw ex;
		}
		
		if (obj.containsKey("text") && obj.containsKey("recipient")) {
			return JSON.parseObject(json, DirectMessage.class);
		}
		if (obj.containsKey("direct_message")) {
			return JSON.parseObject(obj.getJSONObject("direct_message").toJSONString(), DirectMessage.class);
		}
		if (obj.containsKey("text")) {
			return JSON.parseObject(json, Tweet.class);
		}
		if (obj.containsKey("event")) {
			String event_type = obj.getString("event");
			if (event_type.equals("follow")) {
				return JSON.parseObject(json, FollowEvent.class);
			}
			if (event_type.equals("favorite")) {
				return JSON.parseObject(json, FavoriteEvent.class);
			}
			if (event_type.equals("list_member_added")) {
				return JSON.parseObject(json, ListMemberAddedEvent.class);
			}
			if (event_type.equals("list_member_removed")) {
				return JSON.parseObject(json, ListMemberRemovedEvent.class);
			}
			if (event_type.equals("block") || event_type.equals("user_update") || event_type.equals("unfavorite")) {
				return JSON.parseObject(json, NotShownEvent.class);
			}
		}
		throw new UnknownJSONObjectException();
	}
	
	public static void showMainSpinner() {
		mainSpinnerDisplays++;
		TimelineActivity ta = TimelineActivity.getInstance();
		if (ta != null) {
			final View spinner = ta.findViewById(R.id.spinnerMain);
			final View refreshButton = ta.findViewById(R.id.btnRefresh);
			ta.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (spinner != null) {
						spinner.setVisibility(View.VISIBLE);
					}
					if (refreshButton != null) {
						refreshButton.setVisibility(View.INVISIBLE);
					}
				}
			});
		}
	}
	
	public static void hideMainSpinner() {
		mainSpinnerDisplays--;
		if (mainSpinnerDisplays <= 0) {
			mainSpinnerDisplays = 0;
			TimelineActivity ta = TimelineActivity.getInstance();
			if (ta != null) {
				final View spinner = ta.findViewById(R.id.spinnerMain);
				final View refreshButton = ta.findViewById(R.id.btnRefresh);
				ta.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (spinner != null) {
							spinner.setVisibility(View.INVISIBLE);
						}
						if (refreshButton != null) {
							refreshButton.setVisibility(View.VISIBLE);
						}
					}
				});
			}
		}
	}
	
	public static void setDesign(Activity a) {
		SharedPreferences prefs = a.getSharedPreferences(Constants.PREFS_APP, 0);
		boolean useDarkTheme = prefs.getBoolean("pref_dark_theme", false);
		if (useDarkTheme) {
			if (Build.VERSION.SDK_INT < 11) {
				a.setTheme(android.R.style.Theme_Black);
			} else {
				a.setTheme(android.R.style.Theme_Holo);
			}
		}
	}
	
	public static Bitmap resizeBitmap(String path, int reqHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		int imageHeight = options.outHeight;
		
		if(imageHeight > reqHeight) {
//			options.inSampleSize = Math.round( (float) imageHeight / (float) reqHeight);
			options.inSampleSize = Integer.highestOneBit(imageHeight / reqHeight);
		}
		
		options.inJustDecodeBounds = false;
		
		return BitmapFactory.decodeFile(path, options);
	}
	
	public static String getProperty(String key) {
		if (properties == null) {
			properties = new Properties();
			try {
				InputStream stream = Geotweeter.getContext().getResources().openRawResource(R.raw.geotweeter);
				properties.load(stream);
				stream.close();
			} catch (Exception caught_exception) {
				RuntimeException exception = new RuntimeException("Could not load file '/raw/geotweeter.properties'.");
				exception.initCause(caught_exception);
				throw exception;
			}
		}
		
		if (!properties.containsKey(key)) {
			throw new RuntimeException("Couldn't find property '" + key + "'");
		}
		return properties.getProperty(key);
	}
	
	public static byte[] reduceImageSize(File file, long imageSize) throws IOException {
		Log.d(LOG, "Before resizeFile: " + file.length());
		int scale = (int) (file.length() / imageSize);
//		scale = 2 * Integer.highestOneBit(scale);
		Log.d(LOG, "scale: " + scale);
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, opt);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		if (file.getName().endsWith(".png")) {
			bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
		} else {
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
		}
		
		byte[] bytes = out.toByteArray();
		Log.d(LOG, "After resizeFile: " + bytes.length);
		return bytes;
	}

	public static PictureService getPictureService(Url url) {
		try {
			URL finalUrl = new URL(url.expanded_url);
			String host = finalUrl.getHost();
			if (host.endsWith("twitpic.com")) {
				return PictureService.TWITPIC;
			}
			if (host.contains("yfrog")) {
				return PictureService.YFROG;
			}
			if (host.endsWith("imgur.com")) {
				return PictureService.IMGUR;
			}
			if (host.endsWith("instagr.am")) {
				return PictureService.INSTAGRAM;
			}
			if (host.endsWith("instagram.com")) {
				return PictureService.INSTAGRAM;
			}
			if (host.endsWith("plixi.com")) {
				return PictureService.PLIXI;
			}
			if (host.endsWith("lockerz.com")) {
				return PictureService.LOCKERZ;
			}
			if (host.endsWith("img.ly")) {
				return PictureService.IMGLY;
			}
			if (host.contains("youtube.")) {
				return PictureService.YOUTUBE;
			}
			if (host.endsWith("youtu.be")) {
				return PictureService.YOUTUBE;
			}
			if (host.endsWith("moby.to")) {
				return PictureService.MOBYTO;
			}
			/* Das wird noch etwas komplizierter hier */
//			if (host.endsWith("vimeo.com")) {
//				return PictureService.VIMEO;
//			}
			/* More services to follow */
			return PictureService.NONE;
		} catch (MalformedURLException e) {
			return PictureService.NONE;
		}
	}

	  /**
	   * Returns the substring before the first occurrence of a delimiter. The
	   * delimiter is not part of the result.
	   *
	   * @param string    String to get a substring from.
	   * @param delimiter String to search for.
	   * @return          Substring before the first occurrence of the delimiter.
	   */
	  public static String substringBefore( String string, String delimiter )
	  {
	    int pos = string.indexOf( delimiter );

	    return pos >= 0 ? string.substring( 0, pos ) : string;
	  }

	  /**
	   * Returns the substring after the first occurrence of a delimiter. The
	   * delimiter is not part of the result.
	   * @param string    String to get a substring from.
	   * @param delimiter String to search for.
	   * @return          Substring after the last occurrence of the delimiter.
	   */
	  public static String substringAfter( String string, String delimiter )
	  {
	    int pos = string.indexOf( delimiter );

	    return pos >= 0 ? string.substring( pos + delimiter.length() ) : "";
	  }
	
	public static String formatString(int string_id, Object... args) {
		return String.format(Geotweeter.getContext().getString(string_id), args);
	}
	
	public static String getString(int string_id) {
		return Geotweeter.getContext().getString(string_id);
	}
	
	/**
	 * Computes the number of pixels based on screen density 
	 * @param dip the densitiy independent pixels to transform to pixels
	 * @return Number of Pixels representing the densitiy independent pixel
	 */
	public static int convertDipToPixel(int dip) {
		return (int) (dip * Geotweeter.getContext().getResources().getDisplayMetrics().density + 0.5f);
	}
}
