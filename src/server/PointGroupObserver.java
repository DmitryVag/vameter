import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

interface DoubleFunction {
    double f(double x);
}

abstract class Approximation {
    abstract int getCoefficientCount();
    abstract String[] getCoefficientNames();
    abstract String getEvaluation();
    double coefficients[];
    abstract void approximate(PointGroup group);
    abstract DoubleFunction getFunction();
}

class SimpleLinearApproximation extends Approximation{
    @Override
    int getCoefficientCount() {
        return 1;
    }

    @Override
    String[] getCoefficientNames() {
        return new String[]{"k"};
    }

    @Override
    String getEvaluation() {
        return "y = k * x";
    }

    @Override
    void approximate(PointGroup group) {
        coefficients = new double[getCoefficientCount()];
        double sumXY = group.getSum((x, y) -> x*y);
        double sumX2 = group.getSum((x, y) -> x*x);
        coefficients[0] = sumXY / sumX2;
    }

    @Override
    DoubleFunction getFunction () {
     return x -> coefficients[0] * x;
    }

}

abstract class LinearizingApproximation extends Approximation {
    @Override
    int getCoefficientCount() {
        return 2;
    }

    abstract double LinearizingFunction(double y);

    @Override
    void approximate(PointGroup group) {
        coefficients = new double[getCoefficientCount()];
        double sumX = group.getSum((x, y) -> x);
        double sumY = group.getSum((x, y) -> LinearizingFunction(y));
        double sumXY = group.getSum((x, y) -> x * LinearizingFunction(y));
        double sumX2 = group.getSum((x, y) -> x * x);
        int n = group.contents.size();

        coefficients[0] = (n*sumXY - sumX*sumY) / (n*sumX2 - sumX*sumX);
        coefficients[1] = (sumY - coefficients[0] * sumX) / n;
    }
}

class LinearApproximation extends LinearizingApproximation {
    @Override
    String getEvaluation() {
        return "y = k * x + b";
    }

    @Override
    String[] getCoefficientNames() {
        return new String[]{"k", "b"};
    }

    @Override
    double LinearizingFunction(double y) {
        return y;
    }

    @Override
    DoubleFunction getFunction () {
        return x -> coefficients[0] * x + coefficients[1];
    }
}

class ExponentialApproximation extends LinearizingApproximation {
    @Override
    String getEvaluation() {
        return "y = exp (k * x + b)";
    }

    @Override
    String[] getCoefficientNames() {
        return new String[]{"k", "b"};
    }

    @Override
    double LinearizingFunction(double y) {
        return y >= 0 ? Math.log(y) : 0;
    }

    @Override
    DoubleFunction getFunction () {
        return x -> Math.exp(coefficients[0] * x + coefficients[1]);
    }
}


public class PointGroupObserver extends JFrame{
    static final int POINTS_IN_LINE = 50;
    String apprTypes[] = {"None", "Simple linear", "Linear","Exponential"};
    JComboBox apprTypeSelector = new JComboBox<>(apprTypes);
    JTextArea descriptionText = new JTextArea("");
    private PointGroup selectedGroup;

    JTable dataTable = new JTable();
    GraphPanel graphPanel = new GraphPanel();


    void setSelectedGroup(PointGroup group) {
        dataTable.setModel(group);
        dataTable.setFillsViewportHeight(true);
        selectedGroup = group;
        graphPanel.pointHolder.addPointGroup(group);
    }

    PointGroupObserver() {
        setSize(700,700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        descriptionText.setEditable(false);
        apprTypeSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(graphPanel.pointHolder.getLines().size() > 0) {
                    graphPanel.pointHolder.deleteLines();
                }
                Approximation approximation;
                if (apprTypeSelector.getSelectedIndex() == 1)
                    approximation = new SimpleLinearApproximation();
                else if(apprTypeSelector.getSelectedIndex() == 2)
                    approximation = new LinearApproximation();
                else if(apprTypeSelector.getSelectedIndex() == 3)
                    approximation = new ExponentialApproximation();
                else return;
                approximation.approximate(selectedGroup);

                StringBuilder text = new StringBuilder();
                text.append("Evaluation: ").append(approximation.getEvaluation()).append("\n");
                text.append("Parameters found: \n");
                for(int i = 0; i < approximation.getCoefficientCount(); i++) {
                    text.append(approximation.getCoefficientNames()[i]).append(" = ").append(approximation.coefficients[i]).append("\n");
                }

                descriptionText.setText(text.toString());
                ArrayList<GraphPoint> points = selectedGroup.contents;
                graphPanel.pointHolder.addLine(new GraphLine(approximation.getFunction(),
                        POINTS_IN_LINE, Color.BLUE));
            }
        });

        setLayout(new GridBagLayout());
        Insets stdInsets = new Insets(3, 5, 3, 5);

        add(new JLabel("Approximation type:"), new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                stdInsets, 0, 0));
        add(apprTypeSelector, new GridBagConstraints(0, 1, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                stdInsets, 0, 0));
        add(descriptionText, new GridBagConstraints(0, 2, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                stdInsets, 0, 0));
        add(dataTable, new GridBagConstraints(1, 0, 1, 3, 0, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                stdInsets, 0, 0));
        add(graphPanel, new GridBagConstraints(2, 0, 1, 3, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                stdInsets, 0, 0));
    }
}
