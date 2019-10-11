package myParty;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.NoModel;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.boaframework.SortedOutcomeSpace;
import genius.core.issue.Issue;
import genius.core.issue.Value;

public class myOffer extends OfferingStrategy {
    public myOffer() {
    }

    public void init(NegotiationSession var1, OpponentModel var2, OMStrategy var3, Map<String, Double> var4) throws Exception {
        this.initializeAgent(var1, var2);
    }

    private void initializeAgent(NegotiationSession var1, OpponentModel var2) {
        this.negotiationSession = var1;
        SortedOutcomeSpace var3 = new SortedOutcomeSpace(this.negotiationSession.getUtilitySpace());
        this.negotiationSession.setOutcomeSpace(var3);
        this.opponentModel = var2;
    }

    public BidDetails determineNextBid() {
        Bid own_last_bid = this.negotiationSession.getOwnBidHistory().getLastBid();
        Bid opp_last_bid = this.negotiationSession.getOpponentBidHistory().getLastBid();
        double current_time = this.negotiationSession.getTime();

        Bid new_bid = negotiationSession.getOutcomeSpace().getBidNearUtility(0.9).getBid();

        for(int i = 0; i < own_last_bid.getIssues().size(); i++) {
            Issue current_issue = own_last_bid.getIssues().get(i);
            Value own_value = own_last_bid.getValue(current_issue);
            Value opp_value = opp_last_bid.getValue(current_issue);
            if(!own_value.equals(opp_value)) {
                new_bid = new_bid.putValue(current_issue.getNumber(), opp_value);
                break;
            }
        }
        System.out.println("last bid: " + own_last_bid.toString());
        System.out.println("new bid: " + new_bid.toString());

        return new BidDetails(new_bid, current_time);
    }

    public BidDetails determineOpeningBid() {
        return negotiationSession.getOutcomeSpace().getMaxBidPossible();
    }

    public String getName() {
        return "myOffer";
    }
}
