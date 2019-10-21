import java.util.List;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.bidding.BidDetails;
import genius.core.issue.Issue;
import genius.core.issue.Value;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.timeline.TimeLineInfo;
import genius.core.utility.AbstractUtilitySpace;

/**
 * This is your negotiation party.
 */
public class Group11 extends AbstractNegotiationParty {

    private TimeLineInfo timelineInfo;
    private AbstractUtilitySpace utilitySpace;
    private Bid lastOfferedBid;
    private Bid lastReceivedBid;


    @Override
    public void init(NegotiationInfo info) {
        super.init(info);

        System.out.println("Discount Factor is " + getUtilitySpace().getDiscountFactor());
        System.out.println("Reservation Value is " + getUtilitySpace().getReservationValueUndiscounted());

        // Initialize variables
        this.lastOfferedBid = null;
        this.lastReceivedBid = null;
        this.timelineInfo = info.getTimeline();
        this.utilitySpace = info.getUtilitySpace();
    }

    @Override
    public Action chooseAction(List<Class<? extends Action>> validActions) {
        if (lastReceivedBid != null && validActions.contains(Accept.class) && determineAcceptability()) {
            return new Accept(getPartyId(), lastReceivedBid);
        } else {
            if (lastOfferedBid == null) {
                lastOfferedBid = determineOpeningBid();
                return new Offer(getPartyId(), lastOfferedBid);
            } else {
                lastOfferedBid = determineNextBid();
                return new Offer(getPartyId(), lastOfferedBid);
            }
        }
    }

    private boolean determineAcceptability() {
        double utilAccept = 0.95;
        double base = 100;

        double time = this.getTimeLine().getTime();
        BidDetails lastBid = new BidDetails(lastReceivedBid, time);
        double opponentBid = lastBid.getMyUndiscountedUtil();

        if(opponentBid > utilAccept * (Math.pow(-base, time) + base) / base) {
            return true;
        }
        return false;
    }

    private Bid determineNextBid() {
        Bid new_bid = lastOfferedBid;

        for(int i = 0; i < lastOfferedBid.getIssues().size(); i++) {
            Issue current_issue = lastOfferedBid.getIssues().get(i);
            Value own_value = lastOfferedBid.getValue(current_issue);
            Value opp_value = lastReceivedBid.getValue(current_issue);
            if(!own_value.equals(opp_value)) {
                new_bid = new_bid.putValue(current_issue.getNumber(), opp_value);
                break;
            }
        }
        return new_bid;
    }

    private Bid determineOpeningBid() {
        try {
            return this.utilitySpace.getMaxUtilityBid();
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
        return null;
    }

    @Override
    public void receiveMessage(AgentID sender, Action action) {
        super.receiveMessage(sender, action);
        if (action instanceof Offer) {
            lastReceivedBid = ((Offer) action).getBid();
        }
    }

    @Override
    public String getDescription() {
        return "example party group N";
    }

}
