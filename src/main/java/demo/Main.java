package demo;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import demo.aux.*;
import java.util.*;
import java.util.stream.Stream;

public class Main {
	
	/**
	 * Main variables to be varied
	 *
	 */
	
	public static int N = 10;
	public static int f = 3;
	public static int Tle = 500;
	public static double alpha = 0.1;
	public static boolean debug_mode = true;

	
	public static void main(String[] args) throws InterruptedException {
		
		// Instantiate an actor system
		final ActorSystem system = ActorSystem.create("system");
		system.log().info("System started with N=" + N );
		
		// Instantiate processes
		// Please note that our processes are named from 0 to N-1 for convenience, NOT 1 TO N
		ArrayList<ActorRef> references = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			final ActorRef a = system.actorOf(Process.createActor(i , N, alpha, debug_mode), "" + i); // bug source
			references.add(a);
		}
		
		//give each process a view of all the other processes
		Members m = new Members(references);
		for (ActorRef actor : references) {
			actor.tell(m, ActorRef.noSender());
		}
	//	Thread.sleep(2000);		

		// launch message to the processes
		for (int i = 0; i < N; i++) {
            references.get(i).tell(new Launch(), ActorRef.noSender());
		}
		
		// shuffle processes and make the first f crash
        Collections.shuffle(references);
        for (int i = 0; i < f; i++) {
            references.get(i).tell(new Crash(), ActorRef.noSender());
        }

        
        
        // sleep for specified duration -- methode hethi mnayka nbadlouha mba3d ken 3ana wa9t
		Thread.sleep(Tle);		
		
		// elect a leader, we choose the last one as he is randomly choose ( shuffled list ) and he could not have crashed
        for (int i = 0; i < (N-1); i++) {
            references.get(i).tell(new Hold(), ActorRef.noSender());
        }
        

        
        
        // code zeyed mayosle7 fi chay
//		OfconsProposerMsg opm = new OfconsProposerMsg(100);
//		references.get(0).tell(opm, ActorRef.noSender());
	}
}