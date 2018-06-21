import arduino.Arduino;
import com.fazecast.jSerialComm.*;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ArduinoProcessor implements Runnable{
    private static final int MODE_INPUT = 0;
    private static final int MODE_OUTPUT = 1;
    private static final int MODE_UNDEFINED = 2;

    private int mode = MODE_UNDEFINED;

    private Arduino arduino;
    private SerialPort port;

    private Runnable onStartAction;
    private Runnable onFinishAction;

    private CommandList commands;
    private GraphPointHolder points;

    MyScanner scanner;
    PrintWriter writer ;

    void setMode(int mode) {
        if(mode == MODE_INPUT) {
            port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            //scanner = new Scanner(port.getInputStream());

        } else if(mode == MODE_OUTPUT){
            port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        }
    }


    ArduinoProcessor(Arduino arduino, Runnable onStartAction, Runnable onFinishAction,
                     CommandList commands, GraphPointHolder points){
        this.arduino = arduino;
        this.onStartAction = onStartAction;
        this.onFinishAction = onFinishAction;
        this.commands = commands;
        this.points = points;
        this.port = arduino.getSerialPort();

        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    void getPoints(String desc) {
        String data = scanner.nextWord();
        if(!data.equals("Got")) {
            System.err.println("Wrong input: got \"" + data + "\", expected \"Got\"");
        }
        int num = scanner.nextInt();

        ArrayList<GraphPoint> points1 = new ArrayList<>(num);
        for(int i = 0; i < num; i++) {
            double x = scanner.nextDouble()/100;
            double y = scanner.nextDouble()/100;

            GraphPoint p = new GraphPoint(x,y,0,0, Color.BLUE);
            points1.add(p);
        }

        points.addPoints(points1, desc);
    }

    @Override
    public void run() {

        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scanner = new MyScanner(port.getInputStream());
        writer = new PrintWriter(port.getOutputStream());

        setMode(MODE_INPUT);
        String startInput = scanner.nextWord();
        if(startInput.equals("Available")) {
            System.out.println("Connected successfully");
        } else {
            System.err.println("Error connecting: wrong start input: " + startInput);
        }

        while(true) {
            if(commands.hasNext()) {
                String cmd = commands.next();
                if(cmd.equals("#finish"))
                    return;
                String[] commandParts = cmd.split("#");
                cmd = commandParts[0];
                String description= commandParts[1];


                SwingUtilities.invokeLater(onStartAction);
                setMode(MODE_OUTPUT);
                writer.println(cmd);
                writer.flush();
                setMode(MODE_INPUT);

                if(cmd.startsWith("Get"))
                    getPoints(description);

                String data = scanner.nextWord();
                if(data.equals("Available")) {
                    System.out.println("Command \"" + cmd + "\" applied successfully");
                } else {
                    System.err.println("Error by command " + cmd + " wrong input: " + data);
                }

                SwingUtilities.invokeLater(onFinishAction);
            } else {
                Thread.yield();
            }
        }
    }



}
