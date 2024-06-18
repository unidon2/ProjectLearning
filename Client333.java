import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.sound.midi.Receiver;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Client extends JFrame implements MouseListener,ActionListener {
	//private Player player;
	private Receiver receiver; //データ受信用オブジェクト
	private Container c; // コンテナ
	private JLabel l1;
	private JButton game_start, ranking;
	public Client() {
		c = getContentPane();
		home();
	}

	public void home() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		c.setLayout(null);
		setTitle("ホーム画面");
		setSize(370,490);
		setLocationRelativeTo(null);
		
		l1 = new JLabel("英単語タイピングパズルゲーム");
		l1.setBounds(0,0,370,80);
		l1.setFont(new Font("SanSerif", Font.BOLD,24));
		l1.setHorizontalAlignment(JLabel.CENTER);
		
		game_start = new JButton("ネットワーク対戦");
		game_start.setBounds(85,150,200,20);
		game_start.setFont(new Font("SanSerif",Font.BOLD,20));
		game_start.addActionListener(this);
		
		ranking = new JButton("ランキング");
		ranking.setBounds(85,190,200,20);
		ranking.setFont(new Font("SanSerif",Font.BOLD,20));
		ranking.addActionListener(this);
		
		c.add(l1);
		c.add(game_start);
		c.add(ranking);
		
		setVisible(true);
	}
	
	public void matching() {
		JLabel jlmat1 = new JLabel();
		JLabel jlmat2 = new JLabel();
		System.out.println("マッチング中");
		setTitle("マッチング中");
		
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
		c.setVisible(true);
	}
	
	public void ranking() {
		String header[] = {"順位"};
	}
	
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==game_start) {
			c.setVisible(false);
			c.removeAll();
			matching();
		}
		else if(e.getSource()==ranking) {
			c.setVisible(false);
			c.removeAll();
			ranking();
		}
	}
	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	
	public static void main(String args[]){
		Client oclient = new Client(); //引数としてオセロオブジェクトを渡す
	}
}
