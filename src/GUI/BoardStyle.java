package GUI;

import game.board.Piece;
import game.board.PieceType;
import game.board.Side;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.*;


public class BoardStyle {
    private static final String defaultStyle = "default";
    private static final File stylesDir = new File(System.getProperty("user.dir"), "styles");

    private String currentStyleName = defaultStyle;

    private final Image[][] imgs = new Image[Side.Count.ordinal()][PieceType.Count.ordinal()];
    private final Image[][] cacheImgs = new Image[Side.Count.ordinal()][PieceType.Count.ordinal()];

    // colors
    public Color baseWhite = new Color(222, 189, 144);
    public Color baseBlack = new Color(189, 122, 67);

    public Color sqihCheck = new Color(255, 0, 0, 128);
    public Color sqihMoved = new Color(255, 179, 0, 128);
    public Color sqihArrived = new Color(255, 179, 0, 128);

    public Color sqmhSelected = new Color(0, 72, 17, 128);
    public Color sqmhMove = new Color(0, 255, 50, 128);

    public boolean showCoordinates = true;

    public BoardStyle() {
        loadStyle(defaultStyle);
    }

    public String getCurrentStyleName() {
        return currentStyleName;
    }

    public Image getPieceTexture(Piece piece) {
        return getPieceTexture(piece.side, piece.type);
    }

    public Image getPieceTexture(Side side, PieceType pieceType) {
        return imgs[side.ordinal()][pieceType.ordinal()];
    }

    public Image getPieceTexResized(Piece piece, Dimension size) {
        return getPieceTexResized(piece.side, piece.type, size);
    }

    public Image getPieceTexResized(Side side, PieceType pieceType, Dimension size) {
        Image cachedImg = cacheImgs[side.ordinal()][pieceType.ordinal()];
        if(cachedImg != null && cachedImg.getWidth(null) == size.width && cachedImg.getHeight(null) == size.height)
            return cachedImg;
        else {
            cacheImgs[side.ordinal()][pieceType.ordinal()] =
                    getPieceTexture(side, pieceType)
                            .getScaledInstance(size.width, size.height, Image.SCALE_AREA_AVERAGING);
            return cacheImgs[side.ordinal()][pieceType.ordinal()];
        }
    }

    public void loadStyle(String styleName) {
        JSONParser parser = new JSONParser();
        File dir = new File(stylesDir, styleName);
        File json = new File(dir, dir.getName() + ".json");

        try(
                var in = new FileReader(json)
                ) {
            JSONObject obj = (JSONObject) parser.parse(in);

            String[] sides = { "white", "black" };
            String[] pieces = { "king", "queen", "bishop", "knight", "rook", "pawn" };
            for(int sideIdx = 0; sideIdx < 2; sideIdx++) {
                JSONObject sideObj = (JSONObject) obj.get(sides[sideIdx]);

                for(int pieceIdx = 0; pieceIdx < PieceType.Count.ordinal(); pieceIdx++) {
                    Object piecePathObj = sideObj.get(pieces[pieceIdx]);

                    if(piecePathObj == null)
                        continue;

                    String piecePath = (String)piecePathObj;
                    piecePath = piecePath.replace('/', File.separatorChar) + ".png";
                    File pieceFile = new File(dir, piecePath);

                    LoadImage(sideIdx, pieceIdx, pieceFile);
                    System.out.println("Loaded " + sides[sideIdx] + ' ' + pieces[pieceIdx] + ": " + pieceFile.getPath());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        currentStyleName = styleName;
    }

    private void LoadImage(int sIdx, int pIdx, File file) {
        try {
            imgs[sIdx][pIdx] = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
