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

public class SocketServer extends Thread {

	private static BufferedReader input;

	private static BufferedWriter output;

	private static Socket client = null;

	/* Time out */
	final static int TIME_OUT = 1;

	/* Port to listen on */
	public final static int TCP_PORT = 110;

	/* server socket */
	private ServerSocket ss;

	/* Constructor */
	public SocketServer() throws IOException {
		ss = new ServerSocket(TCP_PORT);
	}

	/* Our server starts here */
	public static void main(String[] argv) throws IOException {
		new SocketServer().start();
	}

	/* Run method from Thread */
	public void run() {
		while (true) {
			try {
				System.out.println(SocketServer.class.getSimpleName() + " waiting for connection on TCP port " + TCP_PORT);
				client = ss.accept();

				input = new BufferedReader(new InputStreamReader(client.getInputStream()));
				output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

			} catch (IOException e) {
				System.err.println("Doh! " + e);
			}

			sendClientString("+OK Welcome to a POP3Server in java.");
		}
	}

	public void output(String output) {
		System.out.println(output);
	}

	public void sendClientString(String theString) {
		try {
			output.write(theString + "\r\n");
			output.flush();

			if(client!=null) {
				output(client.getRemoteSocketAddress() + " < " + theString);
			}
		} catch (IOException e) {
			if(client!=null) {
				StackTraceElement[] element = e.getStackTrace();
				output( client.getRemoteSocketAddress() + " IOException writing to client -> " + element[0]);
			}
			//reset it all after the error
			resetConnection();
		}
	}

	private void resetConnection() {
		//close up
		try {
			if(client!=null) {
				client.close();
				client=null;
			}
		} catch(IOException e) {
			if(client!=null) {
				StackTraceElement[] element = e.getStackTrace();
				output( client.getRemoteSocketAddress() + " Exception while closing. \n" + element[0]);
				client=null;
			}
		}
	}
}
