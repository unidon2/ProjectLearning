import java.io.BufferedReader;
import java.io.InputStreamReader;
public class OthelloDriver {
	public static void main (String [] args) throws Exception{
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in), 1);
		Othello game = new Othello(); //初期化
		System.out.println("テスト2：結果：");
		System.out.println("start()を行う前のgetTurn()は : "+game.getTurn());
		game.start();
		System.out.println("start()を行った後のgetTurn()は : "+game.getTurn());
		//printStatus(game);
		//printGrids(game);
		while(true){
			printStatus(game);
			printGrids(game);
			if(game.pass(game.getTurn())==0) {
			//System.out.println("手番は " + game.getTurn());
			System.out.println("石を置く場所の行をキーボードで入力してください");
			String si = r.readLine();//文字列の入力
			System.out.println("石を置く場所の列をキーボードで入力してください");
			String sj = r.readLine();
			//System.out.println("");
			int pt = game.put(game.getTurn(), Integer.parseInt(si), Integer.parseInt(sj));
			System.out.println("put()出力(0 : 置けた, 1 : 置けなかった) : "+pt);
			if(pt==0) {
				System.out.printf("\n[%s, %s]に石を置きました.\n", si, sj);
				printStatus(game);
				printGrids(game);
			}else {
				System.out.println("置けない場所です. 再入力してください");
			}
			}
			else {
				System.out.println("置ける場所がありませんでした。パス.");
				game.changeTurn();
			}
			if(game.check_end(0)==1)
				break;
		}
		
	}
	//状態を表示する
	public static void printStatus(Othello game){
		//System.out.println("result出力 (0: 引き分け  1,-1: 枚数での勝敗  2,-2: 時間切れでの勝敗  3,-3: 投了 それ以外:切断など)：" + game.result()); //勝敗の決定
		System.out.println("check_end出力 (0 : 終了していない, 1 : 終了した) : " + game.check_end(0)); //対局終了の判定
		if(game.check_end(0)==1) 
			System.out.println("result出力 (終了時:±1, 全てのマスが埋まっていない場合:±2, 引き分け(全埋まり):100, 引き分け（埋まっていない):-100)：" + game.result()); //勝敗の決定
		System.out.println("getTurn出力 (1 : 黒, -1 : 白)：" + game.getTurn()); //て番情報の取得
		System.out.println("pass出力 (0 : パスしない, 1 : パスする) : "+game.pass(game.getTurn()));
		
	}
	//テスト用に盤面を表示する
	public static void printGrids(Othello game){
		game.print();
	}
}