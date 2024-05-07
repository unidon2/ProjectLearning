import java.util.Scanner;

class Othell {
    /* 空き: 0　黒: 1　白: -1  置ける: +-2 両方置ける: 3*/
    private int[][] board = new int[8][8]; //盤面情報
    private int[][] dir = {{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1}}; //8方向への移動用
    private int turn; //手番情報
    private int color; //自分の色(本来はplayerクラスのものなので実装時には消える予定です)
    private int time; //時間
    private int win; //勝敗情報(0: 引き分け  1,-1: 枚数での勝敗  2,-2: 時間切れでの勝敗  3,-3: 投了 それ以外:切断など)

    public Othell() { //盤面の初期化 & 時間設定
        turn = 1;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i == 3 && j == 3 )|| (i == 4 && j == 4)) {
                    board[i][j] = 1;
                } 
                else if ((i == 3 && j == 4) || (i == 4 && j == 3)) {
                    board[i][j] = -1;
                }
                else {
                    board[i][j] = 0;
                }
            }
        }
        set_time(1000); //本来はclientクラスで設定するので実装時には消えます
        reload();
    }
    
    public void timer() { //時間減少
    	while (turn == color) {
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		time--;
    	}
    }
    
    

    public void reload() { //盤面の更新
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == 1 || board[i][j] == -1) { //既に置かれているマスは無視する
                    continue;
                }
                int can = 0; //そのマスに置ける人間が存在するか
                for (int d = 0; d < 8; d++) { //8方向に対して
                    int dis = 1; //元のマスからの距離
                    int already = 0; //挟めるマスが存在するか
                    int cl = 100; //挟めるマスの色
                    while (0 <= i+dis*dir[d][0] && i+dis*dir[d][0] < 8 && 0 <= j+dis*dir[d][1] && j+dis*dir[d][1] < 8) { //盤面外に出ない間
                        if (already == 0 && (board[i+dis*dir[d][0]][j+dis*dir[d][1]] == 1 || board[i+dis*dir[d][0]][j+dis*dir[d][1]] == -1)) { //隣のマスに置かれている場合
                            already = 1; //フラグ管理
                            cl = board[i+dis*dir[d][0]][j+dis*dir[d][1]]; //隣のマスの色記憶
                            dis++; //1つ離れる
                        }
                        else if (board[i+dis*dir[d][0]][j+dis*dir[d][1]] == cl && already == 1) { //挟めるマスが続いている場合
                        	dis++; //1つ離れる
                        }
                        else if (board[i+dis*dir[d][0]][j+dis*dir[d][1]] == -cl && already == 1) { //挟める場合(挟めるマスと違う色が現れた場合)
                        	if (can == 2*cl) { //既にもう片方の色でも置ける場合
                        		can = 3; //両方とも置ける状態に
                        	}
                        	else { //それ以外
                        		can = -2*cl; //置ける色の指定
                        	}
                            break;
                        }
                        else {
                            break;
                        }
                    }
                    if (can == 3) { //両方置ける場合はそれ以上探索しない
                    	break;
                    }
                }
                if (can != 0) { //置ける場合は更新
                    board[i][j] = can;
                }
                else { //逆に置けなくなる場合もあるので0で更新
                	board[i][j] = 0;
                }
            }
        }
    }
    
    public int pass(int color) { //パスするか(置ける場所が存在しないか) (返り値  置ける: 0    パス: 1)
    	for (int i = 0; i < 8; i++) {
    		for (int j = 0; j < 8; j++) {
    			if (board[i][j] == 2*color || board[i][j] == 3) {
    				return 0;
    			}
    		}
    	}
    	return 1;
    }
    

    

    public int put(int color, int i, int j) { //置かれた際の処理(返り値 0: 置けた   1: 置けなかった)
        int success = 0; //フラグ(置くことができるか)
        if (board[i][j] != color*2 && board[i][j] != 3) { //そのマスには置けない場合
        	return 1;
        }
        /* あらかじめ置ける場所は演算しているので以降の処理は置けることを前提としています */
        /* が念のため本当に置けるのかを 変数:success で管理しています*/
        for (int d = 0; d < 8; d++) { //8方向に対して
            int dis = 1; //元のマスからの距離
            int already = 0; //挟めるマスが存在するか
            int can = 0; //挟めたか
            while (0 <= i+dis*dir[d][0] && i+dis*dir[d][0] < 8 && 0 <= j+dis*dir[d][1] && j+dis*dir[d][1] < 8) { //盤面内の間
                if (board[i+dis*dir[d][0]][j+dis*dir[d][1]] == -color) { //置こうとしている色でない色のマスがある間 距離を伸ばし続ける
                    already = 1; //挟めるマスが存在する
                    dis++; //1マス離れる
                }
                else if (board[i+dis*dir[d][0]][j+dis*dir[d][1]] == color && already == 1) { //置こうとしている色と同じマスが見つかった∩既に1マス以上挟めるマスが存在している
                    can = 1; //挟める
                    break;
                }
                else {
                    break;
                }
            }
            if (can == 1) { //(その方向で)挟める場合
                success = 1; //その場所に置くことが可能である
                for (int change = 0; change < dis; change++) { //確認した距離まで
                    board[i+change*dir[d][0]][j+change*dir[d][1]] = color; //色を塗り替える
                }
            }
        }
        if (success == 1) { //置けた場合
            changeturn(); //手番の変更
            return 0;
        }
        else { //(前述の通りここに来ている時点で reload() が間違っていることになります)
        	return 1;
        }
    }

    public void print() { //確認用
		System.out.print(turn + "   置ける場所:" );
		if (turn == 1) {
			System.out.println("*");
		}
		else {
			System.out.println("+");
		}
        System.out.print("  ");
        for(int i=0; i<8; i++) {
            System.out.printf(" %d ", i);
        }
        System.out.println();
        for(int i=0; i<8; i++) {
            System.out.printf("%d|", i);
            for(int j=0; j<8; j++) {
                if(board[i][j] == 0) {
                    System.out.print("  |");
                }
                else if(board[i][j] == 1) {
                    System.out.print("〇|");
                }
                else if(board[i][j] == -1) {
                    System.out.print("●|");
                }
                else if(board[i][j] == 2) {
                    System.out.print(" *|");
                }
                else if (board[i][j] == -2) {
                	System.out.print(" +|");
                }
                else if (board[i][j] == 3) {
                	System.out.print(" !|");
                }
            }
            System.out.println();
        }
        System.out.println();
	}

    void changeturn() { //手番変更
        turn *= -1;
        return;
    }
    
    void set_time(int tm) { //時間の初期化(clientに移行?)
    	time = tm;
    }

    int get_time() { //時間の取得
        return time;
    }

    int check_end(int sp) { //終了判定(返り値 終了していない: 0     終了: 1)
    	if (sp != 0) { //特殊な終了(投了、切断、相手側の時間切れもこちらに含まれる想定です）
    		win = sp;
    		return 1;
    	}	
    	if (time < 0) { //時間切れ
    		System.out.println("time over"); //確認用
    		win = -2*color; //
    		return 1;
    	}
    	for (int i = 0; i < 8; i++) {
    		for (int j = 0; j < 8; j++) {
    			if (board[i][j] == 2 || board[i][j] == -2 || board[i][j] == 3) { //どちらかに置けるマスがまだある場合
    				return 0; //まだ終了でない
    			}
    		}
    	}
    	/* ここに来た時点で全て埋まっている or どちらも置けない状態 */
    	win = count(); //枚数カウントによる勝敗の更新
    	System.out.println("no one can put"); //確認用
    	return 1;
    }
    
    int count() { //枚数カウント(返り値: 勝者の色:(1,-1) or 引き分け: 0)
    	int bw = 0;
    	for (int i = 0; i < 8; i++) {
    		for (int j = 0; j < 8; j++) {
    			bw += board[i][j];
    		}
    	}
    	if (bw > 0) {
    		return 1;
    	}
    	else if (bw < 0) {
    		return -1;
    	}
    	else {
    		return 0;
    	}
    }

    public int[][] get_board() { //盤面情報の取得
        return board;
    }

    public int get_turn() { //手番情報の取得
        return turn;
    }
    
    public int result() { //終了状態の取得
    	return win;
    }
}

public class Othello { //確認用
	public static void main(String[] args) {
		Othell o = new Othell();
		Scanner sc = new Scanner(System.in);
		int x,y;
		int color = 1;
		o.print();
		while (o.check_end(0) == 0) {
			do {
				x = sc.nextInt();
				y = sc.nextInt();
			} while(o.put(color,x,y) == 1);
			o.reload();
			color *= -1;
			o.print();
		}		
	}
}
