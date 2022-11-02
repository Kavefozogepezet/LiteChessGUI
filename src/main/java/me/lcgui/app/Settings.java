package me.lcgui.app;

import me.lcgui.gui.BoardStyle;

import java.io.Serializable;

public class Settings implements Serializable {
    public boolean autoClaimDraw = false;
    public boolean showCoordinates = true;
    public boolean showPossibleMoves = true;
    public boolean showSquareInfo = true;
    public String styleName = BoardStyle.defaultStyle;
}
