package group11;

import java.util.*;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.bidding.BidDetails;
import genius.core.issue.*;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.timeline.TimeLineInfo;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;

import java.util.Map.Entry;


public class Group11 extends AbstractNegotiationParty {
    private TimeLineInfo timelineInfo;
    private AdditiveUtilitySpace utilitySpace;
    private AdditiveUtilitySpace opponentUtilitySpace;
    private Bid lastOfferedBid;
    private Bid lastReceivedBid;
    private Bid previousLastReceivedBid;
    private int issuesAmount;
    private double totalNegotiationTime;
    private double counter_offer_concede_factor;
    private double epsilon;
    private int weightLearnFactor;
    private int ownImportance;
    private int opponentImportance;

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);

        System.out.println("Discount Factor is " + getUtilitySpace().getDiscountFactor());
        System.out.println("Reservation Value is " + getUtilitySpace().getReservationValueUndiscounted());

        // Initialize variables
        this.lastOfferedBid = null;
        this.lastReceivedBid = null;
        this.previousLastReceivedBid = null;
        this.timelineInfo = info.getTimeline();
        this.utilitySpace = (AdditiveUtilitySpace) info.getUtilitySpace();
        this.opponentUtilitySpace = (AdditiveUtilitySpace) this.utilitySpace.copy();
        this.issuesAmount = this.opponentUtilitySpace.getDomain().getIssues().size();
        this.totalNegotiationTime = info.getTimeline().getTotalTime();
        // This is a Number between 0 and 1. A higher value will result in more concession towards the opponent
        this.counter_offer_concede_factor = 0.35;
        // The weight factor for our own utility
        this.ownImportance = 5;
        // The weight factor for the opponents utility
        this.opponentImportance = 3;
        // This is a Number between 0 and 1. This number is used to increase the learning speed used to model the opponent
        this.epsilon = 0.2;
        // This number is used to increase the weights of the opponent model of issues found in successive bids of the opponent
        this.weightLearnFactor = 1;
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

    /**
     * Determine if an offer is worth accepting
     * @return true if the offer is accepted, false otherwise
     */
    private boolean determineAcceptability() {
        double utilAccept = 0.95;
        double base = 100;

        double time = this.getTimeLine().getTime();
        BidDetails lastBid = new BidDetails(lastReceivedBid, time);
        double opponentBid = lastBid.getMyUndiscountedUtil();

        if (opponentBid > utilAccept * (Math.pow(-base, time) + base) / base) {
            return true;
        }
        return false;
    }

    /**
     * Determine the next bid. The next bid is based on our previous bid, the opponent model and the importance assigned
     * to every of our values for each issue.
     * @return The best possible offer, according to our agent
     */
    private Bid determineNextBid() {
        updateOpponentModel();

        IssueRanking ir = new IssueRanking(utilitySpace);
        int importance_upper_bound = (int) Math.round((utilitySpace.getDomain().getIssues().size() - 1) *
                counter_offer_concede_factor);
        System.out.println("Importance upper bound: " + importance_upper_bound);

        // TODO: make combinations between conceding a bit and opting for a random value. Also add points where we increase our own utility.
        ArrayList<Bid> bidList = new ArrayList<>();
        Bid current_bid_favoring_opponent = lastOfferedBid;
        Bid current_bid_with_randomness = lastOfferedBid;

        // Go over every issue and change the values that are different from the opponent.
        for (Issue issue : utilitySpace.getDomain().getIssues()) {
            int issueName = issue.getNumber();
            int issue_importance = ir.getIssueImportance(issueName);
            Value ownValue = lastOfferedBid.getValue(issueName);
            Value oppValue = lastReceivedBid.getValue(issueName);

            try {
                Value ranValue = getRandomValue(issue);

                // TODO: iets met getBidNearUtility
                if (!ownValue.equals(oppValue) && issue_importance <= importance_upper_bound) {
                    current_bid_favoring_opponent = current_bid_favoring_opponent.putValue(issueName, oppValue);
                    current_bid_with_randomness = current_bid_with_randomness.putValue(issueName, ranValue);
                    System.out.println("Conceded a bit on issue: " + issue.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        bidList.add(current_bid_favoring_opponent);
        bidList.add(current_bid_with_randomness);

        return selectBestBid(bidList);
    }

    /**
     * Select the best bid from the given list of bids
     * @param bidList list containing bids
     * @return most suitable bid from the list and the previous bid
     */
    private Bid selectBestBid(ArrayList<Bid> bidList) {
        double ownLastUtility = utilitySpace.getUtility(lastOfferedBid);
        double oppLastUtility = opponentUtilitySpace.getUtility(lastReceivedBid);
        double combinedUtility = (ownImportance * ownLastUtility + opponentImportance * oppLastUtility) / (ownImportance + opponentImportance);
        System.out.println("Last bid: " + lastOfferedBid);
        System.out.println("ownLastUtility: " + ownLastUtility + ", " + "oppLastUtility: " + oppLastUtility + ", " + "combinedUtility: " + combinedUtility);

        Bid bestBid = lastOfferedBid;
        double maxCombinedUtility = combinedUtility;

        for (Bid b : bidList) {
            double currentBidOwnUtility = utilitySpace.getUtility(b);
            double currentBidOppUtility = opponentUtilitySpace.getUtility(b);
            double currentCombinedUtility = (ownImportance * currentBidOwnUtility + opponentImportance * currentBidOppUtility) / (ownImportance + opponentImportance);
            System.out.println("Current bid: " + b);
            System.out.println("currentBidOwnUtility: " + currentBidOwnUtility + ", " + "currentBidOppUtility: " + currentBidOppUtility + ", " + "currentCombinedUtility: " + currentCombinedUtility);

            // TODO: make this dependent on more factors
            if (currentCombinedUtility >= maxCombinedUtility) {
                maxCombinedUtility = currentCombinedUtility;
                bestBid = b;
            }
        }
        System.out.println("Final bid: " + bestBid);
        System.out.println("-------------------------------------------------");
        return bestBid;
    }

    /**
     * Offer the bid with the highest possible utility to start the negotiation
     * @return bid with the highest possible utility
     */
    private Bid determineOpeningBid() {
        try {
            return this.utilitySpace.getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update both the weights and the values of the opponent model, based on the algorithm of [12] from the assignment
     */
    private void updateOpponentModel() {
        double increaseRate = epsilon / issuesAmount;

        if(previousLastReceivedBid == null) {
            initializeOpponentModel();
        } else {
            HashMap<Integer, Integer> opponentBidChanges = opponentBidChanges(lastReceivedBid, previousLastReceivedBid);

            int numberOfChanges = numberOfChanges(opponentBidChanges);

            double totalIncrease = 1.0 + increaseRate / numberOfChanges;
            double maximumWeight = 1.0 - (issuesAmount) * increaseRate / totalIncrease;

            // Update the weights
            for(Integer issueNumber : opponentBidChanges.keySet()) {
                Objective currentObjective = opponentUtilitySpace.getDomain().getObjectivesRoot().getObjective(issueNumber);
                double currentWeight = opponentUtilitySpace.getWeight(issueNumber);
                double newWeight;

                if (opponentBidChanges.get(issueNumber) == 0 && currentWeight < maximumWeight) {
                    newWeight = (currentWeight + increaseRate) / totalIncrease;
                } else {
                    newWeight = currentWeight / totalIncrease;
                }
                opponentUtilitySpace.setWeight(currentObjective, newWeight);
            }

            // Update the values
            try {
                for (Entry<Objective, Evaluator> entry: opponentUtilitySpace.getEvaluators()) {
                    EvaluatorDiscrete value = (EvaluatorDiscrete) entry.getValue();
                    IssueDiscrete issue = ((IssueDiscrete) entry.getKey());

                    ValueDiscrete issueValue = (ValueDiscrete) lastOfferedBid.getValue(issue.getNumber());
                    Integer notNormalizedEvaluation = value.getEvaluationNotNormalized(issueValue);
                    value.setEvaluation(issueValue, (weightLearnFactor + notNormalizedEvaluation));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Calculate the total number of changes between the opponents last 2 bids
     * @param opponentBidChanges Hashmap containing changes between the opponents last 2 bids
     * @return the number of changes
     */
    private int numberOfChanges(HashMap<Integer, Integer> opponentBidChanges) {
        int numberOfChanges = 0;
        for (Integer issueName : opponentBidChanges.keySet()) {
            if (opponentBidChanges.get(issueName) == 0) {
                numberOfChanges += 1;
            }
        }
        return numberOfChanges;
    }

    /**
     * Initialize the opponent model with hard set values, dependent on epsilon and the amount of issues
     */
    private void initializeOpponentModel() {
        double evenWeightValue = 1.0 / issuesAmount;

        for (Entry<Objective, Evaluator> entry : opponentUtilitySpace.getEvaluators()) {
            opponentUtilitySpace.unlock(entry.getKey());
            entry.getValue().setWeight(evenWeightValue);

            try {
                for (ValueDiscrete vd : ((IssueDiscrete) entry.getKey()).getValues())
                    ((EvaluatorDiscrete) entry.getValue()).setEvaluation(vd, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Use this method to store the changes made by the opponent between his last 2 bids. Used for the opponent model.
     * @param bid1
     * @param bid2
     * @return
     */
    private HashMap<Integer, Integer> opponentBidChanges(Bid bid1, Bid bid2) {
        HashMap<Integer, Integer> result = new HashMap<>();
        try {
            for (Issue issue : opponentUtilitySpace.getDomain().getIssues()) {
                int issueNumber = issue.getNumber();
                Value value1 = bid1.getValue(issueNumber);
                Value value2 = bid2.getValue(issueNumber);

                if (value1.equals(value2)) {
                    result.put(issueNumber, 0);
                } else {
                    result.put(issueNumber, 1);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public void receiveMessage(AgentID sender, Action action) {
        super.receiveMessage(sender, action);
        if (action instanceof Offer) {
            if (lastReceivedBid != null) {
                previousLastReceivedBid = lastReceivedBid;
            }
            lastReceivedBid = ((Offer) action).getBid();
        }
    }

    @Override
    public String getDescription() {
        return "Party group 11";
    }

}
