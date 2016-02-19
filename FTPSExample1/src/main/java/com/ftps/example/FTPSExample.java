
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
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.CertificateException;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

/**
 * 
 * @author vikas 
 * FTPS Example class
 */
public final class FTPSExample {

	private  String KEYSTORE_FILE_NAME = "/home/vikas/work/cacerts";
	private  String TRUST_STORE_FILE_NAME = "/home/vikas/work/cacerts";
	private  String KEYSTORE_PASS = "ftpuser";
	 String SERVER = "localhost";
	 String USERNAME = "ftpuser";
	 String PASSWORD = "ftpuser";
	 String REMOTE_PATH = "/remote/a.txt"; // Make Sure you give remote
													// path w.r.to remote ftp
													// user home dir
	 String LOCAL_PATH = "a.txt ";
	 String PROTOCOL = "SSL";
	 FTPSClient ftpsClient = null;

	private  KeyManager[] getKeyManagers()
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException,
			IOException, UnrecoverableKeyException, java.security.cert.CertificateException {
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(KEYSTORE_FILE_NAME), KEYSTORE_PASS.toCharArray());
		KeyManagerFactory tmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		System.out.println("KeyManagerFactory.getDefaultAlgorithm() : " + KeyManagerFactory.getDefaultAlgorithm());
		tmf.init(ks, KEYSTORE_PASS.toCharArray());
		return tmf.getKeyManagers();
	}

	private  TrustManager[] getTrustManagers()
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException,
			IOException, UnrecoverableKeyException, java.security.cert.CertificateException {
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(TRUST_STORE_FILE_NAME), KEYSTORE_PASS.toCharArray());
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		return tmf.getTrustManagers();
	}

	private  void setSSLContext() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException,
			FileNotFoundException, java.security.cert.CertificateException, CertificateException, IOException {
		KeyManager keyManager = getKeyManagers()[0];
		TrustManager trustManager = getTrustManagers()[0];		
		ftpsClient.setKeyManager(keyManager);
		ftpsClient.setTrustManager(trustManager);
		System.out.println("Done Setting keyManager and trustManager");

	}

	
	public  final void doFtp()
			throws NoSuchAlgorithmException, IOException, KeyManagementException, UnrecoverableKeyException,
			KeyStoreException, java.security.cert.CertificateException, CertificateException {

		boolean storeFile = false, binaryTransfer = false, error = false;

		binaryTransfer = true;
		ftpsClient = new FTPSClient(PROTOCOL);
		setSSLContext();
		storeFile = true;
		ftpsClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

		try {
			int reply;
			ftpsClient.connect(SERVER);
			ftpsClient.execPROT("P");

			System.out.println("Connected to " + SERVER + ".");
			reply = ftpsClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpsClient.disconnect();
				System.err.println("FTP server refused connection.");
				System.exit(1);
			}
		} catch (IOException e) {
			if (ftpsClient.isConnected()) {
				try {
					ftpsClient.disconnect();
				} catch (IOException f) {
					// do nothing
				}
			}
			System.err.println("Could not connect to server.");
			e.printStackTrace();
			System.exit(1);
		}

		__login: try {
			System.out.println("Completed SSL Handshake");

			ftpsClient.setBufferSize(1024);
			if (!ftpsClient.login(USERNAME, PASSWORD)) {
				ftpsClient.logout();
				error = true;
				break __login;
			}
			System.out.println("Remote system is " + ftpsClient.getSystemName());
			if (binaryTransfer) {
				ftpsClient.setFileType(FTP.BINARY_FILE_TYPE);
			}
			ftpsClient.enterLocalPassiveMode();
			if (storeFile) {

				InputStream input;
				input = FTPSExample.class.getClassLoader().getResourceAsStream("a.txt");
				ftpsClient.storeFile(REMOTE_PATH, input);
				System.out
						.println("Stored local File : " + LOCAL_PATH + "to Server :" + SERVER + " at : " + REMOTE_PATH);
				input.close();
			} else {
				OutputStream output;
				output = new FileOutputStream(LOCAL_PATH);
				ftpsClient.retrieveFile(REMOTE_PATH, output);
				output.close();
			}

			ftpsClient.logout();
		} catch (FTPConnectionClosedException e) {
			error = true;
			System.err.println("Server closed connection.");
			e.printStackTrace();
		} catch (IOException e) {
			error = true;
			e.printStackTrace();
		} finally {
			if (ftpsClient.isConnected()) {
				try {
					ftpsClient.disconnect();
				} catch (IOException f) {
				}
			}
		}

		System.exit(error ? 1 : 0);
	}
	
	public static void main(String[] args) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, java.security.cert.CertificateException, IOException, CertificateException {
		FTPSExample ftpsExample = new FTPSExample();
		ftpsExample.doFtp();
	}
}