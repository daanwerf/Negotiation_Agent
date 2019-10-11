import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import genius.core.boaframework.AcceptanceStrategy;
import genius.core.boaframework.Actions;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;

public class myAccept extends AcceptanceStrategy {

    public myAccept() {
    }

    public void init(NegotiationSession negoSession, OfferingStrategy strat, OpponentModel opponentModel, Map<String, Double> parameters) {
        this.negotiationSession = negoSession;
        this.offeringStrategy = strat;
    }

    public Actions determineAcceptability() {

        double utilAccept = 0.9;

        double time = negotiationSession.getTime();
        double opponentBid = negotiationSession.getOpponentBidHistory().getLastBidDetails().getMyUndiscountedUtil();

        if (opponentBid > utilAccept * (Math.pow(-2, time) + 2)){
            return Actions.Accept;
        }
        return Actions.Reject;

    }

    public String getName() {
        return "myAccept";
    }
}
