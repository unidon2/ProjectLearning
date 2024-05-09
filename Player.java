/* 最終更新 5/9/14:07 */

public class Player {

	private String myName = ""; //プレイヤ名
	private int myColor; //先手後手情報(白黒)

	// メソッド
	public void setName(String name){ // プレイヤ名を受付
		myName = name;
	}
	public String getName(){	// プレイヤ名を取得
		return myName;
	}
	public void setColor(int c){ // 先手後手情報の受付
		myColor = c;
	}
	public int getColor(){ // 先手後手情報の取得
		return myColor;
	}
}