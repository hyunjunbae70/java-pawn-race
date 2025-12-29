# Java Pawn Race

Imperial 1st year project implementing Pawn Race - a simplified chess variant where you only control pawns.

## What is Pawn Race?

- Two-player game using only pawns on a standard chess board
- Win by advancing one of your pawns to the opposite end
- White pawns start on rank 2, black pawns on rank 7
- Each side has one customizable gap in their pawn formation for added strategy

## Features

- **Player modes**: Human vs human, human vs computer, or computer vs computer
- **Move notation**: Standard Algebraic Notation (SAN) - e.g., `e4` or `exd5`
- **Pawn rules**: Forward moves, diagonal captures, and en passant
- **Computer opponent**: Uses minimax algorithm for move selection
- **Text-based UI**: Clean board display updated after each move

## Quick Start

Compile:
```bash
javac -d build/ -sourcepath src/ src/*.java
```

Run:
```bash
java -cp build PawnRace <player1> <player2> <whiteGap> <blackGap>
```

**Arguments:**
- `player1`, `player2`: `p` for human, `c` for computer
- `whiteGap`, `blackGap`: Column (a-h) with no pawn at start

**Example:**
```bash
java -cp build PawnRace p c a h
```
This starts a game with a human white player vs computer black player, with white missing a pawn in column 'a' and black missing one in column 'h'.

## Gameplay

- Enter moves using SAN notation: `e4`, `exd5`, etc.
- Type `:q` to quit at any time
- Board displays after each move

## Project Structure

The implementation includes:
- Board state management
- Move validation and execution
- SAN parser
- Computer player with minimax search
- Game loop and I/O handling
