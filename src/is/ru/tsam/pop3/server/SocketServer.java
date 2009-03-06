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

interface ServerState {
	int Authorize = 1;
	int Transaction = 2;
	int Update = 3;
	int Quit = 4;
	int Reciving = 5;
}

public class SocketServer extends Thread {

	private static int state;

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
			reset();
		}
		reset();
		//}

	}

	public void work() throws IOException {
		while (true) {

			if (state == ServerState.Authorize) {
				sendMessageToClient("+OK valid username now send PASS");
				sendMessageToClient("+OK your pass is fine!");
				sendMessageToClient("+OK maildrop locked and ready");
				state = ServerState.Transaction;
			} else if (state == ServerState.Transaction) {

				String line = input.readLine();
				System.out.println(line);

				if (line.startsWith("STAT")) { // STAT
					sendMessageToClient("+OK 2 320");
				} else if (line.startsWith("LIST")) { // LIST
					sendMessageToClient("+OK 2 messages (320 octets)");
					sendMessageToClient("1 120");
					sendMessageToClient("2 200");
				} else if (line.startsWith("RETR")) { // RETR
					sendMessageToClient("+OK 120 octets");
					sendMessageToClient("+OK 200 octets");
				} else if (line.startsWith("DELE")) { // DELE
					sendMessageToClient("+OK message 1 deleted");
				} else if (line.startsWith("NOOP")) { // NOOP
					sendMessageToClient("+OK");
				} else if (line.startsWith("REST")) { // REST
					sendMessageToClient("+OK maildrop has 2 messages (320 octets)");
				} else if (line.startsWith("AUTH")) {
					sendMessageToClient("-ERR");
				} else if (line.startsWith("CAPA")) {
					sendMessageToClient("-ERR");
				}

			} else if (state == ServerState.Update) {

			}

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
