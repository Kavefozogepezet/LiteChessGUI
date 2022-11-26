package me.lcgui.gui;

import me.lcgui.game.board.Piece;
import me.lcgui.game.board.PieceType;
import me.lcgui.game.board.Side;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedList;


public class BoardStyle {
    public static final String STYLE = "style";

    public static String[] getAvailableStyles() throws StyleLoadingException {
        LinkedList<String> styleNames = new LinkedList<>();
        File[] files = stylesDir.listFiles();
        if(files == null)
            throw new StyleLoadingException("Styles missing");

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

    private final Image[][] imgs = new BufferedImage[Side.Count.ordinal()][PieceType.Count.ordinal()];

    public Object textureInterpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;

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

    public void loadStyle(String styleName) throws StyleLoadingException {
        if(currentStyleName.equals(styleName))
            return;

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

            if(obj.containsKey("texture_interpolation")) {
                String iStr = (String) obj.get("texture_interpolation");
                textureInterpolation = switch (iStr) {
                    case "bilinear" -> RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                    case "nearest" -> RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                    default -> throw new StyleLoadingException("Interpolation mode not supported.");
                };
            }
        } catch (FileNotFoundException e) {
            throw new StyleLoadingException(styleName + ".json is missing");
        } catch (IOException e) {
            throw new StyleLoadingException(e);
        } catch (ParseException e) {
            throw new StyleLoadingException("Error while parsing the style's json file:\n" + e.getMessage());
        }
        currentStyleName = styleName;
    }

    private void LoadImage(int sIdx, int pIdx, File file) throws StyleLoadingException {
        try {
            imgs[sIdx][pIdx] = ImageIO.read(file);
        } catch (IOException e) {
            throw new StyleLoadingException("Cannot load image: " + file.getPath());
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
