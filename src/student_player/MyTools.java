package student_player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.UnaryOperator;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;

public class MyTools {
	
	//Node for indicating the potential winning node
	private static Node winningNode;
	private static int player_id;
	private static Piece player_piece; 
	private static Piece other_piece; 

	//Unary Operators for coordinates
	private static final UnaryOperator<PentagoCoord> getNextHorizontal = c -> new PentagoCoord(c.getX(), c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextVertical = c -> new PentagoCoord(c.getX()+1, c.getY());
    private static final UnaryOperator<PentagoCoord> getNextDiagRight = c -> new PentagoCoord(c.getX()+1, c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextDiagLeft = c -> new PentagoCoord(c.getX()+1, c.getY()-1);
    
    //Random used for random simulation in later stage
    private static final Random r = new Random();
    
    // turns that we begin real Monte Carlo simulation
    private static final int MONTE_CARLO_START=10;
    
    
    /**
     * Node class used for monte carlo step 
     *
     */
	static class Node {
		private PentagoBoardState state; // state of the board
		private PentagoMove move; // move to take parent to this state

		private Node parent;
		private List<Node> children; // subsequent states
		int visited;
		int win;
		int score;

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
		
		public void setScore(int score) {
			this.score = score;
		}
		
		public int getScore() {
			return score;
		}
		

		public Node selectRandomChild() {
			return children.get((int) (Math.random() * children.size()));
		}

	}

	/**
	 * initiate game
	 * @param boardState: the board State
	 * @param player_id: the id for the AI player
	 */
	public static void init(PentagoBoardState boardState, int player_id) {
		MyTools.player_id = player_id;
		if(boardState.firstPlayer() == player_id) {
			player_piece = Piece.WHITE;
			other_piece = Piece.BLACK;
		}else {
			player_piece = Piece.BLACK;
			other_piece = Piece.WHITE;
		}
	}
	
	/**
	 * Main method, choose a promising move
	 * @param boardState: current board state
	 * @return the move we choose
	 */
	public static Move chooseMove(PentagoBoardState boardState) {

		//set winning node to null
		winningNode = null;
		
		//create one-step children for the roots
		Node root = new Node(boardState, null, null);
		expand(root);

		//take out the extremely bad moves
		takeout(root.getChildren());
		
		//if there is any good moves found in the takeout stage, use it
		if(winningNode != null) {
			return winningNode.move;
		}

		//set timer for the simulation
		PentagoTimer timer = new PentagoTimer(1500);
		if(boardState.getTurnNumber() <= MONTE_CARLO_START) {
			// if the game is within certain range, use fake monte-carlo
			while (!timer.timeout) {
				Node node = decentWithUCT(root);
				rollout(node, boardState.getTurnNumber());
			}
		}else {
			//else use real monte-carlo
			while(!timer.timeout) {
				Node node = decentWithUCT(root);
				if(!node.getState().gameOver()) {
					if(node.getChildren().size() == 0) {
						expand(node);
					}
					Node nodeToGo = node.selectRandomChild();
					rollout(nodeToGo, player_id);
	    		}else {
	    			rollout(node, player_id);
	    		}
			}			
		}


		//get the best move from the children
		Node bestNode = Collections.max(root.getChildren(), Comparator.comparing(n -> (double) n.win / n.visited));
		
		
		
		// if there is a winning node use it
		if(winningNode != null) {
			bestNode = winningNode;
		}
		
		return bestNode.getMove();
	}
	
	/**
	 * Compute the UCT value
	 * @param node
	 * @return: UCT value of that node
	 */
	private static double computeUCT(Node node) {
		if (node.visited == 0) {
			return Integer.MAX_VALUE;
		}

		return node.win / node.visited + Math.sqrt(2 * Math.log(node.getParent().visited) / node.visited);

	}

	/**
	 * find the promising node based on the UCT value
	 * @param node
	 * @return
	 */
	private static Node decentWithUCT(Node node) {
		while (node.getChildren().size() > 0) {
			node = Collections.max(node.getChildren(), Comparator.comparing(n -> computeUCT(n)));
		}		
		return node;
	}

	/**
	 * expand a node by its legal moves
	 * @param node
	 */
	private static void expand(Node node) {
		node.getState().getAllLegalMoves().forEach(m -> {
			PentagoBoardState state = (PentagoBoardState) node.getState().clone();
			state.processMove(m);
			Node child = new Node(state, m, node);
			node.getChildren().add(child);
		});
	}

	/**
	 * do simulation on a node to the end of the game based on some rule
	 * @param node
	 * @param turnNumber
	 */
	private static void rollout(Node node, int turnNumber) {
		PentagoBoardState state = (PentagoBoardState) node.getState().clone();

		int i = 0;
		while (state.getWinner() == Board.NOBODY) {
			Move move;
			//if within same term, do not use random simulation
			if(turnNumber <= MONTE_CARLO_START) {
				move = state.getAllLegalMoves().get(0);
			}else {
				move = state.getAllLegalMoves().get(r.nextInt(state.getAllLegalMoves().size()));
			}

			state.processMove((PentagoMove) move);
			i++;
		}

		//assign score based on the winner
		int winner = state.getWinner();
		int reward = -1;
		if (winner == player_id) {
			reward = (32-2*node.state.getTurnNumber()-i)*1000 + 5000;
		} else if (winner == Board.DRAW) {
			reward = 1000;
		}else {
			reward = -(32-2*node.state.getTurnNumber()-i) * 1000 - 5000;
			if(i <= 1) {
				if(node.parent.children.size() >= 2) {
					node.parent.children.remove(node);
					node.parent = null;
				}
			}
		}
		
		//back propagate
		Node tNode = node;
		while(tNode != null) {
			tNode.visited++;
			tNode.win += reward;
			tNode = tNode.parent;
		}
	}

	/**
	 * take out bad move and look for promisng moves (similar to minimax)
	 * @param nodes
	 */
	private static void takeout(List<Node> nodes) {	
		for(int i = 0; i < nodes.size(); i++) {
			boolean promising = false;
			
			//check if there is any winner and act accordingly
			if(nodes.get(i).state.getWinner() == player_id || nodes.get(i).state.getWinner()==Board.DRAW) {
				//if we can win on next move, just do it
				winningNode = nodes.get(i);
				return;
			}else if(nodes.get(i).state.getWinner() == 1-player_id) {
				if(nodes.size() > 3) {
					nodes.remove(nodes.get(i));
					i--;
					continue;
				}else {
					return;
				}
			}
			
			//check if there is any promisng pattern for the player
			if(checkEndGameCrisis(nodes.get(i).state, player_piece)) {
				promising = true;
			}
			
			//go to depth 2
			List<PentagoMove> moves = nodes.get(i).state.getAllLegalMoves();
			for(PentagoMove move : moves) {
				PentagoBoardState state = (PentagoBoardState)nodes.get(i).getState().clone();
				state.processMove(move);

				//check if there is any winner or promising pattern for the opponent and act
				//accordingly
				if(state.getWinner()==1- player_id) {
					if(nodes.size() > 1) {
						nodes.remove(nodes.get(i));
					}else {
						return;
					}
					i--;
					promising = false;
					break;
				}else if(checkEndGameCrisis(state, other_piece)) {
					if(!promising) {
						if(nodes.size() > 3) {
							nodes.remove(nodes.get(i));
						}else {
							return;
						}
						i--;
						break;
					}
				}
			}
			
			// if a node is promising, we make it as a winning node
			if(promising) {
				winningNode = nodes.get(i);
			}
		}
	}
	
	/**
	 * check for end game crisis of a piece (good for this piece)
	 * @param state
	 * @param piece
	 * @return
	 */
	public static boolean checkEndGameCrisis(PentagoBoardState state, Piece piece) {
		
		PentagoCoord tl = new PentagoCoord(0,0);
		PentagoCoord br = new PentagoCoord(0,5);

		//check two diagonals
		if(checkCrisisPattern(getNextDiagRight, state, piece, tl) 
					|| checkCrisisPattern(getNextDiagLeft, state, piece, br)) {
			return true;
		}
		
		//check all horizontal
		for(int i = 0; i < 5; i++) {
			if(checkCrisisPattern(getNextHorizontal, state, piece, new PentagoCoord(i,0))) {
				return true;
			}
		}
		
		//check all verticals
		for(int i = 0; i < 5; i++) {
			if(checkCrisisPattern(getNextVertical, state, piece, new PentagoCoord(0,i))) {
				return true;
			}
		}
		
		return false;
	}
	
	//check if the board has four successive pattern of the same color
	//eg: _****_
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
