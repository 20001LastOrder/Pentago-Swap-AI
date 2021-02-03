package student_player;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.UnaryOperator;

import boardgame.Move;

import pentago_swap.PentagoPlayer;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoBoardState.Quadrant;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;

/** A player file submitted by a student. */
public class StudentPlayer_F extends PentagoPlayer {
	private int counter = 0;
	private static Piece[][] board = new Piece[6][6];
	
	public int AI = -1;
	public int opp = -1;
	private boolean winningMoveFound = false;
	
    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer_F() {
        super("260732931");
    }
        
    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {
    
    	long startTime = System.currentTimeMillis();
    	
    	if (AI == -1 || opp == -1) {
    		AI = boardState.getTurnPlayer();
    		opp = boardState.getOpponent();
    	}
    	
    	System.out.println("Turn Number = " + boardState.getTurnNumber());
    	System.out.println("Player = " + boardState.getTurnPlayer());
    	
    	ArrayList<PentagoMove> nextMoves = boardState.getAllLegalMoves();		// 	Create a list of possible moves
    	System.out.println("Size: " + nextMoves.size());
    	
    	if(boardState.getTurnNumber() < 8) {
    		Node move = minMax(boardState, 2, AI, Integer.MIN_VALUE, Integer.MAX_VALUE);
        	long endTime = System.currentTimeMillis();
        	long duration = (endTime - startTime);
        	System.out.println("Time taken for depth 2: " + duration/1000.0 + "seconds");
        	return move.getMove();
    	}
    	
    	//System.out.println("Score = " + calStateHeuristic(boardState.getTurnPlayer()));
    	
    	Node move = minMax(boardState, 2, AI, Integer.MIN_VALUE, Integer.MAX_VALUE);
    	long endTime = System.currentTimeMillis();
    	long duration = (endTime - startTime);
    	System.out.println("Time taken for depth 3: " + duration/1000.0 + "seconds");
    	return move.getMove();
    }
    
