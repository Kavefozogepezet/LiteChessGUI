import board.Board;
import board.BoardStyle;
import board.Piece;
import board.Square;
import player.HumanPlayer;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        BoardStyle.loadStyle("default");

        var window = new JFrame("Lite Chess GUI");
        window.setSize(new Dimension(600, 400));

        Board board = new Board();
        setupBoard(board);
        window.add(board);

        HumanPlayer player1 = new HumanPlayer(board);
        player1.myTurn();

        //window.pack();
        window.setDefaultCloseOperation(window.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    public static void setupBoard(Board board) {
        board.setPiece(new Square(4, 0), Piece.WKing);
        board.setPiece(new Square(4, 7), Piece.BKing);

        board.setPiece(new Square(3, 0), Piece.WQueen);
        board.setPiece(new Square(3, 7), Piece.BQueen);

        board.setPiece(new Square(0, 0), Piece.WRook);
        board.setPiece(new Square(0, 7), Piece.BRook);
        board.setPiece(new Square(7, 0), Piece.WRook);
        board.setPiece(new Square(7, 7), Piece.BRook);

        board.setPiece(new Square(1, 0), Piece.WKnight);
        board.setPiece(new Square(1, 7), Piece.BKnight);
        board.setPiece(new Square(6, 0), Piece.WKnight);
        board.setPiece(new Square(6, 7), Piece.BKnight);

        board.setPiece(new Square(2, 0), Piece.WBishop);
        board.setPiece(new Square(2, 7), Piece.BBishop);
        board.setPiece(new Square(5, 0), Piece.WBishop);
        board.setPiece(new Square(5, 7), Piece.BBishop);

        for(int i = 0; i < Board.BOARD_SIZE; i++) {
            board.setPiece(new Square(i, 1), Piece.WPawn);
            board.setPiece(new Square(i, 6), Piece.BPawn);
        }
    }
}