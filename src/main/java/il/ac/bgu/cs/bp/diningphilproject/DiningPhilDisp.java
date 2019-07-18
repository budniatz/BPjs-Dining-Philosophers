package il.ac.bgu.cs.bp.diningphilproject;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsTraversalNode;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspections;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTrace;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PausingEventSelectionStrategyDecorator;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * Class that implements the Graphical User Interface
 */

// implements Runnable
public class DiningPhilDisp implements Runnable {
    final Display dis = new Display();
    // values of sticks:-1-down-right 0-down-left, 1-right, 2-left. 5 sticks initial
    // value is down.
    volatile public static int[] sticks = { 0, 0, 0, 0, 0 };
    // false - not eating , true - eating. makes sense.
    volatile public static boolean[] philEats = { false, false, false, false, false };
    volatile public static boolean[] philEatshalf = { false, false, false, false, false };
    volatile public static boolean[] philthinks = { false, false, false, false, false };

    public static JFrame window;
    int changeSoultionValue = 0;

    private JList logList;
    private final DefaultListModel<String> logModel = new DefaultListModel<>();

    private JButton runBtn, stopBtn, verifyBtn, changeSolution, reset;
    private boolean isRunning = false;
    private BProgramRunner bprogramRunner = null;

    public DiningPhilDisp() {
        prepareGUI();
    }

    private void prepareGUI() {
        window = new JFrame("Dining philosophers problem");
        window.setSize(1000, 1000);
        window.setLayout(new GridLayout(3, 1));
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        addElements();
        showSoultionType();
        window.setVisible(true);
    }

    void setInProgress(boolean inProgress) {
        SwingUtilities.invokeLater(() -> {
            isRunning = inProgress;
            stopBtn.setEnabled(inProgress);
            runBtn.setEnabled(!inProgress);
            verifyBtn.setEnabled(!inProgress);
            reset.setEnabled(!inProgress);
            changeSolution.setEnabled(!inProgress);
        });
    }

    private void addElements() {

        window.setContentPane(dis);
        // buttons
        Box buttons = Box.createHorizontalBox();
        buttons.add(Box.createGlue());
        runBtn = new JButton("Run");
        verifyBtn = new JButton("Verify");
        stopBtn = new JButton("Stop");
        changeSolution = new JButton("Change Soultion");
        reset = new JButton("reset");
        stopBtn.setEnabled(false);
        buttons.add(changeSolution);
        buttons.add(verifyBtn);
        buttons.add(reset);
        buttons.add(runBtn);
        buttons.add(stopBtn);
        window.add(buttons);

        runBtn.addActionListener(e -> runBprogram());
        stopBtn.addActionListener(c -> {
            if (bprogramRunner != null) {
                bprogramRunner.halt();
            }
            setInProgress(false);
        });
        changeSolution.addActionListener(l -> changeSoultionfun());
        reset.addActionListener(d -> resetpositions());
        verifyBtn.addActionListener(a -> verifyPhilosophers());
        logList = new JList(logModel);
        logList.setFixedCellWidth(850);
        window.add(new JScrollPane(logList), BorderLayout.CENTER);
        window.setVisible(true);

    }

    private void resetpositions() {
        for (int i = 0; i < 5; i++) {
            sticks[i] = 0;
        }

    }

    private void changeSoultionfun() {
        changeSoultionValue = 1 - changeSoultionValue;
        showSoultionType();

    }

    void showSoultionType() {
        logModel.clear();
        if (changeSoultionValue == 1) {
            addToLog("Solution that causing Deadlock");
        } else {
            addToLog("Solution that works");
        }
    }

    private void runBprogram() {

        setInProgress(true);
        reset.setEnabled(false);
        changeSolution.setEnabled(false);
        // Setup the b-program from the source
        BProgram bprog = new ResourceBProgram("diningPhil.js");
        bprogramRunner = new BProgramRunner(bprog);
        bprog.putInGlobalScope("pickProgram", changeSoultionValue);
        bprogramRunner.addListener(new BProgramRunnerListenerImpl(this));
        // set pausing ESS
        PausingEventSelectionStrategyDecorator pausingESS = new PausingEventSelectionStrategyDecorator(
                new SimpleEventSelectionStrategy());
        bprog.setEventSelectionStrategy(pausingESS);

        pausingESS.setListener(pess -> {
            try {
                Thread.sleep(1100);
                pess.unpause();
            } catch (InterruptedException ex) {
                System.err.println("Interrupted during event pause");
            }
        });

        // cleanup
        logModel.clear();

        // go!
        new Thread(() -> {
            try {
                bprogramRunner.run();
            } catch (Exception e) {
                e.printStackTrace(System.out);
                addToLog(e.getMessage());
                setInProgress(false);
            }
        }).start();
    }

