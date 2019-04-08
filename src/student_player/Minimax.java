package student_player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.sun.corba.se.spi.orbutil.fsm.State;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;
import student_player.MonteCarlo_Improved.Node;

public class Minimax {
	static class Node {
		private PentagoBoardState state; // state of the board
		private PentagoMove move; // move to take parent to this state

		private List<Node> children; // subsequent class
		private int alpha;
		private int beta;

		public Node(PentagoBoardState state, PentagoMove move, int alpha, int beta) {
			super();
			this.state = state;
			this.move = move;
			this.alpha = alpha;
			this.beta = beta;
			this.children = new ArrayList<>();
		}
	}
	private static int player_id;
	
	public static Move minimax(PentagoBoardState state, int player_id) {
		Node node = new Node(state, null, -10000, 10000);
		Minimax.player_id = player_id;
		long i = System.currentTimeMillis();

		alphabeta(node, 2);
		System.out.println(System.currentTimeMillis() - i);
		System.out.println("alpha: "+node.alpha);
		for(Node c : node.children) {
			if(c.beta == node.alpha) {
				return c.move;
			}
		}
		return null;
	}
	
	public static void alphabeta(Node node, int depth) {
		// if alpha >= beta, then we simply return
		if(node.alpha >= node.beta) {
			return;
		}
		
		if(node.state.getWinner() == 1-player_id) {
			if(node.state.getTurnPlayer() == player_id) {
			    node.alpha = -9999;
			}else {
				node.beta = -9999;
			}
			return;
		}
		
		if(depth == 0) {
			if(node.state.getTurnPlayer() == player_id) {
			    node.alpha = rollout(node);
			}else {
				node.beta = rollout(node);
			}

		    return;
		}
		
		node.state.getAllLegalMoves().forEach(m -> {
			PentagoBoardState state = (PentagoBoardState) node.state.clone();
			state.processMove(m);
			Node child = new Node(state, m, -10000, 10000);
			node.children.add(child);
		});
		
		node.children.forEach(c -> alphabeta(c, depth-1));
		
		if(node.state.getTurnPlayer() == player_id) {
			node.alpha = Collections.max(node.children, Comparator.comparing(n -> n.beta)).beta;
		}else {
			node.beta = Collections.min(node.children, Comparator.comparing(n -> n.alpha)).alpha;
		}
	}
	
	private static int rollout(Node node) {
		PentagoBoardState state = (PentagoBoardState) node.state.clone();

		int i = 0;
		while (state.getWinner() == Board.NOBODY) {
			Move move = state.getAllLegalMoves().get(0);

			state.processMove((PentagoMove) move);
			i++;
		}

		int winner = state.getWinner();
		if (winner == player_id) {
			return 32-2*node.state.getTurnNumber()-i;
		} else if (winner == 1-player_id) {
			return -(32-2*node.state.getTurnNumber()-i);
		}else {
			return 0;
		}
	}
}
