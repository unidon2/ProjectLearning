/* 最終更新 5/9/14:07 */

public class Othello {
    /* 空き: 0　黒: 1　白: -1  置ける: +-2 両方置ける: 3*/
    private int[][] Grid = new int[8][8]; //盤面情報
    private int[][] dir = {{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1}}; //8方向への移動用
    private int turn; //手番情報
    private int win; //勝敗情報(0: 引き分け  1,-1: 枚数での勝敗  2,-2: 時間切れでの勝敗  3,-3: 投了 それ以外:切断など)

    public Othello() { //盤面の初期化
        turn = 100;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i == 3 && j == 3 )|| (i == 4 && j == 4)) {
                    Grid[i][j] = 1;
                } 
                else if ((i == 3 && j == 4) || (i == 4 && j == 3)) {
                    Grid[i][j] = -1;
                }
                else {
                    Grid[i][j] = 0;
                }
            }
        }
        reload();
    }
    

    

    public void reload() { //盤面の更新
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (Grid[i][j] == 1 || Grid[i][j] == -1) { //既に置かれているマスは無視する
                    continue;
                }
                int can = 0; //そのマスに置ける人間が存在するか
                for (int d = 0; d < 8; d++) { //8方向に対して
                    int dis = 1; //元のマスからの距離
                    int already = 0; //挟めるマスが存在するか
                    int cl = 100; //挟めるマスの色
                    while (0 <= i+dis*dir[d][0] && i+dis*dir[d][0] < 8 && 0 <= j+dis*dir[d][1] && j+dis*dir[d][1] < 8) { //盤面外に出ない間
                        if (already == 0 && (Grid[i+dis*dir[d][0]][j+dis*dir[d][1]] == 1 || Grid[i+dis*dir[d][0]][j+dis*dir[d][1]] == -1)) { //隣のマスに置かれている場合
                            already = 1; //フラグ管理
                            cl = Grid[i+dis*dir[d][0]][j+dis*dir[d][1]]; //隣のマスの色記憶
                            dis++; //1つ離れる
                        }
                        else if (Grid[i+dis*dir[d][0]][j+dis*dir[d][1]] == cl && already == 1) { //挟めるマスが続いている場合
                        	dis++; //1つ離れる
                        }
                        else if (Grid[i+dis*dir[d][0]][j+dis*dir[d][1]] == -cl && already == 1) { //挟める場合(挟めるマスと違う色が現れた場合)
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
                    Grid[i][j] = can;
                }
                else { //逆に置けなくなる場合もあるので0で更新
                	Grid[i][j] = 0;
                }
            }
        }
    }
    
    public int pass(int color) { //パスするか(置ける場所が存在しないか) (返り値  置ける: 0    パス: 1)
    	for (int i = 0; i < 8; i++) {
    		for (int j = 0; j < 8; j++) {
    			if (Grid[i][j] == 2*color || Grid[i][j] == 3) {
    				return 0;
    			}
    		}
    	}
    	return 1;
    }
    

    

    public int put(int color, int i, int j) { //置かれた際の処理(返り値 0: 置けた   1: 置けなかった)
        int success = 0; //フラグ(置くことができるか)
        if (i == 100) {
        	return 0;
        }
        if (Grid[i][j] != color*2 && Grid[i][j] != 3) { //そのマスには置けない場合
        	return 1;
        }
        /* あらかじめ置ける場所は演算しているので以降の処理は置けることを前提としています */
        /* が念のため本当に置けるのかを 変数:success で管理しています*/
        for (int d = 0; d < 8; d++) { //8方向に対して
            int dis = 1; //元のマスからの距離
            int already = 0; //挟めるマスが存在するか
            int can = 0; //挟めたか
            while (0 <= i+dis*dir[d][0] && i+dis*dir[d][0] < 8 && 0 <= j+dis*dir[d][1] && j+dis*dir[d][1] < 8) { //盤面内の間
                if (Grid[i+dis*dir[d][0]][j+dis*dir[d][1]] == -color) { //置こうとしている色でない色のマスがある間 距離を伸ばし続ける
                    already = 1; //挟めるマスが存在する
                    dis++; //1マス離れる
                }
                else if (Grid[i+dis*dir[d][0]][j+dis*dir[d][1]] == color && already == 1) { //置こうとしている色と同じマスが見つかった∩既に1マス以上挟めるマスが存在している
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
                    Grid[i+change*dir[d][0]][j+change*dir[d][1]] = color; //色を塗り替える
                }
            }
        }
        if (success == 1) { //置けた場合
            changeTurn(); //手番の変更
            reload();
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
                if(Grid[i][j] == 0) {
                    System.out.print("  |");
                }
                else if(Grid[i][j] == 1) {
                    System.out.print("〇|");
                }
                else if(Grid[i][j] == -1) {
                    System.out.print("●|");
                }
                else if(Grid[i][j] == 2) {
                    System.out.print(" *|");
                }
                else if (Grid[i][j] == -2) {
                	System.out.print(" +|");
                }
                else if (Grid[i][j] == 3) {
                	System.out.print(" !|");
                }
            }
            System.out.println();
        }
        System.out.println();
	}

    void changeTurn() { //手番変更
        turn *= -1;
        return;
    }
    

    public int check_end(int sp) { //終了判定(返り値 終了していない: 0     終了: 1)
    	if (sp != 0) { //特殊な終了(投了、切断、時間切れもこちらに含まれる想定です）
    		win = sp;
    		return 1;
    	}
    	for (int i = 0; i < 8; i++) {
    		for (int j = 0; j < 8; j++) {
    			if (Grid[i][j] == 2 || Grid[i][j] == -2 || Grid[i][j] == 3) { //どちらかに置けるマスがまだある場合
    				return 0; //まだ終了でない
    			}
    		}
    	}
    	/* ここに来た時点で全て埋まっている or どちらも置けない状態 */
    	win = count(0); //枚数カウントによる勝敗の更新
    	System.out.println("no one can put"); //確認用
    	return 1;
    }
    
    int count(int q) { //枚数カウント(返り値: 終了時:±1, 全てのマスが埋まっていない場合:±2, 引き分け(全埋まり):100, 引き分け（埋まっていない):-100)
    	int bw = 0;
    	int count = 0;
    	for (int i = 0; i < 8; i++) {
    		for (int j = 0; j < 8; j++) {
    			if (Grid[i][j] != 0) {
    				count++;
    			}
    			bw += Grid[i][j];
    		}
    	}
    	if (q == 0) {
	    	if (bw > 0 && count == 64) {
	    		return 1;
	    	}
	    	else if (bw > 0) {
	    		return 2;
	    	}
	    	else if (bw < 0 && count == 64) {
	    		return -1;
	    	}
	    	else if (bw < 0) {
	    		return -2;
	    	}
	    	else if (count == 64) {
	    		return 100;
	    	}
	    	else {
	    		return -100;
	    	}
    	}
    	else {
    		return count;
    	}
    }

    public int[] getGrid() { //盤面情報の取得(1次元配列化)
    	int[] g = new int[64];
    	for (int i = 0; i < 64; i++) {
    		g[i] = Grid[i/8][i%8];
    	}
        return g;
    }

    public void start() {
    	turn = 1;
    	return;
    }
    
    public int getTurn() { //手番情報の取得
        return turn;
    }
    
    public int result() { //終了状態の取得
    	return win;
    }
}
