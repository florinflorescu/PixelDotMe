package pixeldotme.florinflorescu.ffl.com.pixeldotme;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by florin.florescu on 2/24/2017.
 */

public class FflHttpRequest {

    void FflHttpRequest(    )
    {

    }


    int makeHttpPost(Map<String,Object> params, String my_url) throws IOException {
        URL url = new URL(my_url);

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        String cstr;
        cstr="";
        for (int c; (c = in.read()) >= 0;) {
            System.out.print((char) c);
            cstr += (char)c;
        }
        Log.d("ClasHTTP:",cstr);
        return conn.getResponseCode();
    }

}
