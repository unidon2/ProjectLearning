//パッケージのインポート
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.Timer;

public class ClientSample4 extends JFrame implements MouseListener,ActionListener {
	private JButton buttonArray[];//オセロ盤用のボタン配列
	private Container c; // コンテナ
	private ImageIcon blackIcon, whiteIcon, boardIcon; //アイコン
	private PrintWriter out;//データ送信用オブジェクト
	private Receiver receiver; //データ受信用オブジェクト
	private OthelloSample1 game; //Othelloオブジェクト
	private PlayerSample1 player; //Playerオブジェクト
	private JLabel playername, color ,turn; //プレイヤ名、色、ターン
	private JLabel restTimenum,restTimetext; // 残り時間ラベル
	private JLabel giveupLabel,guideLabel; //　ガイドラベル
	private JButton giveup1,giveup2;
	private JRadioButton help,nohelp;
	private Timer timer;

	// コンストラクタ
	public ClientSample4(OthelloSample1 game, PlayerSample1 player) { //OthelloオブジェクトとPlayerオブジェクトを引数とする
		this.game = game; //引数のOthelloオブジェクトを渡す
		this.player = player; //引数のPlayerオブジェクトを渡す
		String [] grids = game.getGrids(); //getGridメソッドにより局面情報を取得
		int row = game.getRow(); //getRowメソッドによりオセロ盤の縦横マスの数を取得
		//ウィンドウ設定
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
		setTitle("ネットワーク対戦型オセロゲーム");//ウィンドウのタイトル
		setSize(row * 45 + 10, row * 45 + 200);//ウィンドウのサイズを設定
		c = getContentPane();//フレームのペインを取得
		//アイコン設定(画像ファイルをアイコンとして使う)
		whiteIcon = new ImageIcon("White.jpg");
		blackIcon = new ImageIcon("Black.jpg");
		boardIcon = new ImageIcon("GreenFrame.jpeg");
		c.setLayout(null);//
		//オセロ盤の生成
		buttonArray = new JButton[row * row];//ボタンの配列を作成
		for(int i = 0 ; i < row * row ; i++){
			if(grids[i].equals("black")){ buttonArray[i] = new JButton(blackIcon);}//盤面状態に応じたアイコンを設定
			if(grids[i].equals("white")){ buttonArray[i] = new JButton(whiteIcon);}//盤面状態に応じたアイコンを設定
			if(grids[i].equals("board")){ buttonArray[i] = new JButton(boardIcon);}//盤面状態に応じたアイコンを設定
			c.add(buttonArray[i]);//ボタンの配列をペインに貼り付け
			// ボタンを配置する
			int x = (i % row) * 45;
			int y = (int) (i / row) * 45;
			buttonArray[i].setBounds(x, y, 45, 45);//ボタンの大きさと位置を設定する．
			buttonArray[i].addMouseListener(this);//マウス操作を認識できるようにする
			buttonArray[i].setActionCommand(Integer.toString(i));//ボタンを識別するための名前(番号)を付加する
		}
			playername = new JLabel("お名前 : "+player.getName());//名前ラベルを作成
			c.add(playername); //名前ラベルをペインに貼り付け
			playername.setBounds(0, row * 45 + 10, (row * 45 + 10) / 3, 30);//名前ラベルの境界を設定
			//色ラベル
			color = new JLabel("あなたの色 : "+player.getColor());//作成
			c.add(color); //コンテナに貼り付け
			color.setBounds((row * 45 + 10) / 3, row * 45 + 10, (row * 45 + 10 ) / 3, 30);//境界を設定
			turn = new JLabel(game.getTurn()+"のターン");//ターンラベルを作成
			c.add(turn); //貼り付け
			turn.setBounds((row * 45 + 10) * 2 / 3, row * 45 + 10, (row * 45 + 10 ) / 3, 30);//境界を設定
			//残り時間を表示するラベル
			restTimetext = new JLabel("残り時間 : ");
			restTimetext.setBounds(0, row * 45 + 40 , (row * 45 + 10)/5,30);
			c.add(restTimetext);
			//以下は1秒に値を1だけ減らすタイマーを定義している。
			int msec = 1000;
			ActionListener al = new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					restTimenum.setText(String.valueOf(Integer.valueOf(restTimenum.getText())-1));
					if(restTimenum.getText().equals("-1"))
					//残り時間が0になったら時間切れ。
						{
						restTimenum.setText("0");
						//stopperはタイマーをストップするためのメソッド。
						stopper();
						System.out.println("時間切れ");
					}
				}
			};
			timer = new Timer(msec , al);
			timer.start();
			restTimenum = new JLabel("5");//残り時間を表示するためのラベルを作成
			restTimenum.setBounds((10 + row * 45)/ 5, row * 45 + 40 , (row * 45 + 10) / 5, 30);//境界を設定
			c.add(restTimenum);//残り時間をペインに貼り付け
			guideLabel = new JLabel("|ガイド機能");
			guideLabel.setBounds((row * 45 + 10)*4/10, row * 45 + 40, (row * 45 + 10)/5,30);
			c.add(guideLabel);
			//ガイドを使うか否かのラジオボタン
			help = new JRadioButton("ON",false); 
			nohelp = new JRadioButton("OFF",true);
			ButtonGroup group = new ButtonGroup();
			group.add(help);
			group.add(nohelp);
			help.setBounds((10 + row * 45)*6/10,row * 45 + 40,(10 + row * 45)*3/20,30);
			nohelp.setBounds((row * 45 + 10)*15 /20,row * 45 + 40 , (10 + row * 45)*3/20,30);
			help.addActionListener(this);
			nohelp.addActionListener(this);
			c.add(help);
			c.add(nohelp);
			giveup1 = new JButton("投了");
			giveup1.setBounds((10 + row * 45)*3/ 8, row * 45 + 100, (10 + row * 45)/ 4,30);
			c.add(giveup1);
			giveup1.addMouseListener(this);
			giveup1.setActionCommand("giveup1");
	}

	// メソッド
	public void connectServer(String ipAddress, int port){	// サーバに接続
		Socket socket = null;
		try {
			socket = new Socket(ipAddress, port); //サーバ(ipAddress, port)に接続
			out = new PrintWriter(socket.getOutputStream(), true); //データ送信用オブジェクトの用意
			receiver = new Receiver(socket); //受信用オブジェクトの準備
			receiver.start();//受信用オブジェクト(スレッド)起動
		} catch (UnknownHostException e) {
			getError("ホストのIPアドレスが判定できません: " + e);
			System.exit(-1);
		} catch (IOException e) {
			getError("サーバ接続時にエラーが発生しました: " + e);
			System.exit(-1);
		}
	}

	public void sendMessage(String msg){	// サーバに操作情報を送信
		out.println(msg);//送信データをバッファに書き出す
		out.flush();//送信データを送る
		System.out.println("サーバにメッセージ " + msg + " を送信しました"); //テスト標準出力
	}
	public void sentData(OthelloSample1 game) {
		out.println(game);
		out.flush();
	}
	// データ受信用スレッド(内部クラス)
	class Receiver extends Thread {
		private InputStreamReader sisr; //受信データ用文字ストリーム
		private BufferedReader br; //文字ストリーム用のバッファ

		// 内部クラスReceiverのコンストラクタ
		Receiver (Socket socket){
			try{
				sisr = new InputStreamReader(socket.getInputStream()); //受信したバイトデータを文字ストリームに
				br = new BufferedReader(sisr);//文字ストリームをバッファリングする
			} catch (IOException e) {
				getError("データ受信時にエラーが発生しました: " + e);
			}
		}
		// 内部クラス Receiverのメソッド
		public void run(){
			try{
				while(true) {//データを受信し続ける
					String inputLine = br.readLine();//受信データを一行分読み込む
					if (inputLine != null){//データを受信したら
						receiveMessage(inputLine);//データ受信用メソッドを呼び出す
					}
				}
			} catch (IOException e){
				getError("データ受信時にエラーが発生しました: " + e);
			}
		}
	}

	public void receiveMessage(String msg){	// メッセージの受信
		System.out.println("サーバからメッセージ " + msg + " を受信しました"); //テスト用標準出力
	}
	public void updateDisp(String place){	// 画面を更新する
		c.setVisible(false);
		if(game.puttable(Integer.valueOf(place),game.getTurn())==(false)) {
			return;
		}
		String str[] = game.putStone(Integer.valueOf(place),game.getTurn());
		if(str[Integer.valueOf(place)]=="board") {
			
		}
		else {
			for(int i=0;i<64;i++) {
				System.out.print(str[i]);
				if((i+1)%8==0) {
					System.out.println();
				}
				buttonArray[i].setVisible(false);
				if(str[i]=="board") {
					buttonArray[i]=new JButton(boardIcon);
				}
				if(str[i]=="black") {
					buttonArray[i]=new JButton(blackIcon);
				}
				else if(str[i]=="white") {
					buttonArray[i]=new JButton(whiteIcon);
				}
				c.add(buttonArray[i]);
				c.setVisible(true);
				int x = (i % 8) * 45;
				int y = (int) (i / 8) * 45;
				buttonArray[i].setBounds(x, y, 45, 45);//ボタンの大きさと位置を設定する．
				buttonArray[i].addMouseListener(this);//マウス操作を認識できるようにする
				buttonArray[i].setActionCommand(String.valueOf(i));//ボタンを識別するための名前(番号)を付加する
				game.changeTurn();
			}
		}
	}
	public void acceptOperation(String command,Container c){	// プレイヤの操作を受付
		if(game.getTurn()!=player.getColor()) {
			c.setVisible(false);
		}
		else
			c.setVisible(true);
	}

	public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
	public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
	public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
	public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	public void stopper() {
		timer.stop();
	}
	
	//エラーメッセージを出力するメソッド
	public void getError(String message) {
		Container cont = new Container();
		JLabel jl = new JLabel();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
		setTitle("エラー");//ウィンドウのタイトル
		setSize(8 * 45 + 10, 8 * 45 + 200);//ウィンドウのサイズを設定
		jl = new JLabel(message);
		jl.setBounds(0,200,370,140);
		jl.setFont(new Font("Century",Font.BOLD,20));
		jl.setHorizontalAlignment(JLabel.CENTER);
		cont = getContentPane();
		cont.add(jl);
	}
	
	//結果を取得して出力するメソッド
	//結果をサーバから受け取るためのものはまた別に作成予定
	public void getResult(String judge) {
		Container cont = new Container();
		JLabel jl = new JLabel();
		//judgeは勝敗判定
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
		setTitle("勝敗判定");//ウィンドウのタイトル
		setSize(8 * 45 + 10, 8 * 45 + 200);//ウィンドウのサイズを設定
		jl = new JLabel(judge);
		jl.setBounds(0,200,370,140);
		jl.setFont(new Font("Century",Font.BOLD,60));
		jl.setHorizontalAlignment(JLabel.CENTER);
		cont = getContentPane();
		cont.add(jl);
	}
  	//マウスクリック時の処理
	public void mouseClicked(MouseEvent e) {
		JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
		String command = theButton.getActionCommand();//ボタンの名前を取り出す
		if(command.equals("giveup1")) {
			System.out.println("giveup");
			}
		//置けるマスにクリックをした想定。
		//この際にタイマーの値を取得する。
		else {
				System.out.println("マウスがクリックされました。押されたボタンは " + command + "です。");//テスト用に標準出力
				updateDisp(command);
				System.out.println(restTimenum.getText());
				}
		}
	//ガイド機能のコマンド
	public void actionPerformed(ActionEvent e) {
		String str = e.getActionCommand();
		if(str.equals("ON")) {
			System.out.println("ガイド機能ON");
		}
		else if(str.equals("OFF")) {
			System.out.println("ガイド機能OFF");
		}
	}
	//テスト用のmain
	public static void main(String args[]){
		//ログイン処理
		String myName = JOptionPane.showInputDialog(null,"名前を入力してください","名前の入力",JOptionPane.QUESTION_MESSAGE);
		if(myName.equals("")){
			myName = "No name";//名前がないときは，"No name"とする
		}
		PlayerSample1 player = new PlayerSample1(); //プレイヤオブジェクトの用意(ログイン)
		player.setName(myName); //名前を受付
		OthelloSample1 game = new OthelloSample1(); //オセロオブジェクトを用意
		ClientSample4 oclient = new ClientSample4(game, player); //引数としてオセロオブジェクトを渡す
		oclient.setVisible(true);
		oclient.connectServer("localhost", 10000);
	}
}