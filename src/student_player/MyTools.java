package student_player;

import java.util.HashMap;
import java.util.Random;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Quadrant;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;

public class MyTools {
	static class MoveUsage{
		public int xMove;
	    public int yMove;
	    public Quadrant aSwap;
	    public Quadrant bSwap;
	    
	    public MoveUsage(int xMove, int yMove, Quadrant aSwap, Quadrant bSwap) {
			super();
			this.xMove = xMove;
			this.yMove = yMove;
			this.aSwap = aSwap;
			this.bSwap = bSwap;
		}

	    @Override
	    public int hashCode() {
	        return xMove<<6+yMove<<3+aSwap.hashCode()<<1+bSwap.hashCode();
	    }
	    
		public boolean equals  (Object obj) {
	    	MoveUsage others = (MoveUsage) obj;
	    	boolean equal = (this.xMove==others.xMove &&
	    					 this.yMove==others.yMove &&
	    					 this.aSwap==others.aSwap &&
	    					 this.bSwap==others.bSwap);
			return equal;
	    	
	    }

	}
	
    public static double getSomething() {
        return Math.random();
    }
    
    public static Move random(PentagoBoardState boardState, int player_id) {
    	HashMap<MoveUsage, Integer> map = new HashMap<MoveUsage, Integer>();
    	long t = System.currentTimeMillis();
    	long end = t + 1900;
    	//int i = 0;
        while(System.currentTimeMillis()<end) {
        	//i++;
        	PentagoBoardState thisState = (PentagoBoardState) boardState.clone();
        	Random r = new Random();
        	PentagoMove thisMove = (PentagoMove)thisState.getAllLegalMoves().get(r.nextInt(thisState.getAllLegalMoves().size()));
			thisState.processMove(thisMove);
        	while(thisState.getWinner() == Board.NOBODY) {
        		Move move = thisState.getAllLegalMoves().get(r.nextInt(thisState.getAllLegalMoves().size()));
    			thisState.processMove((PentagoMove) move);
        	}
        	PentagoCoord c = thisMove.getMoveCoord();
        	Quadrant aq = thisMove.getASwap();
        	Quadrant bq = thisMove.getBSwap();
        	MoveUsage usage = new MoveUsage(c.getX(), c.getY(), aq, bq);
        	
    		Integer currentWin = map.get(usage);
    		if(currentWin == null) {
    			currentWin = 0;
    		}
        	if(thisState.getWinner()==player_id) {
        		map.put(usage, currentWin+1);
        	}else if(thisState.getWinner()==Board.DRAW) {
        		map.put(usage, currentWin);
        	}else {
        		map.put(usage, currentWin-1);
        	}
        }
        //System.out.println(i);
        HashMap.Entry<MoveUsage, Integer> maxEntry = null;
      
        for (HashMap.Entry<MoveUsage, Integer> entry : map.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }
        MoveUsage maxUsage = maxEntry.getKey();
        Move move = new PentagoMove(maxUsage.xMove, maxUsage.yMove, maxUsage.aSwap, maxUsage.bSwap, player_id);
        return move;
    }
}
