/**
* @file Process.java
* @author Hatem Mnaouer Ahmed Bouali Salma Ezzina
* @version 1.0
*
* @section LICENSE
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License as
* published by the Free Software Foundation; either version 2 of
* the License, or (at your option) any later version.
*
* @section DESCRIPTION
*
* This is the class that emulates the processes.
*/

/**
* @brief The process class
*
* This class implements the various methods they need
*/

package demo;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.aux.*;
import java.time.Duration;
import java.util.*;

public class Process extends UntypedAbstractActor {
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);// Logger attached to actor
	private final int N;//number of processes
	private final int id;//id of current process
	private Members processes;//other processes' references
	
	// OFcons algorithm arguments
	private int ballot;
	private Integer proposal;
	private Integer readBallot;
	private Integer imposeBallot;
	private Integer estimate;
	private int[][] states;
	private int gatherNumber;
	private int ackNumber;

	// process state arguments
	private boolean faultProne;
	private boolean crashed;
	private boolean debug;
	private boolean is_proposing;
	private boolean is_halted;
	
	// other arguments
	private double alpha;
	private int ReProposeTime = 200;

	// Initialise process
	public void init_states() {
		ballot = id-N;
		proposal = null;
		readBallot = 0;
		imposeBallot = id-N;
		estimate = -1;
		states = new int[N][2];
		gatherNumber = 0;
		ackNumber = 0;
	}
	
	public Process(int ID, int nb, double al, boolean deb) {
		N = nb;
		id = ID;
		init_states();
		
		alpha = al;
		faultProne = false;
		crashed = false;
		debug = deb;
		is_halted = false;
	}

	
	public String toString() {
		return "Process{" + "id=" + id ;
	}


	// Static function creating actor
	public static Props createActor(int ID, int nb, double al, boolean deb) {
		return Props.create(Process.class, () -> {
			return new Process(ID, nb,al, deb);
		});
	}


