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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.mail.internet.ContentType;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class background extends Service 
{
	Context context = this;
	Boolean stop_request = false;
	Boolean close_request = false;
	Boolean running = false;

	Boolean do_alarm = false;
	preferences preferences = new preferences(context);
	
	Thread pollthread;
	
	public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate() 
    {
        Log.d("background", "onCreate");
    }

    private NotificationCompat.Builder notification = null;
    private int notification_id = 0x1337;
    
    private void run_foreground(){
        Intent notificationIntent = new Intent(this, ListActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this, 0,
                notificationIntent, 
                Intent.FLAG_ACTIVITY_NEW_TASK);

        notification = new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.drawable.ic_stat_qm)
                                    .setContentText(getString(R.string.app_name))
                                    .setContentIntent(pendingIntent)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                            R.drawable.ic_launcher))
                                    .setOngoing(true);
        
        startForeground(notification_id, notification.build());
    }
    
	private quickmsg_db db = new quickmsg_db(context);

	final mail mail = new mail() {
		public Boolean recv_quickmsg_cb(String from, List<attachment> attachments, String subtype)
		{
			Log.d("recv cb", "Received quickmsg from "+ from + " of subtype: " + subtype);
			
			pgp pgp = new pgp(context);
			
			for (int i = 0; i < attachments.size(); i++) {
				attachment attachment = attachments.get(i);
				Log.d("recv cb", "name: " + attachment.name + 
						", content type :" + attachment.datahandler.getContentType());
				try {
					ContentType ct = new ContentType(attachment.datahandler.getContentType());
					if (subtype.toLowerCase().equals("encrypted") &&
						ct.getSubType().toLowerCase().equals("octet-stream")) {
						Log.d("recv cb", "Got encrypted message");
						attachment ac;
						try {
							ac = pgp.decrypt_verify(attachment);
						} catch (OutOfMemoryError e) {
							Log.e("recv_cb", "out of memory: attachment to big?");
							return false;
						}
						if (ac != null) {
							Log.d("recv cb", "Got decrypted message");
							List<attachment> as = multipart_get_attachments(ac);
							Log.d("recv cb", "Got decrypted message attachments");
							
							attachment a_msg = null;
							message msg = null;
							String a_ext = ".dat";
							
							for (int j = 0; j < as.size(); j++) {
								attachment a = as.get(j);
								ContentType ct2 = new ContentType(a.datahandler.getContentType());
								String subtype2 = ct2.getSubType();
								String basetype = ct2.getBaseType();
								Log.d("recv db", "attachment " + j + " " + basetype);

								if (subtype2.toLowerCase().equals("pgp-keys")) {
									received_key(pgp, db, from, a, true);
								} else if (subtype2.toLowerCase().equals("quickmsg")) {
									msg = received_quickmsg(pgp, db, from, a);
								} else {
									/* maybe part of a message */
									String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(basetype);
									
									Log.d("recv db", "probably an attachment ext: " + ext);
									a_msg = a;
									a_ext = ext;
								}
							}

							if (msg != null && a_msg != null) {
								received_attachment(db, msg, a_msg, a_ext);
							}
							if (msg != null) {
								db.message_add(msg);
							}
								
						} else {
							Log.d("recv cb", "Message could not be decrypted/verified");
						}
					}
					if (ct.getSubType().toLowerCase().equals("pgp-keys")) {
						received_key(pgp, db, from, attachment, false);
					}
				}
				catch (Exception e) {
					Log.e("recv_cb", e.getMessage());
				}
				
			}
			return true;
		}
	};

	private void do_mail_noop()
	{
    	Thread noop_thread = new Thread(new Runnable() {
            public void run() {
            	mail.noop(null);
            }
    	});
    	
    	noop_thread.start();
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) 
    {
        Log.d("background", "onStartCommand()");

        local_message.send_connection(context, mail.imap_connected);
        
        int state = 1;
        if (intent != null) {
        	state = intent.getIntExtra("state", 1);
        }
        
        if (state == 0) {
        	Log.d("background", "stop request");
        	stop_request = true;
        	do_mail_noop();
            
            return START_REDELIVER_INTENT;
        }
        if (state == 2) {
        	Log.d("background", "sync request");
        	close_request = true;
        	do_mail_noop();

        	return START_REDELIVER_INTENT;
        }
        if (state == 3) {
        	update_ui(true);
        	
        	return START_REDELIVER_INTENT;
        }
        if (running) {
        	if (stop_request == true) {
        		stop_request = false;
        		close_request = true;
            	do_mail_noop();
        	}
        	Log.d("background", "already running");
        	return START_REDELIVER_INTENT;
        }
        running = true;
        stop_request = false;
        Log.d("background", "start request");
        
        run_foreground();
        
    	pollthread = new Thread(new Runnable() {
            public void run() {

            	String email_address;

           		email_address = preferences.get("email_address"); 
            		
           		if (email_address == null) {
           			stop_request = false;
           			running = false;
           			return;
           		}
            		
            	do {
            		Log.d("background", "mail loop");
                	do_alarm = false;
            		mail.recv(context);
            		update_ui(false);
                	if (do_alarm)
                		alarm();
                	// flush old stuff
                	Log.d("background", "mail loop going to flush");
                	mail.flush(context);

           			long timeout = 5 * 60 * 1000;
           			Log.d("background", "mail loop going to idle");
           			mail.idle(context, timeout);
           			Log.d("background", "mail loop close: " + close_request + " stop: " + stop_request);
           			if (close_request)
           				mail.imap_close();
           			close_request = false;
            	} while (!stop_request);
            	
            	stop_request = false;
            	running = false;
            	Log.d("background", "stop request received");
       			local_message.send_connection(context, false);

       			stopForeground(true);

            }
        });
    	
    	pollthread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() 
    {
        Log.d("background", "onDestroy");
        local_message.send_connection(context, false);
        mail.imap_connected = false;
    	stop_request = true;
    }
    
    public message received_quickmsg(pgp pgp, quickmsg_db db, String from, attachment a)
    {
		
		quickmsg qm = new quickmsg();
		qm.parse_attachment(a);
		Log.d("recv db", "got quickmsg");
		
		contact cdb, cdbf;
		contact cf = qm.get_contact();
		message mf = qm.get_message();
		int group = qm.get_group();
		
		cdbf = db.contact_get_person_by_address(from);
		if (cdbf.keystat_get() != cdbf.KEYSTAT_VERIFIED)
			return null;

		if (qm.is_group || qm.is_grouppost) {
			cdb = db.contact_get_group_by_address_and_group(qm.group_owner, group);
			if (cdb == null) {
				if (qm.group_owner.length() == 0)
					return null;
				cdb = new contact();
				cdb.type_set(contact.TYPE_GROUP);
				cdb.address_set(qm.group_owner);
				cdb.group_set(qm.group_id);
				db.contact_add(cdb);
				cdb = db.contact_get_group_by_address_and_group(qm.group_owner, group);
			}
		} else {
			cdb = cdbf;
		}
		if (qm.is_post && mf != null) {
			Log.d("recv cb", "Has a post, add to db");
			mf.id_set(cdb.id_get());
			mf.from_set(cdbf.id_get());
			Log.d("recv cb", "id: " + cdb.id_get());
			if (cdb.id_get() == 0) {
				Log.d("recv cb", "Not a valid id");
				return null;
			}
			if (qm.is_grouppost) {
				List<String> members = cdb.members_get();
				boolean foundmember = false;
				
				for (int i = 0; i < members .size(); i++) {
					if (members.get(i).equals(from)) {
						foundmember = true;
						break;
					}
				}
				if (!foundmember) {
					Log.d("msg received", "Sender is not a group member");
					return null;
				}
			}
			cdb.time_lastact_set(mf.time_get());
			cdbf.name_set(cf.name_get());
			
			do_alarm = true;
		}
		if (qm.is_group){
			if (!from.equals(qm.group_owner)) {
				Log.e("recv cb", "Only owner can modify group");
				return null;
			}
			cdb.members_set(cf.members_get());
			cdb.time_lastact_set(cf.time_lastact_get());
			
			if (cdb.members_get().size() == 0) {
				Log.d("recv cb", "group has no members, delete it");
				db.contact_remove(cdb);
				return null;
			}
			cdb.name_set(cf.name_get());
		}
		
		db.contact_update(cdb);
		db.contact_update(cdbf);
    	
		return mf;
    }
    
	public void received_key(pgp pgp, quickmsg_db db, String from, attachment a, Boolean signed)
	{
		String add;
		Boolean verified = false;
		Log.d("recv cb", "Received pgp key attachement");
		
		contact contact = db.contact_get_person_by_address(from);
		if (contact != null)
			if (contact.keystat_get() == contact.KEYSTAT_VERIFIED)
				verified = signed;
		
		InputStream is;
		try {
			is = a.datahandler.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Log.d("recv cb", is.toString());
		add = pgp.public_keyring_add_key(is);
		
		if (add != null) {
			Log.d("recv cb", "adding key to ring was successfull");
			contact = db.contact_get_person_by_address(add);
			if (contact == null) {
				Log.d("recv_cb", "new key, add contact");
				
				Date now = new Date();
				long time_now = now.getTime() / 1000;

				contact = new contact();
				contact.address_set(add);
				contact.type_set(contact.TYPE_PERSON);
				contact.time_lastact_set(time_now);
				if (!verified)
					contact.keystat_set(contact.KEYSTAT_RECEIVED);
				else
					contact.keystat_set(contact.KEYSTAT_VERIFIED);
				
				db.contact_add(contact);
			} else {
				Log.d("recv_db", "Existing contact, update if needed");
				if (contact.type_get() == contact.TYPE_PERSON) {
					Date now = new Date();
					long time_now = now.getTime() / 1000;

					if (contact.keystat_get() != contact.KEYSTAT_VERIFIED)
						contact.time_lastact_set(time_now);
					if (!verified && contact.keystat_get() != contact.KEYSTAT_VERIFIED)
						contact.keystat_set(contact.KEYSTAT_RECEIVED);
					else
						contact.keystat_set(contact.KEYSTAT_VERIFIED);
					Log.d("recv_db", "update db");
					db.contact_update(contact);
				}
			}
		} else {
			Log.d("recv_db", "something is wrong with the key");
		}
		
	}

	public void received_attachment(quickmsg_db db, message m, attachment a, String ext)
	{
		Log.d("received attachement", "going to save attachment");
		File sd = Environment.getExternalStorageDirectory();
		String dir = sd.getAbsolutePath() + "/QuickMSG";
		File df = new File(dir);
		String filenamebase;
		if (a.name == null) {
			filenamebase = UUID.randomUUID().toString() + "." + ext;
		} else {
			filenamebase = a.name;
		}
		File mf;
		int nr = 0;
		String filename;
		do {
			filename = filenamebase + (nr == 0 ? "" : "." + nr );
			mf = new File(dir, filename);
			if (!mf.exists()) {
				break;
			}
			Log.d("received attachment", "exists: " + nr);
			nr++;
		} while (true);
		String name = mf.getPath();
		df.mkdirs();
		OutputStream os;
		String type = a.datahandler.getDataSource().getContentType();
		type = type.split(";")[0];
		String basetype = type.split("/")[0].toLowerCase();
		
		Log.d("received attachment", "name: " + name);
		
		try {
			os = new FileOutputStream(mf);
			a.datahandler.writeTo(os);
			os.flush();
			os.close();
		} catch (Exception e) {
			String err = e.getMessage();
			Log.e("received attachment", "err: " + (err != null ? err : "null"));
			return;
		}
		
		Uri uri;
		if (basetype.equals("image")) {
			Log.d("received attachment", "add image to media library");
			ContentValues values = new ContentValues(5);

			values.put(Images.Media.TITLE, filename);
			values.put(Images.Media.DISPLAY_NAME, name);
			values.put(Images.Media.DATE_TAKEN, m.time_get());
			values.put(Images.Media.MIME_TYPE, type);
			values.put(Images.Media.DATA, name);

			ContentResolver cr = context.getContentResolver();
			uri = cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
		} else if (basetype.equals("video")) {
			Log.d("received attachment", "add video to media library");
			ContentValues values = new ContentValues(5);

			values.put(Video.Media.TITLE, filename);
			values.put(Video.Media.DISPLAY_NAME, name);
			values.put(Video.Media.DATE_TAKEN, m.time_get());
			values.put(Video.Media.MIME_TYPE, type);
			values.put(Video.Media.DATA, name);

			ContentResolver cr = context.getContentResolver();
			uri = cr.insert(Video.Media.EXTERNAL_CONTENT_URI, values);
		} else if (basetype.equals("audio")) {
			Log.d("received attachment", "add audio to media library");
			ContentValues values = new ContentValues(5);

			values.put(Audio.Media.TITLE, filename);
			values.put(Audio.Media.DISPLAY_NAME, name);
			values.put(Audio.Media.DATE_ADDED, m.time_get());
			values.put(Audio.Media.MIME_TYPE, type);
			values.put(Audio.Media.DATA, name);

			ContentResolver cr = context.getContentResolver();
			uri = cr.insert(Audio.Media.EXTERNAL_CONTENT_URI, values);
		} else {
			uri = Uri.parse(name);
		}
		m.uri_set(uri);
	}
	
	public void alarm()
	{
		SharedPreferences getAlarms = PreferenceManager.
                getDefaultSharedPreferences(getBaseContext());
		Boolean notify = getAlarms.getBoolean("notifications_new_message", false);
		Log.d("alarm", "notify? " + (notify ? "yes" : "no"));
		if (!notify)
			return;

		String alarms = getAlarms.getString("notifications_new_message_ringtone", "default ringtone");
		Uri uri = Uri.parse(alarms);
		Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), uri);
		r.play();
		Boolean vibrate = getAlarms.getBoolean("notifications_new_message_vibrate", false);
		Log.d("alarm", "vibrate? " + (vibrate ? "yes" : "no"));
		if (!vibrate)
			return;

		Vibrator v = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);
		 // Vibrate for 500 milliseconds
		v.vibrate(500);
	}


	public static void activity_resume()
	{
		activity_status = true;
		Log.d("background", "resume, status: " + activity_status);
	}
	
	public static void activity_pause()
	{
		activity_status = false;
		Log.d("background", "pause, status: " + activity_status);
	}
	
	private static boolean activity_status = false;
	
	public void update_ui(boolean ack)
	{
		int unread = 0;
		int unread_contacts = 0;
		int unread_contact = 0;

		if (activity_status) {
			Log.d("background", "update ui");
			Intent intent = new Intent();
			intent.setAction("net.vreeken.quickmsg.update_ui");
			sendBroadcast(intent);
		} else {
			Log.d("background", "notification");
			
			/* get unread messages */
			List<contact> contacts = db.contact_get_all();
			
			for (int i = 0; i < contacts.size(); i++) {
				contact contact = contacts.get(i);
				long lastact = contact.time_lastact_get();
				long unreadt = contact.unread_get();
				
				if (lastact > unreadt) {
					int cu = db.message_get_count_by_id(contact.id_get(), unreadt);
					unread += cu;
					if (cu > 0) {
						unread_contacts++;
						unread_contact = contact.id_get();
						Log.d("update_ui", "unread from " + unread_contact);
					}
				}
			}
		}
		
		if (notification != null) {

			// Creates an explicit intent for an Activity in your app
			Intent resultIntent;
			if (unread_contacts != 1) {
				resultIntent = new Intent(this, ListActivity.class);
			} else {
				resultIntent = new Intent(this, MainActivity.class);
				resultIntent.putExtra("id", unread_contact);
				Log.d("update_ui", "extra id: " + unread_contact);
			}
			
			PendingIntent pending_intent = PendingIntent.getActivity(context, 0, resultIntent, 
					PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			notification.setNumber(unread);
			notification.setContentTitle(getString(R.string.app_name));
			if (unread_contacts > 0 && !ack) {
				notification.setSmallIcon(R.drawable.ic_launcher);
                notification.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_launcher));
			} else {
                notification.setSmallIcon(R.drawable.ic_stat_qm);
                notification.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_stat_qm));
			}
			notification.setContentText(getString(R.string.status_unread)  + unread);
			notification.setContentIntent(pending_intent);

	        mNotificationManager.notify(notification_id, notification.build());

		}
	}

	
}
