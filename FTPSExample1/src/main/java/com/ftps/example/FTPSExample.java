
package com.ftps.example;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

public final class FTPSExample {

	public static final void main(String[] args) throws NoSuchAlgorithmException, IOException {
		
		boolean storeFile = false, binaryTransfer = false, error = false;
		String server, username, password, remote, local;
		String protocol = "SSL"; 
		FTPSClient ftps;
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