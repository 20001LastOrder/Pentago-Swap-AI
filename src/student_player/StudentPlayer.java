package student_player;

import java.util.ArrayList;

import boardgame.Move;
import pentago_swap.*;
import pentago_swap.PentagoBoardState.Piece;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("xxxxxxxxx");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
        //MyTools.getSomething();

        //always start with put piece at the center of a quadrant
        if (boardState.getTurnNumber() < 2) {
        		for (PentagoMove move:boardState.getAllLegalMoves()) {
        			PentagoCoord coord = move.getMoveCoord();
        			if ((coord.getX()==1&&coord.getY()==1) || (coord.getX()==1&&coord.getY()==4) 
        					|| (coord.getX()==4&&coord.getY()==1) || (coord.getX()==4&&coord.getY()==4)) return move;
        		}
        	}
        
        int opponent = boardState.getOpponent();
        Piece opponentPiece;
        Piece myPiece;
        if (opponent==1)  {
        		opponentPiece = Piece.BLACK;
        		myPiece = Piece.WHITE;
        }
        else {
        		opponentPiece = Piece.WHITE;
        		myPiece = Piece.BLACK;
        }
        
        ArrayList<PentagoMove> legalMoves = boardState.getAllLegalMoves();
       
        Piece [][] pieceMap= new Piece[6][6];
        for (int i=0; i<6; i++) {
        		for (int j=0; j<6; j++)  pieceMap[i][j]=boardState.getPieceAt(i, j);
        }
        // break offensive move
        if (boardState.getTurnNumber() < 6) {
        		for (int i=0; i<6; i++) {
        			int count = 0;
        			for (int j=0; j<6; j++) {
        				if ( pieceMap[i][j]==opponentPiece )  count ++;
        				if ( pieceMap[i][j]==myPiece ) break;
        			}
        			if (count >= 3) {
        				for (PentagoMove move:boardState.getAllLegalMoves()) {
                			PentagoCoord coord = move.getMoveCoord();
                			if (coord.getX()==i)  return move;
                		}
        			}
        		}
        		for (int i=0; i<6; i++) {
        			int count = 0;
        			for (int j=0; j<6; j++) {
        				if ( pieceMap[j][i]==opponentPiece )  count ++;
        				if ( pieceMap[j][i]==myPiece ) break;
        			}
        			if (count >= 3) {
        				for (PentagoMove move:boardState.getAllLegalMoves()) {
                			PentagoCoord coord = move.getMoveCoord();
                			if (coord.getY()==i)  return move;
                		}
        			}
        		}
        }
        
        
        // choose winning move, eliminate losing moves
        for (int i=legalMoves.size()-1; i>=0; i--) {
    			PentagoMove move = legalMoves.get(i);
    			PentagoBoardState nextboardState1 = (PentagoBoardState) boardState.clone();
    			nextboardState1.processMove(move);
    			if (nextboardState1.gameOver() && nextboardState1.getWinner()!=opponent) return move;
    			else {
    				for (PentagoMove opponentMove : nextboardState1.getAllLegalMoves()) {
        				PentagoBoardState nextboardState2 = (PentagoBoardState) nextboardState1.clone(); 
        				nextboardState2.processMove(opponentMove);
        				if (nextboardState2.getWinner()==opponent) {
        					legalMoves.remove(i);
        					break;
        				}
        			}
    			}
        }
        
        if (legalMoves.size()<=0) {
        		return boardState.getRandomMove(); 
        }
        
        
        // Simplified MCTS
        int[] simulationResult = new int[legalMoves.size()]; 
       
        
	    for (int i=0; i<legalMoves.size(); i++) {
	    		PentagoMove move = legalMoves.get(i);
	    		PentagoBoardState treeMoveState = (PentagoBoardState) boardState.clone();
	    		treeMoveState.processMove(move);
	    		for (int j=0; j<30; j++) {
	    			PentagoBoardState defaultMoveState = (PentagoBoardState) treeMoveState.clone();
	    			int currentTurnNumber = defaultMoveState.getTurnNumber();
	    			while ( !defaultMoveState.gameOver() && defaultMoveState.getTurnNumber()>10) {
	    				defaultMoveState.getRandomMove();
	    			}
	    			if (defaultMoveState.getWinner()!=opponent) simulationResult[i]++;
	    		}
	    	}
        
        
        
        int optMove = 0;
        int maxWinning = simulationResult[0];
        for (int i=0; i<simulationResult.length; i++) {
        		if (simulationResult[i]>maxWinning) {
        			optMove = i;
        			maxWinning = simulationResult[i];
        		}
        }
        
        
        // Return move to be processed by the server.
        Move myMove = legalMoves.get(optMove);
        return myMove;
    }
}