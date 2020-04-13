package ovh.jujulacuillere.withingstostrava.ctrl.withingsactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.browser.BrowserWhitelist;
import net.openid.appauth.browser.VersionedBrowserMatcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ovh.jujulacuillere.withingstostrava.R;
import ovh.jujulacuillere.withingstostrava.ctrl.SettingsActivity;
import ovh.jujulacuillere.withingstostrava.ctrl.stravaapi.StravaWebservice;
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
                File file = new java.io.File(getDataDir(), fileName);
                exporter.exportFit(a, file);

                try {
                    AppAuthConfiguration appAuthConfig = new AppAuthConfiguration.Builder()
                            .setBrowserMatcher(new BrowserWhitelist(VersionedBrowserMatcher.CHROME_BROWSER)).build();
                   AuthorizationService authService = new AuthorizationService(WithingsActivitiesActivity.this, appAuthConfig);
                    WithingsActivitiesActivity.this.readAuthState().performActionWithFreshTokens(authService, new AuthState.AuthStateAction() {
                        @Override public void execute(
                                String accessToken,
                                String idToken,
                                AuthorizationException ex) {
                            if (ex != null) {
                                ex.printStackTrace();
                                // negotiation for fresh tokens failed, check ex for more details
                            } else {
                                WithingsActivitiesActivity.this.createActivity(accessToken, file);
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void createActivity(String accessToken, File file) {
        StravaWebservice sws = new StravaWebservice(accessToken);
        try {
            sws.createActivity(file);
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public AuthState readAuthState() throws JSONException {
        SharedPreferences authPrefs = getSharedPreferences("stravaAuth", MODE_PRIVATE);
        String stateJson = authPrefs.getString("stateJson", null);
        AuthState state;
        if (stateJson != null) {
            return AuthState.jsonDeserialize(stateJson);
        } else {
            return new AuthState();
        }
    }
}
