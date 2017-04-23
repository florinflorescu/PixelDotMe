package pixeldotme.florinflorescu.ffl.com.pixeldotme;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ParseException;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelUuid;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by florin.florescu on 2/24/2017.
 */

public class PixelDotMeService extends Service implements LocationListener {


    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private BroadcastReceiver mReceiver;

    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_INT_VALUE = 3;
    static final int MSG_SET_STRING_VALUE = 4;

    public static final ParcelUuid MY_SERVICE = ParcelUuid.fromString("00006810-864C-02B7-E5CA-9EA81A6EEFE0");

    private static boolean isRunning = false;
    private int counter = 0, incrementby = 1;
    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.

    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.

    private final IBinder mBinder = new LocalBinder();

    private static final String TAG = "BeaconActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Map beacons;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        PixelDotMeService getService() {
            return PixelDotMeService.this;
        }
    }


    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                Thread.sleep(5000);
                Log.i("Service", "AAAAAAAAAA");
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }


    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        isRunning = true;

         beacons = new HashMap();

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        mBluetoothAdapter = manager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();


        ScanFilter beaconFilter = new ScanFilter.Builder()
                .setServiceUuid(MY_SERVICE)
                .build();

        ParcelUuid  myuuid = beaconFilter.getServiceUuid();

        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
        //filters.add(beaconFilter);



        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);





    }

    private ScanCallback mScanCallback = new ScanCallback() {
        public void onScanResult(int callbackType, ScanResult result) {
            // Log.d(TAG, "onScanResult "+result.toString());
            //Toast.makeText(getApplicationContext(),"callback",Toast.LENGTH_SHORT).show();
            int inside_rssi = -70;
            int grey_zone_rssi = 20;

            byte[] bali = result.getScanRecord().getBytes();





            StringBuilder sb = new StringBuilder();
            FflBeacon fflB = new FflBeacon(bali);

            if (fflB.beacon_uuid == (short)0xFEAA)
                Log.d("Temperatura:",""+fflB.beacon_temperature);

            StringBuilder str_mfg = new StringBuilder();
            str_mfg.append(String.format("%02X", bali[6]));
            str_mfg.append(String.format("%02X", bali[5]));

            StringBuilder str_prefix = new StringBuilder();
            str_prefix.append(String.format("%02X", bali[8]));
            str_prefix.append(String.format("%02X", bali[7]));





/*
                for (byte b : bali) {
                    sb.append(String.format("%02X ", b));
                }
*/
            for (int i=0;i<bali.length;i++)
            {
                sb.append(String.format("%02X ", bali[i]));
            }

            Log.d("Filtered UUID RSSI is",result.getDevice().getAddress() + "<<-->> "+result.getRssi()+" <-> "+sb.toString());

            //  if ((bali[5]==0x4c) && (bali[6]==0x0))
            {
                StringBuilder str_tx_power = new StringBuilder();
                str_tx_power.append(String.format("%d", bali[29]));
                double f_m = 0;//calculateAccuracy(bali[29],result.getRssi());


                //Log.i("---","----------------------------------------");

                ParcelUuid s=null;
                String my_uuid = null;
                Map<ParcelUuid, byte[]> sss = result.getScanRecord().getServiceData();

                if ((bali[5]==(byte) 0x9a) && (bali[6]==(byte) 0xfe))
                {

                    StringBuilder sb1 = new StringBuilder();
                    for (int i=12;i<31;i++)
                    {
                        sb1.append(String.format("%02X", bali[i]));
                    }
                    my_uuid = sb1.toString();
                    Log.d("Filtered UUID is", sb.toString());
                    Log.d("Beacon RSSI: "+my_uuid, (String.format("%d", result.getRssi())));
                    Log.d("-------", "------------------------");
                }
                if ((result.getScanRecord() != null) && (my_uuid != null))
                if (result.getScanRecord().getServiceUuids()!=null) {
                    //Log.i("UUIDsiuze: ", String.valueOf(result.getScanRecord().getServiceUuids().size()));
                    s = result.getScanRecord().getServiceUuids().get(0);
                    if(beacons.containsKey(my_uuid))
                    {
                        Log.i("Beacon map:","already in place");


                        if (Integer.parseInt(beacons.get(my_uuid).toString()) == 0)
                        {
                          //beacon is outside
                            if (result.getRssi()> -70) {
                                beacons.put(my_uuid, new Integer(1));
                                PostUuidLocationImei(my_uuid,"0","0","my_imei","myurl",1);
                            }
                        }
                        else
                        {
                            if (result.getRssi()< -90) {
                                beacons.put(my_uuid, new Integer(0));
                                PostUuidLocationImei(my_uuid,"0","0","my_imei","myurl",0);
                            }
                        }
                    }
                    else
                    {
                        Log.i("Beacon map:","adding to the map");
                        if (result.getRssi()> -70)
                            beacons.put(my_uuid,new Integer(1));
                        if (result.getRssi() < -90)
                            beacons.put(my_uuid,new Integer(0));
                        //PostUuidLocationImei(my_uuid,"0","0","my_imei","myurl",0);
                    }

                        //PostUuidLocationImei(s.toString(),"0","0","my_imei","myurl");
                }


                if (result.getDevice() != null && result.getDevice().getName()!=null)
                    Log.i("Beacon Nm :",result.getDevice().getName());

                BluetoothDevice btDevice = result.getDevice();
                btDevice.fetchUuidsWithSdp();
                ParcelUuid[] retval = btDevice.getUuids();





            }
        }


        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResults: "+results.size()+" results");
            for (ScanResult result : results) {
                // processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.w(TAG, "LE Scan Failed: "+errorCode);
        }
    };
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);
        Log.i("Service", "onCreate");
        // If we get killed, after returning from here, restart


        new Thread(new Runnable() {
            @Override
            public void run() {


                //Your logic that service will perform will be placed here
                //In this example we are just looping and waits for 1000 milliseconds in each loop.
                while (true) {
                    try {
                        Thread.sleep(5000);
                        Log.i("Mainloop", "Recording is .");
                        //ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                        //toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                    } catch (Exception e) {
                    }
                }

                //Stop service once it finishes its task
                //stopSelf();
            }
        }).start();

        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

           // return TODO;
        }
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        mlocManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return mMessenger.getBinder();

    }

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_INT_VALUE:
                    incrementby = msg.arg1;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.i("LOCATION","loc lat"+location.getLatitude()+" lon:"+location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public static boolean isRunning()
    {
        return isRunning;
    }


    private int PostUuidLocationImei(String sUuid, String sLat, String sLong, String sImei, String url, int dDirection)
    {
        Map<String,Object> params = new LinkedHashMap();
        TelephonyManager telephonyManager = null ;
        String sDirection = "";
        if (dDirection == 1)
            sDirection = "inside";
        else
            sDirection = "outside";

        telephonyManager = (TelephonyManager)getApplication().getSystemService(Context.TELEPHONY_SERVICE);
        //API 18
        //params.put("register_imei", telephonyManager.getDeviceId(1));
        //params.put("imei", telephonyManager.getDeviceId());
        params.put("uuid", sUuid);
        params.put("Lat", sLat);
        params.put("Long",sLong);

        try {
            makeHttpGet(params,"http://cts-iot-lab.hobby-site.org/site/api.php/beacon/"+sUuid+"/"+telephonyManager.getDeviceId().toString()+"/"+sDirection);
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

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }



        return conn.getResponseCode();
    }


    private int makeHttpGet(Map<String,Object> params, String my_url) throws IOException {
        URL url = new URL(my_url);
        byte[] buffer = new byte[150];
        Arrays.fill( buffer, (byte) 32 );
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            in.read(buffer);
            String doc2 = new String(buffer, "UTF-8");
            Log.d("B Response",doc2);
            String url2 = "http://www.example.com";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setFlags(FLAG_ACTIVITY_NEW_TASK);
            i.setData(Uri.parse(doc2));
            startActivity(i);
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")));
            //readStream(in);
        } finally {
            urlConnection.disconnect();
        }

        return 0;
    }

}
