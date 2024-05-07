import java.util.Scanner;

class Othell {
    /* 空き: 0　黒: 1　白: -1  置ける: +-2 両方置ける: 3*/
    private int[][] board = new int[8][8];
    private int[][] dir = {{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1}};
    private int turn;
    private int color;
    private int time;
    private int win;

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
        set_time(1000);
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
                        if (already == 0 && (board[i+dis*dir[d][0]][j+dis*dir[d][1]] == 1 || board[i+dis*dir[d][0]][j+dis*dir[d][1]] == -1)) {
                            already = 1;
                            cl = board[i+dis*dir[d][0]][j+dis*dir[d][1]];
                            dis++;
                        }
                        else if (board[i+dis*dir[d][0]][j+dis*dir[d][1]] == cl && already == 1) {
                        	dis++;
                        }
                        else if (board[i+dis*dir[d][0]][j+dis*dir[d][1]] == -cl && already == 1) {
                        	if (can == 2*cl) {
                        		can = 3;
                        	}
                        	else {
                        		can = -2*cl;
                        	}
                            break;
                        }
                        else {
                            break;
                        }
                    }
                    if (can == 3) {
                    	break;
                    }
                }
                if (can != 0) {
                    board[i][j] = can;
                }
                else {
                	board[i][j] = 0;
                }
            }
        }
    }
    

    

    public int put(int color, int i, int j) {
        int success = 0;
        if (board[i][j] != color*2 && board[i][j] != 3) {
        	return 1;
        }
        for (int d = 0; d < 8; d++) {
            int dis = 1;
            int already = 0;
            int can = 0;
            while (0 <= i+dis*dir[d][0] && i+dis*dir[d][0] < 8 && 0 <= j+dis*dir[d][1] && j+dis*dir[d][1] < 8) {
                if (board[i+dis*dir[d][0]][j+dis*dir[d][1]] == -color) {
                    already = 1;
                    dis++;
                }
                else if (board[i+dis*dir[d][0]][j+dis*dir[d][1]] == color && already == 1) {
                    can = 1;
                    break;
                }
                else {
                    break;
                }
            }
            if (can == 1) {
                success = 1;
                for (int change = 0; change < dis; change++) {
                    board[i+change*dir[d][0]][j+change*dir[d][1]] = color;
                }
            }
        }
        if (success == 1) {
            changeturn();
            return 0;
        }
        else {
        	return 1;
        }
    }

    public void print() {
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

    void changeturn() {
        turn *= -1;
        return;
    }
    
    void set_time(int tm) {
    	time = tm;
    }

    int get_time() {
        return time;
    }

    int check_end(int sp) {
    	if (sp != 0) {
    		win = sp;
    		return 1;
    	}	
    	if (time < 0) {
    		System.out.println("time over");
    		win = -color;
    		return 1;
    	}
    	for (int i = 0; i < 8; i++) {
    		for (int j = 0; j < 8; j++) {
    			if (board[i][j] == 2 || board[i][j] == -2 || board[i][j] == 3) {
    				return 0;
    			}
    		}
    	}
    	System.out.println("no one can put");
    	return 1;
    }

    public int[][] get_board() {
        return board;
    }

    public int get_turn() {
        return turn;
    }
    
    public int result() {
    	return win;
    }
}

public class Othello {
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
