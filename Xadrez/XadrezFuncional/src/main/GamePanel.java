package main;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JPanel;

import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Piece;
import piece.Queen;
import piece.Rook;

public class GamePanel extends JPanel implements Runnable{
	
	public static final int WIDTH = 1100;
	public static final int HEIGHT = 800;
	final int FPS = 60;
	Thread gameThread;// faz o loop do jogo acontecer
	Board board = new Board();
	Mouse mouse = new Mouse();
	
	//PEÇAS
	public static ArrayList<Piece> pieces = new ArrayList<>();
	public static ArrayList<Piece> simPieces = new ArrayList<>();
	ArrayList<Piece> promoPieces = new ArrayList<>();
	Piece activeP, checkingP;
	public static Piece castlingP;
	
	//COR
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	int currentColor = WHITE;
	
	//BOOLEANOS
	boolean canMove;
	boolean validSquare;
	boolean promotion;
	boolean gameover;
	boolean stalemate;
	
	
	public GamePanel() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setBackground(Color.black);
		addMouseMotionListener(mouse);
		addMouseListener(mouse);
		
		setPieces();
	//	testPromotion();
	//	testIllegal();
		copyPieces(pieces, simPieces);
	}
	
	public void launchGame() {
		gameThread = new Thread(this);
		gameThread.start();// chama o metodo run
		
	}
	public void setPieces() {
		
		//Time Branco
		pieces.add(new Pawn(WHITE, 0, 6));
		pieces.add(new Pawn(WHITE, 1, 6));
		pieces.add(new Pawn(WHITE, 2, 6));
		pieces.add(new Pawn(WHITE, 3, 6));
		pieces.add(new Pawn(WHITE, 4, 6));
		pieces.add(new Pawn(WHITE, 5, 6));
		pieces.add(new Pawn(WHITE, 6, 6));
		pieces.add(new Pawn(WHITE, 7, 6));
		pieces.add(new Rook(WHITE, 0, 7));
		pieces.add(new Rook(WHITE, 7, 7));
		pieces.add(new Knight(WHITE, 1, 7));
		pieces.add(new Knight(WHITE, 6, 7));
		pieces.add(new Bishop(WHITE, 2, 7));
		pieces.add(new Bishop(WHITE, 5, 7));
		pieces.add(new Queen(WHITE, 3, 7));
		pieces.add(new King(WHITE, 4, 7));
		
		//Time Preto
		pieces.add(new Pawn(BLACK, 0, 1));
		pieces.add(new Pawn(BLACK, 1, 1));
		pieces.add(new Pawn(BLACK, 2, 1));
		pieces.add(new Pawn(BLACK, 3, 1));
		pieces.add(new Pawn(BLACK, 4, 1));
		pieces.add(new Pawn(BLACK, 5, 1));
		pieces.add(new Pawn(BLACK, 6, 1));
		pieces.add(new Pawn(BLACK, 7, 1));
		pieces.add(new Rook(BLACK, 0, 0));
		pieces.add(new Rook(BLACK, 7, 0));
		pieces.add(new Knight(BLACK, 1,0));
		pieces.add(new Knight(BLACK, 6,0));
		pieces.add(new Bishop(BLACK, 2,0));
		pieces.add(new Bishop(BLACK, 5,0));
		pieces.add(new Queen(BLACK, 3,0));
		pieces.add(new King(BLACK, 4,0));
		
	}
	public void testPromotion() {
		pieces.add(new Pawn(WHITE, 0, 3));
		pieces.add(new Pawn(BLACK, 5, 4));
	}
	public void testIllegal() {
		pieces.add(new Pawn(WHITE, 7, 6));
		pieces.add(new King(WHITE, 3, 7));
		pieces.add(new King(BLACK, 0, 3));
		pieces.add(new Bishop(BLACK, 1, 4));
		pieces.add(new Queen(BLACK, 4, 5));
	}
	
	private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
		
		target.clear();
		for(int i = 0; i < source.size(); i ++) {
			target.add(source.get(i));
		}
	}
	
	@Override
	public void run() { // pro runnable funcionar adicionamos o run
		
		//LOOP DO JOGO
		double drawInterval = 1000000000/ FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;
		
		while(gameThread != null) {
			
			currentTime = System.nanoTime();
			
			delta += (currentTime - lastTime)/drawInterval;
			lastTime = currentTime;
			
			if(delta >= 1) {
				update();
				repaint();
				delta--;
			}
		}
	}
	
	private void update() { // vai ser responsavel pelos updates, como posição das peças
		
		if(promotion) {
			promoting();
		}
		else if(gameover == false && stalemate == false){
			//BOTÃO DO MOAUSE PRECIONADO
			if(mouse.pressed) {
				if(activeP == null) {
					//se o activeP é null então ele checa para pegar uma peça
					for(Piece piece : simPieces) {
						//se o mouse estiver em uma peça aliad pega ela e conta com uma activeP
						if(piece.color == currentColor && 
								piece.col == mouse.x/Board.SQUARE_SIZE &&
								piece.row == mouse.y/Board.SQUARE_SIZE) {
							
							activeP = piece;
						}
					}
				}
				else {
					//caso o player esteja segurando uma peça ele simula o movimento
					simulate();
				}
			}
			
			///// LARGAR O BOTÃO DO MOUSE /////
			if(mouse.pressed == false) {
				
				if(activeP != null) {
					if(validSquare) {
						
						
						
						copyPieces(simPieces, pieces);
						activeP.updatePosition();// quando o player largar o botão do mouse ele chama o metodo update
						if(castlingP != null) {
							castlingP.updatePosition();
						}
						
						if(isKingInCheck() && isCheckmate()) {
							gameover = true;
						}
						else if (isStalemate() && isKingInCheck() == false) {
							stalemate = true;
						}
						else {// O jogo ainda esta acontecendo
							
							if(canPromote()) {
								promotion = true;
							}
							else { //O jogo ainda esta rodando
								
								changePlayer();
							}
						}
																
					}
					else {
						copyPieces(pieces, simPieces);
						activeP.resetPosition();
						activeP = null;
					}
					 
					
				}
			}
		}
		

	}
	private void simulate() {
		
		canMove = false;
		validSquare = false;
		
		copyPieces(pieces, simPieces);
		
		//Reseta a posição do Roque
		if(castlingP != null) {
			castlingP.col = castlingP.preCol;
			castlingP.x = castlingP.getX(castlingP.col);
			castlingP = null;
		}
		
		//caso a peça esteja sendo segurada faz o updade desssa posisão
		activeP.x = mouse.x - Board.RALF_SQUARE_SIZE;
		activeP.y = mouse.y - Board.RALF_SQUARE_SIZE;
		activeP.col = activeP.getCol(activeP.x);
		activeP.row = activeP.getRow(activeP.y);
		
		//Checa para ver se a peça esta em um quadrado possivel de chegar
		if(activeP.canMove(activeP.col, activeP.row)) {
			canMove = true;
			
			//se colidir com outra peça inimiga faz ela desaparecer
			if(activeP.hittingP != null) {
				simPieces.remove(activeP.hittingP.getIndex());
			}
			checkCastling();
			
			if(isIllegal(activeP) == false && opponentCanCaptureKing() == false) {
				validSquare = true;
			}
			
			validSquare = true;
		}
	}
	private boolean isIllegal(Piece king) {
		
		if(king.type == Type.KING) {
			for(Piece piece :simPieces) {
				if(piece != king && piece.color != king.color && piece.canMove(king.col, king.row)) {
					return true;
				}
			}
		}
		
		return false;
	}
	private boolean opponentCanCaptureKing() {
		Piece king = getKing(false);
	
		for(Piece piece : simPieces) {
			if(piece.color != king.color && piece.canMove(king.col, king.row)) {
				return true;
			}
		}
		return false;
	}
	private boolean isKingInCheck() {
		
		Piece king = getKing(true);
		
		if(activeP.canMove(king.col, king.row)) {
			checkingP = activeP;
			return true;
		}
		else {
			checkingP = null;
		}
		
		return false;
	}
	private Piece getKing(boolean opponent) {
		Piece king = null;
		
		for(Piece piece : simPieces) {
			if(opponent) {
				if(piece.type == Type.KING && piece.color != currentColor) {
					king = piece;
				}
			}
			else {
				if(piece.type == Type.KING && piece.color == currentColor) {
					king = piece;
				}
			}
		}
		return king;
	}
	private boolean isCheckmate() {
		
		Piece king = getKing(true);
		
		if(KingCanMove(king)) {
			return false;
		}
		else {
			//A ultima chance é bloquear o ataque inimigo com alguma outra peça
			
			int colDiff = Math.abs(checkingP.col - king.col);
			int rowDiff = Math.abs(checkingP.row - king.row);
			
			if(colDiff == 0) {
				//A peça que faz o cheque esta vindo verticalmente
				if(checkingP.row < king.row) {
					//a peça que vai dar cheque no Rei esta vindo de cima
					for(int row = checkingP.row; row < king.row; row++) {
						for(Piece piece : simPieces) {
							if(piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
								return false;
							}
						}
					}
				}
				if(checkingP.row > king.row) {
					//A peça que vai dar o cheque ta vindo de baixo
					for(int row = checkingP.row; row > king.row; row--) {
						for(Piece piece : simPieces) {
							if(piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
								return false;
							}
						}
					}
				}
			}
			if(rowDiff == 0) {
				//A peça que faz o cheque esta vindo horizontalmente
				if(checkingP.col < king.col) {
					//A peça de cheque esta vindo da esquerda
					for(int col = checkingP.col; col < king.col; col++) {
						for(Piece piece : simPieces) {
							if(piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
								return false;
							}
						}
					}
				}
				if (checkingP.col > king.col) {
					//Apeça de chequie esta vindo da esquerda
					for(int col = checkingP.col; col > king.col; col--) {
						for(Piece piece : simPieces) {
							if(piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
								return false;
							}
						}
					}
				}
				
			}
			if(colDiff == rowDiff) {
				//A peça que faz o cheque esta vindo diagonalmente
				if(checkingP.row < king.row) {
					//a peça que ets dando cheque no rei vem de cima
					if(checkingP.col < king.col) {
						//A peça esta na esquerda em cima
						for(int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col, row)) {
									return false;
								}
							}
						}
					}
					if(checkingP.col > king.col) {
						//A peça esta na direita em cima
						for(int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col, row)) {
									return false;
								}
							}
						}
					}
				}
				if(checkingP.row > king.row) {
					//a peça que ets dando cheque no rei vem de baixo
					if(checkingP.col < king.col) {
						//A peça esta na esquerda em baixo
						for(int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col, row)) {
									return false;
								}
							}
						}
					}
					if(checkingP.col > king.col) {
						//A peça esta na direita em baixo
						for(int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col, row)) {
									return false;
								}
							}
						}
					}
				}
				
			}
