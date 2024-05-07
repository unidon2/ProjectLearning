// 制限時間の設定画面
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

class LimitTimeSet extends JFrame implements MouseListener, ActionListener{
		
		JLabel tlabel1, tlabel2, tlabel3, restTimetext, restTimenum;
		JButton Ok;
		JTextField tf1, tf2;
		Timer timer;

		LimitTimeSet (String title){
			super(title);
			JPanel p =(JPanel)getContentPane();
			p.setLayout(new FlowLayout());
			
			tlabel1 = new JLabel("希望時間を入力してください\n");
			p.add(tlabel1);
			
			tlabel2 = new JLabel("( 5分 ~ 15分 )");
			p.add(tlabel2);
			
			tf1 = new JTextField(2);
			tf1.setPreferredSize(new Dimension(150,50));
			p.add(tf1);
			
			tlabel3 = new JLabel(":");
			p.add(tlabel3);
			
			tf2 = new JTextField(2);
			tf2.setPreferredSize(new Dimension(150,50));
			p.add(tf2);
			
			Ok = new JButton("OK");
			Ok.addMouseListener(this);
			p.add(Ok);
			
			restTimetext = new JLabel("残り時間 : ");
			p.add(restTimetext);
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
			restTimenum = new JLabel("20");//残り時間を表示するためのラベルを作成
			p.add(restTimenum);
			
			
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
			setSize(200, 300);//ウィンドウのサイズを設定
			setVisible(true);
			
		}
		
		//希望時間の取得
		String get_desired_time() {
			return(tf1.getText() + ":" + tf2.getText());
		}
		
		public void mouseClicked(MouseEvent e) {
			if(e.getSource() == Ok) {
				int input_time = Integer.parseInt(tf1.getText())*60 + Integer.parseInt(tf2.getText());
				if(input_time < 300 || input_time > 600) {
					tlabel1.setText("希望時間を再入力してください\n");
				}else {
					Accept accept = new Accept("承認画面", get_desired_time());
					setVisible(false);
					accept.setVisible(true);
				}
			}
		}

		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void stopper() {
			timer.stop();
		}
		public void actionPerformed(ActionEvent e) {}
		
        public static void main(String[] args){
            new LimitTimeSet("時間の入力");
        }

		
	}

class Accept extends JFrame implements MouseListener{
	
	JLabel l1, restTimetext;
	JTextField tf;
	JButton Accept, NoAccept1, NoAccept2;
	String desired_time;
	
	Accept(String title, String desired_time){
		super(title);
		this.desired_time = desired_time;
		JPanel p =(JPanel)getContentPane();
		p.setLayout(new FlowLayout());
		
		l1 = new JLabel("相手の希望時間");
		p.add(l1);
		
		tf = new JTextField(desired_time, 16);
		tf.setHorizontalAlignment(JTextField.CENTER);
		p.add(tf);
		
		Accept = new JButton("承認");
		Accept.setBounds(50,50,60,30);
		p.add(Accept);
		
		NoAccept1 = new JButton("非承認（減らしてほしい）");
		NoAccept1.setBounds(50,50,60,30);
		p.add(NoAccept1);
		
		NoAccept2 = new JButton("非承認（増やしてほしい）");
		NoAccept2.setBounds(50,50,60,30);
		p.add(NoAccept2);
		
		restTimetext = new JLabel("残り時間 : ");
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
		setSize(200, 300);//ウィンドウのサイズを設定
		setVisible(true);
	}
	
	public void mouseClicked(MouseEvent e) {
	}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

}
