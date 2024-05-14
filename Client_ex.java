//パッケージのインポート
import java.awt.Color;
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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

public class Client extends JFrame implements MouseListener,ActionListener {
	private JButton buttonArray[];//オセロ盤用のボタン配列
	private Container c; // コンテナ
	private ImageIcon blackIcon, whiteIcon, boardIcon,yboardIcon; //アイコン
	private PrintWriter out;//データ送信用オブジェクト
	private Receiver receiver; //データ受信用オブジェクト
	private Othello game; //Othelloオブジェクト
	private Player player; //Playerオブジェクト
	private JLabel playername, color= new JLabel("") ,turn = new JLabel(""); //プレイヤ名、色、ターン
	private JLabel restTimenum,restTimetext,oprestTimetext,oprestTimenum; // 残り時間ラベル
	private JLabel giveupLabel,guideLabel,score; //　ガイドラベル
	private JLabel jld, jld2;
	private JButton giveup1,giveup2;
	private JRadioButton help,nohelp;
	private Timer timer;
	private static boolean match;
	private static boolean assist;
	private int time;
	private int optime;
	public int section = 3;
	private int jrr = 5;
	private int notouch;
	private int nott;
	static int decided = 0;
	static boolean passing = false;
	static boolean RE = false;
	
	private JLabel input_restTimetext, input_restTimenum;
	private JLabel access_restTimetext, access_restTimenum;
	private JButton Ok;
	private JComboBox<String> cbmin, cbsec;
	private JLabel label1, label2, labelm, labels;//希望時間画面表示のラベル
	private boolean flag_cb = false; //希望時間入力時に使われるflag
	private int input_time; //先手から入力された制限時間(s)
	private JLabel desired_time; 
	private boolean selected = false; //希望時間が定まったとき真になる
	private Timer timer_input;	//先手用タイマー
	private Timer timer_access;	//後手用タイマー
	private Timer jrrr;
	private JButton Accept, NoAccept1, NoAccept2;
	private JLabel l1,l2,l3;
	// コンストラクタ
	public Client() { //引数なし
		player = new Player(); // プレイヤクラスの確保
		inputName(); // 名前入力
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //ウィンドウを閉じる際の動作設定
		setTitle("ネットワーク対戦型オセロゲーム");//ウィンドウのタイトル
		setSize(370,490); // ウィンドウサイズの設定
		setLocationRelativeTo(null); //ウィンドウを画面中央へ
		setVisible(true); //ウィンドウの可視化
		c = getContentPane(); //描画先を取得
		init(); //初期化
		connectServer("localhost",10000); //サーバー接続
		restarter reset = new restarter(); //再戦フラグ監視用のサブクラス定義
		reset.start(); // サブクラスの動作
		waitMatching(); //本プログラムの開始
	}
	
	public void inputName() { //名前の入力
		String myName = JOptionPane.showInputDialog(null,"名前を入力してください","名前の入力",JOptionPane.QUESTION_MESSAGE);
		if(myName.equals("")){
			myName = "No name";//名前がないときは，"No name"とする
		}
		player.setName(myName);
	}
	
	public void init() { //初期化
		game = new Othello(); //オセロクラスの取得
		buttonArray = new JButton[8 * 8]; //本来はPlayingメソッドで良いのですが、回線切断によってupdateDisp("800")が呼び出された際に未定義でエラーを吐く（動作には影響しませんが）のが嫌だったので移してあります
		flag_cb = false;
		selected = false;
		section = 3;
		decided = 0;
		match = false;
		assist = false;
		time = 10;
		optime = 10;
		jrr = 5;
		notouch = 0;
		passing = false;
		RE = false;
		c.setVisible(false);
		c.removeAll();
		c.setVisible(true);
		System.out.println("aaa");
	}
	
