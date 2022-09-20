import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

class Square {
    int file, rank;

    Square(int file, int rank) {
        this.file = file;
        this.rank = rank;
    }
}

enum PieceType {
    Empty, King, Queen, Bishop, Knight, Rook, Pawn, Count
}

enum Side {
    None, White, Black
}

enum Piece {
    Empty(PieceType.Empty, Side.None),
    WKing(PieceType.King, Side.White), WQueen(PieceType.Queen, Side.White), WBishop(PieceType.Bishop, Side.White), WKnight(PieceType.Knight, Side.White), WRook(PieceType.Rook, Side.White), WPawn(PieceType.Pawn, Side.White),
    BKing(PieceType.King, Side.Black), BQueen(PieceType.Queen, Side.Black), BBishop(PieceType.Bishop, Side.Black), BKnight(PieceType.Knight, Side.Black), BRook(PieceType.Rook, Side.Black), BPawn(PieceType.Pawn, Side.Black);

    public final PieceType type;
    public final Side side;

    Piece(PieceType t, Side s) {
        type = t;
        side = s;
    }
}

class SquareMouseListener implements MouseListener {
    private final SquareButton button;

    SquareMouseListener(SquareButton button) {
        this.button = button;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton() != MouseEvent.BUTTON1)
            return;
        button.notifyBoard();
    }

    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
}

class SquareButton extends JPanel {
    private final Board board;
    private final Color baseColor;
    public Piece piece = Piece.Empty;

    static Color hColor = Color.green;

    public SquareButton(Board board, Square sq) {
        this.board = board;
        baseColor = (sq.file + sq.rank) % 2 == 0 ?
                new Color(222, 189, 144) :
                new Color(189, 122, 67);

        setBackground(baseColor);
        addMouseListener(new SquareMouseListener(this));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if(piece == Piece.Empty)
            return;

        var size = getSize();
        var pieceImg =
                BoardStyle.getPieceTexture(piece)
                        .getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);
        g.drawImage(pieceImg, 0, 0, this);
    }

    public void notifyBoard() {
        // TODO
        setHighlight(true);
    }

    public void setHighlight(boolean highlight) {
        Color newColor = highlight ?
                ColorUtilities.blend(baseColor, hColor) :
                baseColor;
        setBackground(newColor);
    }
}

public class Board extends JPanel {
    public static final int BOARD_SIZE = 8;

    private final SquareButton[][] squares = new SquareButton[BOARD_SIZE][BOARD_SIZE];

    public Board() {
        setLayout(new GridBagLayout());
        JPanel container = new JPanel(new GridLayout(8, 8)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension parent = getParent().getSize();
                int size = Math.min(parent.width, parent.height);
                size = Math.max(128, size);
                return new Dimension(size, size);
            }
        };

        for(int rank = BOARD_SIZE - 1; rank >= 0; rank--) {
            for(int file = 0; file < BOARD_SIZE; file++) {
                var sqb = new SquareButton(this, new Square(file, rank));
                squares[rank][file] = sqb;
                container.add(sqb);
            }
        }
        add(container);
    }

    public void setPiece(Square square, Piece piece) {
        squares[square.rank][square.file].piece = piece;
    }

    public Piece getPiece(Square square) {
        return squares[square.rank][square.file].piece;
    }
}
