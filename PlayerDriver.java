public class PlayerDriver {
	public static void main(String [] args) throws Exception{
		Player player = new Player();
		System.out.println("setNameで「電情太郎」を入力します");
		player.setName("電情太郎");
		System.out.println("getName出力: " + player.getName());
		System.out.println("setColorで「1」を入力します(黒：1, 白：-1)");
		player.setColor(1);
		System.out.println("getColor:" + player.getColor());
	}
}
