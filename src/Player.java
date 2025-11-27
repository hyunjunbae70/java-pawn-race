import java.util.*;

public class Player {

    private final Game game;
    private final Board board;
    private final Colour col;
    private final boolean isComputer;
    private Player opponent;
    
    // Search parameters
    private static final int MAX_DEPTH = 6;
    private static final int QUIESCENCE_DEPTH = 4;
    
    // Evaluation weights
    private static final int PAWN_VALUE = 100;
    private static final int ADVANCEMENT_VALUE = 10;
    private static final int PASSED_PAWN_BONUS = 50;
    private static final int PROMOTION_BONUS = 1000;

    public Player(Game game, Board board, Colour col, boolean isComputer) {
        this.game = game;
        this.board = board;
        this.col = col;
        this.isComputer = isComputer;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public Colour getColour() {
        return col;
    }

    public boolean isComputer() {
        return isComputer;
    }

    public Square[] getAllPawns() {

        List<Square> squares = new ArrayList<>();

        for (int i=0; i<Utils.dim; i++) {
            for (int j=0; j<Utils.dim; j++) {
                Square thisSq = board.getSquare(i, j);
                if (thisSq.occupiedBy() == col) {
                    squares.add(thisSq);
                }
            }
        }

        return squares.toArray(new Square[squares.size()]);

    }

    public Move[] getAllValidMoves() {

        Colour opponent = (col == Colour.WHITE) ? Colour.BLACK : Colour.WHITE;

        // Set the starting square depending on the player
        int startRow = (col == Colour.WHITE) ? 1 : 6;

        List<Move> validMoves = new ArrayList<>();
        Square[] playerPawns = getAllPawns();

        for (Square sq : playerPawns) {

            // Set the multiplier (direction) depending on white/black
            int dir = (col == Colour.WHITE) ? 1 : -1;

            // If this player is White/Black, try the different moves
            // in the appropriate direction

            // Look if the previous move was a 2 square move
            Move lastMove = game.getLastMove();

			if (lastMove != null) {
				if (Math.abs(lastMove.getFrom().getY() - lastMove.getTo().getY()) == 2) {
					// There could be an "en-passant" capture possible
					// Make sure the move happened in the column to the left or right
					if (Math.abs(lastMove.getTo().getX() - sq.getX()) == 1) {
						// Final check: the White and Black pawn must now be on the same row
						// to count as an en-passant move
						if (lastMove.getTo().getY() == sq.getY()) {
							// Find the square containing the opponent
							int captureY = (lastMove.getFrom().getY() + lastMove.getTo().getY()) / 2;
							Square enPassant = board.getSquare(lastMove.getTo().getX(), captureY);
							if (enPassant != null && enPassant.occupiedBy() == Colour.NONE) {
								Move mEPass = new Move(sq, enPassant, true, true);
								validMoves.add(mEPass);
							}
						}
					}
				}
			}

            // Look for capture moves
            if (sq.getX() > 0) {
                Square capt = board.getSquare(sq.getX() - 1, sq.getY() + (1 * dir));
                // Only valid if square exists and is occupied by the opponent
                if (capt != null && capt.occupiedBy() == opponent) {
                    Move mCapt = new Move(sq, capt, true, false);
                    validMoves.add(mCapt);
                }
            }
            if (sq.getX() < Utils.dim - 1) {
                Square captAlt = board.getSquare(sq.getX() + 1, sq.getY() + (1 * dir));
                // Only valid if square exists and is occupied by the opponent
                if (captAlt != null && captAlt.occupiedBy() == opponent) {
                    Move mCaptAlt = new Move(sq, captAlt, true, false);
                    validMoves.add(mCaptAlt);
                }
            }

            // Look for standard 1 square moves
            Square sOne = board.getSquare(sq.getX(), sq.getY() + (1 * dir));
            // Only valid if square exists and is empty
            if (sOne != null && sOne.occupiedBy() == Colour.NONE) {
                Move mOne = new Move(sq, sOne, false, false);
                validMoves.add(mOne);
            }
            else {
                continue;
            }

            // If this is the starting square, can move forward 2 squares
            if (sq.getY() == startRow) {
                Square sTwo = board.getSquare(sq.getX(), sq.getY() + (2 * dir));
                // Only valid if square exists and is empty
                if (sTwo != null && sTwo.occupiedBy() == Colour.NONE) {
                    Move mTwo = new Move(sq, sTwo, false, false);
                    validMoves.add(mTwo);
                }
            }

        }

        return validMoves.toArray(new Move[validMoves.size()]);

    }

    public boolean isPassedPawn(Square square) {
        if (square.occupiedBy() != col) {
            return false;
        }
        
        Colour opponentCol = (col == Colour.WHITE) ? Colour.BLACK : Colour.WHITE;
        int dir = (col == Colour.WHITE) ? 1 : -1;
        int endRow = (col == Colour.WHITE) ? 7 : 0;
        int x = square.getX();
        int y = square.getY();
        
        // Check squares in front of the pawn
        for (int row = y + dir; row != endRow + dir; row += dir) {
            // Check the square directly in front
            if (row >= 0 && row < Utils.dim) {
                Square frontSq = board.getSquare(x, row);
                if (frontSq != null && frontSq.occupiedBy() == opponentCol) {
                    return false;
                }
            }
            
            // Check diagonally left
            if (x > 0 && row >= 0 && row < Utils.dim) {
                Square leftSq = board.getSquare(x - 1, row);
                if (leftSq != null && leftSq.occupiedBy() == opponentCol) {
                    return false;
                }
            }
            
            // Check diagonally right
            if (x < Utils.dim - 1 && row >= 0 && row < Utils.dim) {
                Square rightSq = board.getSquare(x + 1, row);
                if (rightSq != null && rightSq.occupiedBy() == opponentCol) {
                    return false;
                }
            }
        }
        
        return true;
    }

    public void randomMove() {
        Move[] valid = getAllValidMoves();
        int randomMove = new Random().nextInt(valid.length);
        game.applyMove(valid[randomMove], true);
    }

    public Move smartMove(boolean lookFuture) {
        // Use alpha-beta pruning with quiescence search
        Move[] validMoves = getAllValidMoves();
        
        if (validMoves.length == 0) {
            return null;
        }
        
        // Sort moves for better alpha-beta pruning (captures first, then by evaluation)
        Arrays.sort(validMoves, new Comparator<Move>() {
            public int compare(Move m1, Move m2) {
                // Prioritize captures
                if (m1.isCapture() && !m2.isCapture()) return -1;
                if (!m1.isCapture() && m2.isCapture()) return 1;
                // Then prioritize moves closer to promotion
                int endRow = (col == Colour.WHITE) ? 7 : 0;
                int dist1 = Math.abs(m1.getTo().getY() - endRow);
                int dist2 = Math.abs(m2.getTo().getY() - endRow);
                return dist1 - dist2;
            }
        });
        
        int bestValue = Integer.MIN_VALUE;
        Move bestMove = validMoves[0];
        
        for (Move move : validMoves) {
            game.applyMove(move, false);
            int value = minimax(MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            game.unapplyMove();
            
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
    /**
     * Minimax algorithm with alpha-beta pruning
     */
    private int minimax(int depth, int alpha, int beta, boolean maximizingPlayer) {
        // Check for terminal states (silent mode during search)
        if (game.isFinished(false)) {
            Colour winner = game.getGameResult();
            if (winner == col) {
                return Integer.MAX_VALUE - (MAX_DEPTH - depth); // Prefer faster wins
            } else if (winner != Colour.NONE) {
                return Integer.MIN_VALUE + (MAX_DEPTH - depth); // Prefer slower losses
            }
        }
        
        // If depth is 0, use quiescence search or static evaluation
        if (depth == 0) {
            return quiescenceSearch(alpha, beta, QUIESCENCE_DEPTH, maximizingPlayer);
        }
        
        Player currentPlayer = maximizingPlayer ? this : opponent;
        Move[] validMoves = currentPlayer.getAllValidMoves();
        
        if (validMoves.length == 0) {
            // No moves available - this player loses
            return maximizingPlayer ? Integer.MIN_VALUE + (MAX_DEPTH - depth) 
                                   : Integer.MAX_VALUE - (MAX_DEPTH - depth);
        }
        
        // Sort moves for better alpha-beta pruning
        Arrays.sort(validMoves, new Comparator<Move>() {
            public int compare(Move m1, Move m2) {
                if (m1.isCapture() && !m2.isCapture()) return -1;
                if (!m1.isCapture() && m2.isCapture()) return 1;
                return 0;
            }
        });
        
        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : validMoves) {
                game.applyMove(move, false);
                int eval = minimax(depth - 1, alpha, beta, false);
                game.unapplyMove();
                
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Alpha-beta cutoff
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : validMoves) {
                game.applyMove(move, false);
                int eval = minimax(depth - 1, alpha, beta, true);
                game.unapplyMove();
                
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // Alpha-beta cutoff
                }
            }
            return minEval;
        }
    }
    
    /**
     * Quiescence search - continues searching in tactical positions (captures, promotions)
     */
    private int quiescenceSearch(int alpha, int beta, int depth, boolean maximizingPlayer) {
        // Static evaluation
        int standPat = evaluatePosition();
        
        if (maximizingPlayer) {
            if (standPat >= beta) return beta;
            alpha = Math.max(alpha, standPat);
        } else {
            if (standPat <= alpha) return alpha;
            beta = Math.min(beta, standPat);
        }
        
        // Stop quiescence search if depth exhausted
        if (depth <= 0) {
            return standPat;
        }
        
        Player currentPlayer = maximizingPlayer ? this : opponent;
        Move[] validMoves = currentPlayer.getAllValidMoves();
        
        // Only consider captures and promotions in quiescence search
        List<Move> tacticalMoves = new ArrayList<>();
        Colour currentColour = currentPlayer.getColour();
        for (Move move : validMoves) {
            if (move.isCapture() || isPromotionForColour(move, currentColour)) {
                tacticalMoves.add(move);
            }
        }
        
        if (tacticalMoves.isEmpty()) {
            return standPat;
        }
        
        // Sort by capture value (rough estimate)
        final Colour finalCurrentColour = currentColour;
        tacticalMoves.sort(new Comparator<Move>() {
            public int compare(Move m1, Move m2) {
                int val1 = isPromotionForColour(m1, finalCurrentColour) ? PROMOTION_BONUS : (m1.isCapture() ? PAWN_VALUE : 0);
                int val2 = isPromotionForColour(m2, finalCurrentColour) ? PROMOTION_BONUS : (m2.isCapture() ? PAWN_VALUE : 0);
                return val2 - val1;
            }
        });
        
        if (maximizingPlayer) {
            int maxEval = standPat;
            for (Move move : tacticalMoves) {
                game.applyMove(move, false);
                int eval = quiescenceSearch(alpha, beta, depth - 1, false);
                game.unapplyMove();
                
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Alpha-beta cutoff
                }
            }
            return maxEval;
        } else {
            int minEval = standPat;
            for (Move move : tacticalMoves) {
                game.applyMove(move, false);
                int eval = quiescenceSearch(alpha, beta, depth - 1, true);
                game.unapplyMove();
                
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // Alpha-beta cutoff
                }
            }
            return minEval;
        }
    }
    
    /**
     * Evaluate the current position from this player's perspective
     */
    private int evaluatePosition() {
        // Check for terminal states first (silent mode during search)
        if (game.isFinished(false)) {
            Colour winner = game.getGameResult();
            if (winner == col) {
                return Integer.MAX_VALUE / 2;
            } else if (winner != Colour.NONE) {
                return Integer.MIN_VALUE / 2;
            }
        }
        
        int score = 0;
        
        // Material evaluation
        Square[] myPawns = getAllPawns();
        Square[] opponentPawns = opponent.getAllPawns();
        score += (myPawns.length - opponentPawns.length) * PAWN_VALUE;
        
        // Advancement evaluation
        int endRow = (col == Colour.WHITE) ? 7 : 0;
        int opponentEndRow = (col == Colour.WHITE) ? 0 : 7;
        
        for (Square pawn : myPawns) {
            int distance = Math.abs(pawn.getY() - endRow);
            score += (Utils.dim - distance) * ADVANCEMENT_VALUE;
            
            // Promotion bonus
            if (pawn.getY() == endRow) {
                score += PROMOTION_BONUS;
            }
            
            // Passed pawn bonus
            if (isPassedPawn(pawn)) {
                score += PASSED_PAWN_BONUS;
            }
        }
        
        for (Square pawn : opponentPawns) {
            int distance = Math.abs(pawn.getY() - opponentEndRow);
            score -= (Utils.dim - distance) * ADVANCEMENT_VALUE;
            
            // Promotion threat
            if (pawn.getY() == opponentEndRow) {
                score -= PROMOTION_BONUS;
            }
            
            // Opponent passed pawn penalty
            if (opponent.isPassedPawn(pawn)) {
                score -= PASSED_PAWN_BONUS;
            }
        }
        
        return score;
    }
    
    /**
     * Check if a move results in promotion for a given colour
     */
    private boolean isPromotionForColour(Move move, Colour colour) {
        int endRow = (colour == Colour.WHITE) ? 7 : 0;
        return move.getTo().getY() == endRow;
    }
    
    /**
     * Check if a move results in promotion for this player
     */
    private boolean isPromotion(Move move) {
        return isPromotionForColour(move, col);
    }
    
    private int getMoveWeight(Move move, boolean lookFuture) {
        // Legacy method kept for compatibility, but not used in new implementation
        // Start by generating a random number from 1-100
        int weight;
        weight = new Random().nextInt(100);

        // En-passant moves and captures are prioritised
        if (move.isCapture()) {
            weight = 500;
        }

        // Prioritise moves closer to the finish
        int endRow = (col == Colour.WHITE) ? 7 : 0;
        int distanceFromFinish = Math.abs(move.getTo().getY() - endRow);
        weight += (Utils.dim - distanceFromFinish) * 100;

        if (lookFuture) {
            // Try this move and see what happens
            // If the next player's top move would result in a capture, give this move a low weighting
            game.applyMove(move, false);
            Move otherPlayMove = smartMove(false);
            if (otherPlayMove != null && otherPlayMove.isCapture()) {
                weight = 0;
            }
            game.unapplyMove();
        }

        return weight;
    }

}