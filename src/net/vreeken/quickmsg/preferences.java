package net.vreeken.quickmsg;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class preferences {
	Context context;
	quickmsg_db db;

	public preferences(Context c)
	{
		context = c;
		db = new quickmsg_db(c);
	}
	
	public String get(String pref, String def)
	{
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);

	    return SP.getString(pref, def);
	}
	
	public String get(String pref)
	{
		return get(pref, null);
	}
	
	public void set(String pref, String value)
	{
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = SP.edit();

		editor.putString(pref, value);
		editor.commit();
	}
	
	public void pref2db()
	{
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);

		db.preference_add("display_name", SP.getString("display_name", "").getBytes());
		db.preference_add("email_address", SP.getString("email_address", "").getBytes());
		db.preference_add("imap_user", SP.getString("imap_user", "").getBytes());
		db.preference_add("smtp_user", SP.getString("smtp_user", "").getBytes());
		db.preference_add("imap_pass", SP.getString("imap_pass", "").getBytes());
		db.preference_add("smtp_pass", SP.getString("smtp_pass", "").getBytes());

		db.preference_add("imap_server", SP.getString("imap_server", "").getBytes());
		db.preference_add("imap_port", SP.getString("imap_port", "").getBytes());
		db.preference_add("smtp_server", SP.getString("smtp_server", "").getBytes());
		db.preference_add("smtp_port", SP.getString("smtp_port", "").getBytes());

		db.export_db();
	}
	
	public void db2pref()
	{
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = SP.edit();
		
		editor.putString("display_name", new String(db.preference_get("display_name")));
		editor.putString("email_address", new String(db.preference_get("email_address")));
		editor.putString("imap_user", new String(db.preference_get("imap_user")));
		editor.putString("smtp_user", new String(db.preference_get("smtp_user")));
		editor.putString("imap_pass", new String(db.preference_get("imap_pass")));
		editor.putString("smtp_pass", new String(db.preference_get("smtp_pass")));
		editor.putString("imap_server", new String(db.preference_get("imap_server")));
		editor.putString("smtp_server", new String(db.preference_get("smtp_server")));
		editor.putString("imap_port", new String(db.preference_get("imap_port")));
		editor.putString("smtp_port", new String(db.preference_get("smtp_port")));

		editor.commit();
	}
}
