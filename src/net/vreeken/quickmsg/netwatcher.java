/*
    QuickMSG
    Copyright (C) 2014  Jeroen Vreeken <jeroen@vreeken.net>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.vreeken.quickmsg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class netwatcher extends BroadcastReceiver {
	
    @Override
    public void onReceive(Context context, Intent intent) {
        //here, check that the network connection is available. If yes, start your service. If not, stop your service.
       ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
       NetworkInfo info = cm.getActiveNetworkInfo();
	   Intent bg;

       if (info != null) {
           if (info.isConnected()) {
               Log.d("netwatcher", "connection, start service");
        	   bg = new Intent(context, background.class);
        	   bg.putExtra("state", 1);
       	       context.startService(bg);
       	       return;
           }
       }

       Log.d("netwatcher", "no connection, stop service");
       bg = new Intent(context, background.class);
       bg.putExtra("state", 0);
       context.stopService(bg);
    }
}