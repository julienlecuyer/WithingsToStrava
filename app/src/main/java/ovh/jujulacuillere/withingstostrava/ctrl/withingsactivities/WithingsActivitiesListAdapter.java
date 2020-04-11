package ovh.jujulacuillere.withingstostrava.ctrl.withingsactivities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

import ovh.jujulacuillere.withingstostrava.R;
import ovh.jujulacuillere.withingstostrava.model.WithingsActivity;
import ovh.jujulacuillere.withingstostrava.model.WithingsUser;

public class WithingsActivitiesListAdapter extends RecyclerView.Adapter<WithingsActivitiesListAdapter.WithingsActivityHolder> {

    private final List<WithingsActivity> activities;
    private final WithingsActivitiesActivity listActivity;
    private final WithingsUser wUser;

    private WithingsActivitiesListAdapter() {
        // interdik
        activities = null;
        listActivity = null;
        wUser = null;
    }

    WithingsActivitiesListAdapter(WithingsActivitiesActivity withingsActivitiesActivity, WithingsUser wUserValue, List<WithingsActivity> activitiesValue) {
        this.listActivity = withingsActivitiesActivity;
        this.activities = activitiesValue;
        this.wUser = wUserValue;
    }


    @Override
    public int getItemCount() {
        return activities.size();
    }

    @Override
    public WithingsActivityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_cell, parent, false);
        return new WithingsActivityHolder(view);
    }

    @Override
    public void onBindViewHolder(WithingsActivityHolder holder, int position) {
        WithingsActivity activity = activities.get(position);
        holder.display(activity);
    }

    class WithingsActivityHolder extends RecyclerView.ViewHolder {

        private final TextView date;
        private final CircularImageView img;
        private final TextView userName;
        private final TextView sport;
        private final TextView duration;
        private final TextView hrAvg;
        private final TextView hrMax;

        final SimpleDateFormat dateFormatter;

        WithingsActivityHolder(final View itemView) {
            super(itemView);

            this.dateFormatter = new SimpleDateFormat("dd-MM-yyyy à HH:mm", Locale.FRANCE);
            this.date = itemView.findViewById(R.id.withings_activity_createTime);
            this.img = itemView.findViewById(R.id.withings_activity_image);
            this.userName = itemView.findViewById(R.id.withings_activity_userName);
            this.sport = itemView.findViewById(R.id.withings_activity_sport);
            this.duration = itemView.findViewById(R.id.withings_activity_duration);
            this.hrAvg = itemView.findViewById(R.id.withings_activity_hrAvg);
            this.hrMax = itemView.findViewById(R.id.withings_activity_hrMax);
        }

        void display(WithingsActivity activity) {
            this.itemView.setOnClickListener(WithingsActivitiesListAdapter.this.listActivity.getExportListener(activity));
            this.date.setText(this.dateFormatter.format(activity.getStartTime()));
            this.img.setImageBitmap(WithingsActivitiesListAdapter.this.wUser.getPhoto());
            this.userName.setText(String.format("%s %s",
                    WithingsActivitiesListAdapter.this.wUser.getFirstName(),
                    WithingsActivitiesListAdapter.this.wUser.getLastName()));
            this.sport.setText(activity.getSport().name());
            final Duration duration = Duration.ofMillis(activity.getEndTime() - activity.getStartTime());
            this.duration.setText(this.formatDuration(duration));
            this.hrAvg.setText(String.format(Locale.FRANCE, "%d", activity.getHrAvg()));
            this.hrMax.setText(String.format(Locale.FRANCE, "%d", activity.getHrMax()));
        }

        private String formatDuration(final Duration duration) {
            final long seconds = duration.getSeconds();
            final long absSeconds = Math.abs(seconds);
            final long h = absSeconds / 3600;
            final long m = (absSeconds % 3600) / 60;
            final String positive;
            if(h != 0L) {
                positive = String.format(Locale.FRANCE, "%dh %02dmin", h, m);
            } else {
                positive = String.format(Locale.FRANCE, "%02dmin %02ds", m,  absSeconds % 60);
            }
            return seconds < 0 ? "-" + positive : positive;
        }
    }
}