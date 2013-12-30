Beware of Big Brother
==============================

Orwell
------------------------------

Orwell is an easy to use Java library which provides cryptographic and privacy 
functionality for the pathologically paranoid. Orwell initially started off as
the cryptographic library for [Tinfoil-SMS](https://github.com/tinfoilhat/tinfoil-sms)
as we did not see any cryptographic library that provided the level of security and 
privacy we wanted.


Background
------------------------------

Orwell uses [Stripped Castle](https://github.com/gnu-user/strippedcastle), which is 
a modified version of the the excellent [Bouncy Castle](http://www.bouncycastle.org/java.html) 
library as a basis for much of the functionality provided. Stripped Castle is the same as
Bouncy Castle, just with slight enhancements made to support using Bouncy Castle on Android and
to improve the security of the block ciphers used.


Security
------------------------------

Orwell uses Elliptic Curve Cryptography as the basis for the public key exchange. The
key exchange protocol uses a unique signing scheme based on the use of *a priori*
information to facilitate signing and verifying the keys, rather than depending on a 
centralized Certificate Authority.

After the key exchange a hybrid encryption scheme is used known as Integrated Encryption
Scheme ([IES](http://en.wikipedia.org/wiki/Integrated_Encryption_Scheme)) to facilitate 
sending and receiving encrypted messages by using the Elliptic Curves and *a priori* 
information in order to derive the symmetric keys that are used. 


Copyright (Really Copyleft)
---------------------------

All of the source code in this repository, where the copyright notice is indicated in the source
code, is licensed under the [GNU General Public License, Version 3](http://www.gnu.org/licenses/gpl.html)