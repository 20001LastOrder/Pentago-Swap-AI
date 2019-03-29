package student_player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;
import pentago_swap.PentagoBoardState.Quadrant;

public class MonteCarlo {
	public static volatile boolean shouldRun;
	static class Node{
	    private PentagoBoardState state;   //state of the board
	    private PentagoMove move;		  //move to take parent to this state
	    
		private Node parent;
	    private List<Node> children;		//subsequent class
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
			return children.get((int)(Math.random()*children.size()));
		}
	   
	}
    
    public static Move random(PentagoBoardState boardState, int player_id) {
    	Node root = new Node(boardState, null, null);
    	expand(root);
    	long t = System.currentTimeMillis();
    	long end = t + 1900;
    	//int i = 0;
        while(System.currentTimeMillis()<end) {
        	//i++;
        	Node node = decentWithUCT(root);
        	//if(!node.getState().gameOver()) {
        		//expand(node);
        		//Node nodeToGo = node.selectRandomChild();
        		PentagoBoardState endState = rollout(node);
        		backPropagate(node, endState, player_id);
        	//}else {
        	//	backPropagate(node, node.getState(), player_id);
        	//}
        }
        //t = System.currentTimeMillis();
        //System.out.println(i);
        Node bestNode = Collections.max(root.getChildren(),
				Comparator.comparing(n -> (double)n.win/n.visited));
       // System.out.println("win ratio: "+(double)bestNode.win/bestNode.visited);
        return bestNode.getMove();
    }
    
    private static double computeUCT(Node node) {
    	if(node.visited == 0) {
    		return Integer.MAX_VALUE;
    	}
    	
    	return node.win / node.visited + 
    			Math.sqrt(2*Math.log(node.getParent().visited)/node.visited);
    	
    }
    
    private static Node decentWithUCT(Node node) {
    	while(node.getChildren().size() > 0) {
    		node = Collections.max(node.getChildren(),
    				Comparator.comparing(n -> computeUCT(n)));
    	}
    	return node;
    }
    
    private static void expand(Node node) {
    	node.getState().getAllLegalMoves().forEach(m ->{
    		PentagoBoardState state = (PentagoBoardState) node.getState().clone();
    		state.processMove(m);
    		node.getChildren().add(new Node(state, m, node));
    	});
    }
    
    private static PentagoBoardState rollout(Node node) {
    	PentagoBoardState state = (PentagoBoardState) node.getState().clone();
    	Random r = new Random();
    	while(state.getWinner() == Board.NOBODY) {
    		Move move = state.getAllLegalMoves().get(r.nextInt(state.getAllLegalMoves().size()));
			state.processMove((PentagoMove) move);
    	}
    	return state;
    }
    
    private static void backPropagate(Node node, PentagoBoardState endState, int player_id) {
    	int winner = endState.getWinner();
    	int reward = -1;
    	if(winner == player_id) {
    		reward = 1;
    	}else if(winner==Board.DRAW) {
    		reward = 0;
    	}
    	
    	while(node != null) {
    		node.visited++;
    		node.win+=reward;
    		node = node.parent;
    	}
    }
    
    public static PentagoMove checkThirdTurnEndGameCrisis(PentagoBoardState boardState, int player_id) {
    	int[] xs = new int[3];
    	int[] ys = new int[3];
    	int k =0;
    	for(int i = 0; i < PentagoBoardState.BOARD_SIZE; i++) {
    		for(int j = 0; j < PentagoBoardState.BOARD_SIZE; j++) {
    			if(boardState.getPieceAt(i, j) == PentagoBoardState.Piece.WHITE) {
    				xs[k] = i;
    				ys[k] = j;
    				k++;
    			}
    		}
    	}
    	for(int i = 0; i < 2; i++) {
    		for(int j = i+1; j < 3; j++) {
    			if(Math.abs(xs[i]-xs[j]) == 1||Math.abs(ys[i]-ys[j]) == 1) {
    		    	int x = 0;
    		    	int y = 0;
    				if(i==0 && j == 2) {
    					x = xs[1] + xs[j]-xs[i];
    					y = ys[1] + ys[j]-ys[i];
    				}else if(i==0 && j == 1) {
    					x = xs[2] + xs[j]-xs[i];
    					y = ys[2] + ys[j]-ys[i];
    				}else {
    					x = xs[0] + xs[j]-xs[i];
    					y = ys[0] + ys[j]-ys[i];
    				}
    				return new PentagoMove(x, y, Quadrant.BL, Quadrant.BR, player_id);
    			}
    		}
    	}
    	return null;
    }
        
}
