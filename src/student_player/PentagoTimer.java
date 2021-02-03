package student_player;

import java.util.Timer;
import java.util.TimerTask;

public class PentagoTimer {
    Timer timer;

    public boolean timeout;
    
    public PentagoTimer(int miliseconds) {
        timer = new Timer();
        timer.schedule(new PentagoTask(), miliseconds);
        timeout = false;
	}

    class PentagoTask extends TimerTask {
        public void run() {
        	timeout = true;	//set timeout to true;
        	timer.cancel(); //Terminate the timer thread
        }
    }
}