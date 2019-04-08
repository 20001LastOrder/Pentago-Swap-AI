package student_player;

import java.util.LinkedList;
import java.util.List;
import java.util.function.UnaryOperator;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoPlayer;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;

/** A player file submitted by a student. */
public class StudentPlayer_i extends PentagoPlayer {
	
	//#TODO æµ‹è¯•å‰�å‡ æ¬¡random
	final static int DEPTH = 3;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer_i() {
        super("x");
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
    	MoveValue returnMove = abprune(boardState, Integer.MIN_VALUE+1, Integer.MAX_VALUE-1, DEPTH, player_id);
    	//if(returnMove.getMove() == null) {
    	//	System.out.println("weisha   "+returnMove.getValue()+"   enzheyang");
    		
    	//}
        return returnMove.getMove();
    }
    
    private static final UnaryOperator<PentagoCoord> getNextHorizontal = c -> new PentagoCoord(c.getX(), c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextVertical = c -> new PentagoCoord(c.getX()+1, c.getY());
    private static final UnaryOperator<PentagoCoord> getNextDiagRight = c -> new PentagoCoord(c.getX()+1, c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextDiagLeft = c -> new PentagoCoord(c.getX()+1, c.getY()-1);
    
    private double check(int count, int wj, PentagoBoardState boardState) {
    	if(wj == player_id) {
    		return checkVerticalWin(count,wj,boardState) + checkHorizontalWin(count,wj,boardState) + checkDiagRightWin(count,wj,boardState) + checkDiagLeftWin(count,wj,boardState);
    	}else {
    		double tmp = checkVerticalWin(count,wj,boardState) + checkHorizontalWin(count,wj,boardState) + checkDiagRightWin(count,wj,boardState) + checkDiagLeftWin(count,wj,boardState);
    		return 0.0 - tmp;
    	}
    }

    

    private double checkVerticalWin(int i, int wj, PentagoBoardState boardState) {
        return checkWinRange(i, wj, 0, 2, 0, 6, getNextVertical,boardState);
    }

    private double checkHorizontalWin(int i, int wj,PentagoBoardState boardState) {
        return checkWinRange(i, wj, 0, 6, 0, 2, getNextHorizontal,boardState);
    }

    private double checkDiagRightWin(int i, int wj, PentagoBoardState boardState) {
        return checkWinRange(i, wj, 0, 2, 0, 2, getNextDiagRight,boardState);
    }

    private double checkDiagLeftWin(int i, int wj, PentagoBoardState boardState) {
        return checkWinRange(i, wj, 0 ,2, 6 - 2, 6, getNextDiagLeft,boardState);
    }

    private double checkWinRange(int count, int player, int xStart, int xEnd, int yStart, int yEnd, UnaryOperator<PentagoCoord> direction, PentagoBoardState boardState) {
        boolean fulfill = false;
        for (int i = xStart; i < xEnd; i++) {
            for (int j = yStart; j < yEnd; j++) {
            	fulfill |= checkWin(count, player, new PentagoCoord(i, j), direction,boardState);
                if (fulfill) { return 1.0; }
            }
        }
        return 0.0;
    }

    private boolean checkWin(int count, int player, PentagoCoord start, UnaryOperator<PentagoCoord> direction, PentagoBoardState boardState) {
        int winCounter = 0;
        Piece currColour = player == 0 ? Piece.WHITE : Piece.BLACK;
        PentagoCoord current = start;
        while(true) {
            try {
                if (currColour == boardState.getPieceAt(current.getX(),current.getY())) {
                    winCounter++;
                    current = direction.apply(current);
                } else {
                    break;
                }
            } catch (IllegalArgumentException e) { 
                break;
            }
        }
        return winCounter == count;
    }
    
    // evaluation function
    private double evaluate(PentagoBoardState bs){
    	
    	if(bs.getWinner() == player_id) {
    		return Integer.MAX_VALUE - 1;
    	}else if(bs.getWinner() == 1 - player_id){
    		return Integer.MIN_VALUE + 1;
    	}else if(bs.getWinner() == Board.DRAW) {
    		return 0.0;
    	}else {
    		double reward = 1.0* check(2,player_id,bs) + (10.0)* check(3,player_id,bs) + (100.0)*check(4,player_id,bs);
    		double penalty = 2.0* check(2, 1-player_id,bs) + (20.0)* check(3, 1-player_id,bs) + (200.0)*check(4, 1-player_id,bs);
    		return reward + penalty;
    	}
    }
    
    
    protected MoveValue abprune( PentagoBoardState boardState, double alpha, double beta, int max_depth, int turnplayer) {
    	//if ( max_depth == 0 || boardState.getWinner() != Board.NOBODY) {
    		
    	//	return new MoveValue(evaluate(boardState));
    	//}
    	
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
    				//System.out.println("3   "+bestMove.getValue()+"  asd ng");
    				return bestMove;
    			}
    		}
    		//System.out.println("shang   "+bestMove.getValue()+"  asd ng");
    		return bestMove;
    		
    	}else {
    			for (PentagoMove move : potential_moves) {
        			PentagoBoardState bs_cloned = (PentagoBoardState) boardState.clone();
    				bs_cloned.processMove(move);
    				
    				if(bs_cloned.getWinner() == 1-player_id) {
    	                return new MoveValue(Integer.MIN_VALUE + 1, move);
    	            }
    				
    				returnMove = abprune(bs_cloned, alpha, beta, max_depth-1, 1 - turnplayer);
    				//System.out.println("gangjieshu   "+returnMove.getValue()+"  asd ng");
    				//System.out.println("gangjieshu2 alpha   "+alpha+"  asd ng");
    				//System.out.println("gangjieshu2 beta   "+beta+"  asd ng");
    				//System.out.println("gangjieshu3   "+(bestMove == null)+"  asd ng");
    				//if(bestMove != null)System.out.println("gangjieshu4   "+bestMove.getValue()+"  asd ng");
    				
        		
        			if( (bestMove == null) || bestMove.getValue() > returnMove.getValue()  ) {
        				bestMove = returnMove;
        				bestMove.setMove(move);
        				//System.out.println("4   "+bestMove.getValue()+"  asd ng");
        			} 
        			
        			if(  returnMove.getValue() < beta  ) {
        				beta = returnMove.getValue();
        				bestMove = returnMove;
        				//System.out.println("5   "+bestMove.getValue()+"  asd ng");
        			}
        			
        			if( beta <= alpha ) {
        				// where we prune
        				bestMove.setValue(alpha);
        				//System.out.println("alepha   "+alpha+"  asd ng");
        				bestMove.setMove(null);
        				//System.out.println("6   "+bestMove.getValue()+"  asd ng");
        				return bestMove;
        			}
        		}
    			//System.out.println("xia   "+bestMove.getValue()+"  asd ng");
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
