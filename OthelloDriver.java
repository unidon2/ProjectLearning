import java.io.BufferedReader;
import java.io.InputStreamReader;
public class OthelloDriver {
	public static void main (String [] args) throws Exception{
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in), 1);
		Othello game = new Othello(); //初期化
		System.out.println("テスト１：Othelloクラスのオブジェクトを初期化した結果：");
		game.start();
		printStatus(game);
		printGrids(game);
		while(true){
			System.out.println("手番は " + game.getTurn());
			System.out.println("石を置く場所の行をキーボードで入力してください");
			String si = r.readLine();//文字列の入力
			System.out.println("石を置く場所の列をキーボードで入力してください");
			String sj = r.readLine();
			
			if(game.put(game.getTurn(), Integer.parseInt(si), Integer.parseInt(sj))==0) {
				System.out.printf("\n[%s, %s]に石を置きました.\n", si, sj);
				printStatus(game);
				printGrids(game);
			}else {
				System.out.println("置けない場所です. 再入力してください");
			}
		}
	}
	//状態を表示する
	public static void printStatus(Othello game){
		System.out.println("result出力 (0: 引き分け  1,-1: 枚数での勝敗  2,-2: 時間切れでの勝敗  3,-3: 投了 それ以外:切断など)：" + game.result()); //勝敗の決定
		//System.out.println("check_end出力:" + game.check_end(sp)); //対局終了の判定
		System.out.println("getTurn出力 (黒：1, 白：-1)：" + game.getTurn()); //て番情報の取得
	}
	//テスト用に盤面を表示する
	public static void printGrids(Othello game){
		game.print();
	}
}
