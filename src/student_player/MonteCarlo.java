package student_player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

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
        
}
