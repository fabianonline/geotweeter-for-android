package de.geotweeter;

import java.io.Serializable;

import android.location.Location;

public class GTALocation extends Location implements Serializable {

	private static final long serialVersionUID = 4731849954654094045L;

	public GTALocation(String provider) {
		super(provider);
	}

	public GTALocation(Location l) {
		super(l);
	}

}
