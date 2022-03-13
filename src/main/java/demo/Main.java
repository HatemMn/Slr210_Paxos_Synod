package demo;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import java.util.*;
import java.util.stream.Stream;

public class Main {
	
	/**
	 * Main variables to be varied
	 *
	 */
	
	public static int N = 10;
	public static int f = 5;
	public static int Tle = 500;
	public static double alpha = 0.1;
	public static boolean debug_mode = true;

	
	public static void main(String[] args) throws InterruptedException {
		
		// Instantiate an actor system
		final ActorSystem system = ActorSystem.create("system");
		system.log().info("System started with N=" + N );
		
		// Instantiate processes		
		ArrayList<ActorRef> references = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			final ActorRef a = system.actorOf(Process.createActor(i + 1, N, alpha, debug_mode), "" + i);
			references.add(a);
		}
		
		//give each process a view of all the other processes
		Members m = new Members(references);
		for (ActorRef actor : references) {
			actor.tell(m, ActorRef.noSender());
		}
		
		Thread.sleep(Tle);
		
		// shuffle processes and make the first f crash
        Collections.shuffle(references);
        for (int i = 0; i < f; i++) {
            references.get(i).tell(new Crash(), ActorRef.noSender());
            System.out.println("p" + references.get(i).path().name() + " will be soon crashed.");
        }
        
        
		

//		OfconsProposerMsg opm = new OfconsProposerMsg(100);
//		references.get(0).tell(opm, ActorRef.noSender());
	}
}