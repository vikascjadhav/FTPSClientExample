
package com.ftps.example;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.CertificateException;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

public final class FTPSExample {

	private static String KEYSTORE_FILE_NAME = "/home/vikas/work/cryptography/test/clientkeystore.jks";
	private static String KEYSTORE_PASS = "password";
	
    private static SSLContext getSSLContext() throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, UnrecoverableKeyException, IOException, java.security.cert.CertificateException {
      TrustManager[] tm = getTrustManagers();
      System.out.println("Init SSL Context");
      SSLContext sslContext = SSLContext.getInstance("SSLv3");
      sslContext.init(null, tm, null);
       return sslContext;
    }
    
    
	private static KeyManager[] getKeyManagers() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, java.security.cert.CertificateException {
         KeyStore ks = KeyStore.getInstance("JKS");
         ks.load(new FileInputStream(KEYSTORE_FILE_NAME), KEYSTORE_PASS.toCharArray());
         KeyManagerFactory tmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
         System.out.println("KeyManagerFactory.getDefaultAlgorithm() : "+KeyManagerFactory.getDefaultAlgorithm());
         tmf.init(ks, KEYSTORE_PASS.toCharArray());
         return tmf.getKeyManagers();
    }
	 
	private static TrustManager[] getTrustManagers() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, java.security.cert.CertificateException {
        KeyStore ks = KeyStore.getInstance("JKS");
	    ks.load(new FileInputStream(KEYSTORE_FILE_NAME), KEYSTORE_PASS.toCharArray());
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
        return tmf.getTrustManagers();
    }
	 
	public static final void main(String[] args) throws NoSuchAlgorithmException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, java.security.cert.CertificateException, CertificateException {
	
		//getSSLContext();
		//getTrustManagers();
		 KeyManager keyManager = getKeyManagers()[0];
         TrustManager trustManager = getTrustManagers()[0];
         System.out.println("keyManager : "+keyManager +" trustManager : "+trustManager);
		System.out.println("Completed SSL Handshake");
//		/System.exit(0);
		
		boolean storeFile = false, binaryTransfer = false, error = false;
		String server, username, password, remote, local;
		String protocol = "SSL"; 
		FTPSClient ftps = null;
		server = "localhost";
		username = "ftpuser";
		password = "ftpuser";
		//Make Sure you give remote path w.r.to remote ftp user home dir
		remote = "/remote/a.txt";
		local = "a.txt ";
		binaryTransfer = true;
		ftps = new FTPSClient(protocol);
		
		storeFile = true;
		ftps.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
		
		ftps.setKeyManager(keyManager);
		ftps.setTrustManager(trustManager);
		System.out.println("Done Setting keyManager and trustManager");
		try {
			int reply;
			ftps.connect(server);
			ftps.execPROT("P");
			
			System.out.println("Connected to " + server + ".");
			reply = ftps.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftps.disconnect();
				System.err.println("FTP server refused connection.");
				System.exit(1);
			}
		} catch (IOException e) {
			if (ftps.isConnected()) {
				try {
					ftps.disconnect();
				} catch (IOException f) {
					// do nothing
				}
			}
			System.err.println("Could not connect to server.");
			e.printStackTrace();
			System.exit(1);
		}


		//ftps.setTrustManager();
		__login: try {
			ftps.setBufferSize(1024);
			if (!ftps.login(username, password)) {
				ftps.logout();
				error = true;
				break __login;
			}
			System.out.println("Remote system is " + ftps.getSystemName());
			if (binaryTransfer) {
				ftps.setFileType(FTP.BINARY_FILE_TYPE);
			}			
			ftps.enterLocalPassiveMode();
			if (storeFile) {
				
				InputStream input;
				input = FTPSExample.class.getClassLoader().getResourceAsStream("a.txt");
				ftps.storeFile(remote, input);
				System.out.println("Stored local File : "+ local + "to Server :"+server+" at : "+remote);
				input.close();
			} else {
				OutputStream output;
				output = new FileOutputStream(local);
				ftps.retrieveFile(remote, output);
				output.close();
			}

			ftps.logout();
		} catch (FTPConnectionClosedException e) {
			error = true;
			System.err.println("Server closed connection.");
			e.printStackTrace();
		} catch (IOException e) {
			error = true;
			e.printStackTrace();
		} finally {
			if (ftps.isConnected()) {
				try {
					ftps.disconnect();
				} catch (IOException f) {
				}
			}
		}

		System.exit(error ? 1 : 0);
	}
}