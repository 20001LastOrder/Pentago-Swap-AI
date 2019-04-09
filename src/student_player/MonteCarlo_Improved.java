package student_player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;

public class MonteCarlo_Improved {
	private static Node winningNode;
	private static int player_id;
	private static Piece player_piece; 
	private static Piece other_piece; 

	private static final UnaryOperator<PentagoCoord> getNextHorizontal = c -> new PentagoCoord(c.getX(), c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextVertical = c -> new PentagoCoord(c.getX()+1, c.getY());
    private static final UnaryOperator<PentagoCoord> getNextDiagRight = c -> new PentagoCoord(c.getX()+1, c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextDiagLeft = c -> new PentagoCoord(c.getX()+1, c.getY()-1);
    
    
    
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

	public static void init(PentagoBoardState boardState, int player_id) {
		MonteCarlo_Improved.player_id = player_id;
		if(boardState.firstPlayer() == player_id) {
			player_piece = Piece.WHITE;
			other_piece = Piece.BLACK;
		}else {
			player_piece = Piece.BLACK;
			other_piece = Piece.WHITE;
		}
	}
	
	public static Move random(PentagoBoardState boardState) {

		//set winning node to null
		winningNode = null;
		Node root = new Node(boardState, null, null);
		expand(root);
		long time = System.currentTimeMillis();
		takeout(root.getChildren());
		if(winningNode != null) {
			return winningNode.move;
		}

		long timeForTake = System.currentTimeMillis() - time;
		long t = System.currentTimeMillis();
		long end = t + 1000-timeForTake;
		int i = 0;
		while (System.currentTimeMillis() < end) {
			i++;
			Node node = decentWithUCT(root);
			rollout(node);
		}
//		System.out.println(root.getChildren().size());
//		System.out.println("iteration: " + i);
		Node bestNode = Collections.max(root.getChildren(), Comparator.comparing(n -> (double) n.win / n.visited));
		
		
		
		// if there is a winning node use it
		if(winningNode != null) {
			bestNode = winningNode;
		}
		
//		System.out.println("win ratio: "+(double)bestNode.win/bestNode.visited);
		//System.out.println(root.getChildren().size());
		return bestNode.getMove();
	}
	
	private static double computeUCT(Node node) {
		if (node.visited == 0) {
			return Integer.MAX_VALUE;
		}

		return node.win / node.visited + Math.sqrt(2 * Math.log(node.getParent().visited) / node.visited);

	}

	private static Node decentWithUCT(Node node) {
		while (node.getChildren().size() > 0) {
			node = Collections.max(node.getChildren(), Comparator.comparing(n -> computeUCT(n)));
		}		
		return node;
	}

	private static void expand(Node node) {
		node.getState().getAllLegalMoves().forEach(m -> {
			PentagoBoardState state = (PentagoBoardState) node.getState().clone();
			state.processMove(m);
			Node child = new Node(state, m, node);
			node.getChildren().add(child);
		});
	}

	private static void rollout(Node node) {
		PentagoBoardState state = (PentagoBoardState) node.getState().clone();

		int i = 0;
		while (state.getWinner() == Board.NOBODY) {
			//Move move = state.getAllLegalMoves().get(r.nextInt(state.getAllLegalMoves().size()));
			Move move = state.getAllLegalMoves().get(0);

			state.processMove((PentagoMove) move);
			i++;
		}

		int winner = state.getWinner();
		int reward = -1;
		if (winner == player_id) {
			reward = 32-2*node.state.getTurnNumber()-i;
		} else if (winner == Board.DRAW) {
			reward = 1;
		}else {
			reward = -(32-2*node.state.getTurnNumber()-i);
			if(i <= 1) {
				if(node.parent.children.size() >= 2) {
					System.out.println("sad");
					node.parent.children.remove(node);
					node.parent = null;
				}
			}
		}
		Node tNode = node;
		while(tNode != null) {
			tNode.visited++;
			tNode.win += reward;
			tNode = tNode.parent;
		}
	}

	private static void takeout(List<Node> nodes) {	
		System.out.println(nodes.size());
		System.out.println(player_id);
		for(int i = 0; i < nodes.size(); i++) {
			boolean promising = false;
			if(nodes.get(i).state.getWinner() == player_id || nodes.get(i).state.getWinner()==Board.DRAW) {
				winningNode = nodes.get(i);
				return;
			}else if(nodes.get(i).state.getWinner() == 1-player_id) {
				nodes.get(i).state.printBoard();

				if(nodes.size() > 1) {
					nodes.remove(nodes.get(i));
					i--;
					continue;
				}else {
					return;
				}
			}
			
			if(checkEndGameCrisis(nodes.get(i).state, player_piece)) {
				promising = true;
			}
			
			List<PentagoMove> moves = nodes.get(i).state.getAllLegalMoves();
			for(PentagoMove move : moves) {
				PentagoBoardState state = (PentagoBoardState)nodes.get(i).getState().clone();
				state.processMove(move);

				if(state.getWinner()==1- player_id) {
					if(nodes.size() > 3) {
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
			if(promising) {
				winningNode = nodes.get(i);
			}
		}
	}
	
	private static boolean checkEndGameCrisis(PentagoBoardState state, Piece piece) {
		
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
//		if(state.getPieceAt(coor) != piece && state.getPieceAt(coor) != Piece.EMPTY) {
//			return false;
//		}
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
