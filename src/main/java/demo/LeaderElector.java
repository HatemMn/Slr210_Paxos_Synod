package demo;

import java.time.Duration;
import java.util.*;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.*;
import demo.aux.*;

public class LeaderElector extends UntypedAbstractActor {

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private final int N;
	
	private ArrayList<ActorRef> references;
	
	public LeaderElector(Members mem, int N) {
		this.references = mem.references;
		this.N = N;
	}

	// Static function creating actor
	public static Props createActor(Members m, int n) {
		return Props.create(LeaderElector.class, () -> {
			return new LeaderElector(m, n);
		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		if(message instanceof Members){
			Members m = (Members) message;
			this.references = m.references;
		} else if (message instanceof Elect) {

			// elect a leader, we choose the last one as he is randomly choose ( shuffled list ) and he could not have crashed
	        for (int i = 0; i < (N-1); i++) {
	            references.get(i).tell(new Hold(), ActorRef.noSender());
	        }
	        
		}

	}
	
}
