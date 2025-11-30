# java-pawn-race

This project is an implementation of Pawn Race, a simplified chess variant developed as part of the Imperial College London 1st Year curriculum. Pawn Race is a strategic two-player game where each player controls a set of pawns and competes to be the first to advance one of their pawns to the opposite end of the board. The game features a standard chess board setup with white pawns starting on the second rank and black pawns starting on the seventh rank, with customizable gaps in each player's pawn formation to add strategic depth and variety to each game session.

The implementation supports both human and computer players, allowing for flexible gameplay configurations including human versus human, human versus computer, and computer versus computer matches. Players can input moves using standard algebraic notation (SAN), and the game includes full support for pawn movement rules including forward moves, diagonal captures, and en passant captures. The computer player utilizes intelligent move selection algorithms to provide a challenging opponent experience.

The game is implemented in Java with a modular architecture that separates concerns between board representation, game logic, player management, and move parsing. The codebase includes classes for managing the game board state, validating and applying moves, parsing user input, and handling both human and computer player interactions. The project can be compiled and executed from the command line, with the game state displayed in a text-based format that shows the current board position after each move.

## How to Run

To compile the project, navigate to the project directory and run the following command to compile all Java source files into the `build` directory:

```bash
javac -d build/ -sourcepath src/ src/*.java
```

Once compiled, the game can be executed using the following command:

```bash
java -cp build PawnRace <player1> <player2> <whiteGap> <blackGap>
```

The command requires four arguments: `player1` specifies whether the white player is a human (`p`) or computer (`c`), `player2` specifies whether the black player is a human (`p`) or computer (`c`), `whiteGap` is a letter from a-h indicating which column should have no white pawn in the initial setup, and `blackGap` is a letter from a-h indicating which column should have no black pawn in the initial setup. For example, to run a game with a human white player against a computer black player, with white missing a pawn in column 'a' and black missing a pawn in column 'h', you would execute:

```bash
java -cp build PawnRace p c a h
```

During gameplay, human players enter moves using standard algebraic notation (e.g., `e4` for a pawn move to e4, or `exd5` for a capture). Players can type `:q` at any time to quit the game.
