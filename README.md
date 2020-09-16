This is a fork from the original QuickMSG

<div>

<center><img src="https://quickmsg.vreeken.net/qm.png"><h1>QuickMSG</h1></center>

<div>
<h2>What is QuickMSG?</h2>
<p>
	QuickMSG is a secure messaging format and an Android App of the same name.
	With it you can send messages either to other contacts directly or to
	groups.
	Its aim is to combine the ease of use that messaging apps give with the
	security of encrypted email.
</p>
</div>

<div>
<h3>Quick links</h3>
<p>
	<a href="https://quickmsg.vreeken.net/QuickMSG.apk">Download Android app</a><br>
	<a href="https://quickmsg.vreeken.net/usage.html">Usage</a><br>
	<a href="https://quickmsg.vreeken.net/details.html">Technical details</a><br>
	<a href="https://quickmsg.vreeken.net/updates.html">Updates</a><br>
</p>
<p>
	Current version: 20150304
	Current status: beta, pretty stable
</p>
</div>

<div>
<h3>What is special about QuickMSG?</h3>
<p>
	Nothing really.
	QuickMSG uses a number of existing technologies and combines them
	such that the user can send messages to contacts and friends.
	
	There are a few differences with most messaging services:
	</p><ul>
		<li>
		There is no central QuickMSG server or infrastructure.
		</li><li>
		Your messages will be sent via the existing SMTP/IMAP
		E-mail infrastructure.
		</li><li>
		Your messages will be sent using encryption from end to end.
		Nobody in between can read it.
		</li><li>
		QuickMSG uses well known and established PGP encryption.
		By default QuickMSG will use 2048 bit RSA encryption for each
		message.
		</li><li>
		You identify with an email address, not with a phone number.
		QuickMSG is thus not bound to a phone, but can be used on
		other device types as well.
	</li></ul>
	
	There are also some differences with regular email:
	<ul>
		<li>
		No subject.
		The subject is not really needed for 'quick' chatting.
		Besides it isn't encrypted anyway.
		</li><li>
		Easy to setup.
		Encryption is the default. No need to jump through hoops to
		setup PGP.
		</li><li>
		Easy group handling. Groups are easy to create and are
		synchronized to the members automaticly.
		They can be used the same way as any other contact.
	</li></ul>
	
	To sum up: email with the ease of use of a chat app.
<p></p>
</div>

<div>
<h3>Is SMTP and IMAP suitable for instant messaging?</h3>
<p>
	The SMTP infrastructure of the internet has been handling huge amounts
	of traffic for years. Its decentralized nature ensures that no single
	entity can easily take down communications.
	And with IMAP messages can be delivered to the receipient almost
	instantanious.
	The user experience is not much different from using a central messaging
	service.
</p>
</div>

<div><h3>Who can read my messages?</h3>
<p>
You and the contacts you are sending it to (or receiving it from) and nobody else.
The message is encrypted from end to end. Only on the end devices is is encrypted/decrypted.
</p>
<p>
The contents of your messages are stored on the device itself.
Think carefully what you do with it. For example: backing up your phone is good, but think
about were you are sending all your data and who has access to it. (tip: use encryption)
</p>
</div>

<div><h3>Can I send images or videos?</h3>
<p>
Yes, besides simple text messages almost any kind of media can be send.
The QuickMSG app can only view images inline in the app itself,
but any kind of data can be shared.
</p>
</div>

<div><h3>Screenshots</h3>
<p>
<img src="https://quickmsg.vreeken.net/SC20140318-141932.png">
<img src="https://quickmsg.vreeken.net/SC20140318-142017.png">
</p>
</div>

<div><h3>Is it limited to Android?</h3>
<p>
	QuickMSG messages can be sent from any platform. 
	However I only made an Android app. 
	There is nothing that prevents creation of an app or plugin or
	whatever for your platform of choice... somebody just has to do it.
</p>
<p>
	There is a command line tool for Linux, it is working, 
	but mostly usefull for experiments and not very userfriendly.
</p>
</div>

<div><h3>License</h3>
<p>
	The QuickMSG app is licensed under the GNU GPL v3.
</p>
<p>
	Note that this license applies to the implementation, not the message format.
	You can still use the message format in your own program under any license you want.
</p>
<p>
	Some of the libraries it uses are licensed under the MIT license or
	Apache license or CDDL/GPL v2 license with classpath exception.
</p>
</div>

<div><h3>Source code</h3>
<p>
	<a href="https://quickmsg.vreeken.net/releases/QuickMSG-20150304.tar.gz">Current Android Source</a><br>
	<a href="https://quickmsg.vreeken.net/QuickMSG.git">QuickMSG.git archive</a><br>
	<a href="https://quickmsg.vreeken.net/releases/quickmsg-cli-20140415.tar.gz">CLI Source</a><br>
</p>
<p>
	The following sources are here for historical reasons.
	Current versions of the QuickMSG source have these integrated.
</p>
<p>
	<a href="https://quickmsg.vreeken.net/spongycastle.git">Spongycastle sources (git archive)</a><br>
	These sources are a clone of the archive at 
	<a href="http://rtyley.github.io/spongycastle/">http://rtyley.github.io/spongycastle/</a><br>
	<a href="https://quickmsg.vreeken.net/javamail-android.tar.gz">Javamail for android sources</a><br>
	These sources are a download from 
	<a href="http://code.google.com/p/javamail-android/">
	http://code.google.com/p/javamail-android/
	</a>
</p>
</div>

<div><b>Contact</b>
<p>
	jeroen@vreeken.net
</p>
</div>

</div>
