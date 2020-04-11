package ovh.jujulacuillere.withingstostrava.ctrl.withingsactivities;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ovh.jujulacuillere.withingstostrava.model.WithingsActivity;
import ovh.jujulacuillere.withingstostrava.withingsapi.NoHRDataException;
import ovh.jujulacuillere.withingstostrava.withingsapi.WithingsWebservice;

public class WithingsActivitiesGetterTask extends AsyncTask<String, Void, List<WithingsActivity>>{

    private Exception exception;
    private final WeakReference<WithingsActivitiesActivity> wActivity;

    WithingsActivitiesGetterTask(WithingsActivitiesActivity activityValue) {
        this.wActivity = new WeakReference<WithingsActivitiesActivity>(activityValue);
        this.exception = null;
    }

    @Override
    protected List<WithingsActivity> doInBackground(String... voids) {
        try {
            return this.getAllActivities();
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
            this.exception = e;
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.wActivity.get().getProgressBar().setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(final List<WithingsActivity> result) {
        this.wActivity.get().fillListView(result, this.exception);
        this.wActivity.get().getProgressBar().setVisibility(View.GONE);
    }

    private List<WithingsActivity> getAllActivities()
            throws JSONException, UnsupportedEncodingException {
        final List<WithingsActivity> activities = new ArrayList<WithingsActivity>();
        final JSONArray actArray = WithingsWebservice.getINSTANCE().getActivities();

        for (int i = 0; i < actArray.length(); i++) {
            final JSONObject data = actArray.getJSONObject(i);
            if (data.getInt("subcategory") != 37 /* Sleeping */
                    && data.getJSONObject("data").length() != 0) {
                int hrAvg = 0;
                int hrMax = 0;
                try {
                    hrAvg = data.getJSONObject("data").getInt("hr_average");
                    hrMax = data.getJSONObject("data").getInt("hr_max");
                } catch(JSONException e) {
                    // po grave tanpik
                }
                final WithingsActivity a = new WithingsActivity(
                        data.getLong("id"),
                        data.getLong("startdate") * 1000,
                        data.getLong("enddate") * 1000,
                        this.getSport(data.getInt("subcategory")),
                        hrAvg,
                        hrMax
                );
                try {
                    this.fillActivityRecords(a);
                    activities.add(a);
                } catch (final NoHRDataException e) {
                    // NA
                }
            }
        }
        return activities.stream().sorted(Comparator.comparingLong(WithingsActivity::getStartTime).reversed())
                .collect(Collectors.toList());
    }

    private void fillActivityRecords(final WithingsActivity a)
            throws UnsupportedEncodingException, NoHRDataException, JSONException {
        Map<Long, Integer> hrData;
        hrData = WithingsWebservice.getINSTANCE().getHeartRateData(a.getStartTime(), a.getEndTime());
        hrData.forEach((date, hr) -> {
            a.addRecord(date, hr);
        });

    }

    private WithingsActivity.WithingsSport getSport(final int subCat) {
        WithingsActivity.WithingsSport sport;
        switch (subCat) {
            case 1:
                sport = WithingsActivity.WithingsSport.Walk;
                break;
            case 2:
                sport = WithingsActivity.WithingsSport.Running;
                break;
            case 3:
                sport = WithingsActivity.WithingsSport.Hiking;
                break;
            case 6:
                sport = WithingsActivity.WithingsSport.Cycling;
                break;
            case 7:
                sport = WithingsActivity.WithingsSport.Swimming;
                break;
            case 12:
                sport = WithingsActivity.WithingsSport.Tennis;
                break;
            case 15:
                sport = WithingsActivity.WithingsSport.Badminton;
                break;
            case 22:
                sport = WithingsActivity.WithingsSport.Football;
                break;
            case 16: // musculation
            case 272:// training
            default:
                sport = WithingsActivity.WithingsSport.Training;
                break;
        }
        return sport;
    }
}
