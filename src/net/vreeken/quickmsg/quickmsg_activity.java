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
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


public class quickmsg_activity extends Activity {
	Context activity_this = this;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			on_update_ui();
		}
	};
	
	public void on_update_ui()
	{
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


	    /* User entered the app, cancel all notifications (also if user did not get in
	     * via the notification) 
	     */
//	    NotificationManager notification_mgr =
//			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//		notification_mgr.cancelAll();
		Intent bg;
		
		bg = new Intent(this, background.class);
		bg.putExtra("state", 3);
       	this.startService(bg);
	}

	@Override
	protected void onResume() {
	  super.onResume();
	  IntentFilter filter = new IntentFilter();
	  filter.addAction("net.vreeken.quickmsg.update_ui");
	  this.registerReceiver(receiver, filter);
	  background.activity_resume();
	  
	  on_update_ui();

	  LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
			      new IntentFilter("local_message"));
		
	}

	@Override
	protected void onPause() {
	  super.onPause();
	  background.activity_pause();
	  this.unregisterReceiver(this.receiver);
	  LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		  @Override
		  public void onReceive(Context context, Intent intent) {
		    // Get extra data included in the Intent
		    String message = intent.getStringExtra("message");
		    Log.d("receiver", "Got message: " + message);

//		    AlertDialog.Builder alert = new AlertDialog.Builder(activity_this);

//	    	alert.setTitle(R.string.action_message);
//	    	alert.setMessage(message);
//	    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//	    		public void onClick(DialogInterface dialog, int whichButton) {
//	       		}
//	    	});
//	        alert.setCancelable(true);
//	        alert.show();
		    int duration = Toast.LENGTH_SHORT;

		    Toast toast = Toast.makeText(context, message, duration);
		    toast.show();
		  }
	};
}