    private static final UnaryOperator<PentagoCoord> getNextHorizontal = c -> new PentagoCoord(c.getX(), c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextVertical = c -> new PentagoCoord(c.getX()+1, c.getY());
    private static final UnaryOperator<PentagoCoord> getNextDiagRight = c -> new PentagoCoord(c.getX()+1, c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextDiagLeft = c -> new PentagoCoord(c.getX()+1, c.getY()-1);
    
    /**
     * This method is used to update the board state of the game.
     * @param state
     */
    private static void update(PentagoBoardState state) {
    	for(int i=0; i<6; i++) {
    		for(int j=0; j<6; j++) {
    			board[i][j] = state.getPieceAt(i, j);
    		}
    	}
    }
     
    /**
     * 
     * @param state
     * @param depth
     * @param player
     * @param alpha
     * @param beta
     * @return
     */
    public Node minMax(PentagoBoardState state, int depth, int player, int alpha, int beta) {
    	update(state);
    	ArrayList<PentagoMove> nextMoves = state.getAllLegalMoves();		// 	Create a list of possible moves
    	int score;
    	Node bestMove = new Node();
    	if(nextMoves.isEmpty() || depth == 0) {	
    		score = calStateHeuristic(AI) - calStateHeuristic(opp);
    		bestMove.setHeuristic(score);
    		return bestMove;
    		
    	} else {
    		for(PentagoMove move: nextMoves) {
    			PentagoBoardState newClonedState = (PentagoBoardState)state.clone();
    			newClonedState.processMove(move);
    			if(player == AI) {	// is maximizing player
    				score = minMax(newClonedState, depth -1, opp, alpha, beta).getHeuristic();
    				
    				if(score > alpha) {
    					alpha = score;
    					bestMove.setMove(move);
    				}
    			} else {
    				score = minMax(newClonedState, depth - 1, AI, alpha, beta).getHeuristic();
    				if(score < beta) {
    					beta = score;
    					bestMove.setMove(move);
    				}
    			}
    			if (alpha > beta) break;
    		}
    	}
    	
		if(player == AI) {
			Node result = new Node();
			result.setMove(bestMove.getMove());
			result.setHeuristic(alpha);
			return result;
		} else {
			Node result = new Node();
			result.setMove(bestMove.getMove());
			result.setHeuristic(beta);
			return result;
		}
    }
    
    /**
     * This method returns the heuristic score of the current state.
     * @param p
     * @param notBlocked
     * @return 
     */
    private int scoreTable(int[] pair) {
    	//System.out.println("Streak: " + pair[0] + " " + "Not blocked: " + pair[1]);
    	if(pair[0] >= 5) {			// ooooo
    		return 300000;
    	} else if(pair[0] == 4) {	
    		if(pair[1] == 2) {	// -oooo-
    			return 300000;
    		} else if(pair[1] == 1) {			// xoooox or -oooox
    			return 3000;
    		} else {
    			return 2600;
    		}
    	} else if (pair[0] == 3) {
    		if(pair[1] == 2) {
    			return 3000;
    		} else if (pair[1] == 1){
    			return 800;
    		} else {
    			return 500;
    		}
    	} else if (pair[0] == 2) {
    		if(pair[1] == 2) {
    			return 650;
    		} else if (pair[1] == 1){
    			return 150;
    		}
    	} else if (pair[0] == 1) {
    		if(pair[1] == 2) {
    			return 100;
    		} else {
    			return 0;
    		}
    	}
    	return 0;
    }  
    
	/**
	 * This method calculate the heuristic value of a given state of the board
	 * @param boardState
	 * @return
	 */
	public int calStateHeuristic(int player) {
		PentagoCoord BL = new PentagoCoord(0, 5);
		// Determine the player number of u
		//System.out.println(player);
		int heuristicVal = 0;
		// Check patterns for col
		PentagoCoord TLRow = new PentagoCoord(0, 0);
		//PentagoCoord TRD = new PentagoCoord();
		for(int i = 0; i<6; i++) {
			int value = checkForPattern(player, TLRow, getNextVertical);
			heuristicVal += value;
			if(i!=5) {
				TLRow = getNextHorizontal.apply(TLRow);
			}
			//System.out.println("Col" + i + " = " + value);
		}
		// Check patterns for diagonally left
		PentagoCoord DR = new PentagoCoord(0, 0);
		int dr = checkForPattern(player, DR, getNextDiagRight);
		//System.out.println("gradient: " + dr);
		heuristicVal += dr;
		//System.out.println("DR" + " = " + dr);
		// Check patterns for diagonally right 
		PentagoCoord TLDR = new PentagoCoord(0, 5);
		int dl = checkForPattern(player, TLDR, getNextDiagLeft);
		heuristicVal += dl;
		//System.out.println("DL" + " = " + dl);
		
		PentagoCoord TLCol = new PentagoCoord(0, 0);
		// Check patterns for row
		for(int i=0; i<6; i++) {
			int value = checkForPattern(player, TLCol, getNextHorizontal);
			heuristicVal += value;
			if(i!=5) {
				TLCol = getNextVertical.apply(TLCol);
			}
			//System.out.println("Row" + i + " = " + value);
		}
		
		PentagoCoord TDUL = new PentagoCoord(0, 1);
		int dul = checkForPattern(player, TDUL, getNextDiagRight);
		heuristicVal += dul;
		
		PentagoCoord TDLL = new PentagoCoord(1, 0);
		int dll = checkForPattern(player, TDLL, getNextDiagRight);
		heuristicVal += dll;
		
		PentagoCoord TDUR = new PentagoCoord(0, 4);
		int dur = checkForPattern(player, TDUR, getNextDiagLeft);
		heuristicVal += dur;
		
		PentagoCoord TDLR = new PentagoCoord(1, 5);
		int dlr = checkForPattern(player, TDLR, getNextDiagLeft);
		heuristicVal += dlr;
		
		return heuristicVal;
	}
	
	/**
	 * This method is used to check for the particular pattern of the board.
	 * @param player which player we are
	 * @param start	the starting coordinate
	 * @param direction which direction to check for the pattern (row/vertical/diagonal)
	 * @param state current boardstate of the game
	 * @return
	 */

    private int checkForPattern(int player, PentagoCoord start, UnaryOperator<PentagoCoord> direction) {
    	int score = 0;
    	int counter = 0;
        int notBlocked = 0;
        Piece currColour = player == 0 ? Piece.WHITE : Piece.BLACK;
        Piece oppColour = player == 0 ? Piece.BLACK : Piece.WHITE;
        //System.out.println(currColour.name());
        PentagoCoord current = start;
        PentagoCoord previous = start;
        if(this.board[current.getX()][current.getY()] == Piece.EMPTY) notBlocked = 1;
        while(true) {
            try {
            	// The next coord is the same as the previous for the target player
                if (currColour == board[current.getX()][current.getY()]) {
                	counter++;
                	if (board[previous.getX()][previous.getY()] == Piece.EMPTY) {
                		if(notBlocked<2) {
                			notBlocked++;
                		}
                	}
                	if (board[previous.getX()][previous.getY()] == oppColour && notBlocked > 0) {
                		if(notBlocked>0) {
                			notBlocked--;
                		}
                	}
                } else {
                	if(board[current.getX()][current.getY()]==Piece.EMPTY && board[previous.getX()][previous.getY()] == currColour) {
                		if(notBlocked<2) {
                			notBlocked++;
                		}
                	} else {
                		if(notBlocked>0) {
                			notBlocked--;
                		}
                	}
                	if(counter >=1) {
                		score += scoreTable(new int[]{counter, notBlocked});
                		counter = 0;
                		notBlocked = 0;
                	}
                }
                previous = current;
                current = direction.apply(current);
                		
            } catch (IllegalArgumentException e) { //We have run off the board
            	if(counter >= 1) {
            		score += scoreTable(new int[]{counter, notBlocked});
            	}
                break;
            }
        }
      return score;
    }
    
    private static void toStringBoard(Piece[][] aboard) {
    	for(int i=0; i<6; i++) {
    		for(int j=0; j<6; j++) {
        		System.out.print(aboard[i][j].name() + " ");
    		}
    		System.out.println();
    	}
    	System.out.println();
    	System.out.println();
    }
    
    class Node {
    	PentagoMove move;
    	int heuristic;
    	
    	public Node() {
    	}
    	
    	public Node(PentagoMove move, int heuristic) {
    		this.move = move;
    		this.heuristic = heuristic;
    	}
    	public PentagoMove getMove() {
    		return this.move;
    	}
    	public int getHeuristic() {
    		return this.heuristic;
    	}
    	public void setMove(PentagoMove move) {
    		this.move = move;
    	}
    	public void setHeuristic(int heuristic) {
    		this.heuristic = heuristic;
    	}
    }
    
    /*
    public static void main(String[] args) {
    	StudentPlayer sp = new StudentPlayer();
    	System.out.println("Testing...");
    	PentagoBoardState state = new PentagoBoardState();
    	PentagoMove move1 =  new PentagoMove( "0 0 BL BR 0");
    	PentagoMove move2 =  new PentagoMove( "5 0 BL BR 1");
    	PentagoMove move3 =  new PentagoMove( "1 1 BL BR 0");
    	PentagoMove move4 =  new PentagoMove( "2 4 BL BR 1");
    	PentagoMove move5 =  new PentagoMove( "2 2 BL BR 0");
    	PentagoMove move6 =  new PentagoMove( "2 3 BL BR 1");
    	PentagoMove move7 =  new PentagoMove( "3 3 BL BR 0");
    	PentagoMove move8 =  new PentagoMove( "4 3 BL BR 1");
    	PentagoMove move9 =  new PentagoMove( "4 4 BL BR 0");
    	PentagoMove move10 =  new PentagoMove( "3 4 BL BR 1");
    	PentagoMove move11 =  new PentagoMove( "4 5 TR BL 0");
    	//PentagoMove move12 =  new PentagoMove( "4 4 BL BR 1");
    	state.processMove(move1);
    	state.processMove(move2);
    	state.processMove(move3);
    	state.processMove(move4);
    	state.processMove(move5);
    	state.processMove(move6);
    	state.processMove(move7);
    	state.processMove(move8);
    	state.processMove(move9);
    	state.processMove(move10);
    	state.processMove(move11);
    	//state.processMove(move12);
    	update(state);
    	toStringBoard(board);
    	System.out.println(state.getTurnPlayer());
    	System.out.println(sp.calStateHeuristic(state.getOpponent()));	
    }*/
    
}