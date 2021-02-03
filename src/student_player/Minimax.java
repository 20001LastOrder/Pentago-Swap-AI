package student_player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import boardgame.Board;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;
import student_player.MyTools.Node;

public class Minimax {
	
	private static final UnaryOperator<PentagoCoord> getNextHorizontal = c -> new PentagoCoord(c.getX(), c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextVertical = c -> new PentagoCoord(c.getX()+1, c.getY());
    private static final UnaryOperator<PentagoCoord> getNextDiagRight = c -> new PentagoCoord(c.getX()+1, c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextDiagLeft = c -> new PentagoCoord(c.getX()+1, c.getY()-1);
    
	
	//scores for different situations
	static class Score{
		public static final int ONE = 10;
		public static final int TWO = 100;
		public static final int THREE = 1000;
		public static final int FOUR = 100000;
		public static final int FIVE = 10000000;
		public static final int BLOCKED_ONE = 1;
		public static final int BLOCKED_TWO = 10;
		public static final int BLOCKED_THREE = 100;
		public static final int BLOCKED_FOUR = 10000;
	}
	
	static class Node {
		private PentagoMove move; // move to take parent to this state
		private PentagoBoardState state;
		private List<PentagoMove> childrenMoves;
		private int score;

		public PentagoMove getMove() {
			return move;
		}

		public void setMove(PentagoMove move) {
			this.move = move;
		}
		
		public void setScore(int score) {
			this.score = score;
		}
		
		public int getScore() {
			return score;
		}
	}
	
	public static Piece playerPiece;
	public static Piece otherPiece;
	public static int player_id;
	public static int other_id;
	
    public static Node alphaBeta(PentagoBoardState state, int depth, Piece player, int alpha, int beta) {
    	ArrayList<PentagoMove> nextMoves = state.getAllLegalMoves();		// 	Create a list of possible moves
    	Piece otherP;
    	int thisOppId;
		if(playerPiece == player) {
			otherP = otherPiece;
			thisOppId = other_id;
		}else {
			otherP = playerPiece;
			thisOppId = player_id;
		}
    	    	
    	int score;
    	Node bestMove = new Node();
    	if(nextMoves.isEmpty() || depth == 0) {	
    		score = evaluate(player, state);
    		bestMove.setScore(score);
    		return bestMove;
    		
    	} else {
    	
    		for(PentagoMove move: nextMoves) {
    			PentagoBoardState newClonedState = (PentagoBoardState)state.clone();
    			newClonedState.processMove(move);
    			if(player == playerPiece) {	// is maximizing player
    				score = alphaBeta(newClonedState, depth -1, otherPiece, alpha, beta).getScore();
    				
    				if(score > alpha) {
    					alpha = score;
    					bestMove.setMove(move);
    				}
    			} else {
    				score = alphaBeta(newClonedState, depth - 1, playerPiece, alpha, beta).getScore();
    				if(score < beta) {
    					beta = score;
    					bestMove.setMove(move);
    				}
    			}
    			if (alpha > beta) break;
    		}
    	}
    	
		if(player == playerPiece) {
			Node result = new Node();
			result.setMove(bestMove.getMove());
			result.setScore(alpha);
			return result;
		} else {
			Node result = new Node();
			result.setMove(bestMove.getMove());
			result.setScore(beta);
			return result;
		}
    }
	
	
	public static int evaluate(Piece p, PentagoBoardState state) {
		Piece otherP;
		if(playerPiece == p) {
			otherP = otherPiece;
		}else {
			otherP = playerPiece;
		}
		
		PentagoCoord tl = new PentagoCoord(0,0);
		PentagoCoord br = new PentagoCoord(0,5);
		int score = 0;
		int otherScore = 0;
		score += updateScore(getNextDiagRight, state, p, tl);
		otherScore += updateScore(getNextDiagRight, state, otherP, tl);
//		if(checkCrisisPattern(getNextDiagRight, state, p, tl)) {
//			return Score.FIVE;
//		}else if(checkCrisisPattern(getNextDiagRight, state, otherP, tl)) {
//			return -Score.FIVE;
//		}

		score += updateScore(getNextDiagLeft, state, p, br);
		otherScore += updateScore(getNextDiagLeft, state, otherP, br);
//		if(checkCrisisPattern(getNextDiagLeft, state, p, br)) {
//			return Score.FIVE;
//		}else if(checkCrisisPattern(getNextDiagLeft, state, otherP, br)) {
//			return -Score.FIVE;
//		}
		for(int i = 0; i < 5; i++) {
			score += updateScore(getNextHorizontal, state, p, new PentagoCoord(i,0));
			otherScore += updateScore(getNextHorizontal, state, otherP, new PentagoCoord(i,0));
			
//			if(checkCrisisPattern(getNextHorizontal, state, p, new PentagoCoord(i,0))) {
//				return Score.FIVE;
//			}else if(checkCrisisPattern(getNextHorizontal, state, otherP, new PentagoCoord(i,0))) {
//				return -Score.FIVE;
//			}
		}
		
		//check all verticals
		for(int i = 0; i < 5; i++) {
			score += updateScore(getNextVertical, state, p, new PentagoCoord(0,i));
			otherScore += updateScore(getNextVertical, state, otherP, new PentagoCoord(0,i));
//			if(checkCrisisPattern(getNextHorizontal, state, p, new PentagoCoord(0,i))) {
//				return Score.FIVE;
//			}else if(checkCrisisPattern(getNextHorizontal, state, otherP, new PentagoCoord(0,i))) {
//				return -Score.FIVE;
//			}
		}
		
		int finalScore = (score - otherScore); 
		
		return finalScore;
	}
	
	//check if the board has four successive pattern of the same color
	//eg: _****_
	private static int updateScore(UnaryOperator<PentagoCoord> operator, PentagoBoardState state, 
										Piece piece, PentagoCoord coor) {
		
		boolean foundP = false;
		int blockedCount = 0;
		int count = 0;
		
		for(int i = 0; i < 5; i++) {
			
			// check successive pattern of P
			if(state.getPieceAt(coor) == piece) {
				if(foundP) {
					count++;
				}else {
					foundP = true;
					count = 1;
				}
			//if found a place without P, means that P has been blocked
			}else if(state.getPieceAt(coor) != Piece.EMPTY) {
				blockedCount++;
			}
			
			//break look as soon as we finish the pattern found
			if(state.getPieceAt(coor) != piece && foundP) {
				break;
			}
			coor = operator.apply(coor);
		}
		
		if(blockedCount == 0) {
			switch(count) {
				case 1:
					return Score.ONE;
				case 2:
					return Score.TWO;
				case 3:
					return Score.THREE;
				case 4:
					return Score.FOUR;
				case 5:
					return Score.FIVE;
			}
		}else if(blockedCount == 1) {
			switch(count) {
			case 1:
				return Score.BLOCKED_ONE;
			case 2:
				return Score.BLOCKED_TWO;
			case 3:
				return Score.BLOCKED_THREE;
			case 4:
				return Score.BLOCKED_FOUR;
			case 5:
				return Score.FIVE;
			}
		}
		
		return 0;
		
	}
	
	private static boolean checkCrisisPattern(UnaryOperator<PentagoCoord> operator, PentagoBoardState state, 
			Piece piece, PentagoCoord coor) {
		
		for(int i = 0; i < 4; i++) {
			coor = operator.apply(coor);
			if(state.getPieceAt(coor) != piece) {
				return false;
			}
		}
			coor = operator.apply(coor);
		if(state.getPieceAt(coor) == Piece.EMPTY) {
			return true;
		}
			return false;

	}
}
