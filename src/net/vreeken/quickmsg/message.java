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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import android.net.Uri;
import android.util.Log;

public class message {
	int _id; // contact id this messag belongs to
	int _from; // contact that send it
	long _time;
	String _text = "";
	Uri _uri = null;
	String _queue = null;
	
	public message() 
	{
		
	}
	public message(int id, int from, int time, String text)
	{
		id_set(id);
		from_set(from);
		time_set(time);
		text_set(text);
	}
	
	public void id_set(int id)
	{
		_id = id;
	}
	public int id_get()
	{
		return _id;
	}
	
	public void from_set(int from)
	{
		_from = from;
	}
	public int from_get()
	{
		return _from;
	}
	
	public void time_set(long time)
	{
		_time = time;
	}
	public long time_get()
	{
		return _time;
	}
	
	public void text_set(String text)
	{
		_text = text;
	}
	public String text_get()
	{
		if (_text == null) {
			Log.e("message", "_text == null");
			return "";
		}
		return _text;
	}

	public void uri_set(Uri uri)
	{
		_uri = uri;
	}
	public Uri uri_get()
	{
		return _uri;
	}
	public String uri_get_string()
	{
		if (_uri != null)
			return _uri.toString();
		else
			return null;
	}
	
	public String queue_get()
	{
		return _queue;
	}
	public void queue_set(String queue)
	{
		_queue = queue;
	}
	
	public String toString()
	{
		return "id=" + _id + ", " +
				"from=" + _from + ", " +
				"time=" + _time + ", " +
				"text=" + _text;
	}
	
}