	public void waiting(int n) { //Timerを用いて引数の秒数だけ待つメソッドです Thread.sleep(n*1000)と等価ですが、sleepだと画面遷移がうまくいかない場所があったりした場合に使用する目的で作成してあります
		JLabel time = new JLabel();
		int msec = 1000;
		ActionListener second = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				time.setText(String.valueOf(Integer.valueOf(time.getText())-1));
				if(time.getText().equals("-1"))
					//残り時間が0になったら時間切れ。
					{	
					return;
					}
			}
		};
		Timer timer = new Timer(msec , second);
		timer.start();
		time.setText(Integer.toString(n));
	}
	
	public void waitMatching() { //メインのプログラム　実際の画面遷移に沿って各メソッドが呼び出されていると捉えていいです
		RE = false; //init()でも初期化していますが、再戦の際は init() -> RE = true -> waitMatching()呼び出し の順なのでここで変えないと死にます
		matching(); //マッチング画面の表示
		while (true) {
			boolean m = match;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (m) { //マッチングしたら次へ
				break;
			}	
		}
		c.setVisible(false);
		c.removeAll(); //1度画面を初期化
		if (player.getColor() == 1) { //先手の場合
			getTime(); //先に時間設定
		}
		else {
			setTime(); //後手は承認から
		}
		if (RE) { //この段階で回線切断が発生した場合にwaitMatchingを終了させる
			return;
		}
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (player.getColor() == -1) { //さっきの逆
			getTime();
		}
		else {
			setTime();
		}
		if (RE) { //同様
			return;
		}
		
		Playing(); //対局画面の表示
		
		if (RE) {
			return;
		}
	}
	
	public void matching() {
		JLabel jlmat1 = new JLabel();
		JLabel jlmat2 = new JLabel();
		System.out.println("マッチング中");
		setTitle("マッチング中");
		c.setVisible(false);
		c.setVisible(true);
		
		jlmat1 = new JLabel("マッチング中");
		jlmat1.setBounds(0,90,370,30);
		jlmat1.setFont(new Font("SanSerif",Font.PLAIN,20));
		jlmat1.setHorizontalAlignment(JLabel.CENTER);
		
		jlmat2 = new JLabel("対局相手が見つかるまでお待ちください");
		jlmat2.setBounds(0,130,370,30);
		jlmat2.setFont(new Font("SanSerif",Font.PLAIN,15));
		jlmat2.setHorizontalAlignment(JLabel.CENTER);
		
		c.add(jlmat2);
		c.add(jlmat1);
		c.setVisible(false);
		c.setVisible(true);
		
	}
	
	public void getTime() {
		setLimitTime(player);
		while(true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(selected) {
				break;
			}
			if (RE) {
				return; //breakじゃなくてreturnなのは↓の処理を行って再度初期化しないといけなくなるのを防ぐためです
			}
		}
		decided = 0;
		System.out.println("exit");
		c.setVisible(false);
		c.removeAll();
		selected = false;
	}
	
	public void setTime() {
		accept(player);
		while(true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(decided !=0) {
				break;
			}
			if (RE) {
				return;
			}
		}
		selected = false;
		decided = 0;
		c.setVisible(false);
		c.removeAll();
		System.out.println("exit");
	}
	
	
		
	public void Playing() {
		game.start(); //Othelloクラスのturn = 1
		setSize(370,600);
		int row = 8;
		int[] grids = game.getGrid();
		setTitle("ネットワーク対戦型オセロゲーム");//ウィンドウのタイトル
		//アイコン設定(画像ファイルをアイコンとして使う)
		whiteIcon = new ImageIcon("White.jpg");
		blackIcon = new ImageIcon("Black.jpg");
		boardIcon = new ImageIcon("GreenFrame.jpeg");
		yboardIcon = new ImageIcon("YellowFrame.jpeg");
		//オセロ盤の生成
		buttonArray = new JButton[row * row];//ボタンの配列を作成
		for(int i = 0 ; i < row * row ; i++){
			if(grids[i] == 1){ buttonArray[i] = new JButton(blackIcon);}//盤面状態に応じたアイコンを設定
			else if(grids[i] == -1){ buttonArray[i] = new JButton(whiteIcon);}//盤面状態に応じたアイコンを設定
			else if(grids[i] == 0){ buttonArray[i] = new JButton(boardIcon);}//盤面状態に応じたアイコンを設定
			else if (grids[i] == player.getColor()*2 || grids[i] == 3) {
				if (player.getColor() == game.getTurn() && assist) buttonArray[i] = new JButton(yboardIcon);
				else buttonArray[i] = new JButton(boardIcon);
			}
			else {
				buttonArray[i] = new JButton(boardIcon);
			}
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
		color = new JLabel("あなたの色 : 黒");//作成
		if (player.getColor() == -1) {
			color.setText("あなたの色 : 白");
		}
		c.add(color); //コンテナに貼り付け
		color.setBounds((row * 45 + 10) / 3, row * 45 + 10, (row * 45 + 10 ) / 3, 30);//境界を設定
		turn = new JLabel("黒のターン");//ターンラベルを作成
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
				if (player.getColor() == game.getTurn()) {
					restTimenum.setText(String.valueOf(Integer.valueOf(restTimenum.getText())-1));
				}
				if (restTimenum.getText().equals("-1")) {
					restTimenum.setText(String.valueOf(time));
					stopper();
					System.out.println("時間切れ");
					sendMessage("ooo");
					//c.setVisible(false);
					game.check_end(-player.getColor()*3);
					endmsg(game.result());
					
				}
			}
		};
		timer = new Timer(msec , al);
		timer.start();
		restTimenum = new JLabel(String.valueOf(time));//残り時間を表示するためのラベルを作成
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
		oprestTimetext = new JLabel("相手の残り時間");
		oprestTimetext.setBounds(0,row * 45 + 70, (row * 45 + 10)/ 4, 30);
		c.add(oprestTimetext);
		oprestTimenum = new JLabel(String.valueOf(optime));
		oprestTimenum.setBounds((row * 45 + 10)/4,row * 45 + 70, (row * 45 + 10)/4,30);
		c.add(oprestTimenum);
		giveup1 = new JButton("投了");
		giveup1.setBounds((10 + row * 45)*3/ 8, row * 45 + 130, (10 + row * 45)/ 4,30);
		c.add(giveup1);
		giveup1.addMouseListener(this);
		giveup1.setActionCommand("giveup1");
		
		c.setVisible(true);
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
	
	class restarter extends Thread { //再戦フラグの監視を行う内部クラス
		public void run() {
			try {
				while (true) {
					boolean a = RE;
					Thread.sleep(1000);
					if (a) { //再戦フラグが立っていたら再戦するだけ
						waitMatching();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
		System.out.println(msg);
		if (msg.equals("b")) { //先手後手情報の受信
			player.setColor(1);
			color.setText("あなたの色 : 黒");
			match = true;
			return;
		}
		if (msg.equals("w")) { //先手後手情報の受信
			player.setColor(-1);
			color.setText("あなたの色 : 白");
			match = true;
			return;
		}
		
		if (msg.charAt(0) == '0') { // 手番情報の受信([1,2] = 座標, [3,5] = 残り時間)
			updateDisp(msg.substring(1,3));
			optime = Integer.valueOf(msg.substring(3));
			oprestTimenum.setText(String.valueOf(optime));
			if (game.check_end(0) == 1) {
				endmsg(game.result());
				return;
			}
			if (game.pass(game.getTurn()) == 1) {
				detect("You cannot put!!");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				c.setVisible(false);
				game.changeTurn();
				updateDisp("800");
			}
		}
		
		if (msg.equals("ooo")) { //相手の時間切れ
			game.check_end(player.getColor()*3);
			endmsg(game.result());
		}
		if(msg.equals("giveup")) { //相手の降参
			endmsg(555);
		}
		if (msg.equals("xxx")) { //相手の回線切断
			int played = game.count(1); //現在置かれているマス数の獲得
			if (played < 20) { // 20は適当に決めた値なので変更していいです
				endmsg(1000); // = no contest
			}
			else { // 枚数判定
				endmsg(game.count(0)*10000);
			}
		}
		else if(msg.charAt(0)=='A') {
			msg = msg.substring(1);
			if(msg.equals("Accept")) {
				time = input_time;
				//希望時間が最終的に決められたのでselectedをtrueに
				selected=true;
				//後に修正予定
				System.out.println("最終的な残り時間が決められた");
			}
			if((msg.equals("NoAccept1"))||(msg.equals("NoAccept2"))) {
				if(msg.equals("NoAccept1")) {
					label1.setText("時間を減らしてほしいです");
				}else {
					label1.setText("時間を増やしてほしいです");
				}
				
				cbsec.setEnabled(true);
				cbmin.setEnabled(true);
				Ok.setEnabled(true);
			
				//後手が非承認したら、先手用のタイマー更新および希望時間を再入力
				int msec = 1000;
				ActionListener al_input = new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						input_restTimenum.setText(String.valueOf(Integer.valueOf(input_restTimenum.getText())-1));
						if(input_restTimenum.getText().equals("-1"))
						//残り時間が0になったら時間切れ。画面上に表示されている時間をそのまま相手に送信
							{
							input_restTimenum.setText("0");
							//stopperはタイマーをストップするためのメソッド。
							stopper_input();
							if(Integer.parseInt((String)cbmin.getSelectedItem()) == 15) {
								input_time=Integer.parseInt((String)cbmin.getSelectedItem())*60;
							}else {
								input_time=Integer.parseInt((String)cbmin.getSelectedItem())*60 + Integer.parseInt((String)cbsec.getSelectedItem());
							}
						
							sendMessage("A"+Integer.toString(input_time)); //先手用の制限時間が切れたら、画面上の時間を希望時間として送信
							cbsec.setEnabled(false);
							cbmin.setEnabled(false);
							Ok.setEnabled(false);
							label1.setText("・・承認待機中・・");
						}
					}
				};
				timer_input = new Timer(msec , al_input);
				timer_input.start();
				input_restTimetext.setText("残り時間：");	
				input_restTimenum.setText("20");//残り時間を表示するためのラベルを作成
				input_restTimetext.setVisible(true);
				input_restTimenum.setVisible(true);
			}
			//(int型)希望時間(s)の受信の場合
			else{
				if(selected==false){
					desired_time.setText(str_desired_time(Integer.valueOf(msg)));
					optime = Integer.valueOf(msg);
					//以下は1秒に値を1だけ減らすタイマーを定義している。
					if(Integer.valueOf(l3.getText())<(1)) {
						Accept.setEnabled(true);
						//NoAccept1.setEnabled(false);
						//NoAccept2.setEnabled(false);
					}
					else {
						Accept.setEnabled(true);
						NoAccept1.setEnabled(true);
						NoAccept2.setEnabled(true);
					}
					int msec = 1000;	
					ActionListener al_access = new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							access_restTimenum.setText(String.valueOf(Integer.valueOf(access_restTimenum.getText())-1));
							if(access_restTimenum.getText().equals("-1"))
								//残り時間が0になったら時間切れ。
								{	
								access_restTimenum.setText("0");
								stopper_access();	
								System.out.println("時間切れ");
								sendMessage("AAccept");	//後手用の制限時間が切れたら承認とみなす
								decided = 1;
								}
						}
					};
					timer_access = new Timer(msec , al_access);
					timer_access.start();
					access_restTimenum.setText("10");	
					access_restTimetext.setVisible(true);
					access_restTimenum.setVisible(true);
				}
			}
		}
		
		
		System.out.println("サーバからメッセージ " + msg + " を受信しました"); //テスト用標準出力
	}
	public void detect(String str) {
		jld = new JLabel(str);
		if(str.equals("You cannot put!!")) {
			jld2 = new JLabel("Pass!!");
		}
		else {
			jld2 = new JLabel("Your turn!!");
		}

		jld.setBounds(10,10,350,50);
		jld2.setBounds(10,60,350,50);
		c.add(jld);
		c.add(jld2);
		passing = true;
		c.setVisible(true);
	}
	
	public int updateDisp(String place){	// 画面を更新する(800呼び出しは再描画)
		if(notouch==1) {
			for(int i=0;i<64;i++) {
				buttonArray[i].setVisible(false);
			}
		}
		else {
		c.setVisible(false);
		int pl = Integer.valueOf(place);
		if(game.put(game.getTurn(),pl/8,pl%8) == 1) {
			c.setVisible(true);
			return 1;
		}
		int grids[] = game.getGrid();
		int t = game.getTurn();
		
		if (t == 1) {
			turn.setText("黒のターン");
		}
		else {
			turn.setText("白のターン");
		}
		
		for(int i=0;i<64;i++) {
			System.out.printf("%2d",grids[i]);
			if((i+1)%8==0) {
				System.out.println();
			}
			buttonArray[i].setVisible(false);
			if(grids[i] == 1){ buttonArray[i] = new JButton(blackIcon);}//盤面状態に応じたアイコンを設定
			else if(grids[i] == -1){ buttonArray[i] = new JButton(whiteIcon);}//盤面状態に応じたアイコンを設定
			else if(grids[i] == 0){ buttonArray[i] = new JButton(boardIcon);}//盤面状態に応じたアイコンを設定
			else if (grids[i] == player.getColor()*2 || grids[i] == 3) {
				if (player.getColor() == t && assist) buttonArray[i] = new JButton(yboardIcon);
				else buttonArray[i] = new JButton(boardIcon);
			}
			else {
				buttonArray[i] = new JButton(boardIcon);
			}
			c.add(buttonArray[i]);
			c.setVisible(true);
			int x = (i % 8) * 45;
			int y = (int) (i / 8) * 45;
			buttonArray[i].setBounds(x, y, 45, 45);//ボタンの大きさと位置を設定する．
			buttonArray[i].addMouseListener(this);//マウス操作を認識できるようにする
			buttonArray[i].setActionCommand(String.valueOf(i));//ボタンを識別するための名前(番号)を付加する
		}
			
		if (place != "800") {
			timer.start();
			if (passing) {
				c.remove(jld);
				c.remove(jld2);
			}
		}
		}
		return 0;
	}
	public int count(int n) {
		int counter = 0;
		int board[] = game.getGrid();
		for(int i=0;i<64;i++) {
			if(board[i]==n) {
				counter++;
			}
		}
		return counter;
	}
	public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
	public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
	public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
	public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	public void stopper() {
		timer.stop();
	}
	public void stopper_input() {
		timer_input.stop();
	}
	public void stopper_access() {
		timer_access.stop();
	}
	public void rebattle() { //再戦画面の表示
		c.removeAll();
		JLabel jl = new JLabel();
		JButton jby = new JButton();
		JButton jbn = new JButton();
		setTitle("再戦");
		jl = new JLabel("再戦しますか?");
		jl.setHorizontalAlignment(JLabel.CENTER);
		jl.setBounds(0,50,370,50);
		jby = new JButton("Yes");
		jby.setBounds((10 + 8 * 45)/ 8,100, (10 + 8 * 45)/ 4,50);
		jby.addMouseListener(this);
		jby.setActionCommand("r");
		jbn = new JButton("No");
		jbn.setBounds((10 + 8 * 45)*5/ 8,100, (10 + 8 * 45)/ 4,50);
		jbn.addMouseListener(this);
		jbn.setActionCommand("e");
		c.add(jl);
		c.add(jby);
		c.add(jbn);
		c.setVisible(true);
	}
	//エラーメッセージを出力するメソッド
	public void getError(String message) {
		Container coer = new Container();
		JLabel jl = new JLabel();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
		setTitle("エラー");//ウィンドウのタイトル
		setSize(8 * 45 + 10, 8 * 45 + 200);//ウィンドウのサイズを設定
		jl = new JLabel(message);
		jl.setBounds(0,200,370,140);
		jl.setFont(new Font("SanSerif",Font.BOLD,20));
		jl.setHorizontalAlignment(JLabel.CENTER);
		coer = getContentPane();
		coer.add(jl);
	}
	
	//結果を取得して出力するメソッド
	public void getResult(String judge) {
		JLabel jl = new JLabel();
		JLabel jl2 = new JLabel();
		score = new JLabel();
		//judgeは勝敗判定
		setTitle("勝敗判定");//ウィンドウのタイトル
		if(judge == "Opponent surrendered. You win"||judge == "You surrendered. You lose"||judge =="No one can put Draw"
				||judge =="Disconnected" + "No contest"||judge =="Disconnected.../n Draw"||judge =="Time over You win"||judge == "Time over You lose") {
			score = new JLabel();
		}
		else {
			score = new JLabel(String.valueOf(count(player.getColor()))+ " vs " +String.valueOf(count(-player.getColor())));
		}
		score.setBounds(0,50,370,100);
		score.setFont(new Font("SanSerif",Font.BOLD,60));
		score.setHorizontalAlignment(JLabel.CENTER);
		if(judge == "Draw"||judge == "You win"||judge == "You lose") {
			jl = new JLabel();
			jl2 = new JLabel(judge);
		}
		else {
			if(judge == "Opponent surrendered. You win") {
				jl = new JLabel("Opponent surrendered");
				jl2 = new JLabel("You win");
			}
			else if(judge == "You surrendered. You lose") {
				jl = new JLabel("You surrendered");
				jl2 = new JLabel("You lose");
			}
			else if(judge == "Disconnected"+"No contest") {
				c.removeAll(); //時間設定時の回線切断などがここに含まれるので、ここだけ全て削除した後に配置するようにしています
				jl = new JLabel("Disconnected");
				jl2 = new JLabel("No contest");
			}
			else if(judge == "Disconnected You win") {
				jl = new JLabel("Disconnected");
				jl2 = new JLabel("You win");
			}
			else if(judge == "Disconnected You lose") {
				jl = new JLabel("Disconnected");
				jl2 = new JLabel("You lose");
			}
			else if(judge == "Disconnected Draw") {
				jl = new JLabel("Disconnected");
				jl2 = new JLabel("Draw");
			}
			else if(judge == "No one can put You win") {
				jl = new JLabel("No one can put");
				jl2 = new JLabel("You win");
			}
			else if(judge == "No one can put You lose") {
				jl = new JLabel("No one can put");
				jl2 = new JLabel("You lose");
			}
			else if(judge == "No one can put Draw") {
				jl = new JLabel("No one can put");
				jl2 = new JLabel("Draw");
			}
			else if(judge == "Time over You lose") {
				jl = new JLabel("Time over");
				jl2 = new JLabel("You lose");
			}
			else if(judge == "Time over You win") {
				jl = new JLabel("Time over");
				jl2 = new JLabel("You win");
			}
		}
		jl.setBounds(0,170,370,30);
		jl.setFont(new Font("SanSerif",Font.BOLD,30));
		jl.setHorizontalAlignment(JLabel.CENTER);
		jl2.setBounds(0,200,370,140);
		jl2.setFont(new Font("SanSerif",Font.BOLD,50));
		jl2.setHorizontalAlignment(JLabel.CENTER);
		c = getContentPane();
		c.add(score);
		c.add(jl);
		c.add(jl2);
		notouch = 1;
		int msec = 1000;
		ActionListener al = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				jrr--;
				if(jrr==-1) {
					c.setVisible(false);
					notouch =0;
					score.setText("");
					score.setVisible(false);
					jrrr_stopper();
					jrr = 0;
					rebattle();
				}
			}
		};
		jrrr = new Timer(msec , al);
		jrrr.start();
	}
	
	public void jrrr_stopper() {
		jrrr.stop();
	}
	
  	//マウスクリック時の処理
	public void mouseClicked(MouseEvent e) {
		if(notouch == 1) {
			
		}
		else {
		JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
		String command = theButton.getActionCommand();//ボタンの名前を取り出す
		if(command.equals("r")) { //再戦ボタンが押された場合
			init(); //初期化
			sendMessage(command); //サーバー側でマッチング待ちに入れてもらう
			RE = true; //フラグ変化
			return;
		}
		else if (command.equals("e")) { //終了ボタンが押された場合
			sendMessage(command); //サーバー側で初期化してもらう
			goodbye(); //終了画面の表示
		}
		else if(command.equals("giveup1")) { //降参ボタンを押したとき
			sendMessage("giveup"); //相手に降参を伝える
			endmsg(556);
			}
		//置けるマスにクリックをした想定。
		//この際にタイマーの値を取得する。
		else {
				System.out.println("マウスがクリックされました。押されたボタンは " + command + "です。");//テスト用に標準出力
				if (player.getColor() != game.getTurn()) {
					return;
				}
				if (updateDisp(command) == 0) {
					stopper();
					time = Integer.valueOf(restTimenum.getText());
					if (command.length() == 1) {
						sendMessage("00" + command + time);
					}
					else {
						sendMessage("0" + command + time);
					}
					System.out.println(time);
					if (game.check_end(0) == 1) {
						endmsg(game.result());
						return;
					}
					if (game.pass(game.getTurn()) == 1) {
						detect("opponent cannot put!!");
						try {
							Thread.sleep(10);
						} catch (InterruptedException e1) {
							// TODO 自動生成された catch ブロック
							e1.printStackTrace();
						}
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e1) {
							// TODO 自動生成された catch ブロック
							e1.printStackTrace();
						}
						c.setVisible(false);
						game.changeTurn();
						timer.start();
						updateDisp("800");
					}
				}
				System.out.println(restTimenum.getText());
				}
		}
		}
	
	public void endmsg(int n) {
		if (n >= 1000 || n <= -1000) {
			sendMessage("+");
		}
		else {
			if (player.getColor() == 1) {
				sendMessage("+");
			}
			else {
				sendMessage("-");
			}
		}
		String s;
		if (n == 100) {
			s = "Draw";
		}
		else if(n == 555) {
			s = "Opponent surrendered. You win";
			restTimenum.setText(String.valueOf(time));
		}
		else if(n == 556) {
			s = "You surrendered. You lose";
		}
		else if (n == player.getColor()) {
			s = "You win";
		}
		else if (n == -player.getColor()) {
			s = "You lose";
		}
		else if (n == 2*player.getColor()) {
			s = "No one can put You win";
		}
		else if (n == -2*player.getColor()) {
			s = "No one can put You lose";
		}
		else if (n == -100) {
			s = "No one can put Draw";
		}
		else if (n == 3*player.getColor()) {
			s = "Time over You win";
		}
		else if (n == -3*player.getColor()) {
			s = "Time over You lose";
			restTimenum.setText("0");
		}
		else if (n == 1000) {
			s = "Disconnected" + "No contest";
		}
		else {
			n /= 10000;
			if (n == player.getColor() || n == player.getColor()*2) {
				s = "Disconnected You win";
			}
			else if (n == -player.getColor() || n == -player.getColor()*2) {
				s = "Disconnected You lose";
			}
			else {
				s = "Disconnected Draw";
			}
		}
		getResult(s);
		c.setVisible(false);
		c.setVisible(true);
		if (s != "Disconnected"+"No contest") {
			updateDisp("800");
		}
	}
	
	//ガイド機能のコマンド
	public void actionPerformed(ActionEvent e) {
		String str = e.getActionCommand();
		if(str.equals("ON")) {
			System.out.println("ガイド機能ON");
			assist = true;
			updateDisp("800");
		}
		else if(str.equals("OFF")) {
			System.out.println("ガイド機能OFF");
			assist = false;
			updateDisp("800");
		}
		if((Integer.parseInt((String)cbmin.getSelectedItem()) == 15) && flag_cb == false) {
			cbsec.setEnabled(false);
			flag_cb = true;
		}else if((Integer.parseInt((String)cbmin.getSelectedItem()) != 15) && flag_cb == true){
			cbsec.setEnabled(true);
			flag_cb = false;
		}
	}

	
