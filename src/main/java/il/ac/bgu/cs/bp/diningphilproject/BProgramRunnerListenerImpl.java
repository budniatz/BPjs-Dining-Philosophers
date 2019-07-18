package il.ac.bgu.cs.bp.diningphilproject;

import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;
import javax.swing.SwingUtilities;

/**
 * This listener is used to update the UI when the BProgram is running.
 * 
 * 
 */
class BProgramRunnerListenerImpl implements BProgramRunnerListener {
    
    private final DiningPhilDisp mw;

    public BProgramRunnerListenerImpl(final DiningPhilDisp mw) {
        this.mw = mw;
    }

    @Override
    public void starting(BProgram bprog) {
        mw.showSoultionType();
        mw.addToLog("Starting...");
    }

    @Override
    public void started(BProgram bp) {
        mw.addToLog("Started");
    }

    @Override
    public void superstepDone(BProgram bp) {
       // mw.addToLog("Superstep done - no more internal events");
        mw.setInProgress(false); // generally not true, but here it is, since there's no environment.
    }

    @Override
    public void ended(BProgram bp) {
        mw.addToLog("Ended");
        mw.setInProgress(false);
    }

    @Override
    public void halted(BProgram bp) {
        mw.addToLog("Program Halted");
        mw.setInProgress(false);
    }
    
    @Override
    public void assertionFailed(BProgram bp, FailedAssertion theFailedAssertion) {
        mw.addToLog("Failed Assertion: " + theFailedAssertion.getMessage());
    }

    @Override
    public void bthreadAdded(BProgram bp, BThreadSyncSnapshot theBThread) {
        mw.addToLog(" + " + theBThread.getName() + " added");
    }

    @Override
    public void bthreadRemoved(BProgram bp, BThreadSyncSnapshot theBThread) {
        mw.addToLog(" - " + theBThread.getName() + " removed");
    }

    @Override
    public void bthreadDone(BProgram bp, BThreadSyncSnapshot theBThread) {
        mw.addToLog(" - " + theBThread.getName() + " completed");
    }

    @Override
    public void eventSelected(BProgram bp, BEvent theEvent) {
        SwingUtilities.invokeLater(() -> {
            String eventName = theEvent.getName();
            if (eventName.startsWith("Philosopher 1")){
                
                if(eventName.endsWith("Pick stick 1")){
                    mw.updateFlagsSticks(0,1);
                }
                if(eventName.endsWith("Pick stick 2")){
                    mw.updateFlagsSticks(1,2);
                }
                if(eventName.endsWith("Rel stick 1")){
                    mw.updateFlagsSticks(0,0);
                }
                if(eventName.endsWith("Rel stick 2")){
                    mw.updateFlagsSticks(1,0);
                }
                if(eventName.endsWith("Eating")){
                    mw.updateFlagsPhilEats(0, true);
                }
                if(eventName.endsWith("Thinking")){
                    mw.updateFlagsThink(0, true);
                }
            }
            if (eventName.startsWith("Philosopher 2")){
            
                if(eventName.endsWith("Pick stick 2")){
                    mw.updateFlagsSticks(1,1);
                }
                if(eventName.endsWith("Pick stick 3")){
                    mw.updateFlagsSticks(2,2);
                }
                if(eventName.endsWith("Rel stick 2")){
                    mw.updateFlagsSticks(1,0);
                }
                if(eventName.endsWith("Rel stick 3")){
                    
                    mw.updateFlagsSticks(2,0);
                }
                if(eventName.endsWith("Eating")){
                
                    mw.updateFlagsPhilEats(1, true);
                }
                if(eventName.endsWith("Thinking")){
                    mw.updateFlagsThink(1, true);
                }
            }
            if (eventName.startsWith("Philosopher 3")){
                if(eventName.endsWith("Pick stick 3")){
                    mw.updateFlagsSticks(2,1);
                }
                if(eventName.endsWith("Pick stick 4")){
                    mw.updateFlagsSticks(3,2);
                }
                if(eventName.endsWith("Rel stick 3")){
                    mw.updateFlagsSticks(2,0);
                }
                if(eventName.endsWith("Rel stick 4")){
                    
                    mw.updateFlagsSticks(3,0);
                }
                if(eventName.endsWith("Eating")){
                  
                    mw.updateFlagsPhilEats(2, true);
                }
                if(eventName.endsWith("Thinking")){
                    mw.updateFlagsThink(2, true);
                }
            }
            if (eventName.startsWith("Philosopher 4")){
                if(eventName.endsWith("Pick stick 4")){
                    mw.updateFlagsSticks(3,1);
                }
                if(eventName.endsWith("Pick stick 5")){
                    mw.updateFlagsSticks(4,2);
                }
                if(eventName.endsWith("Rel stick 4")){
                    mw.updateFlagsSticks(3,0);
                }
                if(eventName.endsWith("Rel stick 5")){
                    
                    mw.updateFlagsSticks(4,0);
                }
                if(eventName.endsWith("Eating")){
                    mw.updateFlagsPhilEats(3, true);
                }
                if(eventName.endsWith("Thinking")){
                    mw.updateFlagsThink(3, true);
                }
            }
            if (eventName.startsWith("Philosopher 5")){
                if(eventName.endsWith("Pick stick 5")){
                    mw.updateFlagsSticks(4,1);
                }
                if(eventName.endsWith("Pick stick 1")){
                    mw.updateFlagsSticks(0,2);
                }
                if(eventName.endsWith("Rel stick 5")){
                    mw.updateFlagsSticks(4,0);
                }
                if(eventName.endsWith("Rel stick 1")){
                    
                    mw.updateFlagsSticks(0,0);
                }
                if(eventName.endsWith("Eating")){
                    mw.updateFlagsPhilEats(4, true);
                }
                if(eventName.endsWith("Thinking")){
                    mw.updateFlagsThink(4, true);
                }
            }
            
            mw.addToLog("Event: " + theEvent.toString());
        });
    }
    
}
