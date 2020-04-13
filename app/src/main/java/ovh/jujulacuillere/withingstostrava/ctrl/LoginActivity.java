package ovh.jujulacuillere.withingstostrava.ctrl;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ovh.jujulacuillere.withingstostrava.R;
import ovh.jujulacuillere.withingstostrava.ctrl.withingsactivities.WithingsActivitiesActivity;
import ovh.jujulacuillere.withingstostrava.model.WithingsUser;
import ovh.jujulacuillere.withingstostrava.withingsapi.WithingsWebservice;

public class LoginActivity extends AppCompatActivity {
    private EditText loginText;
    private EditText pwText;
    private Button logInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_login);
        this.loginText = findViewById(R.id.activity_login_login_text);
        this.pwText = findViewById(R.id.activity_login_pw_text);
        this.logInBtn = findViewById(R.id.activity_login_logIn_btn);
        this.logInBtn.setEnabled(false);
        this.logInBtn.setBackgroundColor(getColor(R.color.colorPrimaryDisabled));

        this.loginText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean enabled =s.toString().length() != 0 && pwText.getText().length() != 0;
                logInBtn.setEnabled(enabled);
                if (enabled)
                    logInBtn.setBackgroundColor(getColor(R.color.colorPrimary));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        this.pwText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean enabled =s.toString().length() != 0 && loginText.getText().length() != 0;
                logInBtn.setEnabled(enabled);
                if (enabled)
                    logInBtn.setBackgroundColor(getColor(R.color.colorPrimary));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        this.logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.logInBtn.setEnabled(false);
                LoginActivity.this.logInBtn.setBackgroundColor(getColor(R.color.colorPrimaryDisabled));

                WithingsUser user = new WithingsUser(loginText.getText().toString(), pwText.getText().toString());
                new AsyncTask<String, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(String... voids) {
                        try {
                            JSONObject res =  WithingsWebservice.getINSTANCE().connect(user.getLogin(), user.getPassword());
                            if(res == null) {
                                return false;
                            } else {
                                user.setId(res.getInt("id"));
                                user.setFirstName(res.getString("firstname"));
                                user.setLastName(res.getString("lastname"));
                                String path = res.getJSONObject("p4").getString("32x32");
                                URL url = new URL(String.format("%s%s", "https://p4.withings.com/", path));
                                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                user.setPhoto(bmp);
                                return true;
                            }
                        } catch (UnsupportedEncodingException | JSONException e) {
                            e.printStackTrace();
                            LoginActivity.this.displayErrorMsg(v, e.getMessage());
                            return false;
                        } catch (IOException e) {
                            e.printStackTrace(); // tanpik
                            return true;
                        }
                    }
                    @Override
                    protected void onPostExecute(final Boolean success) {
                        if (success) {
                            Intent intent = new Intent(LoginActivity.this, WithingsActivitiesActivity.class);
                            intent.putExtra("user", user);
                            startActivity(intent);
                        } else {
                          LoginActivity.this.displayErrorMsg(v, "Login/password incorrect");
                        }
                    }

                }.execute();
            }
        });
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
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayErrorMsg(View v, String msg) {
        new AlertDialog.Builder(v.getContext()).setTitle("Connection error").setMessage(msg).show();
    }
}
