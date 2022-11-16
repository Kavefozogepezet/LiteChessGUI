package me.lcgui.gui;

import me.lcgui.game.board.Piece;
import me.lcgui.game.board.PieceType;
import me.lcgui.game.board.Side;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.util.LinkedList;


public class BoardStyle {
    public static final String STYLE = "style";

    public static String[] getAvailableStyles() {
        LinkedList<String> styleNames = new LinkedList<>();
        File[] files = stylesDir.listFiles();
        if(files == null)
            throw new RuntimeException("Styles folder missing");

        for(var file : files) {
            if(!file.isDirectory())
                continue;

            String dirName = file.getName();
            File jsonFile = new File(file, dirName + ".json");
            if(jsonFile.exists())
                styleNames.add(dirName);
        }
        return styleNames.toArray(new String[0]);
    }

    public static final String defaultStyle = "modern";
    private static final File stylesDir = new File(System.getProperty("user.dir"), "styles");

    private String currentStyleName = "";

    private final Image[][] imgs = new Image[Side.Count.ordinal()][PieceType.Count.ordinal()];
    private Image[][] cacheImgs = new Image[Side.Count.ordinal()][PieceType.Count.ordinal()];

    // colors
    public Color baseLight = new Color(222, 189, 144);
    public Color baseDark = new Color(189, 122, 67);

    public Color sqihCheck = new Color(255, 0, 0, 128);
    public Color sqihMoved = new Color(255, 179, 0, 128);
    public Color sqihArrived = new Color(255, 179, 0, 128);

    public Color sqmhSelected = new Color(0, 72, 17, 128);
    public Color sqmhMove = new Color(0, 255, 50, 128);


    public BoardStyle() {}

    public String getCurrentStyleName() {
        return currentStyleName;
    }

    public Image getPieceTexture(Piece piece) {
        return getPieceTexture(piece.side, piece.type);
    }

    public Image getPieceTexture(Side side, PieceType pieceType) {
        return imgs[side.ordinal()][pieceType.ordinal()];
    }

    public Image getResizedPieceTexture(Piece piece, Dimension size) {
        System.out.println("fetched texture with dimensions. " + size);
        return getResizedPieceTexture(piece.side, piece.type, size);
    }

    public Image getResizedPieceTexture(Side side, PieceType pieceType, Dimension size) {
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
        if(currentStyleName.equals(styleName))
            return;

        cacheImgs = new Image[Side.Count.ordinal()][PieceType.Count.ordinal()];

        JSONParser parser = new JSONParser();
        File dir = new File(stylesDir, styleName);
        File json = new File(dir, dir.getName() + ".json");

        try(
                var in = new FileReader(json)
                ) {
            JSONObject obj = (JSONObject) parser.parse(in);

            JSONObject board = (JSONObject) obj.get("board");
            baseLight = readRGB(board, "light");
            baseDark = readRGB(board, "dark");
            sqihCheck = readRGBA(board, "check");
            sqihMoved = readRGBA(board, "piece left");
            sqihArrived = readRGBA(board, "piece arrived");
            sqmhSelected = readRGBA(board, "selected");
            sqmhMove = readRGBA(board, "destination");

            String[] sides = { "white", "black" };
            String[] pieces = { "king", "queen", "bishop", "knight", "rook", "pawn" };
            for(int sideIdx = 0; sideIdx < 2; sideIdx++) {
                JSONObject sideObj = (JSONObject) obj.get(sides[sideIdx]);

                for(int pieceIdx = 0; pieceIdx < PieceType.Count.ordinal(); pieceIdx++) {
                    Object piecePathObj = sideObj.get(pieces[pieceIdx]);

                    if(piecePathObj == null)
                        continue;

                    String piecePath = (String)piecePathObj;
                    piecePath = piecePath.replace('.', File.separatorChar) + ".png";
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

    private Color readRGB(JSONObject parent, String key) {
        JSONArray array = (JSONArray) parent.get(key);
        Long
                r = (Long)array.get(0),
                g = (Long)array.get(1),
                b = (Long)array.get(2);
        return new Color(r.intValue(), g.intValue(), b.intValue());
    }

    private Color readRGBA(JSONObject parent, String key) {
        JSONArray array = (JSONArray) parent.get(key);
        Long
                r = (Long)array.get(0),
                g = (Long)array.get(1),
                b = (Long)array.get(2),
                a = (Long)array.get(3);
        return new Color(r.intValue(), g.intValue(), b.intValue(), a.intValue());
    }
}
