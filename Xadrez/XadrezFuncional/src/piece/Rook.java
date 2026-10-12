package piece;

import main.GamePanel;
import main.Type;

public class Rook extends Piece{

	public Rook(int color, int col, int row) {
		super(color, col, row);
		
		type = Type.ROOK;
			
			if(color == GamePanel.WHITE) {
				image = getImage("/piece/w-rook");
			}
			else {
				image = getImage("/piece/b-rook");
			}
		}
	public boolean canMove(int targetCol, int targetRow) {
		if(isWithinBoard(targetCol,targetRow) && isSameSquare(targetCol, targetRow) == false) {
			// a torre consegue se mover enquanto o Col e ROw não mudarem
			if(targetCol == preCol || targetRow == preRow) {
				if(isValidSquare(targetCol, targetRow) && piecesIsOnStraightLine(targetCol,targetRow) == false) {
					return true;
				}
			}
			
		}
		return false;
	}

	
	}

