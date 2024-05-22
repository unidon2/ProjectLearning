import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class Connecter {
	private int number;
	private PrintWriter out;
	private Receiver receiver;
	Socket socket;
	
	Connecter(int n){
		number = n;
		connectServer("localhost",10000);
	}
	
	public void connectServer(String ipAddress, int port) {
		try {
			socket = new Socket(ipAddress, port);
			out = new PrintWriter(socket.getOutputStream(), true);
			receiver = new Receiver(socket);
			receiver.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String msg) {
		out.println(msg);
		out.flush();
		System.out.println(number + "人目の接続者が " + msg + " を送信しました");
	}
	
	public void disconnect() {
		try {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class Receiver extends Thread {
		private InputStreamReader sisr;
		private BufferedReader br;

		Receiver (Socket socket){
			try{
				sisr = new InputStreamReader(socket.getInputStream());
				br = new BufferedReader(sisr);
			} catch (IOException e) {
			}
		}
		
		public void run(){
			try{
				while(true) {
					String inputLine = br.readLine();
					if (inputLine != null) {
						System.out.println(number + "人目の接続者が " + inputLine + " を受信しました");
					}
				}
			} catch (IOException e){
			}
		}
	}
}

public class ServerDriver extends Thread{
	Server server;
	
	public void run() {
		server = new Server(10000);
		server.acceptClient();
	}
	
	public void check() throws InterruptedException{
		System.out.println("サーバ―に1人目の接続を行います");
		Connecter c1 = new Connecter(1);
		Thread.sleep(1000);
		System.out.println("サーバ―に2人目の接続を行います");
		Connecter c2 = new Connecter(2);
		Thread.sleep(1000);
		System.out.println("サーバ―に3人目の接続を行います");
		Connecter c3 = new Connecter(3);
		Thread.sleep(1000);
		System.out.println("対戦中の1,2人目間でデータのやり取りを行えることを確認します");
		c1.sendMessage("from 1 to 2");
		c2.sendMessage("from 2 to 1");
		Thread.sleep(1000);
		System.out.println();
		System.out.println("対戦中の1,2人目間の試合を終了させます");
		c1.sendMessage("+");
		c2.sendMessage("-");
		Thread.sleep(100);
		server.printStatus();
		Thread.sleep(1000);
		System.out.println("1人目の接続を切ります");
		c1.sendMessage("e");
		Thread.sleep(100);
		server.printStatus();
		Thread.sleep(1000);
		System.out.println("2人目を再度マッチングさせます");
		c2.sendMessage("r");
		Thread.sleep(100);
		server.printStatus();
		Thread.sleep(1000);
		System.out.println("対戦中の2,3人目間の試合を終了させます");
		c2.sendMessage("+");
		c3.sendMessage("-");
		Thread.sleep(100);
		server.printStatus();
		Thread.sleep(1000);
		System.out.println("3人目の接続を切ります");
		c3.sendMessage("e");
		Thread.sleep(100);
		server.printStatus();
		Thread.sleep(1000);
		System.out.println("2人目の接続を切ります");
		c2.sendMessage("e");
		Thread.sleep(100);
		server.printStatus();
		System.exit(0);
	}
	
	public static void main(String[] args) {
		ServerDriver sd = new ServerDriver();
		sd.start();
		try {
			sd.check();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
