package demo;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.*;

public class Process extends UntypedAbstractActor {
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);// Logger attached to actor
	private final int N;//number of processes
	private final int id;//id of current process
	private Members processes;//other processes' references

	// main arguments
	private int ballot;
	private Integer proposal;
	private Integer readBallot;
	private Integer imposeBallot;
	private Integer estimate;
	private int[][] states;

	// other arguments
	private double alpha;
	private boolean faultProne;
	private boolean crashed;

	// Initialise process
	public Process(int ID, int nb, double al) {
		N = nb;
		id = ID;
		ballot = id-N;
		proposal = null;
		readBallot = 0;
		imposeBallot = 0;
		estimate = null;
		states = new int[N][2];

		alpha = al;
		faultProne = false;
		crashed = false;
	}

	public String toString() {
		return "Process{" + "id=" + id ;
	}


	// Static function creating actor
	public static Props createActor(int ID, int nb, double al) {
		return Props.create(Process.class, () -> {
			return new Process(ID, nb,al);
		});
	}


	private void ofconsProposeReceived(Integer v) {

		proposal = v;
		for (ActorRef actor : processes.references) {
			actor.tell(new ReadMsg(ballot), this.getSelf());
			log.info("Read ballot " + ballot + " msg: p" + self().path().name() + " -> p" + actor.path().name());
		}
	}

	private void readReceived(int newBallot, ActorRef pj) {
		log.info("read received " + self().path().name() );
	}


	public void onReceive(Object message) throws Throwable {
		if (faultProne && !crashed) {
			if ( Math.random() < alpha ) {
				crashed = true;
			}
		}
		if( !crashed ) {
			if (message instanceof Members) {//save the system's info
				Members m = (Members) message;
				processes = m;
				log.info("p" + self().path().name() + " received processes info");
			}
			else if (message instanceof OfconsProposerMsg) {
				OfconsProposerMsg m = (OfconsProposerMsg) message;
				this.ofconsProposeReceived(m.v);

			}
			else if (message instanceof ReadMsg) {
				ReadMsg m = (ReadMsg) message;
				this.readReceived(m.ballot, getSender());
			}

		}
	}
}
