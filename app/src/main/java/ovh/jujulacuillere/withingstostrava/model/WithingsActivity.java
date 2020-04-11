package ovh.jujulacuillere.withingstostrava.model;

import java.util.ArrayList;
import java.util.List;

public class WithingsActivity {

	private final long id;
	private final long startTime;
	private final long endTime;
	private final WithingsSport sport;
	private final int hrAvg;
	private final int hrMax;
	private final List<WithingsActivityRecord> records;

	public WithingsActivity(final long idValue, final long startTimeValue, final long endTimeValue,
			final WithingsSport sportValue, final int hrAvgValue, final int hrMaxValue) {
		this.id = idValue;
		this.startTime = startTimeValue;
		this.endTime = endTimeValue;
		this.sport = sportValue;
		this.hrAvg = hrAvgValue;
		this.hrMax = hrMaxValue;
		this.records = new ArrayList<WithingsActivityRecord>();
	}

	public long getId() {
		return this.id;
	}

	public long getStartTime() {
		return this.startTime;
	}

	public long getEndTime() {
		return this.endTime;
	}

	public WithingsSport getSport() {
		return this.sport;
	}

	public int getHrAvg() {
		return hrAvg;
	}

	public int getHrMax() {
		return hrMax;
	}

	public List<WithingsActivityRecord> getRecords() {
		return this.records;
	}

	public void addRecord(final long timestampValue, final int hrValue, final double latValue, final double lngValue) {
		this.records.add(new WithingsActivityRecord(timestampValue, hrValue, latValue, lngValue));
	}

	public void addRecord(final long timestampValue, final int hrValue) {
		this.records.add(new WithingsActivityRecord(timestampValue, hrValue));
	}

	public void addRecord(final long timestampValue, final double latValue, final double lngValue) {
		this.records.add(new WithingsActivityRecord(timestampValue, latValue, lngValue));
	}

	public enum WithingsSport {
		Running, Cycling, Walk, Hiking, Badminton, Training, Swimming, Tennis, Football
	}
}
