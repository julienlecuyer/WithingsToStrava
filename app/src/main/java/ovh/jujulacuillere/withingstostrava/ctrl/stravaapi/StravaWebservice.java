package ovh.jujulacuillere.withingstostrava.ctrl.stravaapi;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StravaWebservice {

    private final String token;
    private Executor executor;

    public StravaWebservice(String token) {
        this.token = token;
        this.executor = Executors.newSingleThreadExecutor();
    }

    private boolean uploadActivity(String bearer, File file) {
        JSONObject jsonObj = null;

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(
                "https://www.strava.com/api/v3/uploads");
        httpPost.addHeader("Authorization", "Bearer " + bearer);
        httpPost.setHeader("enctype", "multipart/form-data");

        MultipartEntity reqEntity = new MultipartEntity(
                HttpMultipartMode.BROWSER_COMPATIBLE);
        try {
            //reqEntity.addPart("activity_type", new StringBody("ride"));
            reqEntity.addPart("external_id", new StringBody(file.getName()));
            reqEntity.addPart("data_type", new StringBody("fit"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
        FileBody bin = new FileBody(file);
        reqEntity.addPart("file", bin);

        httpPost.setEntity(reqEntity);

        HttpResponse response;
        try {
            response = httpClient.execute(httpPost);

            HttpEntity respEntity = response.getEntity();

            if (respEntity != null) {
                // EntityUtils to get the response content
                String content = EntityUtils.toString(respEntity);
                System.out.println(content);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public void createActivity(File file) throws UnsupportedEncodingException, JSONException {
        executor.execute(() -> {
            StravaWebservice.this.uploadActivity(token, file);
        });
    }
}
