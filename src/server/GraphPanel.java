import javax.swing.*;
import java.awt.*;

class GraphPanel extends JPanel {
    GraphPointHolder pointHolder = new GraphPointHolder();

    Drawer drawer = new Drawer();

    double additionalC = 0.05;
    double pointSz = 5;

    int numCountX = 10;
    int numCountY = 10;

    double minX = 0;
    double maxX = 0;

    double minY = 0;
    double maxY = 0;

    Color backgroundColor = Color.WHITE;
    Color lineColor = Color.BLUE;
    Color axesColor = Color.BLACK;
    Color gridColor = Color.LIGHT_GRAY;

    private double getStep(double val) {
        double power10 = 1;
        while (val>1) {
            val /= 10;
            power10 *= 10;
        }
        while (val<=0.1) {
            val *= 10;
            power10 /= 10;
        }
        double step;
        if(val < 0.2) {
            step = 0.2;
        } else if(val < 0.5) {
            step = 0.5;
        } else {
            step = 1;
        }
        return step*power10;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        //super.paintComponent(graphics);
        Graphics2D g2d = (Graphics2D) graphics;

        if (pointHolder != null) {
            maxX = maxY = 0;
            minX = minY = 0;
            for (GraphPoint point : pointHolder.getPoints()) {
                if (point.x > maxX) {
                    maxX = point.x;
                }
                if (point.y > maxY) {
                    maxY = point.y;
                }
                if (point.x < minX) {
                    minX = point.x;
                }
                if (point.y < minY) {
                    minY = point.y;
                }
            }
        }

        double distX = maxX - minX;
        double distY = maxY - minY;

        if (distX == 0 || distY == 0)
            return;

        double stepX = getStep(distX / numCountX);
        double stepY = getStep(distY / numCountY);

        minX = ((int)(minX/stepX)*stepX);
        minY = ((int)(minY/stepY)*stepY);

        double pointW = distX * pointSz / getWidth();
        double pointH = distY * pointSz / getHeight();


        drawer.initFullScreen(g2d, minX - distX * additionalC, maxY + distY * additionalC,
                maxX + distX * additionalC, minY - distY * additionalC, getWidth(), getHeight());

        drawer.setColor(backgroundColor);
        drawer.fillRect(minX - distX * additionalC, maxY + distY * additionalC,
                distX * (1 + 2 * additionalC), distY * (1 + 2 * additionalC));

        if (distX == -1 || distY == -1)
            return;


        drawer.setColor(gridColor);

        for (double i = minX; i <= maxX; i += stepX/5) {
            drawer.drawLine(i, minY - distY * additionalC, i, maxY + distY * additionalC);
        }

        for (double i = minY; i <= maxY; i += stepY/5) {
            drawer.drawLine(minX - distX * additionalC, i, maxX + distX * additionalC, i);
        }

        drawer.setColor(axesColor);
        drawer.drawLine(0, minY - distY * additionalC, 0, maxY + distY * additionalC);
        drawer.drawLine(minX - distX * additionalC, 0, maxX + distX * additionalC, 0);

        for (double i = minX; i <= maxX; i += stepX) {
            drawer.drawLine(i, pointH, i, -pointH);
            drawer.drawString(i + pointW, pointH, Double.toString((int) (i * 100) / 100.0));
        }

        for (double i = minY; i <= maxY; i += stepY) {
            drawer.drawLine(pointW, i, -pointW, i);
            drawer.drawString(pointW, i + pointH, Double.toString((int) (i * 100) / 100.0));
        }

        drawer.setColor(lineColor);


        if (pointHolder != null) {
            for (GraphPoint point : pointHolder.getPoints()) {
                drawer.setColor(point.color);
                drawer.fillOval(point.x - pointW / 2, point.y + pointH / 2, pointW, -pointH);
//                drawer.drawLine(point.x + point.deltaX, point.y, point.x - point.deltaX, point.y);
//                drawer.drawLine(point.x, point.y + point.deltaY, point.x, point.y - point.deltaY);
            }
            for (GraphLine line: pointHolder.getLines()) {
                drawer.setColor(line.color);
                for(int i = 0; i < line.nPoints - 1; i++) {
                    double p1x = minX + (maxX - minX) * i / (line.nPoints-1);
                    double p2x = minX + (maxX - minX) * (i+1) / (line.nPoints-1);
                    double p1y = line.function.f(p1x);
                    double p2y = line.function.f(p2x);
                    drawer.drawLine(p1x, p1y, p2x, p2y);
                }
            }
        }
    }
}
