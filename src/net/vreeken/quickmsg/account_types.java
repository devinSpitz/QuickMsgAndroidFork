package net.vreeken.quickmsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class account_types {
	/* email services and settings known to work. */
	public static account_type openmailbox = new account_type(
			"openmailbox.org", "foobar@openmailbox.org",
			null,
			"imap.openmailbox.org", "143", 
			"smtp.openmailbox.org", "587");
	public static account_type gmail = new account_type(
			"Gmail", "foo.bar@gmail.com",
			null,
			"imap.gmail.com", "993", 
			"smtp.gmail.com", "587");

	/* email services and settings that should work, but not confirmed */
	public static account_type mykolab = new account_type(
			"MyKolab", "foobar@mykolab.com",
			null,
			"imap.mykolab.com", "993", 
			"smtp.mykolab.com", "587");
	public static account_type riseup = new account_type(
			"Riseup", "foobar@riseup.net",
			"@riseup.net",
			"mail.riseup.net", "143", 
			"mail.riseup.net", "465");
	
	public static List<account_type> account_types= 
	    new ArrayList<account_type>(Arrays.asList(
			gmail,
			openmailbox, 
			mykolab,
			riseup));
}
