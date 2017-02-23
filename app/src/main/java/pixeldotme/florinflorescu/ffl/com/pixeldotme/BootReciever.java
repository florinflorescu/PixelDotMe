package pixeldotme.florinflorescu.ffl.com.pixeldotme; /**
 * Created by florin.florescu on 2/23/2017.
 */


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class BootReciever extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Intent myIntent = new Intent(context, PixelDotMe.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle1 = new Bundle();
        bundle1.putInt("Source", 1);
        myIntent.putExtras(bundle1);
        context.startActivity(myIntent);
    }

}