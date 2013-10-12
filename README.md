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

Orwell uses the excellent [Bouncy Castle](http://www.bouncycastle.org/java.html) library
as a basis for much of the functionality provided, with enhancements made to improve the 
security of the block ciphers used.


Security
------------------------------

Orwell uses Elliptic Curve Cryptography as the basis for the public key exchange. The
key exchange protocol uses a unique signing scheme based on the use of *a priori*
information to facilitate signing and verifying the keys, rather than depending on a 
centralized Certificate Authority.



Copyright (Really Copyleft)
---------------------------

All of the source code in this repository, where the copyright notice is indicated in the source
code, is licensed under the [GNU General Public License, Version 3](http://www.gnu.org/licenses/gpl.html)