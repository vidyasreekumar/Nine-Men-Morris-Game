import java.io.*;
import java.util.*;

public class ABGameBlack {

    public static void main(String[] args) throws Exception{
        String inputFile = args[0];
        String outputFile = args[1];
        int depth = Integer.parseInt(args[2]);

        Board initial_board = readBoardFromFile(inputFile);
        Board swapped_board = swapBoard(initial_board);
        Move best_move = alphaBeta(swapped_board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
        Board final_board = swapBoard(best_move.getBoard());
        writeBoardToFile(outputFile, final_board);
        System.out.println("Board Position: " + final_board.convertToString());
        System.out.println("Positions evaluated by static estimation: " + best_move.getPositionsEvaluated());
        System.out.println("MINIMAX estimate: " + best_move.getMinimaxEstimate());
    }

    public static Board swapBoard(Board board) {
        for(int i=0;i<board.convertToString().length();i++) {
            if(board.getPositions()[i] == 'B')
                board.getPositions()[i] = 'W';
            else if(board.getPositions()[i] == 'W')
                board.getPositions()[i] = 'B';
        }
        return board;
    }

    public static Board readBoardFromFile(String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();
        br.close();
        return new Board(line);
}

    public static void writeBoardToFile(String file, Board board) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(board.toString());
        bw.close();
}

    public static Move alphaBeta(Board board, int depth, int alpha, int beta, boolean is_max_player) {
        if (depth == 0) {
            int estimate = staticEstimation(board);
            return new Move(board, estimate, 1);
        }

        List<Board> possible_moves = generateMovesGame(board, is_max_player);
        int best_estimate_value = is_max_player ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        Board best_board = null;
        int position_evaluated_count = 0;

        for (Board move : possible_moves) {
            Move current_move = alphaBeta(move, depth - 1, alpha, beta, !is_max_player);
            position_evaluated_count += current_move.getPositionsEvaluated();

            if (is_max_player) {
                if (current_move.getMinimaxEstimate() > best_estimate_value) {
                    best_estimate_value = current_move.getMinimaxEstimate();
                    best_board = move;
                }
                alpha = Math.max(alpha, best_estimate_value);
            } else {
                if (current_move.getMinimaxEstimate() < best_estimate_value) {
                    best_estimate_value = current_move.getMinimaxEstimate();
                    best_board = move;
                }
                beta = Math.min(beta, best_estimate_value);
            }

            if (beta <= alpha) {
                break; 
            }
        }

        return new Move(best_board, best_estimate_value, position_evaluated_count + 1);
    }

    public static List<Board> generateMovesGame(Board board, boolean is_white_player) {
        int player_piece_count = 0;
        char player = is_white_player ? 'W' : 'B';
        for (char position : board.getPositions()) {
            if (position == player) 
                player_piece_count++;
        }
        if(player_piece_count == 3)
            return generateHopping(board, is_white_player);
        else    
            return generateMove(board, is_white_player);
    }

