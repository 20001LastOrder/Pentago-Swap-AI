package student_player;

import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Quadrant;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;
import pentago_swap.PentagoPlayer;

public class StudentPlayer_V5 extends PentagoPlayer{
    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer_V5() {
        super("V5");
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
    	//Move myMove = MyTools.random(boardState, this.player_id);
    	
    	if(boardState.getTurnNumber() <= 1) {
    		Move myMove = boardState.getRandomMove();
    		if(boardState.isPlaceLegal(new PentagoCoord(1,1))) {
    			myMove = new PentagoMove(1,1,Quadrant.BL, Quadrant.BR, player_id);
    		}else if(boardState.isPlaceLegal(new PentagoCoord(4,1))) {
    			myMove = new PentagoMove(4,1,Quadrant.BL, Quadrant.BR, player_id);
    		}else if(boardState.isPlaceLegal(new PentagoCoord(1,4))) {
    			myMove = new PentagoMove(1,4,Quadrant.BL, Quadrant.BR, player_id);
    		}if(boardState.isPlaceLegal(new PentagoCoord(4,4))) {
    			myMove = new PentagoMove(4,4,Quadrant.BL, Quadrant.BR, player_id);
    		}
    		return myMove;
    	}

    	
    	Move myMove = Minimax.minimax(boardState, player_id);
        // Return your move to be processed by the server.
        return myMove;
    }
}