//			else { Não é necessario pois o ataque do cavaleiro é um pulo ou seja ele pula por cima de outras peças
//				//A peça que faz o cheque é o cavalo
//				
//			}
		}
		
		return true;
	}
	private boolean KingCanMove(Piece king) {
		
		//simula se team algum quadrado para onde o rei possa se mover
		if(isValidMove(king, -1, -1)) {return true;}
		if(isValidMove(king, 0, -1)) {return true;}
		if(isValidMove(king, 1, -1)) {return true;}
		if(isValidMove(king, -1, 0)) {return true;}
		if(isValidMove(king, 1, 0)) {return true;}
		if(isValidMove(king, -1, 1)) {return true;}
		if(isValidMove(king, 0, 1)) {return true;}
		if(isValidMove(king, 1, 1)) {return true;}
		
		return false;
	}
	private boolean isValidMove(Piece king, int colPlus, int rowPlus) {
		
		boolean isValidMove = false;
				
		//Faz o update do Rei a cada segundo
		king.col += colPlus;
		king.row += rowPlus;
		
		if(king.canMove(king.col, king.row)) {
			
			if(king.hittingP != null) {
				simPieces.remove(king.hittingP.getIndex());
			}
			if(isIllegal(king) == false) {
				isValidMove = true;
			}
		}
		//reseta a posição do rei e reseta as outras peças
		king.resetPosition();
		copyPieces(pieces, simPieces);
	
		return isValidMove;
	}
	
	private boolean isStalemate() {
		
		int count = 0;
		//Conta o número de peças
		for(Piece piece : simPieces) {
			if(piece.color != currentColor) {
				count++;
			}
		}
		
		//Se a unica peça sobrando for o rei
		if(count == 1) {
			if(KingCanMove(getKing(true)) == false) {
				return true;
			}
		}
	
		return false;
	}
	private void checkCastling() {
		
		if(castlingP != null) {
			if(castlingP.col == 0) {
				castlingP.col += 3;
			}
			else if(castlingP.col == 7) {
				castlingP.col -= 2;
			}
			castlingP.x = castlingP.getX(castlingP.col);
		}
	}
	private void changePlayer() {
		
		if(currentColor == WHITE) {
			currentColor = BLACK;
			//Resesta os dois quadrados andados das peças pretas
			for(Piece piece : pieces) {
				if(piece.color == BLACK) {
					piece.twoStepped = false;
				}
			}
		}
		else {
			currentColor =WHITE;
			//Resesta os dois quadrados andados das peças brancas
			for(Piece piece : pieces) {
				if(piece.color == WHITE) {
					piece.twoStepped = false;
				}
			}
		}
		activeP = null;
	}
	private boolean canPromote() {
		
		if(activeP.type == Type.PAWN) {
			if(currentColor == WHITE && activeP.row == 0 || currentColor == BLACK && activeP.row == 7) {
				promoPieces.clear();
				promoPieces.add(new Rook(currentColor, 9,2));
				promoPieces.add(new Knight(currentColor, 9,3));
				promoPieces.add(new Bishop(currentColor, 9,4));
				promoPieces.add(new Queen(currentColor, 9,5));
				return true;
			}
		}
		
		return false;
	}
	private void promoting() {
		
		if(mouse.pressed) {
			for(Piece piece : promoPieces) {
				if(piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE) {
					switch(piece.type) {
					case ROOK: simPieces.add(new Rook(currentColor, activeP.col, activeP.row)); break;
					case KNIGHT: simPieces.add(new Knight(currentColor, activeP.col, activeP.row)); break;
					case BISHOP: simPieces.add(new Bishop(currentColor, activeP.col, activeP.row)); break;
					case QUENN: simPieces.add(new Queen(currentColor, activeP.col, activeP.row)); break;
					default: break;
					}
					simPieces.remove(activeP.getIndex());
					copyPieces(simPieces, pieces);
					activeP = null;
					promotion = false;
					changePlayer();
				}
			}
		}
	}
	public void paintComponent(Graphics g) { // é usado para "desenhar" cada elemento do painel
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		
		//TABULEIRO
		board.draw(g2);
		
		//PEÇAS
		for(Piece p : simPieces) {
			p.draw(g2);
		}
		//OPACIDADE DA PEÇA
		if(activeP != null) {
			if(canMove) {
				if(isIllegal(activeP) || opponentCanCaptureKing()) {
					g2.setColor(Color.gray);
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
					g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE,
							Board.SQUARE_SIZE, Board.SQUARE_SIZE);
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));	
				}
				else {
					g2.setColor(Color.white);
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
					g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE,
							Board.SQUARE_SIZE, Board.SQUARE_SIZE);
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));	
				}
			
			}

			
			activeP.draw(g2);
		}
		
		//MENSAGEM DE STATUS
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
		g2.setColor(Color.white);
		
		if(promotion) {
			g2.drawString("Promover para:", 840, 150);
			for(Piece piece : promoPieces) {
				g2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row),
						Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
			}
		}
		else {
			if(currentColor == WHITE) {
				g2.drawString("Peças Brancas",840, 550);
				if(checkingP != null && checkingP.color == BLACK) {
					g2.setColor(Color.red);
					g2.drawString("O Rei", 840, 650);
					g2.drawString("Esta em check", 840, 700);
				}
			}
			else {
				g2.drawString("Peças Pretas",840, 250);
				if(checkingP != null && checkingP.color == WHITE) {
					g2.setColor(Color.red);
					g2.drawString("O Rei", 840, 100);
					g2.drawString("Esta em check", 840, 150);
			}	
		}
		

	}
		if(gameover) {
			String s ="";
			if(currentColor ==WHITE) {
				s = "Branco Venceu";
			}
			else {
				s = "Preto Venceu";
			}
			g2.setFont(new Font("Arial", Font.PLAIN, 90));
			g2.setColor(Color.green);
			g2.drawString(s, 200, 420);
		}
		if(stalemate) {
			g2.setFont(new Font("Arial", Font.PLAIN, 90));
			g2.setColor(Color.lightGray);
			g2.drawString("Stalemate", 200, 420);
			
		}
	}
	
}
		