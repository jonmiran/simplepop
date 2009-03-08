/**
 * 
 */
package is.ru.tsam.pop3.server;

/**
 * @author gunnl
 *
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

interface ServerState {
	int Authorize = 1;
	int Transaction = 2;
	int Update = 3;
	int Quit = 4;
	int Reciving = 5;
}

public class SocketServer extends Thread {

	/* server state */
	private static int state;

	/* buffer to read input to */
	private static BufferedReader input;

	/* buffer to write output */
	private static BufferedWriter output;

	/* socket to client */
	private static Socket client = null;

	/* accepted users */
	private static HashMap<String, String> users = null;

	/* Time out */
	final static int TIME_OUT = 1;

	/* Port to listen on */
	public final static int TCP_PORT = 110;

	/* server socket */
	private ServerSocket ss;

	/* number of mails */
	private static ArrayList<File> fileArray = null;

	/* Constructor */
	public SocketServer() throws IOException {
		ss = new ServerSocket(TCP_PORT);
		users = new HashMap<String, String>();
		users.put("gunni", "12345");
		users.put("svenni", "12345");
		users.put("palli", "12345");
		fileArray = new ArrayList<File>();
		fileArray.add(new File("mail.txt"));
		fileArray.add(new File("mail_2.txt"));

		getOneMailMessage(fileArray.get(0));
	}

	/* Our server starts here */
	public static void main(String[] argv) throws IOException {
		new SocketServer().start();
	}

	/* Run method from Thread */
	public void run() {
		System.out.println(SocketServer.class.getSimpleName() + " waiting for connection on TCP port " + TCP_PORT);
		//while (true) {
		try {

			client = ss.accept();

			input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

		} catch (IOException e) {
			System.err.println("Doh! " + e);
		}

		sendMessageToClient("+OK Welcome to a " + SocketServer.class.getSimpleName() + " in java.");

		state = ServerState.Authorize;

		try {
			work();
		} catch (Exception e) {
			System.out.println(e);
			//reset();
		}
		reset();
		//}

	}

	public void work() throws IOException {
		String line = "";
		while (true) {

			if (state == ServerState.Authorize) {

				line = input.readLine();
				System.out.println(line);

				if (line.startsWith("USER")) {
					sendMessageToClient("+OK valid username now send PASS\r\n");
				}
				else if (line.startsWith("PASS")) {
					sendMessageToClient("+OK your pass is fine!\r\n");
					state = ServerState.Transaction;
				} 
				else {
					sendMessageToClient("-ERR\r\n");
				}

			} else if (state == ServerState.Transaction) {

				line = input.readLine();
				System.out.println(line);

				if (line.startsWith("STAT")) { // STAT
					sendMessageToClient("+OK " + numberOfMailMessages() + " messages (" + getTotalMailSizeInOctets() + " octets)\r\n");
				} 
				else if (line.startsWith("LIST")) { // LIST
					sendMessageToClient("+OK " + numberOfMailMessages() + " messages (" + getTotalMailSizeInOctets() + " octets)\r\n");

					for (int i = 0; i < fileArray.size(); i++) {
						sendMessageToClient(i+1 + " " + fileSizeInOctets(fileArray.get(i)) + "\r\n");
					}
					sendMessageToClient(".");
				} 
				else if (line.startsWith("RETR")) { // RETR
					for (int i = 0; i < fileArray.size(); i++) {
						sendMessageToClient(i+1 + " " + fileSizeInOctets(fileArray.get(i)) + "\r\n");
						sendMessageToClient(getOneMailMessage(fileArray.get(i)));
					}
				} 
				else if (line.startsWith("DELE")) { // DELE
					sendMessageToClient("+OK message 1 deleted\r\n");
				} 
				else if (line.startsWith("NOOP")) { // NOOP
					sendMessageToClient("+OK\r\n");
				} 
				else if (line.startsWith("REST")) { // REST
					sendMessageToClient("+OK maildrop has " + numberOfMailMessages() + " messages (" + getTotalMailSizeInOctets() + " octets)\r\n");
				} 
				else {
					sendMessageToClient("-ERR\r\n");
				}

			} else if (state == ServerState.Update) {

			}

		}
	}

	public String getOneMailMessage(File file) {
		StringBuffer buffer = new StringBuffer(1000);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file.getName()));
			char[] buf = new char[1024];
			int numRead=0;
			while((numRead=reader.read(buf)) != -1){
				String readData = String.valueOf(buf, 0, numRead);
				buffer.append(readData);
				buf = new char[1024];
			}
			reader.close();
		} catch ( IOException e) {
			return "-ERR\r\n";
		}
		return buffer.toString();
	}

	public int fileSizeInOctets(File file) {
		return (int) file.length();
	}

	public int numberOfMailMessages() {
		return fileArray.size();
	}

	public int getTotalMailSizeInOctets() {
		int total = 0;

		for (int i = 0; i < fileArray.size(); i++) {
			File f = fileArray.get(i);
			total += f.length();
		}

		return total;
	}

	public void output(String output) {
		System.out.println(output);
	}

	public void sendMessageToClient(String message) {
		try {
			output.write(message + "\r\n");
			output.flush();

			if(client != null) {
				output(client.getRemoteSocketAddress() + " < " + message);
			}
		} catch (IOException e) {
			if(client != null) {
				StackTraceElement[] element = e.getStackTrace();
				output(client.getRemoteSocketAddress() + " IOException writing to client -> " + element[0]);
			}
			reset();
		}
	}

	private void reset() {
		try {
			if(client != null) {
				client.close();
				client = null;
			}
		} catch(IOException e) {
			if(client != null) {
				StackTraceElement[] element = e.getStackTrace();
				output( client.getRemoteSocketAddress() + " Exception while closing. \n" + element[0]);
				client = null;
			}
		}
	}

	//	public boolean doLogin(HashMap<String, String> users, String user, String password) {
	//		if (password.equalsIgnoreCase(users.get(user))) {
	//			sendMessageToClient("+OK valid username now send PASS");
	//			sendMessageToClient("+OK your pass is fine!");
	//			return true;
	//		} else {
	//			return false;
	//		}
	//	}

}