    private void verifyPhilosophers() {
        setInProgress(true);
        stopBtn.setEnabled(false);
        BProgram bprog = new ResourceBProgram("diningPhil.js");
        bprog.putInGlobalScope("pickProgram", changeSoultionValue);

        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        vfr.setMaxTraceLength(8);
        vfr.addInspection(ExecutionTraceInspections.DEADLOCKS);

        vfr.setProgressListener(new DfsBProgramVerifier.ProgressListener() {
            @Override
            public void started(DfsBProgramVerifier v) {
                showSoultionType();
                addToLog("Verification started");
            }

            @Override
            public void iterationCount(long count, long statesHit, DfsBProgramVerifier v) {
                addToLog(" ~ " + count + " iterations, " + statesHit + " states visited.");
            }

            @Override
            public void maxTraceLengthHit(List<DfsTraversalNode> trace, DfsBProgramVerifier v) {
                return;

            }

            @Override
            public void done(DfsBProgramVerifier v) {
                addToLog("Verification done");
                setInProgress(false);
            }

            @Override
            public boolean violationFound(Violation aViolation, DfsBProgramVerifier vfr) {

                return false; // do not continue searching,
            }
        });

        logModel.clear();
        // go!
        new Thread(() -> {
            try {
                VerificationResult res = vfr.verify(bprog);
                addToLog(String.format("Scanned %,d states\n", res.getScannedStatesCount()));
                addToLog(String.format("Time: %,d milliseconds\n", res.getTimeMillies()));
                if (!res.isViolationFound()) {
                    addToLog("No counterexample found for dinning philosophers.");
                } else {
                    SwingUtilities.invokeLater(() -> setVerificationResult(res));
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
                addToLog(e.getMessage());
                setInProgress(false);
            }
        }).start();

    }

    private void setVerificationResult(VerificationResult res) {

        addToLog("Found a counterexample");
        final ExecutionTrace trace = res.getViolation().get().getCounterExampleTrace();
        trace.getNodes().forEach(nd -> addToLog(" " + nd.getEvent()));

        BProgramSyncSnapshot last = trace.getLastState();
        addToLog("selectableEvents: " + trace.getBProgram().getEventSelectionStrategy().selectableEvents(last));
        last.getBThreadSnapshots().stream().sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).forEach(s -> {
            addToLog(s.getName());
            addToLog(" " + s.getSyncStatement());
            addToLog("\n");
        });

    }

