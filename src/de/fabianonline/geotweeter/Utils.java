package de.fabianonline.geotweeter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
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
}
