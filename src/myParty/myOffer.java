package myParty;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.NoModel;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.boaframework.SortedOutcomeSpace;

public class myOffer extends OfferingStrategy {


    public myOffer() {
    }

    public void init(NegotiationSession negoSession, OpponentModel model, OMStrategy oms, Map<String, Double> parameters) throws Exception {
        this.negotiationSession = negoSession;
    }

    public BidDetails determineOpeningBid() {
        return negotiationSession.getOutcomeSpace().getBidNearUtility(1);
    }

    public BidDetails determineNextBid() {
        return negotiationSession.getOutcomeSpace().getBidNearUtility(1);
    }

    public String getName() {
        return "myOffer";
    }
}
