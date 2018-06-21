// Вспомогательный класс, отвечающий за масштабируемое рисование
// Вагин Дмитрий, 9а
// 10.12.2017


import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.util.ArrayList;


public class Drawer {

    public class DImage {
        BufferedImage originalImg;
        Image showingImg;
        double pScale = 1;
        double sScale = 1;
        void setScale(double pScale, double sScale){
            this.pScale = pScale;
            this.sScale = sScale;
            showingImg = originalImg.getScaledInstance(
                    (int)(originalImg.getWidth()*pScale*sScale),
                    (int)(originalImg.getHeight()*pScale*sScale), 0);
        }
        void setSScale (double sScale){
            this.sScale = sScale;
            showingImg = originalImg.getScaledInstance(
                    (int)(originalImg.getWidth()*pScale*sScale),
                    (int)(originalImg.getHeight()*pScale*sScale), 0);
        }

    }



    Graphics2D graphics;
    private double scaleX;
    private double scaleY;
    private int beginX;
    private int beginY;
    private int borderX;
    private int borderY;
    private int width;
    private int height;


    DImage getDImage(BufferedImage img, int size, double reqSize){
        DImage image = new DImage();
        image.originalImg = img;
        if(scaleX!=0)
            image.setScale(reqSize/size, scaleX);
        else
            image.setScale(reqSize/size, 1);
        return image;
    }

    void initFullScreen(Graphics2D g, double x1, double y1, double x2, double y2, int width, int height) {
        this.width = width;
        this.height = height;

        graphics = g;
        g.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        scaleX = width / (x2 - x1);
        scaleY = height / (y2 - y1);

        beginX = (int) (-x1 * scaleX);
        beginY = (int) (-y1 * scaleY);

    }

    void initProportional(Graphics2D g, double x1, double y1, double x2, double y2, int width, int height) {
        this.width = width;
        this.height = height;
        graphics = g;

        double prevScaleX = scaleX;

        scaleX = width / (x2 - x1);
        scaleY = height / (y2 - y1);
        scaleY = scaleX = Math.min(scaleY, scaleX);

        beginX = (int) (-x1 * scaleX + (width - (x2 - x1) * scaleX) / 2);
        beginY = (int) (-y1 * scaleY + (height - (y2 - y1) * scaleY) / 2);

        borderX = (int) (width - (x2 - x1) * scaleX) / 2;
        borderY = (int) (height - (y2 - y1) * scaleY) / 2;
    }

    void initWithoutScale(Graphics2D g, double x1, double y1, double x2, double y2, int width, int height) {
        this.width = width;
        this.height = height;
        graphics = g;

        scaleY = scaleX = 1;

        beginX = (int) (-x1 * scaleX + (width - (x2 - x1) * scaleX) / 2);
        beginY = (int) (-y1 * scaleY + (height - (y2 - y1) * scaleY) / 2);
    }

    void setColor(double red, double green, double blue) {
        graphics.setColor(new Color((int) (red * 255), (int) (green * 255), (int) (blue * 255)));
    }

    void setColor(Color c) {
        graphics.setColor(c);
    }

    void setBackground(double red, double green, double blue) {
        graphics.setBackground((new Color((int) (red * 255), (int) (green * 255), (int) (blue * 255))));
    }


    void drawImage(DImage img, double cx, double cy) {
        if(img.sScale != scaleX) {
            img.setSScale(scaleX);
        }
        int imgW = img.showingImg.getWidth(null);
        int imgH = img.showingImg.getHeight(null);
        graphics.drawImage(img.showingImg, (int)(cx*scaleX+beginX)-imgW/2, (int)(cy*scaleY+beginY)-imgH/2, null);
    }


    void drawLine(double x1, double y1, double x2, double y2) {
        graphics.drawLine((int) (x1 * scaleX) + beginX,
                (int) (y1 * scaleY) + beginY,
                (int) (x2 * scaleX) + beginX,
                (int) (y2 * scaleY) + beginY);
    }


    void drawRect(double x1, double y1, double w, double h) {
        graphics.drawRect((int) (x1 * scaleX) + beginX,
                (int) (y1 * scaleY) + beginY,
                (int) (w * scaleX),
                (int) (h * scaleY));
    }

    void fillRect(double x1, double y1, double w, double h) {
        graphics.fillRect((int) (x1 * scaleX) + beginX,
                (int) (y1 * scaleY) + beginY,
                Math.abs((int) (w * scaleX)),
                Math.abs((int) (h * scaleY)));
    }

    void drawOval(double x1, double y1, double w, double h) {
        graphics.drawOval((int) (x1 * scaleX) + beginX,
                (int) (y1 * scaleY) + beginY,
                (int) (w * scaleX),
                (int) (h * scaleY));
    }

    void fillOval(double x1, double y1, double w, double h) {
        graphics.fillOval((int) (x1 * scaleX) + beginX,
                (int) (y1 * scaleY) + beginY,
                (int) (w * scaleX),
                (int) (h * scaleY));
    }

    void fillPolygon(double[] x, double[] y, int n) {
        int[] x1 = new int[n];
        int[] y1 = new int[n];
        for (int i = 0; i < n; i++) {
            x1[i] = (int) (x[i] * scaleX) + beginX;
            y1[i] = (int) (y[i] * scaleY) + beginY;
        }
        graphics.fillPolygon(x1, y1, n);
    }

    void drawPolygon(double[] x, double[] y, int n) {
        int[] x1 = new int[n];
        int[] y1 = new int[n];
        for (int i = 0; i < n; i++) {
            x1[i] = (int) (x[i] * scaleX) + beginX;
            y1[i] = (int) (y[i] * scaleY) + beginY;
        }
        graphics.drawPolygon(x1, y1, n);
    }

    void drawText(String s, double sz, double x, double y, double anchX, double anchY) {
        //fillOval(x-0.01, y-0.01, 0.02,0.02);

        int newSz = (int) Math.round(sz * scaleY);

        Font f = new Font(null, 0, newSz);

        graphics.setFont(f);

        //f = graphics.getFont();

        FontRenderContext frc = graphics.getFontRenderContext();

        Rectangle2D bounds = f.getStringBounds(s, frc);

        int rx = (int) (beginX + x * scaleX - anchX * bounds.getWidth());
        int ry = (int) (beginY + y * scaleY + (0.85 - anchY) * bounds.getHeight());

        graphics.drawString(s, rx, ry);
    }



    void drawString(double x, double y, String text) {
        graphics.drawString(text, (int) (x * scaleX) + beginX, (int) (y * scaleY) + beginY);
    }

    void fillBorders(Color color) {
        graphics.setColor(color);
        graphics.fillRect(0, 0, borderX, height);
        graphics.fillRect(0, 0, width, borderY);
        graphics.fillRect(width - borderX, 0, borderX, height);
        graphics.fillRect(0, height - borderY, width, borderY);
    }

    Point2D getCoordinates (double x, double y){
        return new Point2D.Double(beginX+x*scaleX, beginY+y*scaleY);
    }
}