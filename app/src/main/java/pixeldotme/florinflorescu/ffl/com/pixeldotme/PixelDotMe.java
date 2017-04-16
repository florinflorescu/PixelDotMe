package pixeldotme.florinflorescu.ffl.com.pixeldotme;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
//test


public class PixelDotMe extends AppCompatActivity {
    final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0x12;
    final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0x13;
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0x14;
    TextView textStatus, textIntValue, textStrValue;
    private BroadcastReceiver mReceiver;
    boolean bHaveAllPermissions = true;
    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());






    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;



    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PixelDotMeService.MSG_SET_INT_VALUE:
                    textIntValue.setText("Int Message: " + msg.arg1);
                    break;
                case PixelDotMeService.MSG_SET_STRING_VALUE:
                    String str1 = msg.getData().getString("str1");
                    textStrValue.setText("Str Message: " + str1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }



    @Override
    protected void onDestroy() {
        Log.d("Activity", "onDestroy: ");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pixel_dot_me);


        //ask for permissions to track the device


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            bHaveAllPermissions = false;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            bHaveAllPermissions = false;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }


        if (bHaveAllPermissions) {
            Intent intent = new Intent(this, PixelDotMeService.class);

            startService(intent);
        } else {
            Toast.makeText(this,"Please allow the app to use your location and restart", Toast.LENGTH_LONG).show();
        }



        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("PixelDotMe Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);



        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.i("MENU","Menu option "+item.getItemId());
        if (item.getItemId() == R.id.id_main_menu_settings)
        {
            Log.i("MENU - ","item is settings");

            if (PixelDotMeService.isRunning()) {
                doBindService();
            }
        }

        return true;
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            //textStatus.setText("Attached.");
            try {
                Message msg = Message.obtain(null, PixelDotMeService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            //textStatus.setText("Disconnected.");
        }
    };

    void doBindService() {
        bindService(new Intent(this, PixelDotMeService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        //textStatus.setText("Binding.");
    }
    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, PixelDotMeService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            //textStatus.setText("Unbinding.");
        }
    }


    private int PostUuidLocationImei(String sUuid, String sLat, String sLong, String sImei, String url)
    {
        Map<String,Object> params = new LinkedHashMap();
        TelephonyManager telephonyManager = null ;

        telephonyManager = (TelephonyManager)getApplication().getSystemService(Context.TELEPHONY_SERVICE);
        //API 18
        //params.put("register_imei", telephonyManager.getDeviceId(1));
        params.put("imei", telephonyManager.getDeviceId());
        params.put("uuid", sUuid);
        params.put("Lat", sLat);
        params.put("Long",sLong);

        try {
            makeHttpPost(params,url);
        } catch (IOException e) {
            e.printStackTrace();
        }

    return 0;
    }

    private int makeHttpPost(Map<String,Object> params, String my_url) throws IOException {
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
        Log.i("HTTP:",cstr);


        Object obj = null;
        JSONParser parser = new JSONParser();
        try {
            try {
                obj = parser.parse(cstr);
            } catch (org.json.simple.parser.ParseException e) {
                e.printStackTrace();
            }

            JSONArray jsonArray = (JSONArray) obj;


            for (int i = 0; i < jsonArray.size(); i++) {

                JSONObject jsonObjectRow = (JSONObject) jsonArray.get(i);

                String nume_atribut = (String)jsonObjectRow.get("att_name");
                String val_atribut = (String) jsonObjectRow.get("att_value");
                Log.i("JSON:",nume_atribut+":"+val_atribut);
  /*
                if (nume_atribut.equals("AlarmPhone")) {
                    cfgRet.alarm_phone = val_atribut;
                }
                if (nume_atribut.equals("AlarmPhone_1")) {
                    cfgRet.alarm_phone_1 = val_atribut;
                }
                if (nume_atribut.equals("AlarmPhone_2")) {
                    cfgRet.alarm_phone_2 = val_atribut;
                }
                if (nume_atribut.equals("AlarmPhone_3")) {
                    cfgRet.alarm_phone_3 = val_atribut;
                }
                if (nume_atribut.equals("AlarmPhone_4")) {
                    cfgRet.alarm_phone_4 = val_atribut;
                }
                if (nume_atribut.equals("zone_1")) {
                    cfgRet.zone_1 = val_atribut;
                }
                if (nume_atribut.equals("zone_2")) {
                    cfgRet.zone_2 = val_atribut;
                }
                if (nume_atribut.equals("zone_3")) {
                    cfgRet.zone_3 = val_atribut;
                }
                if (nume_atribut.equals("zone_4")) {
                    cfgRet.zone_4 = val_atribut;
                }

*/

            }











        } catch (ParseException e) {
            e.printStackTrace();
        }



        return conn.getResponseCode();
    }
}
