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

    public void init(NegotiationSession negoSession, OfferingStrategy strat, OpponentModel opponentModel, Map<String, Double> parameters) throws Exception {

    }

    public Actions determineAcceptability() {
        return Actions.Accept;
    }

    public String getName() {
        return "myAccept";
    }
}
