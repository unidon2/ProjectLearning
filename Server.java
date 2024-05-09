/* 最終更新 5/9/14:07 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Server{
 private int port; // サーバの待ち受けポート
 private int mx = 100;
 private int mnow = 0;
 private int pnow = 0;
 private boolean [] online; //オンライン状態管理用配列
 private int [] status;
 private int [] opp;
 private PrintWriter [] out; //データ送信用オブジェクト
 private Receiver [] receiver; //データ受信用オブジェクト

 //コンストラクタ
 public Server(int port) { //待ち受けポートを引数とする
  this.port = port; //待ち受けポートを渡す
  out = new PrintWriter [mx]; //データ送信用オブジェクトを2クライアント分用意
  receiver = new Receiver [mx]; //データ受信用オブジェクトを2クライアント分用意
  online = new boolean[mx]; //オンライン状態管理用配列を用意
  status = new int[mx];
  opp = new int[mx];
  Arrays.fill(opp,-1);
  
 }

 // データ受信用スレッド(内部クラス)
 class Receiver extends Thread {
  private InputStreamReader sisr; //受信データ用文字ストリーム
  private BufferedReader br; //文字ストリーム用のバッファ
  private int playerNo; //プレイヤを識別するための番号

  // 内部クラスReceiverのコンストラクタ
  Receiver (Socket socket, int playerNo){
   try{
    this.playerNo = playerNo; //プレイヤ番号を渡す
    sisr = new InputStreamReader(socket.getInputStream());
    br = new BufferedReader(sisr);
   } catch (IOException e) {
    System.err.println("データ受信時にエラーが発生しました: " + e);
   }
  }
  // 内部クラス Receiverのメソッド
  public void run(){
   try{
    while(true) {// データを受信し続ける
     String inputLine = br.readLine();//データを一行分読み込む
     System.out.println(playerNo + "から" + inputLine + "を受信しました");
     if (inputLine != null){ //データを受信したら
      forwardMessage(inputLine, playerNo); //もう一方に転送する
     }
    }
   } catch (Exception e){ // 接続が切れたとき
    System.out.println("プレイヤ " + playerNo + "との接続が切れました．");
    if (opp[playerNo] >= 0) {
    	forwardMessage("xxx", playerNo); //もう片方にエラーを伝える
    	keep(opp[playerNo]);
    }
    init(playerNo);
    printStatus(); //接続状態を出力する
   }
  }
 }

 // メソッド

 public void acceptClient(){ //クライアントの接続(サーバの起動)
  try {
   System.out.println("サーバが起動しました．");
   ServerSocket ss = new ServerSocket(port); //サーバソケットを用意
   while (pnow < mx) {
    Socket socket = ss.accept(); //新規接続を受け付ける
    pnow++;
    for (int i = 0; i < mx; i++) {
    	if (online[i] == false) {
    		online[i] = true;
    		receiver[i] = new Receiver(socket,i);
    		receiver[i].start();
    		out[i] = new PrintWriter(socket.getOutputStream());
    		System.out.println(i);
    		matching(i);
    		printStatus(); //接続状態を出力する
    		break;
    	}
    }
   }
  } catch (Exception e) {
   System.err.println("ソケット作成時にエラーが発生しました: " + e);
  }
 }
 
 public void init(int a) {
	 status[a] = 0;
	 opp[a] = -1;
	 online[a] = false;
	 pnow--;
 }
 
 public void keep(int a) {
	 status[a] = -mx;
	 opp[a] = -1;
 }
 
 public void matching(int a) {
	 for (int i = 0; i < mx; i++) {
		 if (status[i] == mx) {
			 for (int j = 1; j < mx/2 + 1; j++) {
				 for (int k = 0; k < mx; k++) {
					 if (status[k] == j) {
						 break;
					 }
					 if (k == mx-1) {
						 status[i] = j;
						 status[a] = -j;
						 opp[i] = a;
						 opp[a] = i;
						 mnow++;
						 sendColor(i,a);
						 return;
					 }
				 }
			 }
		 }
	 }
	 status[a] = mx;
	 return;
 }

 public void printStatus(){ //クライアント接続状態の確認
	 System.out.println("現在のオンライン状況:");
	 System.out.println("ID :  状態");
	 for (int i = 0; i < mx; i++) {
		 if (online[i]) {
			 System.out.print(i + " : ");
			 if (status[i] == mx) {
				 System.out.println("マッチング中");
			 }
			 else if (status[i] == -mx) {
				 System.out.println("対局終了");
			 }
			 else {
				 System.out.println("対局中");
			 }
		 }
	 }
	 System.out.println();
	 System.out.println("対局中のマッチ: " + mnow);
	 for (int i = 1; i < mx/2 + 1; i++) {
		 for (int j = 0; j < mx; j++) {
			 if (status[j] == i) {
				 System.out.print("Match " + i + " : " + j);
			 }
		 }
		 for (int j = 0; j < mx; j++) {
			 if (status[j] == -i) {
				 System.out.println(" vs " + j);
			 }
		 }
	 }
	 
 }

 public void sendColor(int bl, int wt){ //先手後手情報(白黒)の送信
	  out[bl].println("b");
	  out[bl].flush();
	  out[wt].println("w");
	  out[wt].flush();
 }

 public void forwardMessage(String msg, int playerNo){ //操作情報の転送
	 if (msg.charAt(0) == '-') {
		 keep(playerNo);
		 return;
	 }
	 if (msg.charAt(0) == '+') {
		 keep(playerNo);
		 mnow--;
		 return;
	 }
	 if (msg.charAt(0) == 'r') {
		 matching(playerNo);
		 return;
	 }
	 if (msg.charAt(0) == 'e') {
		 init(playerNo);
		 return;
	 }
	 if (opp[playerNo] >= 0) {
		 out[opp[playerNo]].println(msg);
		 out[opp[playerNo]].flush();
	 }
 }
 

 public static void main(String[] args){ //main
  Server server = new Server(10000); //待ち受けポート10000番でサーバオブジェクトを準備
  server.acceptClient(); //クライアント受け入れを開始
 }
}