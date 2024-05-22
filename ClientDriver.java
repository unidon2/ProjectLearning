import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientDriver extends Exception{
	private Client oclient;
	int puttern = 0;
	ClientDriver(){
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in), 1);
		oclient = new Client();
		System.out.println("名前を入力してください");
		oclient.prepare();//名前の準備、エコーサーバに接続
		oclient.matching();//マッチング
		String st = null;
		System.out.println("お名前が入力されました。");
		System.out.println("色を決めるため、bを入力してください");//wは未実装
		try {
			st = r.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		oclient.sendMessage(st);//st=bを送って黒側として接続。
		while(true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(oclient.getStatus()==1||oclient.getStatus()==-1) {
				oclient.delete();//画面の初期化
				break;
			}
		}
		System.out.println("あなたの色は黒です。次にあなたの制限時間を決めます...");
		if(oclient.getStatus()==1) {//こちら側が600秒を申請する。
			oclient.setLimitTime();//普通に送信すると、まだ宣言していない
			oclient.setInputTime(600);//acceptが呼び出されるので、疑似的に送信しているように見せかけている
			while(true) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
					oclient.sendMessage("AAccept");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(String.valueOf(oclient.getdecided())=="true") {
						oclient.resetselected();
						oclient.resetdecided();
						oclient.delete();
						System.out.println("承認されました。あなたの制限時間は600秒です。");
						break;
				}
			}
			oclient.stopper_input();
			oclient.accept();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			while(true) {
				try {
					if(puttern == 0) {
						oclient.sendMessage("A300");
						oclient.setInputTime(300);
						System.out.println("相手が300秒を要求します。「多くしろ」と拒否してください");
						Thread.sleep(10000);
						System.out.println("拒否完了");
					}
					else if(puttern == 1){
						Thread.sleep(1000);
						oclient.sendMessage("A900");
						oclient.setInputTime(900);
						System.out.println("相手が900秒を要求します。「少なくしろ」と拒否してください");
						Thread.sleep(10000);
						System.out.println("拒否完了");
					}
					else if(puttern == 2){
						oclient.sendMessage("A750");
						oclient.setInputTime(750);
						System.out.println("相手が750秒を要求します。「少なくしろ」と拒否してください");
						Thread.sleep(10000);
						System.out.println("拒否完了");
					}
					else {
						oclient.sendMessage("A600");
						oclient.setInputTime(600);
						System.out.println("相手が600秒を要求します。承認してください");
						oclient.setvalue();
						Thread.sleep(10000);
						System.out.println("承認完了。相手の制限時間は600秒です。");
					}
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				oclient.setvalue();
				if(puttern == 0)
					puttern = 1;
				else if(puttern == 1) 
					puttern = 2;
				else if(puttern == 2)
					puttern = 3;
				else 
					break;
			}
		}
		oclient.delete();
		System.out.println("置いたときの操作を1ターンずつ行います。黒いコマを置いてください : ");
		oclient.Playing();
		while(true) {
			try {
				System.out.println();
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(oclient.changed()==-1) {
				//oclient.delete();
				//oclient.updateDisp("800");
				System.out.println("あなたの色を白にしました.白を置いてください : ");
				oclient.changeTurn();
				break;
			}
		}
		while(true) {
			try {
				System.out.println();
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(oclient.changed()==1) {
				System.out.println("投了ボタンを押してください.");
				break;
			}
		}
	}
	public static void main(String args[]) {
		ClientDriver cd = new ClientDriver();
	}
}
