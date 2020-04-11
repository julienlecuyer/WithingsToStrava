package ovh.jujulacuillere.withingstostrava.fitexporter;

import com.garmin.fit.Activity;
import com.garmin.fit.ActivityMesg;
import com.garmin.fit.DateTime;
import com.garmin.fit.File;
import com.garmin.fit.FileEncoder;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.Fit;
import com.garmin.fit.Manufacturer;
import com.garmin.fit.RecordMesg;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.Sport;

import java.util.Date;
import java.util.List;

import ovh.jujulacuillere.withingstostrava.model.WithingsActivity;
import ovh.jujulacuillere.withingstostrava.model.WithingsActivity.WithingsSport;
import ovh.jujulacuillere.withingstostrava.model.WithingsActivityRecord;

public class FitExporter {

	public FitExporter() {

	}

	public void exportFit(final WithingsActivity a, java.io.File file) {
		FileEncoder encode = null;

		try {
			encode = new FileEncoder(file, Fit.ProtocolVersion.V2_0);

			// Generate FileIdMessage
			final FileIdMesg fileIdMesg = new FileIdMesg();
			// message
			fileIdMesg.setManufacturer(Manufacturer.DYNASTREAM);
			fileIdMesg.setType(File.ACTIVITY);
			fileIdMesg.setProduct(9001);
			fileIdMesg.setSerialNumber(1701L);
			fileIdMesg.setTimeCreated(new DateTime(new Date(a.getStartTime())));
			fileIdMesg.setProductName("Withings Steel HR");
			encode.write(fileIdMesg); // Encode the FileIDMesg

			// Generate ActivityMesg
			final ActivityMesg activityMesg = new ActivityMesg();
			activityMesg.setTimestamp(new DateTime(new Date(a.getStartTime())));
			activityMesg.setType(Activity.AUTO_MULTI_SPORT);
			encode.write(activityMesg);

			// Session
			final SessionMesg sessionMesg = new SessionMesg();
			sessionMesg.setTimestamp(new DateTime(new Date(a.getStartTime())));
			sessionMesg.setSport(this.mapSport(a.getSport()));
			encode.write(sessionMesg);

			final RecordMesg record = new RecordMesg();

			final List<WithingsActivityRecord> wRecords = a.getRecords();
			for (final WithingsActivityRecord wRecord : wRecords) {
				record.setHeartRate((short) wRecord.getHr());
				record.setTimestamp(new DateTime(new Date(wRecord.getTimestamp())));
				encode.write(record);
			}
		} finally {
			if (encode != null) {
				encode.close();
			}
		}

		System.out.println("Encoded FIT file " + file);
	}

	private Sport mapSport(final WithingsSport wsport) {
		Sport sport = Sport.TRAINING;
		switch (wsport) {
			case Football:
			sport = Sport.SOCCER;
			break;
			case Hiking:
			sport = Sport.HIKING;
			break;
			case Cycling:
			sport = Sport.CYCLING;
			break;
			case Running:
			sport = Sport.RUNNING;
			break;
			case Tennis:
			sport = Sport.TENNIS;
		default:
			sport = Sport.TRAINING;
		}

		return sport;
	}
}
