package ovh.jujulacuillere.withingstostrava.model;

public class WithingsActivityRecord {

	private final long timestamp;
	private final Integer hr;
	private final Double lat;
	private final Double lng;

	public WithingsActivityRecord(final long timestampValue, final int hrValue) {
		this.timestamp = timestampValue;
		this.hr = hrValue;
		this.lat = null;
		this.lng = null;
	}

	public WithingsActivityRecord(final long timestampValue, final double latValue, final double lngValue) {
		this.timestamp = timestampValue;
		this.hr = null;
		this.lat = latValue;
		this.lng = lngValue;
	}

	public WithingsActivityRecord(final long timestampValue, final int hrValue, final double latValue,
			final double lngValue) {
		this.timestamp = timestampValue;
		this.hr = hrValue;
		this.lat = latValue;
		this.lng = lngValue;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public int getHr() {
		return this.hr;
	}

	public double getLat() {
		return this.lat;
	}

	public double getLng() {
		return this.lng;
	}
}
