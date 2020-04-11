package ovh.jujulacuillere.withingstostrava.ctrl.stravaconnection;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ovh.jujulacuillere.withingstostrava.R;
import ovh.jujulacuillere.withingstostrava.model.WithingsUser;

public class StravaConnectActivity extends AppCompatActivity {
    private EditText loginText;
    private EditText pwText;
    private Button logInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_login);
        final TextView titleLabel = findViewById(R.id.activity_login_title_label);
        this.loginText = findViewById(R.id.activity_login_login_text);
        this.pwText = findViewById(R.id.activity_login_pw_text);
        this.logInBtn = findViewById(R.id.activity_login_logIn_btn);
        this.logInBtn.setEnabled(false);
        this.logInBtn.setBackgroundColor(getColor(R.color.colorStravaDisabled));
        titleLabel.setText("Strava connection");
        titleLabel.setTextColor(getColor(R.color.colorStrava));

        this.loginText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean enabled =s.toString().length() != 0 && pwText.getText().length() != 0;
                logInBtn.setEnabled(enabled);
                if (enabled)
                    logInBtn.setBackgroundColor(getColor(R.color.colorStrava));
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
                    logInBtn.setBackgroundColor(getColor(R.color.colorStrava));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        this.logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StravaConnectActivity.this.logInBtn.setEnabled(false);
                StravaConnectActivity.this.logInBtn.setBackgroundColor(getColor(R.color.colorStravaDisabled));

                WithingsUser user = new WithingsUser(loginText.getText().toString(), pwText.getText().toString());
                new AsyncTask<String, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(String... voids) {
                        System.out.println("Strava connection");
                        return false;
                    }
                    @Override
                    protected void onPostExecute(final Boolean success) {

                    }

                }.execute();
            }
        });
    }

    private void displayErrorMsg(View v, String msg) {
        new AlertDialog.Builder(v.getContext()).setTitle("Connection error").setMessage(msg).show();
    }
}
