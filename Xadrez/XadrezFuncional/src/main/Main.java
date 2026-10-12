package main;

import javax.swing.JFrame;

public class Main {
	public static void main (String[]args) {
		
	 JFrame window = new JFrame("simple Chess"); //nome da pagina
	 window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// quando a pagina fecha o jogo também
	 window.setResizable(false);//não sera possivel realterar o tamanho da tela
	 
	 // Adiciona o GamePanel Para a Janela
	 GamePanel gp = new GamePanel ();
	 window.add(gp);
	 window.pack();
	 
	 window.setLocationRelativeTo(null);// vai sempre mostrar no centro do monitor
	 window.setVisible(true);//vai mostrar a tela
	 
	 gp.launchGame();
	}

}
