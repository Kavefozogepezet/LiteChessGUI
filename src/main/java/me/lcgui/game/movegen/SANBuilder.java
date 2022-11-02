package me.lcgui.game.movegen;

import me.lcgui.game.board.PieceType;
import me.lcgui.game.board.Square;

public class SANBuilder {
    StringBuilder SAN = new StringBuilder();

    private boolean canCheck = true;

    public SANBuilder(Move move, MoveGen movegen) {
        if(move.isCastle()) {
            canCheck = false;
            if (move.is(Move.CASTLE_K))
                SAN.append("O-O");
            else
                SAN.append("O-O-O");
            return;
        }

        SAN.append(move.to.toString());

        if(move.isCapture())
            SAN.insert(0, 'x');

        if(move.moving.type == PieceType.Pawn) {
            if(move.isCapture())
                SAN.insert(0, Square.file2char(move.from.file));
        } else if(move.moving.type == PieceType.King) {
            canCheck = false;
            SAN.insert(0, PieceType.King.toString().toUpperCase());
            return;
        } else {
            String pieceStr = move.moving.toString().toUpperCase();
            boolean
                    useFileNotation = false,
                    useRankNotation = false;

            for(var sameTo : movegen.to(move.to)) {
                if(sameTo.moving != move.moving || sameTo.from.equals(move.from))
                    continue;
                else if(sameTo.from.file != move.from.file)
                    useFileNotation = true;
                else
                    useRankNotation = true;
            }

            if(useRankNotation)
                SAN.insert(0, Square.rank2char(move.from.rank));
            if(useFileNotation)
                SAN.insert(0, Square.file2char(move.from.file));
            SAN.insert(0, pieceStr);
        }
    }

    public void check() {
        if(canCheck)
            SAN.append('+');
    }

    public void mate() {
        if(canCheck)
            SAN.append('#');
    }

    @Override
    public String toString() {
        return SAN.toString();
    }
}