    void addToLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (logModel.size() > 30 * 1024) {
                logModel.clear();
            }
            logModel.addElement(msg);
        });
    }

    // update the fields => we want to know how to paint according to those fields
    void updateFlagsSticks(int numStick, int pick) {
        sticks[numStick] = pick;

    }

    void updateFlagsThink(int numPhil, boolean eat) {

        philthinks[numPhil] = true;
    }

    void updateFlagsPhilEats(int numPhil, boolean eat) {

        philEats[numPhil] = true;
        philEatshalf[numPhil] = true;
    }

    @Override
    public void run() {
        while (true) {
            dis.repaint();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        DiningPhilDisp mw = new DiningPhilDisp();
        mw.run();

    }

    static class Display extends JPanel {

        // arrays of arrays 5 positions - 5 arrays that contain:
        // [pointx,pointy,sizex,sizey]
        private int[][] allpositionsNsize = { { 245, 331, 80, 80 }, { 396, 277, 80, 80 }, { 495, 400, 80, 80 },
                { 425, 535, 80, 80 }, { 245, 495, 80, 80 } };

        private int[][] allpositionSticks = { { 240, 430, 46, 46 }, { 330, 304, 35, 35 }, { 480, 340, 42, 42 },
                { 485, 495, 45, 45 }, { 345, 556, 48, 48 } };
        private int[] alldegreesSticks = { 290, 0, 100, 160, 230 };

        private int[][] allposStickspicksi = { { 255, 390, 50, 50 }, { 368, 280, 47, 47 }, { 510, 370, 50, 50 },
                { 485, 535, 47, 47 }, { 300, 540, 47, 47 } };
        private int[] alldegreesStickspicksi = { 310, 40, 135, 200, 260 };

        private int[][] allposStickspicksj = { { 235, 470, 42, 42 }, { 270, 300, 50, 50 }, { 465, 297, 47, 47 },
                { 500, 470, 53, 53 }, { 390, 570, 47, 47 } };
        private int[] alldegreesStickspicksj = { 260, 310, 40, 135, 200 };

        // the positions of the thinking bubles, the last cell of every array containes
        // if the buble tilt to
        // the left(1) or the right(0)
        private int[][] allposBubles = { {175, 310, 75, 75, 1 }, {445, 190, 75, 75, 0 }, { 585,375, 75, 75, 0 },
                {510, 565, 75, 75, 0 }, { 170, 485, 75, 75, 1 } };

        private BufferedImage table;
        private BufferedImage plate;
        private BufferedImage fork;
        private BufferedImage forkRoated;
        private BufferedImage halfullplate;
        private BufferedImage emptyplate;
        private BufferedImage thinkR;
        private BufferedImage thinkL;
        private int [] thinkflag = {0,0,0,0,0};

        public BufferedImage rotateImageByDegrees(BufferedImage img, double angle) {

            double rads = Math.toRadians(angle);
            double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
            int w = img.getWidth();
            int h = img.getHeight();
            int newWidth = (int) Math.floor(w * cos + h * sin);
            int newHeight = (int) Math.floor(h * cos + w * sin);

            BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = rotated.createGraphics();
            AffineTransform at = new AffineTransform();
            at.translate((newWidth - w) / 2, (newHeight - h) / 2);

            int x = w / 2;
            int y = h / 2;

            at.rotate(rads, x, y);
            g2d.setTransform(at);
            g2d.drawImage(img, 0, 0, this);
            g2d.dispose();

            return rotated;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            try {

                InputStream isInput = new FileInputStream("images/table1.png");
                table = ImageIO.read(isInput);
                isInput = new FileInputStream("images/platefull.png");
                plate = ImageIO.read(isInput);
                isInput = new FileInputStream("images/half.png");
                halfullplate = ImageIO.read(isInput);
                isInput = new FileInputStream("images/empty.png");
                emptyplate = ImageIO.read(isInput);
                isInput = new FileInputStream("images/fork1.png");
                fork = ImageIO.read(isInput);
                isInput = new FileInputStream("images/thinkR.png");
                thinkR = ImageIO.read(isInput);
                isInput = new FileInputStream("images/thinkL1.png");
                thinkL = ImageIO.read(isInput);

            } catch (IOException ex) {
                System.out.println("problem in imageio\n");
                System.exit(0);
            }
            g2.drawImage(table, 200, 250, 400, 400, null);

            for (int i = 0; i < 5; i++) {
                int value = sticks[i];
                switch (value) {
                case 0:
                    forkRoated = rotateImageByDegrees(fork, alldegreesSticks[i]);
                    g2.drawImage(forkRoated, allpositionSticks[i][0], allpositionSticks[i][1], allpositionSticks[i][2],
                            allpositionSticks[i][3], null);
                    window.setVisible(true);
                    break;
                case 1:
                    forkRoated = rotateImageByDegrees(fork, alldegreesStickspicksi[i]);
                    g2.drawImage(forkRoated, allposStickspicksi[i][0], allposStickspicksi[i][1],
                            allposStickspicksi[i][2], allposStickspicksi[i][3], null);
                    window.setVisible(true);
                    break;

                case 2:
                  
                    forkRoated = rotateImageByDegrees(fork, alldegreesStickspicksj[i]);
                    g2.drawImage(forkRoated, allposStickspicksj[i][0], allposStickspicksj[i][1],
                            allposStickspicksj[i][2], allposStickspicksj[i][3], null);
                    window.setVisible(true);
                    break;

                default: // not supposed to be here
                    System.err.println("updated wrong value");
                    break;

                }
                if(thinkflag[i] == 1){
                    philEats[i]=false;
                    thinkflag[i] = 0;
                }
                if (philthinks[i]) {
                    if (allposBubles[i][4] == 0) {
                        g2.drawImage(thinkR, allposBubles[i][0], allposBubles[i][1], allposBubles[i][2],
                                allposBubles[i][3], null);
                    } else {
                        g2.drawImage(thinkL, allposBubles[i][0], allposBubles[i][1], allposBubles[i][2],
                                allposBubles[i][3], null);

                    }
                    thinkflag[i] = 1;
                    philthinks[i]=false;
                    

                }

                if (!philEatshalf[i] && philEats[i]) {
                    g2.drawImage(emptyplate, allpositionsNsize[i][0], allpositionsNsize[i][1], allpositionsNsize[i][2],
                            allpositionsNsize[i][3], null);
                    window.setVisible(true);
                    continue;
                } else if (philEatshalf[i]) {

                    philEatshalf[i] = false;
                    g2.drawImage(halfullplate, allpositionsNsize[i][0], allpositionsNsize[i][1],
                            allpositionsNsize[i][2], allpositionsNsize[i][3], null);
                    window.setVisible(true);

                } else if (!philEatshalf[i] && !philEats[i]) {
                    g2.drawImage(plate, allpositionsNsize[i][0], allpositionsNsize[i][1], allpositionsNsize[i][2],
                            allpositionsNsize[i][3], null);

                }
               
            }
        }

    }
}
