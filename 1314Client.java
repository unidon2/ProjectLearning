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
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

public class Client extends JFrame implements MouseListener,ActionListener {
	private JButton buttonArray[];//オセロ盤用のボタン配列
	private Container c,cont,conts,conres; // コンテナ
	private JFrame jf,jfslt,jfs;
	private ImageIcon blackIcon, whiteIcon, boardIcon,yboardIcon; //アイコン
	private PrintWriter out;//データ送信用オブジェクト
	private Receiver receiver; //データ受信用オブジェクト
	private Othello game; //Othelloオブジェクト
	private Player player; //Playerオブジェクト
	private JLabel playername, color= new JLabel("") ,turn; //プレイヤ名、色、ターン
	private JLabel restTimenum,restTimetext,oprestTimetext,oprestTimenum; // 残り時間ラベル
	private JLabel giveupLabel,guideLabel,score; //　ガイドラベル
	private JButton giveup1,giveup2;
	private JRadioButton help,nohelp;
	private Timer timer;
	private boolean match;
	private boolean assist;
	static int time = 300;
	static int optime;
	public int section = 3;
	int jrr = 5;
	int notouch;
	int nott;
	static int decided = 0;
	
	private JLabel input_restTimetext, input_restTimenum;
	private JLabel access_restTimetext, access_restTimenum;
	private JButton Ok;
	private JTextField tf1, tf2;
	private JComboBox<String> cbmin, cbsec;
	private JLabel label1, label2, labelm, labels;//希望時間画面表示のラベル
	private boolean flag_cb = false; //希望時間入力時に使われるflag
	private int input_time; //先手から入力された制限時間(s)
	private JLabel desired_time; 
	private boolean selected = false; //希望時間が定まったとき真になる
	private Timer timer_input;	//先手用タイマー
	private Timer timer_access;	//後手用タイマー
	JButton Accept, NoAccept1, NoAccept2;
	JLabel l1,l2,l3;
	// コンストラクタ
	public Client(Othello game, Player player) { //OthelloオブジェクトとPlayerオブジェクトを引数とする
		match = false;
		assist = false;
		this.game = game; //引数のOthelloオブジェクトを渡す
		this.player = player; //引数のPlayerオブジェクトを渡す
		int[] grids = game.getGrid(); //getGridメソッドにより局面情報を取得
		int row = 8;
		//ウィンドウ設定
		
		/*if(section == 0) {*/
		jf = new JFrame();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
		jf.setTitle("ネットワーク対戦型オセロゲーム");//ウィンドウのタイトル
		jf.setSize(row * 45 + 10, row * 45 + 230);//ウィンドウのサイズを設定
		c = jf.getContentPane();//フレームのペインを取得
		jf.setLayout(null);
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
				if (player.getColor() == game.getTurn()) buttonArray[i] = new JButton(yboardIcon);
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
			
			if(decided !=1) {
			jfslt = new JFrame();
			jfslt.setTitle("時間の入力");
			jfslt.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
			jfslt.setSize(500, 400);//ウィンドウのサイズを設定
			cont = jfslt.getContentPane();
			
			label1 = new JLabel("希望の対局時間を入力してください");
			label1.setFont(new Font("SanSerif",Font.PLAIN,24));
			label1.setBounds(0,0,500,80);
			label1.setHorizontalAlignment(JLabel.CENTER);
			
			label2 = new JLabel("( 5分　~ 15分 )");
			label2.setBounds(0,80,500,30);
			label2.setFont(new Font("SanSerif",Font.PLAIN,20));
			label2.setHorizontalAlignment(JLabel.CENTER);
			
			String[] min_data = {"10", "5", "6", "7", "8", "9", "11", "12", "13", "14", "15"};
			String[] sec_data = {"00", "15", "30", "45"};
			cbmin = new JComboBox<String>(min_data);
			
			cbmin.setBounds(135, 130, 80, 40);
			cbmin.addActionListener(this);
			
			cbsec = new JComboBox<String>(sec_data);
			cbsec.setBounds(255,130,80,40);
			cbsec.addActionListener(this);
			
			labelm = new JLabel("分");
			labelm.setBounds(215,140,20,20);
			labelm.setFont(new Font("SanSerif",Font.PLAIN,20));
			labelm.setHorizontalAlignment(JLabel.CENTER);

			
			labels = new JLabel("秒");
			labels.setBounds(335,140,20,20);
			labels.setFont(new Font("SanSerif",Font.PLAIN,20));
			labels.setHorizontalAlignment(JLabel.CENTER);
			

			cont.add(label1);
			cont.add(label2);
			cont.add(cbmin);
			cont.add(labelm);
			cont.add(cbsec);
			cont.add(labels);
			
			Ok = new JButton("OK");
			Ok.setBounds(200,200,100,60);
			Ok.setHorizontalAlignment(JLabel.CENTER);
			Ok.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					if(e.getSource() == Ok) {
						if(Integer.parseInt((String)cbmin.getSelectedItem()) == 15) {
							input_time=Integer.parseInt((String)cbmin.getSelectedItem())*60;
						}else {
							input_time=Integer.parseInt((String)cbmin.getSelectedItem())*60 + Integer.parseInt((String)cbsec.getSelectedItem());
						}
						time = input_time;
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

			cont.add(Ok);
			
			input_restTimetext = new JLabel("残り時間：");
			input_restTimetext.setBounds(300,300,100,20);
			input_restTimetext.setFont(new Font("SanSerif",Font.PLAIN,20));
			input_restTimetext.setHorizontalAlignment(JLabel.CENTER);
			cont.add(input_restTimetext);
			//以下は1秒に値を1だけ減らすタイマーを定義している。
			msec = 1000;
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
						sendMessage(Integer.toString(input_time));
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
			input_restTimenum.setBounds(400,300,40,20);
			input_restTimenum.setFont(new Font("Century",Font.PLAIN,20));
			input_restTimenum.setHorizontalAlignment(JLabel.CENTER);
			cont.add(input_restTimenum);
			//相手の承認・非承認による画面の切り替え
			
			
			jfslt.setVisible(false);
			
			jfs= new JFrame();
			
			//JLabel l1, l2, l3;
			
			int num_naccept=3; //非承認をした回数、後にメソッドの引数にして値を渡す予定（修正）
			
			
			jfs.setTitle("承認しますか？");
			jfs.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
			jfs.setSize(500, 400);//ウィンドウのサイズを設定
			conts = jfs.getContentPane();
			conts.setLayout(null);
			
			l1 = new JLabel("相手の希望時間");
			l1.setBounds(0, 0, 500, 80);
			l1.setFont(new Font("SanSerif",Font.BOLD,24));
			l1.setHorizontalAlignment(JLabel.CENTER);
			
			desired_time = new JLabel("・・希望時間入力中・・");
			desired_time.setBounds(0, 80, 500, 80);
			desired_time.setFont(new Font("SanSerif",Font.BOLD,24));
			desired_time.setBorder(new LineBorder(Color.black,1,true));
			desired_time.setHorizontalAlignment(JLabel.CENTER);
			
			l2 = new JLabel("非承認残り回数：");
			l2.setBounds(270, 180, 200, 20);
			l2.setFont(new Font("SanSerif",Font.PLAIN,15));
			l2.setHorizontalAlignment(JLabel.RIGHT);
			
			l3 = new JLabel("3");
			l3 = new JLabel(Integer.toString(num_naccept));
			l3.setBounds(470, 180, 20, 20);
			l3.setFont(new Font("SanSerif",Font.PLAIN,15));
			l3.setHorizontalAlignment(JLabel.LEFT);

			conts.add(l1);
			conts.add(desired_time);
			conts.add(l2);
			conts.add(l3);
			
			Accept = new JButton("承認");
			Accept.setBounds(210,210,80,40);
			Accept.setFont(new Font("SanSerif",Font.BOLD,20));
			
			NoAccept1 = new JButton("非承認（減らしてほしい）");
			NoAccept1.setBounds(20,270,220,40);
			NoAccept1.setFont(new Font("SanSerif",Font.BOLD,15));
			
			NoAccept2 = new JButton("非承認（増やしてほしい）");
			NoAccept2.setBounds(260,270,220,40);
			NoAccept2.setFont(new Font("SanSerif",Font.BOLD,15));
			
			//初期化：承認・非承認無効化
			Accept.setEnabled(false);
			NoAccept1.setEnabled(false);
			NoAccept2.setEnabled(false);
			
			conts.add(NoAccept2);
			conts.add(Accept);
			conts.add(NoAccept1);
			
			Accept.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					if(e.getSource() == Accept) {
						//承認したら、後手用のタイマー停止
						stopper_access();
						access_restTimetext.setVisible(false);
						access_restTimenum.setVisible(false);
						sendMessage("AAccept");
						decided = 1;
						Accept.setEnabled(true);
						NoAccept1.setEnabled(false);
						NoAccept2.setEnabled(false);
						return;
					  }
					}
				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
			});
			
			
			NoAccept1.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					if(e.getSource() == NoAccept1) {
						//非承認したら、後手用のタイマー停止
						stopper_access();
						access_restTimetext.setVisible(false);
						access_restTimenum.setVisible(false);
						l3.setText(String.valueOf(Integer.valueOf(l3.getText())-1));
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
					if(e.getSource() == NoAccept2) {
						sendMessage("ANoAccept2");
						l3.setText(String.valueOf(Integer.valueOf(l3.getText())-1));
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
			access_restTimetext.setBounds(300,330,150,20);
			access_restTimetext.setFont(new Font("SanSerif",Font.PLAIN,15));
			access_restTimetext.setHorizontalAlignment(JLabel.RIGHT);
			
			access_restTimenum = new JLabel("10");//残り時間を表示するためのラベルを作成
			access_restTimenum.setBounds(450,330,30,20);
			access_restTimenum.setFont(new Font("SanSerif",Font.PLAIN,15));
			access_restTimenum.setHorizontalAlignment(JLabel.LEFT);

			c.add(access_restTimetext);
			c.add(access_restTimenum);
			
			access_restTimenum.setVisible(false);
			access_restTimetext.setVisible(false);
			
			jfs.setVisible(false);
			}
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
			}
			if (game.pass(game.getTurn()) == 1) {
				game.changeTurn();
				updateDisp("800");
			}
		}
		
		if (msg.equals("ooo")) {
			game.check_end(player.getColor()*3);
			endmsg(game.result());
		}
		if(msg.equals("giveup")) {
			endmsg(555);
		}
		if (msg.equals("xxx")) {
			int played = game.count(1);
			if (played < 20) {
				endmsg(1000);
			}
			else {
				endmsg(game.count(0)*10000);
			}
		}
		else if(msg.charAt(0)=='A') {
			msg = msg.substring(1);
			if(msg.equals("Accept")) {
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
						
							sendMessage(Integer.toString(input_time)); //先手用の制限時間が切れたら、画面上の時間を希望時間として送信
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
					if(l3.getText().equals("0")) {
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
								}
						}
					};
					timer_access = new Timer(msec , al_access);
					timer_access.start();
					access_restTimenum.setText("10");	
					access_restTimetext.setVisible(true);
					access_restTimenum.setVisible(true);
				}
				if(selected == true) {
					System.out.println("最終的な制限時間：" + time);
					decided = 1;
					return;
				}
			}
		}
		
		
		System.out.println("サーバからメッセージ " + msg + " を受信しました"); //テスト用標準出力
	}
	public void detect(String str) {
		JFrame jfd = new JFrame();
		JLabel jld = new JLabel(str);
		jfd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
		jfd.setTitle("ネットワーク対戦型オセロゲーム");//ウィンドウのタイトル
		jfd.setSize(230,100);//ウィンドウのサイズを設定
		//c = jf.getContentPane();//フレームのペインを取得
		jfd.setLayout(null);
		jld.setBounds(10,10,180,50);
		jfd.add(jld);
		jfd.setVisible(false);
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
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			//turn.setText("黒のターン");
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
	public void rebattle() {
		c.removeAll();
		Container reb = new Container();
		JLabel jl = new JLabel();
		JButton jby = new JButton();
		JButton jbn = new JButton();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("再戦");
		setSize(8 * 45 + 10, 8 * 25);
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
		c = jf.getContentPane();
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
		jl.setFont(new Font("Century",Font.BOLD,20));
		jl.setHorizontalAlignment(JLabel.CENTER);
		coer = getContentPane();
		coer.add(jl);
	}
	
	//結果を取得して出力するメソッド
	//結果をサーバから受け取るためのものはまた別に作成予定
	public void getResult(String judge) {
		//c.removeAll();
		JLabel jl = new JLabel();
		JLabel jl2 = new JLabel();
		score = new JLabel();
		//judgeは勝敗判定
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
		setTitle("勝敗判定");//ウィンドウのタイトル
		setSize(8 * 45 + 10, 8 * 45 + 200);//ウィンドウのサイズを設定
		if(judge == "Opponent surrendered. You win"||judge == "You surrendered. You lose"||judge =="No one can put Draw"
				||judge =="Disconnected" + "No contest"||judge =="Disconnected.../n Draw"||judge =="Time over You win"||judge == "Time over You lose") {
			score = new JLabel();
		}
		else {
			score = new JLabel(String.valueOf(count(player.getColor()))+ " vs " +String.valueOf(count(-player.getColor())));
		}
		score.setBounds(0,50,370,100);
		score.setFont(new Font("Century",Font.BOLD,60));
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
		jl.setFont(new Font("Century",Font.BOLD,30));
		jl.setHorizontalAlignment(JLabel.CENTER);
		jl2.setBounds(0,200,370,140);
		jl2.setFont(new Font("Century",Font.BOLD,50));
		jl2.setHorizontalAlignment(JLabel.CENTER);
		c = jf.getContentPane();
		c.add(score);
		c.add(jl);
		c.add(jl2);
		notouch = 1;
		Timer jrrr;
		int msec = 1000;
		ActionListener al = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				jrr--;
				if(jrr==-1) {
					c.setVisible(false);
					rebattle();
					notouch =0;
					score.setText("");
					score.setVisible(false);
				}
			}
		};
		jrrr = new Timer(msec , al);
		jrrr.start();
	}
  	//マウスクリック時の処理
	public void mouseClicked(MouseEvent e) {
		if(notouch == 1) {
			
		}
		else {
		JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
		String command = theButton.getActionCommand();//ボタンの名前を取り出す
		if(command.equals("e")||command.equals("r")) {
			sendMessage(command);
		}
		else if(command.equals("giveup1")) {
			System.out.println("giveup");
			sendMessage("giveup");
			endmsg(556);
			}
		//置けるマスにクリックをした想定。
		//この際にタイマーの値を取得する。
		else {
			//game.changeTurn()
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
						game.changeTurn();
						timer.start();
						updateDisp("800");
					}
				}
				else if(updateDisp(command)==1) {
					
				}
				System.out.println(restTimenum.getText());
				}
		}
		}
	
	public void endmsg(int n) {
		/*for(int i=0;i<64;i++) {
			buttonArray[i].setVisible(false);
		}*/
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
		updateDisp("800");
	}
	
	//ガイド機能のコマンド
	public void actionPerformed(ActionEvent e) {
		String str = e.getActionCommand();
		if(str.equals("ON")) {
			System.out.println("ガイド機能ON");
			assist = true;
		}
		else if(str.equals("OFF")) {
			System.out.println("ガイド機能OFF");
			assist = false;
		}
		/*else if(str.equals("e")) {
			sendMessage("e");
		}
		else if(str.equals("r")) {
			sendMessage("r");
		}*/
		updateDisp("800");
		if((Integer.parseInt((String)cbmin.getSelectedItem()) == 15) && flag_cb == false) {
			cbsec.setEnabled(false);
			flag_cb = true;
		}else if((Integer.parseInt((String)cbmin.getSelectedItem()) != 15) && flag_cb == true){
			cbsec.setEnabled(true);
			flag_cb = false;
		}
	}
	public void starting() {
		jfs.setVisible(false);
		jfslt.setVisible(false);
		jf.setVisible(true);
		restTimenum.setText(String.valueOf(time));
		oprestTimenum.setText(String.valueOf(optime));
		game.start();
	}
	public void waitMatching() {
		while (true) {
			boolean m = match;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (m) {
				break;
			}
		}
	}
	public void getTime() {
		input_restTimenum.setText("20");
		//c.removeAll();
		//updateDisp("800");
		jfslt.setVisible(true);
		while(true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(decided != 0)
				break;
		}
		decided = 0;
		System.out.println("exit");
		jfslt.setVisible(false);
		//jfslt.setVisible(false);
		jfs.setVisible(false);
		selected = false;
		if(player.getColor()==1) {
			System.out.println(player.getColor());
			setTime();
		}
		else if(player.getColor()==-1){
			decided = 1;
			/*try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}*/
			starting();
		}
	}
	public void setTime() {
		input_restTimenum.setText("5000");
		jfs.setVisible(true);
		while(true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(decided !=0) {
				break;
			}
		}
		selected = false;
		decided = 0;
		System.out.println("exit");
		jfs.setVisible(false);
		jfslt.setVisible(false);
		if(player.getColor()==-1)
			getTime();
		else if(player.getColor()==1) {
			decided = 1;
			starting();
		}
	}
