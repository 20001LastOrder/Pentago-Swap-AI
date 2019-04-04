package student_player;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;
import student_player.MonteCarlo_Improved2.Node;

public class MonteCarlo_Improved {
	private static Hashtable<String, Data> map = new Hashtable<String, Data>();
	private static Node winningNode;
	static class Data {
		int win;
		int total;
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

	public static Move random(PentagoBoardState boardState, int player_id) {
		//set winning node to null
		winningNode = null;
		Node root = new Node(boardState, null, null);
		expand(root);
		
		long time = System.currentTimeMillis();
		takeout(root.getChildren(),player_id);
		if(winningNode != null) {
			return winningNode.move;
		}
		//exploreTakeout(root.getChildren(), player_id);
		long timeForTake = System.currentTimeMillis() - time;
		long t = System.currentTimeMillis();
		long end = t + 1000-timeForTake;
		int i = 0;
		while (System.currentTimeMillis() < end) {
			i++;
			Node node = decentWithUCT(root);
			rollout(node, player_id);
		}
		System.out.println(root.getChildren().size());
		System.out.println("iteration: " + i);
		Node bestNode = Collections.max(root.getChildren(), Comparator.comparing(n -> (double) n.win / n.visited));
		
		
		
		// if there is a winning node use it
		if(winningNode != null) {
			bestNode = winningNode;
		}
		
		System.out.println("win ratio: "+(double)bestNode.win/bestNode.visited);
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
//			Data data = map.get(encodeState(state));
//			if (data != null) {
//				int win = 0;
//				if (player_id == 0) {
//					win = data.win*100;
//				} else {
//					win = data.total*100 - data.win*100;
//				}
//				child.win = win*100;
//				child.visited = data.total*100;
//			}
			node.getChildren().add(child);
		});
	}

	private static void rollout(Node node, int player_id) {
		PentagoBoardState state = (PentagoBoardState) node.getState().clone();
		Random r = new Random();

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
			reward = 0;
			if(i <= 1) {
				if(node.parent.children.size() >= 2) {
					System.out.println("Size : " + node.parent.children.size());
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

	private static void takeout(List<Node> nodes, int player_id) {
		for(int i = 0; i < nodes.size(); i++) {
			if(nodes.get(i).state.getWinner() == player_id || nodes.get(i).state.getWinner()==Board.DRAW) {
				winningNode = nodes.get(i);
			};
			List<PentagoMove> moves = nodes.get(i).state.getAllLegalMoves();
			for(PentagoMove move : moves) {
				PentagoBoardState state = (PentagoBoardState)nodes.get(i).getState().clone();
				state.processMove(move);
				if(state.getWinner()!=Board.NOBODY && state.getWinner() != player_id && state.getWinner()!= Board.DRAW) {
					if(nodes.size() > 1) {
						nodes.remove(nodes.get(i));
					}else {
						return;
					}
					i--;
					break;
				}
			}
		}
	}
	
	public static void exploreTakeout(List<Node> nodes, int player_id) {
		for(int i = 0; i < nodes.size(); i++) {
			PentagoBoardState state = (PentagoBoardState) nodes.get(i).getState().clone();
			Random r = new Random();

			while (state.getWinner() == Board.NOBODY) {
				Move move = state.getAllLegalMoves().get(r.nextInt(state.getAllLegalMoves().size()));
				//Move move = state.getAllLegalMoves().get(0);

				state.processMove((PentagoMove) move);
			}
			
			if(state.getWinner() != player_id && state.getWinner()!= Board.DRAW) {
//				if(nodes.size() > 1) {
//					nodes.remove(nodes.get(i));
//					i--;
//					System.out.println("LEFT: "+ nodes.size());
//				}
				nodes.get(i).visited=1000;
				nodes.get(i).win = 0;
			}else {
				nodes.get(i).visited=1000;
				nodes.get(i).win=1000;
			}
		}
	}
	
	
	public static String encodeState(PentagoBoardState boardState) {
		String TL = "";
		String TR = "";
		String BL = "";
		String BR = "";

		// TL
		for (int i = 0; i <= 2; i++) {
			for (int j = 0; j <= 2; j++) {
				if (boardState.getPieceAt(i, j) == PentagoBoardState.Piece.WHITE) {
					TL += '0';
				} else if (boardState.getPieceAt(i, j) == PentagoBoardState.Piece.BLACK) {
					TL += '1';
				} else {
					TL += 'N';
				}
			}
		}

		// TR
		for (int i = 3; i <= 5; i++) {
			for (int j = 0; j <= 2; j++) {
				if (boardState.getPieceAt(i, j) == PentagoBoardState.Piece.WHITE) {
					TR += '0';
				} else if (boardState.getPieceAt(i, j) == PentagoBoardState.Piece.BLACK) {
					TR += '1';
				} else {
					TR += 'N';
				}
			}
		}

		// BL
		for (int i = 0; i <= 2; i++) {
			for (int j = 3; j <= 5; j++) {
				if (boardState.getPieceAt(i, j) == PentagoBoardState.Piece.WHITE) {
					BL += '0';
				} else if (boardState.getPieceAt(i, j) == PentagoBoardState.Piece.BLACK) {
					BL += '1';
				} else {
					BL += 'N';
				}
			}
		}

		// BR
		for (int i = 3; i <= 5; i++) {
			for (int j = 3; j <= 5; j++) {
				if (boardState.getPieceAt(i, j) == PentagoBoardState.Piece.WHITE) {
					BR += '0';
				} else if (boardState.getPieceAt(i, j) == PentagoBoardState.Piece.BLACK) {
					BR += '1';
				} else {
					BR += 'N';
				}
			}
		}
		return TL + TR + BL + BR;
	}

	public static void readData() {
		String file = "data/table.txt";

		// read old table
		Scanner s;
		try {
			s = new Scanner(new File(file));
			// long t = System.currentTimeMillis();
			while (s.hasNextLine()) {
				String[] line = s.nextLine().split("\\s+");
				int win = Integer.parseInt(line[1]);
				int total = Integer.parseInt(line[2]);
				Data data = new Data();
				data.win = win;
				data.total = total;
				map.put(line[0], data);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
