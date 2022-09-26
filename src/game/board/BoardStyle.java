package game.board;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;


public class BoardStyle {
    private static final File texDir = new File(System.getProperty("user.dir"), "textures");
    private static final Image[][] imgs = new Image[Side.Count.ordinal()][PieceType.Count.ordinal()];

    // colors
    public static Color baseWhite = new Color(222, 189, 144);
    public static Color baseBlack = new Color(189, 122, 67);

    public static Color sqihCheck = new Color(255, 0, 0, 128);
    public static Color sqihMoved = new Color(255, 179, 0, 128);
    public static Color sqihArrived = new Color(255, 179, 0, 128);

    public static Color sqmhSelected = new Color(0, 72, 17, 128);
    public static Color sqmhMove = new Color(0, 255, 50, 128);

    public static boolean showCoordinates = true;

    public static Image getPieceTexture(Piece piece) {
        return getPieceTexture(piece.side, piece.type);
    }

    public static Image getPieceTexture(Side side, PieceType pieceType) {
        return imgs[side.ordinal()][pieceType.ordinal()];
    }

    public static void loadStyle(String styleName) {
        JSONParser parser = new JSONParser();
        File dir = new File(texDir, styleName);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void LoadImage(int sIdx, int pIdx, File file) {
        try {
            imgs[sIdx][pIdx] = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
