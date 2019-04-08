package student_player;

import boardgame.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.UnaryOperator;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoPlayer;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoBoardState.Quadrant;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;

/**
 * @author mgrenander
 */
public class StudentPlayer_i2 extends PentagoPlayer {
    public StudentPlayer_i2() {
    	super("ymynhrs");
    }





	
	//#TODO æµ‹è¯•å‰�å‡ æ¬¡random
	static int DEPTH = 3;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */


    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
    	//System.out.println(player_id);
    	
    	
    	if(( boardState.getTurnNumber() <= 1)) {
    		
    		Move myMove = boardState.getRandomMove();
    		while(true) {
    			int i = (int) (System.currentTimeMillis()%2)*3+1;
        		int j = (int) (System.currentTimeMillis()%2)*3+1;
        		if(boardState.isPlaceLegal(new PentagoCoord(i,j))) {
        			myMove = new PentagoMove(i,j,Quadrant.BL, Quadrant.BR, player_id);
        			break;
        		}
    		}
    		/*
    		if(boardState.isPlaceLegal(new PentagoCoord(1,1))) {
    			myMove = new PentagoMove(1,1,Quadrant.BL, Quadrant.BR, player_id);
    		}else if(boardState.isPlaceLegal(new PentagoCoord(4,1))) {
    			myMove = new PentagoMove(4,1,Quadrant.BL, Quadrant.BR, player_id);
    		}else if(boardState.isPlaceLegal(new PentagoCoord(1,4))) {
    			myMove = new PentagoMove(1,4,Quadrant.BL, Quadrant.BR, player_id);
    		}else if(boardState.isPlaceLegal(new PentagoCoord(4,4))) {
    			myMove = new PentagoMove(4,4,Quadrant.BL, Quadrant.BR, player_id);
    		}*/
    		return myMove;
    	}
    	
    	MoveValue returnMove = abprune(boardState, Integer.MIN_VALUE+1, Integer.MAX_VALUE-1, DEPTH, player_id);
    	//if(returnMove.getMove() == null) {
    	//	System.out.println("weisha   "+returnMove.getValue()+"   enzheyang");
    	
    	//}
    	PentagoBoardState bs = (PentagoBoardState) boardState.clone();
    	
    	Piece currColour = player_id == 0 ? Piece.WHITE : Piece.BLACK;
    	bs.processMove(returnMove.getMove());
    	
    	System.out.println( boardState.getTurnNumber() + ": "+"pl: "+ player_id +" CL: " +currColour  +" : "+evaluate(bs));
    	
