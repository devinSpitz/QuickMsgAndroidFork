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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.app.AlertDialog;
import android.text.Html;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;
import android.util.*;



public class MainActivity extends quickmsg_activity {

	quickmsg_db db = new quickmsg_db(this);
    contact contact;
    TextView msg_viewer;
    final MainActivity act = this;
	
    final static int MAXVIEW_INIT = 10;
    final static int MAXVIEW_ADD = 5;
    int maxview = MAXVIEW_INIT;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        int id;
        String action = intent.getAction();
    	EditText new_msg = (EditText)findViewById(R.id.new_msg);
    	
    	new_msg.setText("");
    	maxview = MAXVIEW_INIT;
    	msg_viewer = (TextView)findViewById(R.id.msg_viewer);
    	
        if (Intent.ACTION_SEND.equals(action)) {
        	Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
            	select_contact(this, uri);
            }
        } else {
            id = intent.getIntExtra("id", -1);        
            Log.d("main actvitity", "id: " + id);
            
            contact = db.contact_get_by_id(id);
        }
        if (contact == null)
        	return;
        
        setTitle(getString(R.string.title_activity_main) + " - " + contact.name_get());
        
        display_contact();
    }
    
    public void select_contact(final Context context, final Uri uri)
    {
		final List<contact> contactlist = db.contact_get_sendable();
		List<CharSequence> names = new ArrayList<CharSequence>();

	    for (int i = 0; i < contactlist.size(); i++) {
			contact p = contactlist.get(i);
			String add = p.address_get();	
			String name = p.name_get() + " (" + add + ")";
			
			names.add(name);
		}

	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    // Set the dialog title
	    builder.setTitle(R.string.action_select_contact);
	    // Specify the list array, the items to be selected by default (null for none),
	    // and the listener through which to receive callbacks when items are selected
	    builder.setSingleChoiceItems(
	    		names.toArray(new CharSequence[names.size()]),
	    		-1,
	            new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				contact = contactlist.get(which);
				Log.d("select contact", "which: " + which);
			}
	    });
	    // Set the action buttons
	    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            	Log.d("select contact", "OK " + (contact == null ? "null" : "contact"));
            	if (contact != null) {
            		Log.d("onresume", "going to send message");
            		send_msg_attachment(context, uri);
            	}
            	finish();
            	return;
	        }
	    });
	    
	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	@Override
	        public void onClick(DialogInterface dialog, int id) {
	    		Log.d("select contact", "cancel");
	    		contact = null;
	    		finish();
	    		return;
	        }
	    });
	    builder.show();
    }
    
    public void on_update_ui()
    {
    	if (contact == null)
    		return;
    	display_contact();
    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

        case R.id.action_settings:

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }    
*/
    
    /** Called when the user clicks the Send button */
    public void send_msg(View view)
    {
    	send_msg_attachment(view.getContext(), null);
		
   		display_contact();
    }
    
    public void send_msg_attachment(final Context context, Uri uri) {
    	EditText new_msg = (EditText)findViewById(R.id.new_msg);
    	String message_text = new_msg.getText().toString();
    	final attachment unenc;
    	
    	if (message_text.length() < 1 && uri == null)
    		return;
    	
    	Date now = new Date();
		long time_now = now.getTime() / 1000;

		final message m = new message();
		m.id_set(contact.id_get());
		m.from_set(1);
		m.time_set(time_now);
		m.text_set(message_text);
		if (uri != null) {
			m.uri_set(uri);
		}
		
		db.message_add(m);
		
   		//new_mail.send(this);
   		Log.d("send_msg", "send to " + contact.name_get() + "id: " + contact.id_get());
   	
   		quickmsg qmsg = new quickmsg();
   		unenc = qmsg.send_message(this, db.contact_get_by_id(1), contact, m);
   		
   		Log.d("send_msg", "got message unencrypted");
   		
   		
   		final List<String> to_adds;
   		if (contact.type_get() == contact.TYPE_GROUP)
   			to_adds = contact.members_get();
   		else {
   			to_adds = new LinkedList<String>();
   			to_adds.add(contact.address_get());
   		}

        new Thread(new Runnable() {
            public void run() {
            	pgp pgp_enc = new pgp(context);
           		
           		attachment id = pgp_enc.pgpmime_id();

           		for (int i = 0; i < to_adds.size(); i++ ) {
           			String to = to_adds.get(i);
           			if (to.equals(pgp.my_user_id))
           				continue;

           			attachment enc;
           			try {
           				enc = pgp_enc.encrypt_sign(unenc, to);
           			} catch (OutOfMemoryError e) {
           				Log.e("send_msg", "Out of memory during encryption, attachment to big?");
           				enc = null;
           			}
           	   		if (enc == null)
           	   			continue;
           	   		enc.disposition = "inline";
           	   		
           	   		Log.d("send_msg", "got message encrypted");
           			mail mail = new mail();
           			List<attachment> attachments = new LinkedList<attachment>();
           			attachments.add(id);
           			attachments.add(enc);
        		
           			String queue = mail.send(context, to, attachments, "encrypted");
           			if (queue != null) {
           				m.queue_set(queue);
           				db.message_update(m);

           				Intent intent = new Intent();
           				intent.setAction("net.vreeken.quickmsg.update_ui");
           				sendBroadcast(intent);
           			}
           		}
        		Log.d("send_msg", "mail.send done");            	
            }
        }).start();
        
        new_msg.setText("");
    }
    
    public void display_contact() {
    	List<message> messages = db.message_get_by_id(contact.id_get(), maxview + 1);
    	Boolean unread = false;
    	Boolean more;
    	Spanned span = new SpannableString("");
    	
    	if (messages.size() > maxview) {
    		messages.remove(0);
    		more = true;
    	} else {
    		more = false;
    	}
    	
    	final Context context = this;
    	DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    	final int maxw = metrics.widthPixels / 2 + 1;
    	final int maxh = metrics.heightPixels / 2 + 1;
  
    	for (int i = 0; i < messages.size(); i++) {
        	String uri_html = null;
        	
    		message m = messages.get(i);
    		
    		Boolean from_me = (m.from_get() == 1);
    		
        	contact from = db.contact_get_by_id(m.from_get());
    		if (from == null) {
    			Log.d("display contact", "from == null" + m.from_get());
    			from = new contact();
    		}
    		
    		if (m.time_get() > contact.unread_get()) {
    			if (!unread) {
    				Spanned unread_span = Html.fromHtml("<p><b><i>New:</i></b></p>");
            		span = (Spanned) TextUtils.concat(span, unread_span);
    			}
    			unread = true;
    			contact.unread_set(m.time_get());
    		}
    		
    		long time_ms = m.time_get() * 1000;
			Date time_d = new java.util.Date(time_ms);
			SimpleDateFormat dateformat = new SimpleDateFormat();
				
			int hdr_color;
        	if (m.from_get() == 1)
        		hdr_color = 0xff808080;
        	else
        		hdr_color = 0xff0050a0;

        	int bgcolor = 0xffd0e8ff;
        	int bgcolorborder = 0xff66b2ff;
        	if (from_me) {
        		bgcolor = 0xffe0e0e0;
        		bgcolorborder = 0xffa0a0a0;
        	}

        	String hdr_name = from.name_get();
        	String hdr_date = dateformat.format(time_d) + "\n";
        	SpannableString span_hdr = new SpannableString(hdr_name + " " + hdr_date);
        	span_hdr.setSpan(new StyleSpan(Typeface.ITALIC), 
        			span_hdr.length() - hdr_date.length(), span_hdr.length(),
        			Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    		span_hdr.setSpan(new StyleSpan(Typeface.BOLD), 
        			0, hdr_name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	if (from_me)
    			span_hdr.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_OPPOSITE),
    				0, span_hdr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	else 
    			span_hdr.setSpan(new ForegroundColorSpan(hdr_color),
        				0, span_hdr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	span_hdr.setSpan(new line_background_span(bgcolor, bgcolorborder, !from_me),
        			0, span_hdr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    		
    		Uri uri = m.uri_get();
    		if (uri != null) {
//    			Log.d("display contact", "uri: " + uri.toString());
    			ContentResolver cR = getContentResolver();
    			String fulltype;
    			boolean uri_fault = false;
    			try {
    				fulltype = cR.getType(uri);
    			} 
    			catch (IllegalStateException e) {
    				Log.e("display contact", "could not get full type");
    				fulltype = "unknown type";
    				uri_fault = true;
    			}
    			boolean handled = false;
    			String handled_string = "";

    			if (fulltype != null) {
        			String type = fulltype.split("/")[0].toLowerCase();
    				Log.d("display contact", "fulltype: " + fulltype + ", type: " + type);
        			
        			if (type.equals("image")) {
        				handled_string = "<img src='image#" + uri.toString() + "'>";
        				handled = true;
        			}
        			if (type.equals("video")) {
        				handled_string = "<img src='video#" + uri.toString() + "'>";
        				handled = true;
        			}
        			if (type.equals("audio")) {
        				handled_string = "<img src='audio#" + uri.toString() + "'>";
        				handled = true;
        			}
    			}
    			Log.d("display contact", "handled_string: " + handled_string);
    			
    			if (uri_fault)
    				handled = false;
    		
    			uri_html = "<p>";
       			if (!handled) {
       				uri_html += fulltype;
       			} else {
       				Intent intent = new Intent(Intent.ACTION_VIEW);
       				intent.setData(uri);
       				List<ResolveInfo> ia = this.getPackageManager().queryIntentActivities(intent, 0);
       		        if (ia.size() > 0) {
            			uri_html += "<a href='" + uri.toString() +
            					"'>" +
            					(handled ? handled_string : uri.toString()) +
            					"</a>";
       		        } else {
    					uri_html += handled ? handled_string : uri.toString();
       		        }
       			}
       			uri_html += "</p>";
       			Log.d("display contact", "uri_html: " + uri_html);
    		}
    		
        	Spannable span_msg = new SpannableString(m.text_get());
        	if (from_me)
    			span_msg.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_OPPOSITE),
    				0, span_msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        	Spanned span_uri = null;
        	
        	if (uri_html != null) {
        		span_uri = Html.fromHtml(uri_html, new Html.ImageGetter() {
        			@Override
        			public Drawable getDrawable(final String img_source) {
        				String source_type = img_source.split("#")[0];
        				String source = img_source.split("#")[1];
        				Log.d("getdrawable", "img_source: " +img_source+ "source_type: " + source_type + " source: " + source);
        				Bitmap bm;
      			  
        				if (source_type.equals("image")) {
        					BitmapFactory.Options options = new BitmapFactory.Options();
      			  
        					InputStream is;
        					try {
        						is = getContentResolver().openInputStream(Uri.parse(source));
        					} catch (FileNotFoundException e) {
        						Log.d("getDrawable", e.getMessage());
        						return null;
        					}
      			  
        					options.inJustDecodeBounds = true;
        					BitmapFactory.decodeStream(is, null, options);
        					int h = options.outHeight;
        					int w = options.outWidth;
      			  
        					int scaleh = (maxh + h) / maxh;
        					int scalew = (maxw + w) / maxw;
      			  
        					options.inSampleSize = Math.max(scaleh, scalew);
        					options.inJustDecodeBounds = false;

        					try {
        						is = getContentResolver().openInputStream(Uri.parse(source));
        					} catch (FileNotFoundException e) {
        						Log.d("getDrawable", e.getMessage());
        						return null;
        					}

        					bm = BitmapFactory.decodeStream(is, null, options);
        				} else if (source_type.equals("video")) {
        					String[] projection = { MediaStore.Video.Media._ID };
        					Cursor cursor = getContentResolver().query(Uri.parse(source), projection, null, null, null);

        					int column_index = cursor
      				            .getColumnIndexOrThrow(MediaStore.Video.Media._ID);

        					cursor.moveToFirst();
        					long video_id = cursor.getLong(column_index);
        					cursor.close();
        					
        					ContentResolver crThumb = getContentResolver();

        					bm = MediaStore.Video.Thumbnails.getThumbnail(crThumb,video_id, MediaStore.Video.Thumbnails.MICRO_KIND, null);
        				} else if (source_type.equals("audio")) {
        					Log.d("get drawable", "audio uri");
        					Drawable dp = getResources().getDrawable(R.drawable.play);
        					if (dp == null) {
        						return null;
        					}
        					dp.setBounds(0, 0, dp.getIntrinsicWidth(), dp.getIntrinsicHeight());
        					return dp;
        				} else {
        					Log.d("get drawable", "unknown source type: " + source_type);
        					return null;
        				}

        				Drawable d = new BitmapDrawable(getResources(), bm);
        				d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
      			  
        				return d;
        			}
        		}, null);
        	}

        	span_msg.setSpan(new line_background_span(bgcolor, bgcolorborder, !from_me),
        			0, span_msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	span = (Spanned) TextUtils.concat(span, span_hdr);
        	span = (Spanned) TextUtils.concat(span, span_msg);
        	if (uri_html != null)
        		span = (Spanned) TextUtils.concat(span, span_uri);
			
    		String queue = m.queue_get();
    		
    		String between = "<p><br></p>";
    		if (queue != null) {
    			mail mail = new mail();
    			
    			if (mail.queue_check(this, queue)) {
    				between = "<p><font color='#990000'>Still in mail queue</font></p>";
    			} else {
    				m.queue_set(null);
    				db.message_update(m);;
    			}
    		}
        	Spanned between_span = Html.fromHtml(between);
    		span = (Spanned) TextUtils.concat(span, between_span);
    	}
    	if (contact.time_lastact_get() > contact.unread_get()) {
    		contact.unread_set(contact.time_lastact_get());
    	}
    	db.contact_update(contact);
    	
    	
    	if (more) {
        	Spannable span_more = new SpannableString("view more messages\n\n");
        	span_more.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                	maxview += MAXVIEW_ADD;
                	Log.d("view contacts", "new maxview: " + maxview);
                	display_contact();
                }
            }, 0, span_more.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        	        	
        	span = (Spanned) TextUtils.concat(span_more, span);
    	}
    	
        msg_viewer.setText(span);
        msg_viewer.setMovementMethod(LinkMovementMethod.getInstance());
 
       final ScrollView scrollview = ((ScrollView) findViewById(R.id.scrollView1));
        scrollview.post(new Runnable() {
            @Override
            public void run() {
            	if (maxview > MAXVIEW_INIT)
            		scrollview.fullScroll(ScrollView.FOCUS_UP);
            	else
            		scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}


