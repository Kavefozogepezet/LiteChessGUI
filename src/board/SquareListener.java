package board;

import java.awt.event.MouseEvent;
import java.util.EventListener;

public interface SquareListener extends EventListener {
    void squareClicked(final Square sq, MouseEvent e);
    void squarePressed(final Square sq, MouseEvent e);
    void squareReleased(final Square sq, MouseEvent e);
}
