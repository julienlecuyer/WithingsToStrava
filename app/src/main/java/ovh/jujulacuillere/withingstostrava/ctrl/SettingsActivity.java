package ovh.jujulacuillere.withingstostrava.ctrl;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ClientSecretBasic;
import net.openid.appauth.ClientSecretPost;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenResponse;
import net.openid.appauth.browser.BrowserWhitelist;
import net.openid.appauth.browser.VersionedBrowserMatcher;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import ovh.jujulacuillere.withingstostrava.R;

public class SettingsActivity extends AppCompatActivity {

    private int RC_AUTH = 3179;
    private AuthorizationService authService;
    private SettingsFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.fragment = new SettingsFragment(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, this.fragment)
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private final SettingsActivity settingsActivity;

        SettingsFragment(SettingsActivity settingsActivity) {
            this.settingsActivity = settingsActivity;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SwitchPreferenceCompat strava_connect = findPreference("strava_connect");
            if (strava_connect != null) {
                try {
                    if (settingsActivity.readAuthState().getNeedsTokenRefresh()) {
                        System.out.println("NEED REFRESH TOKEN");
                        strava_connect.setChecked(false);
                    } else {
                        strava_connect.setChecked(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                strava_connect.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            SettingsFragment.this.settingsActivity.strataConnect();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                        return true;
                    }
                });
            }
        }
    }

    private void strataConnect() throws IOException {
        AuthorizationServiceConfiguration serviceConfig =
                new AuthorizationServiceConfiguration(
                        Uri.parse("https://www.strava.com/oauth/mobile/authorize"), // authorization endpoint
                        Uri.parse("https://www.strava.com/oauth/token")); // token endpoint
        AuthState authState = new AuthState(serviceConfig);

        AuthorizationRequest.Builder authRequestBuilder =
                new AuthorizationRequest.Builder(
                        serviceConfig, // the authorization service configuration
                        getProperty("strava_client_id"), // the client ID, typically pre-registered and static
                        ResponseTypeValues.CODE, // the response_type value: we want a code
                        Uri.parse("https://www.jujulacuillere.ovh/withingstostrava")); // the redirect URI to which the auth response is sent
        AuthorizationRequest authRequest = authRequestBuilder
                .setScope("profile:read_all,activity:write,read_all")
                .build();

        AppAuthConfiguration appAuthConfig = new AppAuthConfiguration.Builder()
                .setBrowserMatcher(new BrowserWhitelist(VersionedBrowserMatcher.CHROME_BROWSER)).build();

        this.authService = new AuthorizationService(this, appAuthConfig);
        Intent authIntent = this.authService.getAuthorizationRequestIntent(authRequest);
        startActivityForResult(authIntent, this.RC_AUTH);
    }


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_AUTH) {
            AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
            AuthorizationException ex = AuthorizationException.fromIntent(data);
            try {
                AuthState auth =  readAuthState();
                auth.update(resp, ex);
                if (resp != null) {
                    String strava_token = getProperty("strava_token");
                    this.authService.performTokenRequest(
                        resp.createTokenExchangeRequest(), new ClientSecretPost(strava_token),
                        new AuthorizationService.TokenResponseCallback() {
                            @Override
                            public void onTokenRequestCompleted(@Nullable TokenResponse response, @Nullable AuthorizationException ex) {
                                if (response != null) {
                                    auth.update(response, ex);
                                    writeAuthState(auth);
                                    ((SwitchPreferenceCompat) Objects.requireNonNull(SettingsActivity.this.fragment.findPreference("strava_connect"))).setChecked(true);
                                } else {
                                    ((SwitchPreferenceCompat) Objects.requireNonNull(SettingsActivity.this.fragment.findPreference("strava_connect"))).setChecked(false);
                                    assert ex != null;
                                    ex.printStackTrace();
                                    //new AlertDialog.Builder(getApplicationContext()).setTitle("Connection error").setMessage("Strava token recuperation error").show();
                                }
                            }
                        }
                    );
                } else {
                    assert ex != null;
                    ex.printStackTrace();
                    //new AlertDialog.Builder(getApplicationContext()).setTitle("Connection error").setMessage("Strava connection error").show();
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                //new AlertDialog.Builder(getBaseContext()).setTitle("Connection error").setMessage("Strava connection error").show();
            }
        } else {
            System.err.println("BIZARRE");
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

    public void writeAuthState(@NonNull AuthState state) {
        System.out.println("AuthState register...");
        SharedPreferences authPrefs = getSharedPreferences("stravaAuth", MODE_PRIVATE);
        authPrefs.edit()
                .putString("stateJson", state.jsonSerializeString())
                .apply();
        System.out.println("AuthState registered!");
    }

    public String getProperty(String key) throws IOException {
        Properties properties = new Properties();;
        AssetManager assetManager = getApplicationContext().getAssets();
        InputStream inputStream = assetManager.open("strava_token.properties");
        properties.load(inputStream);
        return properties.getProperty(key);
    }
}