public void setLimitTime(Player player) {
		
		/*Container cont = getContentPane();
		cont.setLayout(null);
		
		setTitle("時間の入力");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
		setSize(500, 400);//ウィンドウのサイズを設定
		
		label1 = new JLabel("希望の対局時間を入力してください");
		label1.setFont(new Font("Century",Font.PLAIN,24));
		label1.setBounds(0,0,500,80);
		label1.setHorizontalAlignment(JLabel.CENTER);
		
		label2 = new JLabel("( 5分　~ 15分 )");
		label2.setBounds(0,80,500,30);
		label2.setFont(new Font("Century",Font.PLAIN,20));
		label2.setHorizontalAlignment(JLabel.CENTER);
		
		String[] min_data = {"10", "5", "6", "7", "8", "9", "11", "12", "13", "14", "15"};
		String[] sec_data = {"00", "15", "30", "45"};
		cbmin = new JComboBox<String>(min_data);
		
		cbmin.setBounds(135, 130, 80, 40);
		cbmin.addActionListener(this);
		
		cbsec = new JComboBox<String>(sec_data);
		cbsec.setBounds(255,130,80,40);
		cbsec.addActionListener(this);
		
		labelm = new JLabel("分");
		labelm.setBounds(215,140,20,20);
		labelm.setFont(new Font("Century",Font.PLAIN,20));
		labelm.setHorizontalAlignment(JLabel.CENTER);

		
		labels = new JLabel("秒");
		labels.setBounds(335,140,20,20);
		labels.setFont(new Font("Century",Font.PLAIN,20));
		labels.setHorizontalAlignment(JLabel.CENTER);
		

		cont.add(label1);
		cont.add(label2);
		cont.add(cbmin);
		cont.add(labelm);
		cont.add(cbsec);
		cont.add(labels);
		
		Ok = new JButton("OK");
		Ok.setBounds(200,200,100,60);
		Ok.setHorizontalAlignment(JLabel.CENTER);
		Ok.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if(e.getSource() == Ok) {
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

		cont.add(Ok);
		
		input_restTimetext = new JLabel("残り時間：");
		input_restTimetext.setBounds(300,300,100,20);
		input_restTimetext.setFont(new Font("Century",Font.PLAIN,20));
		input_restTimetext.setHorizontalAlignment(JLabel.CENTER);
		cont.add(input_restTimetext);
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
					sendMessage(Integer.toString(input_time));
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
		input_restTimenum.setBounds(400,300,40,20);
		input_restTimenum.setFont(new Font("Century",Font.PLAIN,20));
		input_restTimenum.setHorizontalAlignment(JLabel.CENTER);
		cont.add(input_restTimenum);
		//相手の承認・非承認による画面の切り替え
		
		setVisible(true);*/
	}
	
	//後手用）承認画面の表示メソッド
	 void accept(Player player) {
		 	
	    	/*JLabel l1, l2, l3;
			
			int num_naccept=3; //非承認をした回数、後にメソッドの引数にして値を渡す予定（修正）
			
			Container cont = getContentPane();
			cont.setLayout(null);
			
			setTitle("承認しますか？");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
			setSize(500, 400);//ウィンドウのサイズを設定
			
			l1 = new JLabel("相手の希望時間");
			l1.setBounds(0, 0, 500, 80);
			l1.setFont(new Font("Century",Font.BOLD,24));
			l1.setHorizontalAlignment(JLabel.CENTER);
			
			desired_time = new JLabel("・・希望時間入力中・・");
			desired_time.setBounds(0, 80, 500, 80);
			desired_time.setFont(new Font("Century",Font.BOLD,24));
			desired_time.setBorder(new LineBorder(Color.black,1,true));
			desired_time.setHorizontalAlignment(JLabel.CENTER);
			
			l2 = new JLabel("非承認残り回数：");
			l2.setBounds(270, 180, 200, 20);
			l2.setFont(new Font("Century",Font.PLAIN,15));
			l2.setHorizontalAlignment(JLabel.RIGHT);
			
			l3 = new JLabel("3");
			l3 = new JLabel(Integer.toString(num_naccept));
			l3.setBounds(470, 180, 20, 20);
			l3.setFont(new Font("Century",Font.PLAIN,15));
			l3.setHorizontalAlignment(JLabel.LEFT);

			cont.add(l1);
			cont.add(desired_time);
			cont.add(l2);
			cont.add(l3);
			
			Accept = new JButton("承認");
			Accept.setBounds(210,210,80,40);
			Accept.setFont(new Font("Century",Font.BOLD,20));
			
			NoAccept1 = new JButton("非承認（減らしてほしい）");
			NoAccept1.setBounds(20,270,220,40);
			NoAccept1.setFont(new Font("Century",Font.BOLD,15));
			
			NoAccept2 = new JButton("非承認（増やしてほしい）");
			NoAccept2.setBounds(260,270,220,40);
			NoAccept2.setFont(new Font("Century",Font.BOLD,15));
			
			//初期化：承認・非承認無効化
			Accept.setEnabled(false);
			NoAccept1.setEnabled(false);
			NoAccept2.setEnabled(false);
			
			cont.add(NoAccept2);
			cont.add(Accept);
			cont.add(NoAccept1);
			
			Accept.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					if(e.getSource() == Accept) {
						//承認したら、後手用のタイマー停止
						stopper_access();
						access_restTimetext.setVisible(false);
						access_restTimenum.setVisible(false);
						sendMessage("AAccept");
						Accept.setEnabled(true);
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
					if(e.getSource() == NoAccept1) {
						//非承認したら、後手用のタイマー停止
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
					if(e.getSource() == NoAccept2) {
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
			access_restTimetext.setBounds(300,330,150,20);
			access_restTimetext.setFont(new Font("Century",Font.PLAIN,15));
			access_restTimetext.setHorizontalAlignment(JLabel.RIGHT);
			
			access_restTimenum = new JLabel("10");//残り時間を表示するためのラベルを作成
			access_restTimenum.setBounds(450,330,30,20);
			access_restTimenum.setFont(new Font("Century",Font.PLAIN,15));
			access_restTimenum.setHorizontalAlignment(JLabel.LEFT);

			c.add(access_restTimetext);
			c.add(access_restTimenum);
			
			access_restTimenum.setVisible(false);
			access_restTimetext.setVisible(false);
			
			setVisible(true);*/
		}
	 
	 
	 //先手からの希望時間の取得
		public String str_desired_time(int time) {
			if(time%60 == 0) {
				return(time/60 + ":00");
			}else {
				return(time/60 + ":" + time%60);
			}
		}
	
	//テスト用のmain
	public static void main(String args[]){
		//ログイン処理
		String myName = JOptionPane.showInputDialog(null,"名前を入力してください","名前の入力",JOptionPane.QUESTION_MESSAGE);
		if(myName.equals("")){
			myName = "No name";//名前がないときは，"No name"とする
		}
		Player player = new Player(); //プレイヤオブジェクトの用意(ログイン)
		player.setName(myName); //名前を受付
		Othello game = new Othello(); //オセロオブジェクトを用意
		Client oclient = new Client(game, player); //引数としてオセロオブジェクトを渡す
		oclient.connectServer("localhost", 10000);
		System.out.println("Matching...");
		oclient.waitMatching();
		System.out.println("Matched!");
		if(player.getColor()==1) {
			oclient.getTime();
		}
		else {
			oclient.setTime();
		}
		if(player.getColor()==1) {
			oclient.setTime();
		}
		else {
			oclient.getTime();
		}
		game.start();
		//oclient.setVisible(true);
	}
}