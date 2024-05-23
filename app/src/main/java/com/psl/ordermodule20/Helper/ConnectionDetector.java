package com.psl.ordermodule20.Helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Admin on 02/Nov/2017.
 */

public class ConnectionDetector {

    private static Context _context;

    public ConnectionDetector(Context context) {
        this._context = context;
    }

    public static boolean isConnectingToInternet() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        boolean haveConnectedEthernet = false;

        ConnectivityManager cm = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
            if (ni.getTypeName().equalsIgnoreCase("ETHERNET"))
                if (ni.isConnected())
                    haveConnectedEthernet = true;
        }
        return haveConnectedWifi || haveConnectedMobile || haveConnectedEthernet;
    }

}
