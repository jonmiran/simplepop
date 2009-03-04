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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class SocketServer extends Thread {

	private static String state;

	private static BufferedReader input;

	private static BufferedWriter output;

	private static Socket client = null;

	private static HashMap<String, String> users = null;

	/* Time out */
	final static int TIME_OUT = 1;

	/* Port to listen on */
	public final static int TCP_PORT = 110;

	/* server socket */
	private ServerSocket ss;

	/* Constructor */
	public SocketServer() throws IOException {
		ss = new ServerSocket(TCP_PORT);
		users = new HashMap<String, String>();
		users.put("gunni", "1234");
		users.put("svenni", "1234");
		users.put("palli", "1234");		
	}

	/* Our server starts here */
	public static void main(String[] argv) throws IOException {
		new SocketServer().start();
	}

	/* Run method from Thread */
	public void run() {
		System.out.println(SocketServer.class.getSimpleName() + " waiting for connection on TCP port " + TCP_PORT);
		while (true) {
			try {
				
				client = ss.accept();

				input = new BufferedReader(new InputStreamReader(client.getInputStream()));
				output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

			} catch (IOException e) {
				System.err.println("Doh! " + e);
			}

			sendMessageToClient("+OK Welcome to a " + SocketServer.class.getSimpleName() + " in java.");

			state = "authorization";


			sendMessageToClient("+OK valid username now send PASS");
			sendMessageToClient("+OK your pass is fine!");
		}
	}

	public void work() {
		while (true) {

		}
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
			resetConnection();
		}
	}

	private void resetConnection() {
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

	public boolean doLogin(HashMap<String, String> users, String user, String password) {
		if (password.equalsIgnoreCase(users.get(user))) {
			sendMessageToClient("+OK valid username now send PASS");
			sendMessageToClient("+OK your pass is fine!");
			return true;
		} else {
			return false;
		}
	}

}
