package demo;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import java.util.*;
import java.util.stream.Stream;

public class Main {
	
	// Main variables to vary
	public static int N = 10;
	public static int f = 1;
	public static int Tle = 500;
	public static double alpha = 0.1;
	
	
	public static void main(String[] args) throws InterruptedException {
		
		// Instantiate an actor system
		final ActorSystem system = ActorSystem.create("system");
		system.log().info("System started with N=" + N );
		
		// Instantiate processes		
		ArrayList<ActorRef> references = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			final ActorRef a = system.actorOf(Process.createActor(i + 1, N, alpha), "" + i);
			references.add(a);
		}
		
		//give each process a view of all the other processes
		Members m = new Members(references);
		for (ActorRef actor : references) {
			actor.tell(m, ActorRef.noSender());
		}
		
		// shuffle processes and make the first f crash
        Collections.shuffle(references);
        for (int i = 0; i < f; i++) {
            references.get(i).tell(new Crash(), ActorRef.noSender());
        }
        
        
		

		OfconsProposerMsg opm = new OfconsProposerMsg(100);
		references.get(0).tell(opm, ActorRef.noSender());
	}
}