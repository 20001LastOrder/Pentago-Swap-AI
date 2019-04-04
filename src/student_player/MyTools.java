package student_player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Quadrant;
import student_player.MonteCarlo_Improved.Node;
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
	
	static class Node {
		private PentagoBoardState state; // state of the board
		private PentagoMove move; // move to take parent to this state

		private Node parent;
		private List<Node> children; // subsequent class
		int visited;
		int win;

		public Node(PentagoBoardState state, PentagoMove move, Node parent) {
			super();
			this.state = state;
			this.move = move;
			this.parent = parent;
			this.children = new ArrayList<Node>();
		}

		public PentagoBoardState getState() {
			return state;
		}

		public PentagoMove getMove() {
			return move;
		}

		public Node getParent() {
			return parent;
		}

		public List<Node> getChildren() {
			return children;
		}

		public Node selectRandomChild() {
			return children.get((int) (Math.random() * children.size()));
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
    
    public void minimax(PentagoBoardState boardState, int player_id) {
    	
    }
    
    private static void goDown(int depth, Node root) {
    	Stack<Node> stack = new Stack<Node>();
    	while(depth > 0) {
    		
    	}
    }
    

    
	private static void takeout(List<Node> nodes, int player_id) {
		for(int i = 0; i < nodes.size(); i++) {
			List<PentagoMove> moves = nodes.get(i).state.getAllLegalMoves();
			for(PentagoMove move : moves) {
				PentagoBoardState state = (PentagoBoardState)nodes.get(i).getState().clone();
				state.processMove(move);
				nodes.get(i).children.add(new Node(state, move, nodes.get(i)));
			}
		}
	}
}
