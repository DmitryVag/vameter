import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.util.ArrayList;

class GraphPoint {
    static final int VALUES_COUNT = 2;

    double getValueAt(int index) {
        if(index == 0)
            return x;
        else if(index == 1)
            return y;
        else
            return Double.NaN;
    }

    double x;
    double y;

    double deltaX;
    double deltaY;
    Color color;

    GraphPoint(double x, double y, double deltaX, double deltaY, Color color) {
        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.color = color;
    }
}

class GraphLine {
    Color color;
    DoubleFunction function;
    int nPoints;

    GraphLine(DoubleFunction function, int nPoints, Color color) {
        this.function = function;
        this.color = color;
        this.nPoints = nPoints;
    }
}


interface SummingFunction {
    double f(double x, double y);
}

class PointGroup extends AbstractTableModel {
    ArrayList<GraphPoint> contents = new ArrayList<>();
    String description;

    double getSum(SummingFunction function) {
        double sum = 0;
        for (GraphPoint point: contents) {
            sum += function.f(point.x, point.y);
        }
        return sum;
    }

    @Override
    public String getColumnName(int column) {
        if(column == 0)
            return "Voltage (V)";
        if(column == 1)
            return "Amperage (mA)";
        return "Unexpected column";
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public int getRowCount() {
        return contents.size();
    }

    @Override
    public int getColumnCount() {
        return GraphPoint.VALUES_COUNT;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return String.format("%1.3f", contents.get(rowIndex).getValueAt(columnIndex));
    }
}

class PointSession {
    ArrayList<PointGroup> contents = new ArrayList<>();
    int id;

    @Override
    public String toString() {
        return "Session #" + id;
    }
}


class GraphPointHolder {
    private ArrayList<PointSession> sessions = new ArrayList<>();
    private ArrayList<GraphPoint> points = new ArrayList<>();
    private ArrayList<GraphLine> lines = new ArrayList<>();
    private int currentSession = -1;

    synchronized ArrayList<GraphPoint> getPoints() {
        return new ArrayList<>(points);
    }

    synchronized void addPoints(ArrayList<GraphPoint> points, String desc) {
        if (desc.startsWith("N")) {
            currentSession++;
            PointSession s = new PointSession();
            s.id = currentSession + 1;
            sessions.add(s);
        }

        PointGroup group = new PointGroup();
        group.contents = points;
        group.description = desc.split("\"")[1];

        sessions.get(currentSession).contents.add(group);

        this.points.addAll(points);

    }

    synchronized void addPointGroup (PointGroup pointGroup){
        this.points.addAll(pointGroup.contents);
    }

    synchronized void addLine(GraphLine line) {
        lines.add(line);
    }

    synchronized void deleteLines() {
        lines.clear();
    }


    synchronized ArrayList<GraphLine> getLines() {
        return new ArrayList<>(lines);
    }

    synchronized TreeModel getTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Data");
        for (PointSession session : sessions) {
            DefaultMutableTreeNode sessionNode = new DefaultMutableTreeNode(session);
            for (PointGroup group : session.contents) {
                sessionNode.add(new DefaultMutableTreeNode(group));
            }
            root.add(sessionNode);
        }
        return new DefaultTreeModel(root);
    }

}
