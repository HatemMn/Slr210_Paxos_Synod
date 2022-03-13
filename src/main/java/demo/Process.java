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
	private boolean debug;

	// Initialise process
	public Process(int ID, int nb, double al, boolean deb) {
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
		debug = deb;
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
				if(debug) {log.info("p" + self().path().name() + " has crashed.");};
			}
		}
		// if the process is working normally
		if( !crashed ) {
			//save the system's info
			if (message instanceof Members) {
				Members m = (Members) message;
				processes = m;
				log.info("p" + self().path().name() + " received processes info");
			}
			
			
			
			
			
			// other
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
