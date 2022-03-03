# Virtual-modem-communication
Server-Client info exchange using virtual modem.

The virtual modem was downloaded from the university's web page as a jar file and was imported in the project.
Using it we are able to declare the server response time (speed in Kbps) and the timeout duration. Quick quide 
for the functions of the code.

~ GpsLocationRecv() ~

Asks for coordinate info from satellite, which is returned in "NMEA" format displaying geographical length, amplitude,
number of working satellites and time. Decoding the info we obtained and forming a new request code we ask for an image of 
the university.

~ AutoRepeatRequest() ~

Here we implement a basic idea which is the foundation of today's computer network functionality. Basically we ask for
consecutive echo packets, which contain a 16-bit sequence of chars and after that a 3-digit number which is an encrypted
indicator of whether the message has been changed or not. With appropriate decoding of the 16-bit String and comparing
the result with the 3-digit bit-error-detection helper, we either send an ACK (Send next), or a NACK (Send same).

For the rest of the functions be sure to check the "Networks-2" repository.