public void setLimitTime(Player player) {
		c = getContentPane();
		c.setLayout(null);
		setTitle("時間設定");
		
		label1 = new JLabel("希望の対局時間を入力してください");
		label1.setFont(new Font("SanSerif",Font.BOLD,20));
		label1.setBounds(0,0,370,80);
		label1.setHorizontalAlignment(JLabel.CENTER);
		
		label2 = new JLabel("( 5分 ~ 15分 )");
		label2.setBounds(0,80,370,30);
		label2.setFont(new Font("SanSerif",Font.PLAIN,20));
		label2.setHorizontalAlignment(JLabel.CENTER);
		
		String[] min_data = {"10", "5", "6", "7", "8", "9", "11", "12", "13", "14", "15"};
		String[] sec_data = {"00", "15", "30", "45"};
		cbmin = new JComboBox<String>(min_data);
		
		cbmin.setBounds(100, 130, 70, 40);
		cbmin.addActionListener(this);
		
		cbsec = new JComboBox<String>(sec_data);
		cbsec.setBounds(210,130,70,40);
		cbsec.addActionListener(this);
		
		labelm = new JLabel("分");
		labelm.setBounds(170,140,20,20);
		labelm.setFont(new Font("SanSerif",Font.PLAIN,20));
		labelm.setHorizontalAlignment(JLabel.CENTER);

		
		labels = new JLabel("秒");
		labels.setBounds(280,140,20,20);
		labels.setFont(new Font("SanSerif",Font.PLAIN,20));
		labels.setHorizontalAlignment(JLabel.CENTER);
		

		c.add(label1);
		c.add(label2);
		c.add(cbmin);
		c.add(labelm);
		c.add(cbsec);
		c.add(labels);
		
		Ok = new JButton("OK");
		Ok.setBounds(135,200,100,60);
		Ok.setHorizontalAlignment(JLabel.CENTER);
		Ok.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if(e.getSource() == Ok&&Ok.isEnabled()==true) {
					if(Integer.parseInt((String)cbmin.getSelectedItem()) == 15) {
						input_time=Integer.parseInt((String)cbmin.getSelectedItem())*60;
					}else {
						input_time=Integer.parseInt((String)cbmin.getSelectedItem())*60 + Integer.parseInt((String)cbsec.getSelectedItem());
					}
					sendMessage("A"+Integer.toString(input_time));
					cbsec.setEnabled(false);
					cbmin.setEnabled(false);
					Ok.setEnabled(false);
					input_restTimetext.setVisible(false);
					input_restTimenum.setVisible(false);
					stopper_input();
					label1.setText("・・承認待機中・・");
				  }
				}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});

		c.add(Ok);
		
		input_restTimetext = new JLabel("残り時間：");
		input_restTimetext.setBounds(200,300,100,20);
		input_restTimetext.setFont(new Font("SanSerif",Font.PLAIN,20));
		input_restTimetext.setHorizontalAlignment(JLabel.CENTER);
		c.add(input_restTimetext);
		//以下は1秒に値を1だけ減らすタイマーを定義している。
		int msec = 1000;
		ActionListener al_input= new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				input_restTimenum.setText(String.valueOf(Integer.valueOf(input_restTimenum.getText())-1));
				if(input_restTimenum.getText().equals("-1"))
				//残り時間が0になったら時間切れ。画面上に表示されている時間をそのまま相手に送信
					{
					input_restTimenum.setText("0");
					//stopperはタイマーをストップするためのメソッド。
					stopper_input();
					if(Integer.parseInt((String)cbmin.getSelectedItem()) == 15) {
						input_time=Integer.parseInt((String)cbmin.getSelectedItem())*60;
					}else {
						input_time=Integer.parseInt((String)cbmin.getSelectedItem())*60 + Integer.parseInt((String)cbsec.getSelectedItem());
					}
					sendMessage("A"+Integer.toString(input_time));
					cbsec.setEnabled(false);
					cbmin.setEnabled(false);
					Ok.setEnabled(false);
					label1.setText("・・承認待機中・・");
				}
			}
		};
		timer_input = new Timer(msec , al_input);
		timer_input.start();
		input_restTimenum = new JLabel("20");//残り時間を表示するためのラベルを作成
		input_restTimenum.setBounds(300,300,40,20);
		input_restTimenum.setFont(new Font("SanSerif",Font.PLAIN,20));
		input_restTimenum.setHorizontalAlignment(JLabel.CENTER);
		c.add(input_restTimenum);
		//相手の承認・非承認による画面の切り替え
		
		c.setVisible(true);
		//c.setVisible(false);
}
	
	//後手用）承認画面の表示メソッド
 public void accept(Player player) {
	 	
    	//JLabel l1, l2, l3;
		int num_naccept=3; //非承認をした回数、後にメソッドの引数にして値を渡す予定（修正）
		
		setTitle("承認しますか？");
		c = getContentPane();
		c.setLayout(null);
		
		
		l1 = new JLabel("相手の希望時間");
		l1.setBounds(0, 0, 370, 80);
		l1.setFont(new Font("SanSerif",Font.BOLD,24));
		l1.setHorizontalAlignment(JLabel.CENTER);
		
		desired_time = new JLabel("・・希望時間入力中・・");
		desired_time.setBounds(0, 80, 370, 80);
		desired_time.setFont(new Font("SanSerif",Font.BOLD,24));
		desired_time.setBorder(new LineBorder(Color.black,1,true));
		desired_time.setHorizontalAlignment(JLabel.CENTER);
		
		l2 = new JLabel("非承認残り回数：");
		l2.setBounds(120, 180, 200, 20);
		l2.setFont(new Font("SanSerif",Font.PLAIN,15));
		l2.setHorizontalAlignment(JLabel.RIGHT);
		
		l3 = new JLabel("3");
		l3 = new JLabel(Integer.toString(num_naccept));
		l3.setBounds(320, 180, 20, 20);
		l3.setFont(new Font("SanSerif",Font.PLAIN,15));
		l3.setHorizontalAlignment(JLabel.LEFT);

		c.add(l1);
		c.add(desired_time);
		c.add(l2);
		c.add(l3);
		
		Accept = new JButton("承認");
		Accept.setBounds(150,210,80,40);
		Accept.setFont(new Font("SanSerif",Font.BOLD,20));
		
		NoAccept1 = new JButton("非承認（減らしてほしい）");
		NoAccept1.setBounds(20,270,150,40);
		NoAccept1.setFont(new Font("SanSerif",Font.BOLD,12));
		
		NoAccept2 = new JButton("非承認（増やしてほしい）");
		NoAccept2.setBounds(200,270,150,40);
		NoAccept2.setFont(new Font("SanSerif",Font.BOLD,12));
		
		//初期化：承認・非承認無効化
		Accept.setEnabled(false);
		NoAccept1.setEnabled(false);
		NoAccept2.setEnabled(false);
		
		c.add(NoAccept2);
		c.add(Accept);
		c.add(NoAccept1);
		
		Accept.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if(e.getSource() == Accept&&Accept.isEnabled()==true) {
					//承認したら、後手用のタイマー停止
					stopper_access();
					access_restTimetext.setVisible(false);
					access_restTimenum.setVisible(false);
					sendMessage("AAccept");
					decided = 1;
					Accept.setEnabled(false);
					NoAccept1.setEnabled(false);
					NoAccept2.setEnabled(false);
				  }
				}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
		
		
		NoAccept1.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if(e.getSource() == NoAccept1&&NoAccept1.isEnabled()==true) {
					//非承認したら、後手用のタイマー停止
					desired_time.setText("・・希望時間入力中・・");
					l3.setText(String.valueOf(Integer.valueOf(l3.getText())-1));
					stopper_access();
					access_restTimetext.setVisible(false);
					access_restTimenum.setVisible(false);
					sendMessage("ANoAccept1");
					Accept.setEnabled(false);
					NoAccept1.setEnabled(false);
					NoAccept2.setEnabled(false);
				  }
				}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
		
		
		

		NoAccept2.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if(e.getSource() == NoAccept2&&NoAccept2.isEnabled()==true) {
					l3.setText(String.valueOf(Integer.valueOf(l3.getText())-1));
					desired_time.setText("・・希望時間入力中・・");
					stopper_access();
					access_restTimetext.setVisible(false);
					access_restTimenum.setVisible(false);
					sendMessage("ANoAccept2");
					Accept.setEnabled(false);
					NoAccept1.setEnabled(false);
					NoAccept2.setEnabled(false);
				  }
			}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
		
		access_restTimetext = new JLabel("残り時間 : ");
		access_restTimetext.setBounds(180,330,150,20);
		access_restTimetext.setFont(new Font("SanSerif",Font.PLAIN,15));
		access_restTimetext.setHorizontalAlignment(JLabel.RIGHT);
		
		access_restTimenum = new JLabel("10");//残り時間を表示するためのラベルを作成
		access_restTimenum.setBounds(330,330,30,20);
		access_restTimenum.setFont(new Font("SanSerif",Font.PLAIN,15));
		access_restTimenum.setHorizontalAlignment(JLabel.LEFT);

		c.add(access_restTimetext);
		c.add(access_restTimenum);
		
		access_restTimenum.setVisible(false);
		access_restTimetext.setVisible(false);
		
		c.setVisible(true);
		//c.setVisible(false);
 }
	 
	 
	 //先手からの希望時間の取得
	public String str_desired_time(int time) {
		if(time%60 == 0) {
			return(time/60 + ":00");
		}else {
			return(time/60 + ":" + time%60);
		}
	}
	
	public void goodbye() {
		c.setVisible(false);
		c.removeAll();
		setTitle("終了");
		JLabel bye = new JLabel("またね!");
		bye.setFont(new Font("SanSerif",Font.PLAIN,30));
		bye.setHorizontalAlignment(JLabel.CENTER);
		bye.setBounds(0,50,370,50);
		c.add(bye);
		c.setVisible(true);
		
		JLabel endtime = new JLabel();
		int msec = 1000;
		ActionListener ender = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				endtime.setText(String.valueOf(Integer.valueOf(endtime.getText())-1));
				if(endtime.getText().equals("-1"))
					//残り時間が0になったら時間切れ。
					{	
					System.exit(0);
					}
			}
		};
		Timer end_timer = new Timer(msec , ender);
		end_timer.start();
		endtime.setText("2");	
	}
	
	
	//テスト用のmain
	public static void main(String args[]){
		Client oclient = new Client(); //引数としてオセロオブジェクトを渡す
	}
}