/*	// 3asb o5ra manich fehemha chtaslah
	private void ofconsProposeReceived(Integer v) {

		proposal = v;
		for (ActorRef actor : processes.references) {
			actor.tell(new ReadMsg(ballot), this.getSelf());
			log.info("Read ballot " + ballot + " msg: p" + self().path().name() + " -> p" + actor.path().name());
		}
	}
*/
	
	// chnowa l3asb hethi
	private void readReceived(int newBallot, ActorRef pj) {
		log.info("read received " + self().path().name() );
	}

	
	/**
	 * @brief propose method
	 * 
	 */
	public void ofConsPropose(int v) {
		is_proposing = true;
		proposal = v;
		ballot += N;
		for (ActorRef actor : processes.references) {
			actor.tell(new Read(ballot, id), this.getSelf());
			if( debug ) { log.info("Read ballot " + ballot + " msg: p" + self().path().name() + " -> p" + actor.path().name()); }
		}
		return;
	}
	
	/*
	 * @param b_received is ballot'
	 * 
	 */
	
	public void ofConsReceiveRead(int b_received, int IDj) {
		ActorRef sender = processes.references.get(IDj);

		
		if( readBallot > b_received || imposeBallot > b_received ) {
			sender.tell(new Abort(b_received), getSender());
			if( debug ) { log.info("Abort ballot " + b_received + " : p" + self().path().name() + " -> p" + sender.path().name()); }
		}
		else {
			readBallot = b_received;
			sender.tell(new Gather(b_received, imposeBallot, estimate,id), this.getSelf());
		}
		
	}
	
	public void ofConsGather(int b_received, int estBallot, int est, int IDj) {
		ActorRef sender = processes.references.get(IDj);
		states[IDj][0] = est;
		states[IDj][1] = estBallot;
		gatherNumber++;
		if( gatherNumber > N/2) { // majority
			// check if exists some estBallot > 0
			int maxBal = -1, maxEst = -1;
			for(int i=0; i<N;i++ ) {
				if( states[i][1] > 0 && states[i][1] > maxBal) {
					maxBal = states[i][1];
					maxEst = states[i][0];
				}
			}
			if(maxBal > 0) {
				proposal = maxEst;
			}
			states = new int[N][2];
			for (ActorRef actor : processes.references) {
				actor.tell(new Impose(ballot, proposal,id), this.getSelf());
				if( debug ) { log.info("Someone is imposing : p" + self().path().name() + " -> p" + actor.path().name()); }
			}
		}
	}
	
	public void ofConsImposeReceive(int b_received, int v, int IDj) {
		ActorRef sender = processes.references.get(IDj);
		
		if( readBallot >  b_received || imposeBallot > b_received ) {
			sender.tell(new Abort( b_received ), sender);
		} else {
			estimate = v;
			imposeBallot = b_received;
			sender.tell(new ACK(b_received, id), sender);
		}
	}
	
	/**
	 * The method that handles the received messages
	 *
	 *	 
	 *
	 */
	public void onReceive(Object message) throws Throwable {
		// if process is crashing
		if (faultProne && !crashed) {
			if ( Math.random() < alpha ) {
				crashed = true;
				if(debug) {log.info("p" + self().path().name() + " will enter silent mode.");};
			}
		}
		// if the process is working normally
		if( !crashed ) {
			//save the system's info
			if (message instanceof Members) {
				Members m = (Members) message;
				processes = m;
				for (ActorRef actor : m.references) {
					if( debug ) { log.info("hola ya " + actor.path().name()); }
				}
				if(debug) {log.info("p" + self().path().name() + " received processes info");};
			}
			// making process fault prone
			else if (message instanceof Crash) {
				faultProne = true;
				if(debug) {log.info("p" + self().path().name() + " is now fault prone.");};
			}
			
			// launch the process if it is idle
			else if (message instanceof Launch) {
				if( !is_proposing ) {
					// launch it
					this.ofConsPropose(Math.random() < 0.5 ? 0 : 1);
					if(debug) {log.info("p" + self().path().name() + " will now launch.");};
				}
				// try to keep relaunching
				if( !is_halted ) {
			        getContext().system().scheduler().scheduleOnce(Duration.ofMillis(ReProposeTime), getSelf(), new Launch(), getContext().system().dispatcher(), ActorRef.noSender());
				}
			}
			// put process to hold so he does not invoque anymore
			else if (message instanceof Hold) {
				is_halted = true;
				if(debug) {log.info("p" + self().path().name() + " is now at hold.");};
			}
			
			else if (message instanceof Read) {
				Read r = (Read) message;
				int ballot_red = r.getBallot();
				this.ofConsReceiveRead(r.getBallot(), r.getId());
				if(debug) {log.info("p" + self().path().name() + " is now reading a ballot he recived");};
			}
			

			else if (message instanceof Abort) {
				is_proposing = false;
				init_states();
				if(debug) {log.info("p" + self().path().name() + " has aborted his propose operation");};
			}
			
			else if (message instanceof Gather) {
				Gather g = (Gather) message;
				this.ofConsGather(g.getBallotp(),g.getImpose(),g.getEstimate(),g.getId());
			}
			
			else if ( message instanceof Impose ) {
				Impose im = (Impose) message;
				this.ofConsImposeReceive(im.getBallot(), im.getProposal(), im.getId());
			}
			
			else if ( message instanceof ACK ) {
				ACK akk = (ACK) message;
				this.ackNumber++;
				if( ackNumber > N/2 ) {
					if( debug ) { log.info("This process will send DECIDE and the proposed value to all processes  " + self().path().name()); }
					for (ActorRef actor : processes.references) {
						
					}
				}
			}
			
			// other
			/*
			else if (message instanceof OfconsProposerMsg) {
				OfconsProposerMsg m = (OfconsProposerMsg) message;
				this.ofconsProposeReceived(m.v);

			}
			else if (message instanceof ReadMsg) {
				ReadMsg m = (ReadMsg) message;
				this.readReceived(m.ballot, getSender());
			}
			 */
		}
	}
}
