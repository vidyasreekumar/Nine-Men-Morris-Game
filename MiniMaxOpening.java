import java.io.*;
import java.util.*;

public class MiniMaxOpening {

    public static void main(String[] args) throws Exception {
        String inputFile = args[0];
        String outputFile = args[1];
        int depth = Integer.parseInt(args[2]);

        Board input_board = readBoardFromFile(inputFile);
        Move best_move = miniMax(input_board, depth, true);

        writeBoardToFile(outputFile, best_move.getBoard());
        System.out.println("Board Position: " + best_move.getBoard().convertToString());
        System.out.println("Positions evaluated by static estimation: " + best_move.getPositionsEvaluated());
        System.out.println("MINIMAX estimate: " + best_move.getMinimaxEstimate());
    }

    public static Board readBoardFromFile(String file) throws Exception {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            br.close();
            return new Board(line);
    }

    public static void writeBoardToFile(String file, Board board) throws Exception {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(board.convertToString());
            bw.close();
    }

    public static Move miniMax(Board board, int depth, boolean is_max_player) {
        if (depth == 0) {
            int estimate = staticEstimation(board);
            return new Move(board, estimate, 1);
        }

        List<Board> possible_moves = generateMovesOpening(board, is_max_player);
        int best_estimate_value = is_max_player ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        Board best_board = null;
        int position_evaluated_count = 0;

        for (Board move : possible_moves) {
            Move current_move = miniMax(move, depth - 1, !is_max_player);
            position_evaluated_count += current_move.getPositionsEvaluated();

            if (is_max_player && current_move.getMinimaxEstimate() > best_estimate_value) {
                best_estimate_value = current_move.getMinimaxEstimate();
                best_board = move;
            } 
            else if (!is_max_player && current_move.getMinimaxEstimate() < best_estimate_value) {
                best_estimate_value = current_move.getMinimaxEstimate();
                best_board = move;
            }
        }

        return new Move(best_board, best_estimate_value, position_evaluated_count + 1);
    }

    public static List<Board> generateMovesOpening(Board board, boolean is_white_player) {
        List<Board> moves = new ArrayList<>();
        char player = is_white_player ? 'W' : 'B';
        char opponent = is_white_player ? 'B' : 'W';

        for (int i = 0; i < board.convertToString().length(); i++) {
            if (board.getPositions()[i] == 'x') {
                Board new_board = new Board(board.convertToString());
                new_board.getPositions()[i] = player;
                if (closeMill(i, new_board.getPositions())) {
                    List<Integer> removable_positions = generateRemove(new_board, opponent);
                    for (int pos : removable_positions) {
                        Board copy_board = new Board(new_board.convertToString());
                        copy_board.getPositions()[pos] = 'x';
                        moves.add(copy_board);
                    }
                } else {
                    moves.add(new_board);
                }
            }
        }
        return moves;
    }

    public static int staticEstimation(Board board) {
        int white_pieces_count = 0;
        int black_pieces_count = 0;
        for (char position : board.getPositions()) {
            if (position == 'W') 
                white_pieces_count++;
            if (position == 'B') 
                black_pieces_count++;
        }
        return white_pieces_count - black_pieces_count;
    }

    public static boolean closeMill(int position, char[] board) {
        char chr = board[position];
        switch (position) {
            case 0: return (board[2] == chr && board[4] == chr);
            case 1: return (board[3] == chr && board[5] == chr) || (board[8] == chr && board[17] == chr);
            case 2: return (board[0] == chr && board[4] == chr);
            case 3: return (board[1] == chr && board[5] == chr) || (board[7] == chr && board[14] == chr);
            case 4: return (board[0] == chr && board[2] == chr);
            case 5: return (board[1] == chr && board[3] == chr) || (board[6] == chr && board[11] == chr);
            case 6: return (board[5] == chr && board[11] == chr) || (board[7] == chr && board[8] == chr);
            case 7: return (board[3] == chr && board[14] == chr) || (board[6] == chr && board[8] == chr);
            case 8: return (board[1] == chr && board[17] == chr) || (board[6] == chr && board[7] == chr);
            case 9: return (board[12] == chr && board[15] == chr) || (board[10] == chr && board[11] == chr);
            case 10: return (board[9] == chr && board[11] == chr) || (board[13] == chr && board[16] == chr);
            case 11: return (board[14] == chr && board[17] == chr) || (board[5] == chr && board[6] == chr) || (board[9] == chr && board[10] == chr);
            case 12: return (board[9] == chr && board[15] == chr) || (board[13] == chr && board[14] == chr);
            case 13: return (board[12] == chr && board[14] == chr) || (board[10] == chr && board[16] == chr);
            case 14: return (board[11] == chr && board[17] == chr) || (board[3] == chr && board[7] == chr) || (board[12] == chr && board[13] == chr);
            case 15: return (board[9] == chr && board[12] == chr) || (board[16] == chr && board[17] == chr);
            case 16: return (board[15] == chr && board[17] == chr) || (board[10] == chr && board[13] == chr);
            case 17: return (board[15] == chr && board[16] == chr) || (board[11] == chr && board[14] == chr) || (board[1] == chr && board[8] == chr);
            default: return false;
        }
    }

    public static List<Integer> generateRemove(Board board, char opponent) {
        List<Integer> removable_positions = new ArrayList<>();
        for (int i = 0; i < board.convertToString().length(); i++) {
            if (board.getPositions()[i] == opponent && !closeMill(i, board.getPositions())) {
                removable_positions.add(i);
            }
        }
        if (removable_positions.isEmpty()) {
            for (int i = 0; i < board.convertToString().length(); i++) {
                if (board.getPositions()[i] == opponent) {
                    removable_positions.add(i);
                }
            }
        }
        return removable_positions;
    }
}

class Board {
    private char[] positions;

    public Board(String boardStr) {
        this.positions = boardStr.toCharArray();
    }

    public char[] getPositions() {
        return positions;
    }

    public String convertToString() {
        return new String(positions);
    }
}

class Move {
    private Board board;
    private int minimax_estimate_value;
    private int position_evaluated_count;

    public Move(Board board, int minimax_estimate_value, int position_evaluated_count) {
        this.board = board;
        this.minimax_estimate_value = minimax_estimate_value;
        this.position_evaluated_count = position_evaluated_count;
    }

    public Board getBoard() {
        return board;
    }

    public int getMinimaxEstimate() {
        return minimax_estimate_value;
    }

    public int getPositionsEvaluated() {
        return position_evaluated_count;
    }
}
