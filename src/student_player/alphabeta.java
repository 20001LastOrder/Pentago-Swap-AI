package student_player;

import java.util.ArrayList;
import java.util.function.UnaryOperator;

import boardgame.Move;

import pentago_swap.PentagoPlayer;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;



public class alphabeta extends StudentPlayer_F{
	private Piece[][] board = new Piece[6][6];
	private static final UnaryOperator<PentagoCoord> getNextHorizontal = c -> new PentagoCoord(c.getX(), c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextVertical = c -> new PentagoCoord(c.getX()+1, c.getY());
    private static final UnaryOperator<PentagoCoord> getNextDiagRight = c -> new PentagoCoord(c.getX()+1, c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextDiagLeft = c -> new PentagoCoord(c.getX()+1, c.getY()-1);
    
    
	public Node minimax(int depth, int player, int alpha, int beta, PentagoBoardState boardState) {
		updateBoard(boardState);
		PentagoBoardState cloneState = (PentagoBoardState) boardState.clone();
		ArrayList<PentagoMove> moves = cloneState.getAllLegalMoves();
		int score;
		Node bestMove = new Node(); 
		//PentagoMove bestMove = null;
		
		//my player is maximizing; while the opponent player is minimizing 
		
		
		if(!moves.isEmpty() && depth!=0) {

			for(PentagoMove move: moves) {
				//System.out.println(move.toPrettyString());
				PentagoBoardState newClone= (PentagoBoardState) cloneState.clone();
				newClone.processMove(move); //will the cloneState change every time
				if (player==AI) {//maximizing
					//System.out.println("op"+newClone.getOpponent());
					//System.out.println("myplayer"+player);
					score=minimax(depth-1,opp,alpha, beta, newClone).getScore();
					//System.out.println(score);
					//System.out.println("alpha = "+alpha);
					if(score > alpha) {
						alpha = score;
						//System.out.println("alpha = "+alpha);
						//System.out.println(move.toPrettyString());
						bestMove.setMove(move);
					}
				}
				else { //opponent 
					score=minimax(depth-1,AI,alpha,beta,newClone).getScore();
					if(score<beta) {
						beta=score;
						bestMove.setMove(move);
						//System.out.println("beta = "+beta);
					}
				}
				if(alpha>=beta) break;
			}
			if(player== AI) {
				Node rs = new Node(bestMove.getMove(),alpha);
				//rs.getMove().toPrettyString();
				return rs;
			}
			else {
				Node rs = new Node(bestMove.getMove(),beta);
				//rs.getMove().toPrettyString();
				return rs;
			}
			
		
		}
		else{		//when depth=0
			
			score= heuristic(AI,cloneState)-heuristic(opp,cloneState);		//pass in game state
			//score= heuristic(AI,cloneState);
			bestMove.setScore(score);
			//System.out.println("ai score = "+heuristic(AI,cloneState));
			//System.out.println("opp score = "+heuristic(opp,cloneState));
			//System.out.println("score = "+score);
			//System.out.println(bestMove.getScore());
			
			
			return bestMove;
			
		}
		}
	
	//calculate the heuristic for both player
    public int heuristic(int player, PentagoBoardState state) {
    		int accumScore=0;
    		PentagoCoord startY =new PentagoCoord(0,0);
    		PentagoCoord startX =new PentagoCoord(0,0);
    		//check vertical both diagonal
    		for(int i=0;i<6;i++) {
    			//System.out.println("Checking Column " + i);
    			int Situation2= checkSituation(player, startY, getNextVertical,state);
   			//System.out.println("Column " + i + ":"+ Situation2);
    			accumScore = Situation2+ accumScore;
    			if(i<2) {
    				int Situation3= checkSituation(player, startY, getNextDiagRight,state);
        			accumScore = Situation3+ accumScore;
        			//System.out.println("Diagonal " + i + ":"+  Situation3);
    			}
    			if(i>3) {
    				int Situation4= checkSituation(player, startY, getNextDiagLeft,state);
        			accumScore = Situation4+ accumScore;
        			//System.out.println("Diagonal " + i + ":"+ Situation4);
    			}
    			if(i!=5) {
    				startY=  getNextHorizontal.apply(startY);
    			}
    		}
    		for(int i=0;i<6;i++) {
    			//System.out.println("Checking Column " + i);
    			int Situation5= checkSituation(player,startX, getNextHorizontal,state);
    			accumScore = Situation5 + accumScore;
    			//System.out.println("Checking Column " + i + ":"+Situation5);
    			if(i==1) {
    				int Situation6= checkSituation(player,startX,getNextDiagRight,state);
    				accumScore= accumScore+Situation6;
    				//System.out.println("Checking Column " + i + ":"+Situation6);
    			}
    			if(i!=5) {
    				startX= getNextVertical.apply(startX);
    			}
    		}
    		PentagoCoord specialCheck= new PentagoCoord(1,5);
    		int specialCase= checkSituation(player,specialCheck,getNextDiagLeft,state);
    		//System.out.println("Special " + ":"+ specialCase);
    		accumScore=accumScore+specialCase;
    		return accumScore;
    }
    
    public void updateBoard(PentagoBoardState bs) {
		for(int i=0;i<6;i++) {
			for(int j=0;j<6;j++) {
				board[i][j] = bs.getPieceAt(i,j);
			}
		}
	}
    
  //set the heuristic value for each situation
    public int scoreTable (int number, int emptySide) {
    		if(number >=5) {
    			return 300000;
    		}
    		else if(number ==4) {
    			if(emptySide==2) return 300000;
    			if(emptySide==1) return 3000;
    			else return 2600;
    			
    		}
    		else if(number ==3) {
    			if(emptySide==2) return 3000;
    			if(emptySide==1) return 800;
    			else return 500;
    			
    		}
    		else if(number ==2) {
    			if(emptySide==2) return 650;
    			if(emptySide==1) return 150;
    			
    			
    		}
    		else if(number ==1) {
    			if(emptySide==2) return 100;
    			
    		}
    		return 0;
    }
    
    private int checkSituation(int player, PentagoCoord board, UnaryOperator<PentagoCoord> direction, PentagoBoardState state){
		int[] result = new int[2];
		int sameColor=0;
		int previous = 0;
		int blockSide=0;
		int partialScore=0;
		updateBoard(state);
		int emptySide= 2-blockSide;
		result[0] = sameColor;
		result[1] = emptySide;
		PentagoCoord current= board;
		Piece color = player==0 ? Piece.WHITE : Piece.BLACK;
		Piece oppColor = player==0 ? Piece.BLACK : Piece.WHITE;
		
		while(true) {
			try {
    			if(color== this.board[current.getX()][current.getY()]) {
    				if(previous==0) sameColor++;
    				if(previous==1) {
    					sameColor++;
    					blockSide++;
    					emptySide= 2-blockSide;
    				}
    				if(direction!=getNextHorizontal) {
    					if(current.getX()==0 || current.getX()==5) {
        					blockSide++;
        					emptySide= 2-blockSide;
        				}
    				}
    				if(direction!=getNextVertical) {
    					if(current.getY()==0 || current.getY()==5) {
        					blockSide++;
        					emptySide= 2-blockSide;
    					}
    				}
    				previous=0;
    				current= direction.apply(current);
    			}
    			else if (this.board[current.getX()][current.getY()] == Piece.EMPTY) {
    				partialScore=partialScore+ scoreTable(sameColor,emptySide);
    				//System.out.println("Streak : " + sameColor + " " + "EmptydSide" + emptySide );
    				sameColor=0;
    				previous=0;
    				blockSide=0;
    				emptySide= 2-blockSide;
    				current= direction.apply(current);
    			}
    			else if (oppColor == this.board[current.getX()][current.getY()]){
    				blockSide++;
    				emptySide= 2-blockSide;
    				partialScore=partialScore+ scoreTable(sameColor,emptySide);
    				sameColor=0;
    				blockSide=0;
    				emptySide= 2-blockSide;
    				previous=1;
    				current= direction.apply(current);
    			}
    			else break;

    		}catch(IllegalArgumentException e) {
    			partialScore= partialScore+scoreTable(sameColor,emptySide);
    			break;
    		}
		} 		
		
		
		return partialScore;
}
	
	
	public class Node{
		PentagoMove move;
		int heuristic;
		
		public Node() {
		}
		
		public Node(PentagoMove move, int heuristic) {
			this.move=move;
			this.heuristic = heuristic;
		}
		
		public Node(int heuristic) {
			this.heuristic=heuristic;
		}
		public PentagoMove getMove()
		{
			return this.move;
		}
		public int getScore() {
			return this.heuristic;
		}
		public void setScore(int heuristic) {
			this.heuristic=heuristic;
		}
		public void setMove(PentagoMove move) {
			this.move=move;
		}
	}

	
}
