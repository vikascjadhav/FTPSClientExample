# FTPSClientExample
Setup FTPS on server 
Use this Java program to Test FTPS Connectivity




1) copy certificate
 cp /etc/ssl/certificates/vsftpd.pem vsftpd.pem

2) Import in keystore/truststore using keytool
   openssl x509 -outform der -in vsftpd.pem -out certificate.der
   keytool -import -alias Test_FTP -keystore cacerts -file certificate.der
   this command will ask for password