    public static List<Board> generateMove(Board board, boolean is_white_player){
        List<Board> moves = new ArrayList<>();
        char player = is_white_player ? 'W' : 'B';
        char opponent = is_white_player ? 'B' : 'W';

        for (int i = 0; i < 18; i++) {
            if (board.getPositions()[i] == player) {
                int[] adjacent_positions = getNeighbours(i);
                for (int p : adjacent_positions) {
                    if (board.getPositions()[p] == 'x') {
                        Board new_board = new Board(board.convertToString());
                        new_board.getPositions()[i] = 'x';
                        new_board.getPositions()[p] = player;
                        if (closeMill(p, new_board.getPositions())) {
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
            }
        }
        return moves;
    }  

    public static List<Board> generateHopping(Board board, boolean is_white_player) {
        List<Board> moves = new ArrayList<>();
        char player = is_white_player ? 'W' : 'B';
        char opponent = is_white_player ? 'B' : 'W';

        for (int i = 0; i < 18; i++) {
            if (board.getPositions()[i] == player) {
                for (int j = 0; j < 18; j++) {
                    if (board.getPositions()[j] == 'x') {
                        Board new_board = new Board(board.convertToString());
                        new_board.getPositions()[i] = 'x';
                        new_board.getPositions()[j] = player;
                        if (closeMill(j, new_board.getPositions())) {
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
            }
        }
        return moves;
    }

    public static int staticEstimation(Board board) {
        int white_pieces_count = 0;
        int black_pieces_count = 0;
        int white_mills = 0;
        int black_mills = 0;
        int white_potenital_mills = 0;
        int black_potential_mills = 0;
        int white_blocked = 0;
        int black_blocked = 0;
    
        char[] positions = board.getPositions();
    
        for (int i = 0; i < positions.length; i++) {
            if (positions[i] == 'W') {
                white_pieces_count++;
            if (closeMill(i, positions)) 
                white_mills++;
            if (isBlocked(i, positions)) 
                white_blocked++;
                white_potenital_mills += countPotentialMills(i, positions, 'W');
            } else if (positions[i] == 'B') {
                black_pieces_count++;
                if (closeMill(i, positions)) 
                    black_mills++;
                if (isBlocked(i, positions)) 
                    black_blocked++;
                black_potential_mills += countPotentialMills(i, positions, 'B');
            }
        }
    
        return 80 * (white_mills - black_mills) + 10 * (white_potenital_mills - black_potential_mills) + 5 * (white_pieces_count - black_pieces_count) + 3 * (black_blocked - white_blocked);
    }

    public static boolean isBlocked(int position, char[] board) {
        int[] neighbors = getNeighbours(position);

        for (int pos : neighbors) {
            if (board[pos] == 'x') {
                return false;
            }
        }
        return true;
    }

    public static int countPotentialMills(int position, char[] board, char player) {
        int count = 0;
        switch (position) {
            case 0: if ((board[2] == player && board[4] == 'x') || (board[2] == 'x' && board[4] == player)) count++; break;
            case 1: if ((board[3] == player && board[5] == 'x') || (board[3] == 'x' && board[5] == player) || 
                          (board[8] == player && board[17] == 'x') || (board[8] == 'x' && board[17] == player)) count++; break;
            case 2: if ((board[0] == player && board[4] == 'x') || (board[0] == 'x' && board[4] == player)) count++; break;
            case 3: if ((board[1] == player && board[5] == 'x') || (board[1] == 'x' && board[5] == player) || 
                          (board[7] == player && board[14] == 'x') || (board[7] == 'x' && board[14] == player)) count++; break;
            case 4: if ((board[0] == player && board[2] == 'x') || (board[0] == 'x' && board[2] == player)) count++; break;
            case 5: if ((board[1] == player && board[3] == 'x') || (board[1] == 'x' && board[3] == player) || 
                          (board[6] == player && board[11] == 'x') || (board[6] == 'x' && board[11] == player)) count++; break;
            case 6: if ((board[5] == player && board[11] == 'x') || (board[5] == 'x' && board[11] == player) || 
                          (board[7] == player && board[8] == 'x') || (board[7] == 'x' && board[8] == player)) count++; break;
            case 7: if ((board[3] == player && board[14] == 'x') || (board[3] == 'x' && board[14] == player) || 
                          (board[6] == player && board[8] == 'x') || (board[6] == 'x' && board[8] == player)) count++; break;
            case 8: if ((board[1] == player && board[17] == 'x') || (board[1] == 'x' && board[17] == player) || 
                          (board[6] == player && board[7] == 'x') || (board[6] == 'x' && board[7] == player)) count++; break;
            case 9: if ((board[12] == player && board[15] == 'x') || (board[12] == 'x' && board[15] == player) || 
                          (board[10] == player && board[11] == 'x') || (board[10] == 'x' && board[11] == player)) count++; break;
            case 10: if ((board[9] == player && board[11] == 'x') || (board[9] == 'x' && board[11] == player)) count++; break;
            case 11: if ((board[14] == player && board[17] == 'x') || (board[14] == 'x' && board[17] == player) || 
                          (board[5] == player && board[6] == 'x') || (board[5] == 'x' && board[6] == player) || 
                          (board[9] == player && board[10] == 'x') || (board[9] == 'x' && board[10] == player)) count++; break;
            case 12: if ((board[9] == player && board[15] == 'x') || (board[9] == 'x' && board[15] == player) || 
                           (board[13] == player && board[14] == 'x') || (board[13] == 'x' && board[14] == player)) count++; break;
            case 13: if ((board[12] == player && board[14] == 'x') || (board[12] == 'x' && board[14] == player) || 
                           (board[10] == player && board[16] == 'x') || (board[10] == 'x' && board[16] == player)) count++; break;
            case 14: if ((board[11] == player && board[17] == 'x') || (board[11] == 'x' && board[17] == player) || 
                           (board[3] == player && board[7] == 'x') || (board[3] == 'x' && board[7] == player) || 
                           (board[12] == player && board[13] == 'x') || (board[12] == 'x' && board[13] == player)) count++; break;
            case 15: if ((board[9] == player && board[12] == 'x') || (board[9] == 'x' && board[12] == player) || 
                           (board[16] == player && board[17] == 'x') || (board[16] == 'x' && board[17] == player)) count++; break;
            case 16: if ((board[15] == player && board[17] == 'x') || (board[15] == 'x' && board[17] == player) || 
                           (board[10] == player && board[13] == 'x') || (board[10] == 'x' && board[13] == player)) count++; break;
            case 17: if ((board[15] == player && board[16] == 'x') || (board[15] == 'x' && board[16] == player) || 
                           (board[11] == player && board[14] == 'x') || (board[11] == 'x' && board[14] == player) || 
                           (board[1] == player && board[8] == 'x') || (board[1] == 'x' && board[8] == player)) count++; break;
            default: return 0;
        }
        return count;
    }

    // public static int staticEstimation(Board board) {
    //     int white_pieces_count = 0;
    //     int black_pieces_count = 0;
    //     int black_moves = generateMovesGame(board, false).size();
    //     for (char position : board.getPositions()) {
    //         if (position == 'W') 
    //             white_pieces_count++;
    //         if (position == 'B') 
    //             black_pieces_count++;
    //     }
        
    //     if (black_pieces_count <= 2) 
    //         return 10000;
    //     else if (white_pieces_count <= 2) 
    //         return -10000;
    //     else if (black_moves == 0) 
    //         return 10000;
    //     else 
    //         return 1000 * (white_pieces_count - black_pieces_count) - black_moves;
    // }

    public static int[] getNeighbours(int position) {
        switch(position) {
            case 0: return new int[] {1, 2, 15};
            case 1: return new int[] {0, 3, 8};
            case 2: return new int[] {0, 3, 4, 12};
            case 3: return new int[] {1, 2, 5, 7};
            case 4: return new int[] {2, 5, 9};
            case 5: return new int[] {3, 4, 6};
            case 6: return new int[] {5, 7, 11};
            case 7: return new int[] {3, 6, 8, 14};
            case 8: return new int[] {1, 7, 17};
            case 9: return new int[] {4, 10, 12};
            case 10: return new int[] {9, 11, 13};
            case 11: return new int[] {6, 10, 14};
            case 12: return new int[] {2, 9, 13, 15};
            case 13: return new int[] {10, 12, 14, 16};
            case 14: return new int[] {7, 11, 13, 17};
            case 15: return new int[] {0, 12, 16};
            case 16: return new int[] {13, 15, 17};
            case 17: return new int[] {8, 14, 16};
            default: return new int[] {};
        }
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
        for (int i = 0; i < 18; i++) {
            if (board.getPositions()[i] == opponent && !closeMill(i, board.getPositions())) {
                removable_positions.add(i);
            }
        }
        if (removable_positions.isEmpty()) {
            for (int i = 0; i < 18; i++) {
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

    public Board(String positions) {
        this.positions = positions.toCharArray();
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
    private int positions_evaluated_count;

    public Move(Board board, int minimax_estimate_value, int positions_evaluated_count) {
        this.board = board;
        this.minimax_estimate_value = minimax_estimate_value;
        this.positions_evaluated_count = positions_evaluated_count;
    }

    public Board getBoard() {
        return board;
    }

    public int getMinimaxEstimate() {
        return minimax_estimate_value;
    }

    public int getPositionsEvaluated() {
        return positions_evaluated_count;
    }
}

