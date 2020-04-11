package ovh.jujulacuillere.withingstostrava.ctrl.withingsactivities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import java.util.List;
import ovh.jujulacuillere.withingstostrava.R;
import ovh.jujulacuillere.withingstostrava.ctrl.SettingsActivity;
import ovh.jujulacuillere.withingstostrava.fitexporter.FitExporter;
import ovh.jujulacuillere.withingstostrava.model.WithingsActivity;
import ovh.jujulacuillere.withingstostrava.model.WithingsUser;

public class WithingsActivitiesActivity extends AppCompatActivity {

    private static final String FIT_EXT = ".fit";
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withings_activities);
        progressBar = this.findViewById(R.id.progressBar);
        WithingsActivitiesGetterTask task = new WithingsActivitiesGetterTask(this);
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_withings_activities, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(WithingsActivitiesActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void fillListView(List<WithingsActivity> result, Exception exception) {
        if (exception != null) {
            new AlertDialog.Builder(this.getBaseContext()).setTitle("Error").setMessage("Error during Withings activities recuperation").show();
        } else {
            Intent intent = getIntent();
            if (intent != null) {
                WithingsUser user = intent.getParcelableExtra("user");
                if (user != null) {
                    final RecyclerView rv = findViewById(R.id.withings_activities_list);
                    rv.setLayoutManager(new LinearLayoutManager(this));
                    rv.setAdapter(new WithingsActivitiesListAdapter(this, user, result));
                }
            }
        }
    }

    public ProgressBar getProgressBar() {
       return this.progressBar;
    }

    public View.OnClickListener getExportListener(WithingsActivity a) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FitExporter exporter = new FitExporter();
                String fileName = Long.toString(a.getId()) + FIT_EXT;
                exporter.exportFit(a, new java.io.File(getApplicationInfo().dataDir, fileName));
            }
        };
    }
}