        return returnMove.getMove();
    }
    
    
  
    private int check(PentagoBoardState bs, int wj) {
    	Piece currColour = player_id == 0 ? Piece.WHITE : Piece.BLACK;
    	Piece opColour = 1-player_id == 0 ? Piece.WHITE : Piece.BLACK;
    	int[] vert_p = new int[6];
    	int[] vert_o_n = new int[6];
    	int[] hori_p = new int[6];
    	int[] hori_o_n = new int[6];
    	int[] dial_p = new int[7];
    	int[] dial_o_n = new int[7];
    	int[] diar_p = new int[7];
    	int[] diar_o_n = new int[7];
    	int[] vert_o = new int[6];
    	int[] hori_o = new int[6];
    	int[] dial_o = new int[7];
    	int[] diar_o = new int[7];
    	
    	for(int i = 0; i<6; i++) {
    		for(int j = 0; j<6; j++) {
    			if( bs.getPieceAt(i,j) != Piece.EMPTY) {
    				if (currColour == bs.getPieceAt(i,j) ) {
    					if (j+2<6 && currColour == bs.getPieceAt(i,j+1) && currColour == bs.getPieceAt(i,j+2)){
    						if ( j+4<6 && j-1>=0 &&  (vert_p[j] >= 4 || ((currColour == bs.getPieceAt(i,j+3 ) && (opColour != bs.getPieceAt(i,j+4 ) && (opColour!= bs.getPieceAt(i,j-1))))))) {
    							vert_p[j] = 4;
    						}else{
    							vert_p[j] = 3;
    						}
    					}
    					
    					if (i+2<6 &&currColour == bs.getPieceAt(i+1,j) && currColour == bs.getPieceAt(i+2,j) ){
    						if ( i+4<6 && i-1>=0 && (hori_p[i] >= 4 || (currColour == bs.getPieceAt(i+3,j) && (opColour != bs.getPieceAt(i+4,j) && (opColour != bs.getPieceAt(i-1,j)))))) {
    							hori_p[i] = 4;
    						}else{
    							hori_p[i] = 3;
    						}
    					}
    					
    					if(i+j-2>=0 && i+j-2 <7 && i+2<6 && j-2>=0 && currColour == bs.getPieceAt(i+1,j-1) && currColour == bs.getPieceAt(i+2,j-2)) {
    						if (  i+4<6 && j-4>=0 && i-1>=0 && j+1<6 && (hori_p[i] >= 4 || ((currColour == bs.getPieceAt(i+3,j-3)  && (opColour != bs.getPieceAt(i+4,j-4 )&& (opColour != bs.getPieceAt(i-1,j+1 ))))) )) {
    							dial_p[i] = 4;
    						}else{
    							dial_p[i] = 3;
    						}
    					}
    					
    					if(j-i+3>=0 && j-i+3 <7 && i+2<6 && j+2<6 && currColour == bs.getPieceAt(i+1,j+1) && currColour == bs.getPieceAt(i+2,j+2)) {
    						if (  i+4<6 && j+4<6 && i-1>=0 && j-1>=0 && (hori_p[i] >= 4 || (currColour == bs.getPieceAt(i+3,j+3)) && (opColour != bs.getPieceAt(i+4,j+4 ) && (opColour != bs.getPieceAt(i-1,j-1 )))) ) {
    							diar_p[i] = 4;
    						}else{
    							diar_p[i] = 3;
    						}
    					}
    				}else {
    					Piece colour = bs.getPieceAt(i,j);
    					if (j+2<6 && colour == bs.getPieceAt(i,j+1) && colour == bs.getPieceAt(i,j+2)){
    						if ( j+3<6 &&  (vert_o[j] >= 4 || colour == bs.getPieceAt(i,j+3 ))) {
    							vert_o[j] = 4;
    						}else{
    							vert_o[j] = 3;
    						}
    					}
    					
    					if (i+2<6 && colour == bs.getPieceAt(i+1,j) && colour == bs.getPieceAt(i+2,j)){
    						if ( i+3<6 && (hori_o[i] >= 4 || colour == bs.getPieceAt(i+3,j))) {
    							hori_o[i] = 4;
    						}else{
    							hori_o[i] = 3;
    						}
    					}
    					
    					if(i+j-2>=0 && i+j-2 <7 && i+2<6 && j-2>=0 && colour == bs.getPieceAt(i+1,j-1) && colour == bs.getPieceAt(i+2,j-2)) {
    						if (  i+3<6 && j-3>=0 && (hori_o[i] >= 4 || colour == bs.getPieceAt(i+3,j-3)) ) {
    							dial_o[i] = 4;
    						}else{
    							dial_o[i] = 3;
    						}
    					}
    					
    					if(j-i+3>=0 && j-i+3 <7 && i+2<6 && j+2<6 && colour == bs.getPieceAt(i+1,j+1) && colour == bs.getPieceAt(i+2,j+2)) {
    						if (  i+3<6 && j+3<6 && (hori_o[i] >= 4 || colour == bs.getPieceAt(i+3,j+3)) ) {
    							diar_o[i] = 4;
    						}else{
    							diar_o[i] = 3;
    						}
    					}
    					vert_o_n[j]++;
    					hori_o_n[i]++;
    					if(i+j-2>=0 && i+j-2 <7 )dial_o_n[i+j-2]++;
    					if(j-i+3>=0 && j-i+3 <7 )diar_o_n[j-i+3]++;
    				}
    			}
    		}
    	}
    	// post-processing
    	int output = 0;
    	for(int i = 0; i<6; i++) {
    		if(vert_p[i]==3) output +=12;
    		if(vert_p[i]>3) output +=21;
    		if(vert_o_n[i]==3) {
    			if(vert_o[i]==3) {
    				output -=12;
    			}else {
    				output -=5;
    			}
    		}
    		if(vert_o_n[i]>3) {
    			if(vert_o[i]>3) {
    				output -=15;
    			}else {
    				output -=11;
    			}
    		}
    		
    		if(hori_p[i]==3) output +=12;
    		if(hori_p[i]>3) output +=21;
    		if(hori_o_n[i]==3) {
    			if(hori_o[i]==3) {
    				output -=12;
    			}else {
    				output -=5;
    			}
    		}
    		if(hori_o_n[i]>3) {
    			if(hori_o[i]>3) {
    				output -=15;
    			}else {
    				output -=11;
    			}
    		}
    		
    	}
    	
    	for(int i = 0; i<7; i++) {
    		if(dial_p[i]==3) output +=11;
    		if(dial_p[i]>3) output +=20;
    		if(dial_o_n[i]==3) {
    			if(dial_o[i]==3) {
    				output -=12;
    			}else {
    				output -=5;
    			}
    		}
    		if(dial_o_n[i]>3) {
    			if(dial_o[i]>3) {
    				output -=15;
    			}else {
    				output -=11;
    			}
    		}
    		
    		if(diar_p[i]==3) output +=11;
    		if(diar_p[i]>3) output +=20;
    		if(diar_o_n[i]==3) {
    			if(diar_o[i]==3) {
    				output -=12;
    			}else {
    				output -=5;
    			}
    		}
    		if(diar_o_n[i]>3) {
    			if(diar_o[i]>3) {
    				output -=15;
    			}else {
    				output -=11;
    			}
    		}
    	}
    	
    	return 10*output;
    }
    
    // evaluation function
    private int evaluate(PentagoBoardState bs){
    	
    	if(bs.getWinner() == player_id) {
    		return Integer.MAX_VALUE - 1;
    	}else if(bs.getWinner() == 1 - player_id){
    		return Integer.MIN_VALUE + 1;
    	}else if(bs.getWinner() == Board.DRAW) {
    		return -10;
    	}else {
    		return check(bs,player_id);
    	}
    }
    
    
    private MoveValue abprune( PentagoBoardState boardState, double alpha, double beta, int max_depth, int turnplayer) {
    	
    	boolean max_player = (player_id == turnplayer);
  
    	if(boardState.getWinner() == player_id) {
            return new MoveValue(Integer.MAX_VALUE - 1);
          }
         else if(boardState.getWinner() == 1 - player_id) {
            return new MoveValue(Integer.MIN_VALUE + 1);
         }
         else if(boardState.gameOver()) {
            return new MoveValue(0);
         }
         if(max_depth == 0) {
            return new MoveValue(evaluate(boardState));
         }
    	
    	List<PentagoMove> potential_moves = new LinkedList<PentagoMove>(boardState.getAllLegalMoves());
    	Collections.shuffle(potential_moves, new Random(System.currentTimeMillis()));
    	
    	MoveValue returnMove;
    	MoveValue bestMove = null;
    	
    	if(max_player) {
    		
    		for (PentagoMove move : potential_moves) {
    			PentagoBoardState bs_cloned = (PentagoBoardState) boardState.clone();
				bs_cloned.processMove(move);
				if(bs_cloned.getWinner() == player_id) {
	                return new MoveValue(Integer.MAX_VALUE - 1, move);
	            }
				returnMove = abprune(bs_cloned, alpha, beta, max_depth-1, 1 - turnplayer);
    		
    			if( ( bestMove == null ) || bestMove.getValue() < returnMove.getValue()  ) {
    				bestMove = returnMove;
    				bestMove.setMove(move);
    			} 
    			
    			if(  returnMove.getValue() > alpha  ) {
    				alpha = returnMove.getValue();
    				bestMove = returnMove;
    			}
    			
    			if( beta <= alpha && max_depth != DEPTH) {
    				// where we prune
    				bestMove.setValue(beta);
    				bestMove.setMove(null);
    				return bestMove;
    			}
    		}
    		return bestMove;
    		
    	}else {
    			for (PentagoMove move : potential_moves) {
        			PentagoBoardState bs_cloned = (PentagoBoardState) boardState.clone();
    				bs_cloned.processMove(move);
    				
    				if(bs_cloned.getWinner() == 1-player_id) {
    	                return new MoveValue(Integer.MIN_VALUE + 1, move);
    	            }
    				
    				returnMove = abprune(bs_cloned, alpha, beta, max_depth-1, 1 - turnplayer);
    				
        		
        			if( (bestMove == null) || bestMove.getValue() > returnMove.getValue()  ) {
        				bestMove = returnMove;
        				bestMove.setMove(move);
        			} 
        			
        			if(  returnMove.getValue() < beta  ) {
        				beta = returnMove.getValue();
        				bestMove = returnMove;
        			}
        			
        			if( beta <= alpha ) {
        				// where we prune
        				bestMove.setValue(alpha);
        				bestMove.setMove(null);
        				return bestMove;
        			}
        		}
        		return bestMove;
    	}
    }
	
	
	class MoveValue{
		private double return_value;
		private PentagoMove return_move;
		
		public MoveValue(double v) {
			this.return_value = v;
	    }
		
		public MoveValue(double v, PentagoMove m) {
			this.return_value = v;
			this.return_move = m;
	    }
		
		void setMove(PentagoMove m) {
			this.return_move = m;
		}
		
		void setValue(double v) {
			this.return_value = v;
		}
		
		PentagoMove getMove() {
			return this.return_move;
		}
		
		double getValue() {
			return this.return_value;
		}
	}
}
