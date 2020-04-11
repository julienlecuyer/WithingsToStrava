package ovh.jujulacuillere.withingstostrava.withingsapi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WithingsWebservice {

	private static WithingsWebservice INSTANCE = new WithingsWebservice();

	private Integer userId;
	private String sessionId;
	private Integer accountId;

	private WithingsWebservice() {
		// NA
	}

	public JSONObject connect(final String email, final String password) throws UnsupportedEncodingException {
		JSONObject result = null;

		final HashMap<String, String> params = new HashMap<String, String>();
		final String passwordHash = new String(Hex.encodeHex(DigestUtils.md5(password)));

		params.put("email", email);
		params.put("duration", "900");
		params.put("hash", passwordHash);
		params.put("callctx", "foreground");
		params.put("action", "login");
		params.put("appname", "wiscaleNG");
		params.put("apppfm", "ios");
		params.put("appliver", "4050301");


		// store connection data
		try {
			final String urlParameters = this.formatParams(params);
			final JSONObject authData = new JSONObject(this.post("/cgi-bin/auth", urlParameters));
			this.sessionId = authData.getJSONObject("body").getString("sessionid");
			this.accountId = authData.getJSONObject("body").getJSONArray("account").getJSONObject(0).getInt("id");
			result = this.getUserDatas();
			this.userId = result.getInt("id");
		} catch (final JSONException e) {
			System.err.println("Connection error : ");
			e.printStackTrace();
			result = null;
		}
		return result;
	}

	public JSONObject getUserDatas() throws UnsupportedEncodingException, JSONException {
		final HashMap<String, String> params = new HashMap<String, String>();

		params.put("enrich", "t");
		params.put("recurse_devtype", "1");
		params.put("recurse_use", "7");
		params.put("listmask", "7");
		params.put("callctx", "foreground,SyncForTracker,account");
		params.put("action", "getuserslist");
		params.put("accountid", Integer.toString(this.accountId));
		params.put("appname", "wiscaleNG");
		params.put("appfm", "ios");
		params.put("appliver", "4050301");
		params.put("sessionid", this.sessionId);

		final String urlParameters = this.formatParams(params);
		final JSONObject authData = new JSONObject(this.post("/cgi-bin/account", urlParameters));

		return authData.getJSONObject("body").getJSONArray("users").getJSONObject(0);
	}

	public JSONArray getActivities() throws UnsupportedEncodingException, JSONException {

		if (this.userId != null && this.sessionId != null) {
			final HashMap<String, String> params = new HashMap<String, String>();
			final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			params.put("action", "getbyuserid");
			params.put("userid", Integer.toString(this.userId));
			params.put("startdateymd", "2020-01-01");
			params.put("enddateymd", formatter.format(new Date()));
			params.put("callctx", "foreground");
			params.put("appname", "wiscaleNG");
			params.put("apppfm", "ios");
			params.put("appliver", "4050301");
			params.put("sessionid", this.sessionId);

			final String urlParameters = this.formatParams(params);

			final JSONObject json = new JSONObject(this.post("/cgi-bin/v2/activity", urlParameters));
			return json.getJSONObject("body").getJSONArray("series");
		} else {
			throw new IllegalStateException("No connection detected");
		}
	}

	public HashMap<Long, Integer> getHeartRateData(final long startTime, final long endTime)
			throws UnsupportedEncodingException, NoHRDataException, JSONException {

		if (this.userId != null && this.sessionId != null) {
			final HashMap<String, String> params = new HashMap<String, String>();

			params.put("action", "getvasistas");
			params.put("userid", Integer.toString(this.userId));
			params.put("startdate", Long.toString(startTime / 1000));
			params.put("enddate", Long.toString(endTime / 1000));
			params.put("callctx", "foreground");
			params.put("meastype", "11");
			params.put("vasistas_category", "hr");
			params.put("appname", "wiscaleNG");
			params.put("apppfm", "ios");
			params.put("appliver", "4050301");
			params.put("sessionid", this.sessionId);

			final String urlParameters = this.formatParams(params);

			final JSONArray jsonA = new JSONObject(this.post("/cgi-bin/v2/measure", urlParameters))
					.getJSONObject("body").getJSONArray("series");
			if (jsonA.length() > 0) {
                final JSONObject json = jsonA.getJSONObject(0);
                JSONArray datesArray = json.getJSONArray("dates");
                final List<Object> dates = new ArrayList<>();
                for (int i = 0; i <datesArray.length(); i++) {
                    dates.add(datesArray.get(i));
                }
				final JSONArray hrs = json.getJSONArray("vasistas");

				if (dates.size() != hrs.length()) {
					throw new IllegalStateException("WebService Withings Error : Number of dates != number of HR");
				}

				final HashMap<Long, Integer> data = new HashMap<Long, Integer>();
				for (int i = 0; i < dates.size(); i++) {
					data.put(Long.valueOf((int) dates.get(i)) * 1000, ((JSONArray) hrs.get(i)).getInt(0));
				}

				final LinkedHashMap<Long, Integer> sortedMap = new LinkedHashMap<>();

				data.entrySet().stream().sorted(Entry.comparingByKey())
						.forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));

				return sortedMap;
			} else {
				throw new NoHRDataException();
			}
		} else {
			throw new IllegalStateException("No connection detected");
		}
	}

	private String formatParams(final HashMap<String, String> params) throws UnsupportedEncodingException {
		String urlParameters = "";
		for (final Entry<String, String> param : params.entrySet()) {
			urlParameters += param.getKey() + "=" + URLEncoder.encode(param.getValue(), "UTF-8") + "&";
		}
		// delete last '&'
		return urlParameters.substring(0, urlParameters.length() - 1);
	}

	private String post(final String targetURL, final String urlParameters) {
		HttpURLConnection connection = null;
		try {
			// Create connection
			final URL url = new URL("https://scalews.withings.net" + targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoOutput(true);

			// Send request
			final DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.close();

			// Get Response
			final InputStream is = connection.getInputStream();
			final BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			final StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public static WithingsWebservice getINSTANCE() {
		return INSTANCE;
	}
}
