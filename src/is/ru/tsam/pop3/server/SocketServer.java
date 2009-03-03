/**
 * 
 */
package is.ru.tsam.pop3.server;

/**
 * @author gunnl
 *
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer extends Thread {

	/*
	 * Our server starts here
	 */
	public static void main(String[] argv) throws IOException {
		new SocketServer().start();
	}

	/*
	 * Time out
	 */
	final static int TIME_OUT = 1;

	/*
	 * Port to listen on
	 */
	public final static int TCP_PORT = 110;

	/*
	 * server socket
	 */
	private ServerSocket ss;

	/*
	 * Constructor
	 */
	public SocketServer() throws IOException {
		ss = new ServerSocket(TCP_PORT);
	}

	/*
	 * Run method from Thread
	 */
	public void run() {
		while (true) {
			try {
				System.out.println(SocketServer.class.getSimpleName() + " waiting for connection on TCP port " + TCP_PORT);
				Socket s = ss.accept();
				BufferedReader is = new BufferedReader(new InputStreamReader(s.getInputStream()));
				String name = is.readLine();
				String passwd = is.readLine();
				String domain = is.readLine();
				PrintWriter pout = new PrintWriter(s.getOutputStream(), true);
				pout.println("Welcome to " + domain + ", " + name);
			} catch (IOException e) {
				System.err.println("Doh! " + e);
			}
		}
	}
}
