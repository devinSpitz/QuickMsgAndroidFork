package net.vreeken.quickmsg;

import android.content.Context;

public class account_type {
	String _name;
	String _username_help;
	String _username_remove; /* what should be removed from address to get username*/
	String _imap_server;
	String _smtp_server;
	String _imap_port;
	String _smtp_port;

	public account_type(String name, String username_help, String username_remove, String imap_server, String imap_port, String smtp_server, String smtp_port)
	{
		_name = name;
		_username_help = username_help;
		_username_remove = username_remove;
		_imap_server = imap_server;
		_imap_port = imap_port;
		_smtp_server = smtp_server;
		_smtp_port = smtp_port;
	}
	
	public String name_get()
	{
		return _name;
	}
	
	public void preferences_set(Context ctx, String name, String address, String pass)
	{
		/* we got a gmail account user, fill in the blanks with server info
		 * and commit it
		 */
		preferences preferences = new preferences(ctx);
		String username;
		
		if (_username_remove != null) {
			username = address.replace(_username_remove, "");
		} else
			username = address;

		preferences.set("display_name", name);
		preferences.set("email_address", address);
		preferences.set("imap_user", username);
		preferences.set("smtp_user", username);
		preferences.set("imap_pass", pass);
		preferences.set("smtp_pass", pass);

		preferences.set("imap_server", _imap_server);
		preferences.set("imap_port", _imap_port);
		preferences.set("smtp_server", _smtp_server);
		preferences.set("smtp_port", _smtp_port);
	}
}
