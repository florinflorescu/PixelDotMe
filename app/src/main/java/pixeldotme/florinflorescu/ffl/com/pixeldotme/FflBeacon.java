package pixeldotme.florinflorescu.ffl.com.pixeldotme;

import android.util.Log;

/**
 * Created by florin.florescu on 4/23/2017.
 */

public class FflBeacon {
    int beacon_type;
    short beacon_uuid;
    int beacon_major;
    int beacon_minor;
    int beacon_flags;
    int beacon_battery = 0;
    int beacon_uptime;
    float beacon_temperature = 0;

     FflBeacon(byte[] scan_result_frame)
    {
        int index_1 = 0;
        int index_2 = 0;
        int ad_frame_len = 0;
        boolean processing_done = false;

        while(!processing_done) {
            ad_frame_len = scan_result_frame[index_1];
            index_1++;

            switch (scan_result_frame[index_1]) {
                case 0x01: /*flags*/
                    beacon_flags = scan_result_frame[index_1 + 1];
                    break;
                case 0x03: /*Complete List of 16-bit Service Class UUID;
                            Bluetooth Core Specification:Vol. 3, Part C,
                            section 8.1.1 (v2.1 + EDR, 3.0 + HS and 4.0)Vol. 3,
                            Part C, sections 11.1.1 and 18.2 (v4.0)Core Specification Supplement,
                            Part A, section 1.1*/
                    beacon_uuid = (short) ((short )(scan_result_frame[index_1+2]<<8)|scan_result_frame[index_1+1]);
                    /* TODO: check if the previous UUID has the same value. If not something is very wrong here */
                    break;
                case 0x11: /* Security Manager Out of Band Flags
                            Bluetooth Core Specification:Vol. 3, Part C,
                            sections 11.1.6 and 18.7
                            (v4.0)Core Specification Supplement, Part A, section 1.7*/
                break;
                case 0x16: /*Service Data - 16-bit UUID
                            Core Specification Supplement, Part A, section 1.11
                           */

                    beacon_uuid = (short)((scan_result_frame[index_1+2] << 8) | scan_result_frame[index_1+1]&0xFF);
                    if (beacon_uuid == (short)0xFEAA) {


                        //determine type of datagram TLM,EID,URL
                        byte ad_type = scan_result_frame[index_1 + 3];
                        index_2 = index_1 + 3;
                        switch (ad_type) {
                            case 0x20: /*eddystone Unencrypted TLM Frame Specification*/
                                beacon_battery = scan_result_frame[index_2 + 2]<<8|scan_result_frame[index_2 + 3];
                                Log.d("beacon battery ",""+beacon_battery);
                                beacon_temperature = (float) (scan_result_frame[index_2 + 4] + (scan_result_frame[index_2 + 5]/100.00 ));
                                Log.d("beacon temperature ",""+beacon_temperature);
                                break;
                            default:
                                Log.d("FflBeacon", "unknown AD type in 0x16 datagram");
                        }
                    }
                break;


                default:
                    Log.d("FflBeacon", "unknown AD type");
            }

            index_1+=ad_frame_len;
            if (index_1 > scan_result_frame.length)
                processing_done = true;
            else if (scan_result_frame[index_1] == 0x0)
                processing_done = true;
        }

    }
}
