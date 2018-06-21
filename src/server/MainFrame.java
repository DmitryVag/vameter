// Часть 1 - график функции
// Вагин Дмитрий, 9а
// 10.12.2017


import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import arduino.*;

class CommandList {
    private ArrayList<String> commands = new ArrayList<>();

    synchronized boolean hasNext() {
        return commands.size() > 0;
    }

    synchronized String next() {
        String command = commands.get(0);
        commands.remove(0);
        return command;
    }

    synchronized void add(String command) {
        commands.add(command);
    }
}

class MainPanel extends JPanel {
    Arduino arduino = null;

    JButton refreshDataButton = new JButton("Get data");
    JButton refreshListButton = new JButton("Refresh ports");
    JButton connectButton = new JButton("Connect");
    JButton editButton = new JButton("Edit");
    JTree dataTree = new JTree((Object[]) null);
    PortDropdownMenu menu = new PortDropdownMenu();

    ArduinoProcessor processor;
    CommandList commandList = new CommandList();

    PointGroup selectedPointGroup = null;
    ArrayList<PointGroup> selectedPointGroups = new ArrayList<>();


    MainPanel() {
        refreshListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                menu.refreshMenu();
            }
        });

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (arduino != null) {
                    arduino.closeConnection();
                }
                arduino = new Arduino(menu.getSelectedItem().toString(), 9600);
                arduino.openConnection();
                processor = new ArduinoProcessor(arduino, new Runnable() {
                    @Override
                    public void run() {
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        MainFrame.mainFrame.graphPanel.repaint();
                        dataTree.setModel(MainFrame.mainFrame.graphPanel.pointHolder.getTree());
                        dataTree.setRootVisible(true);
                    }
                }, commandList, MainFrame.mainFrame.graphPanel.pointHolder);
            }
        });
        refreshDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                commandList.add("Wait 0 2000# ");
                commandList.add("Get 2 0#N \"2-0\"");
                commandList.add("Get 3 0# \"3-0\"");
                commandList.add("Get 4 0# \"4-0\"");
                commandList.add("Wait 0 1000# ");
                commandList.add("Get 2 1# \"2-1\"");
                commandList.add("Get 3 1# \"3-1\"");
                commandList.add("Get 4 1# \"4-1\"");
            }
        });


        dataTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                selectedPointGroup = null;
                selectedPointGroups.clear();

                TreePath[] paths = ((JTree) treeSelectionEvent.getSource()).getSelectionPaths();
                for (GraphPoint point : MainFrame.mainFrame.graphPanel.pointHolder.getPoints()) {
                    if (paths == null)
                        point.color = Color.BLUE;
                    else
                        point.color = Color.LIGHT_GRAY;
                }
                if (paths == null) {
                    MainFrame.mainFrame.graphPanel.repaint();
                    return;
                }



                for (TreePath path : paths) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (node.getUserObject().getClass() == PointGroup.class) {
                        PointGroup theGroup = (PointGroup) node.getUserObject();
                        selectedPointGroups.add(theGroup);
                        for (GraphPoint point : theGroup.contents) {
                            point.color = Color.GREEN;
                        }
                    } else if (node.getUserObject().getClass() == PointSession.class) {
                        PointSession session = (PointSession) node.getUserObject();

                        selectedPointGroups.addAll(session.contents);

                        for (PointGroup group : session.contents) {
                            for (GraphPoint point : group.contents) {
                                point.color = Color.GREEN;
                            }
                        }
                    }
                }
                if(selectedPointGroups.size() == 1) {
                    selectedPointGroup = selectedPointGroups.get(0);
                } else if(selectedPointGroups.size() != 0) {
                    selectedPointGroup = new PointGroup();
                    for (PointGroup group : selectedPointGroups)
                        selectedPointGroup.contents.addAll(group.contents);
                }
                MainFrame.mainFrame.graphPanel.repaint();
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(selectedPointGroup != null) {
                    PointGroupObserver observer = new PointGroupObserver();
                    observer.setSelectedGroup(selectedPointGroup);
                    observer.setVisible(true);
                }
            }
        });

        Insets stdInsets = new Insets(3, 5, 3, 5);
        setLayout(new GridBagLayout());
        add(menu, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                stdInsets, 0, 0));
        add(refreshListButton, new GridBagConstraints(1, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                stdInsets, 0, 0));
        add(connectButton, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                stdInsets, 0, 0));
        add(refreshDataButton, new GridBagConstraints(0, 1, 3, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                stdInsets, 0, 0));
        add(dataTree, new GridBagConstraints(0, 2, 3, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                stdInsets, 0, 0));
        add(editButton, new GridBagConstraints(0, 3, 3, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                stdInsets, 0, 0));
    }
}

class ParametersPanel extends JPanel {
    static class IndexedTextField extends JTextField {
        private int index;
        int getIndex() {
            return index;
        }
        IndexedTextField(int index, int columns) {
            super(columns);
            this.index = index;
        }
    }

    static final int PARAMETERS_COUNT = 1;
    String parameterNames[] = {"Voltage step"};
    String parameterIndexes[] = {"vStep"};
    int parameterValues[] = new int[PARAMETERS_COUNT];
    boolean parameterChanged[] = new boolean[PARAMETERS_COUNT];
    JButton applyButton = new JButton("Apply");

    ActionListener textFieldAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            IndexedTextField source = (IndexedTextField) e.getSource();
            parameterChanged[source.getIndex()] = true;
            parameterValues[source.getIndex()] = Integer.parseInt(source.getText());
        }
    };

    ParametersPanel() {
        Insets stdInsets = new Insets(3, 5, 3, 5);
        setLayout(new GridBagLayout());
        for(int i = 0; i < PARAMETERS_COUNT; i++) {
            add(new JLabel(parameterNames[i]), new GridBagConstraints(0, i, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    stdInsets, 0, 0));
            IndexedTextField field = new IndexedTextField(i, 20);
            add(field, new GridBagConstraints(1, i, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    stdInsets, 0, 0));
            field.addActionListener(textFieldAction);
        }
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(int i = 0; i < PARAMETERS_COUNT; i++) {
                    if(parameterChanged[i])
                        MainFrame.mainFrame.mainPanel.commandList.add("Set " + parameterIndexes[i] + " " + parameterValues[i] + "# ");
                }
            }
        });
        add(applyButton, new GridBagConstraints(0, PARAMETERS_COUNT, 2, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                stdInsets, 0, 0));
    }
}

public class MainFrame extends JFrame {
    static MainFrame mainFrame = new MainFrame();
    MainPanel mainPanel = new MainPanel();
    GraphPanel graphPanel = new GraphPanel();


    MainFrame() {
        JPanel panel1 = new JPanel();

        setTitle("Volt-Amper characteristics");
        setSize(800, 500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        panel1.setLayout(new BorderLayout());
        panel1.add(graphPanel);
        panel1.add(mainPanel, BorderLayout.WEST);

        JTabbedPane pane = new JTabbedPane();
        pane.addTab("Main", panel1);
        pane.addTab("Parameters", new ParametersPanel());
        add(pane);
        setVisible(true);
    }

    public static void main(String[] args) {

    }
}
