package group11;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.*;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class EstimatedUtilitySpace {
    private AdditiveUtilitySpace resultUtilitySpace;

    private double userEpsilon;
    private double userInitialepsilon = 0.6;
    private double userRecedingRate = 10;
    private double userFinalEpsilon = 0.2;
    private int userWeightLearnFactor = 1;
    private double currentTime = 0;
    private int issuesAmount;

    private Bid previousBid;
    private Bid currentBid;

    public EstimatedUtilitySpace(Domain dom) {
        AdditiveUtilitySpaceFactory factory = new AdditiveUtilitySpaceFactory(dom);
        this.resultUtilitySpace = factory.getUtilitySpace();
    }

    public AdditiveUtilitySpace getEstimatedUtilitySpace(List<Bid> bidOrder, int issuesAmount) {
        this.issuesAmount = issuesAmount;

        this.previousBid = bidOrder.get(bidOrder.size() - 1);
        for (int i = bidOrder.size() - 1; i-- > 0; ) {
            this.currentBid = bidOrder.get(i);
            //System.out.println(currentBid);
            this.currentTime = (double) (bidOrder.size() - i) / bidOrder.size();
            updateUtilitySpace();
            this.previousBid = currentBid;
        }
        return resultUtilitySpace;
    }

    private void updateUtilitySpace() {
        updateEpsilon();
        double increaseRate = userEpsilon / issuesAmount;

        if (currentTime == 0) {
            initializeUtilitySpace();
        } else {
            HashMap<Integer, Integer> userBidChanges = userBidChanges(currentBid, previousBid);

            int numberOfChanges = numberOfChanges(userBidChanges);

            double totalIncrease = 1.0 + increaseRate / numberOfChanges;
            double maximumWeight = 1.0 - (issuesAmount) * increaseRate / totalIncrease;

            // Update the weights
            for(Integer issueNumber : userBidChanges.keySet()) {
                Objective currentObjective = resultUtilitySpace.getDomain().getObjectivesRoot().getObjective(issueNumber);
                double currentWeight = resultUtilitySpace.getWeight(issueNumber);
                double newWeight;

                if (userBidChanges.get(issueNumber) == 0 && currentWeight < maximumWeight) {
                    newWeight = (currentWeight + increaseRate) / totalIncrease;
                } else {
                    newWeight = currentWeight / totalIncrease;
                }
                resultUtilitySpace.setWeight(currentObjective, newWeight);
            }

            // Update the values
            try {
                for (Map.Entry<Objective, Evaluator> entry: resultUtilitySpace.getEvaluators()) {
                    EvaluatorDiscrete value = (EvaluatorDiscrete) entry.getValue();
                    IssueDiscrete issue = ((IssueDiscrete) entry.getKey());

                    ValueDiscrete issueValue = (ValueDiscrete) currentBid.getValue(issue.getNumber());
                    Integer notNormalizedEvaluation = value.getEvaluationNotNormalized(issueValue);

                    value.setEvaluation(issueValue, ((int) (userWeightLearnFactor / currentTime) + notNormalizedEvaluation));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updateEpsilon() {
        double f1 = userInitialepsilon * (1 - userRecedingRate * Math.pow(currentTime, 4));
        double f2 = userFinalEpsilon;

        //System.out.println("Old epsilon: " + userEpsilon + ". Found at time " + currentTime);
        this.userEpsilon = Double.max(f1, f2);
        //System.out.println("New epsilon: " + userEpsilon + ". Found at time " + currentTime);
    }

    private void initializeUtilitySpace() {
        double evenWeightValue = 1.0 / issuesAmount;

        for (Map.Entry<Objective, Evaluator> entry : resultUtilitySpace.getEvaluators()) {
            resultUtilitySpace.unlock(entry.getKey());
            entry.getValue().setWeight(evenWeightValue);

            try {
                for (ValueDiscrete vd : ((IssueDiscrete) entry.getKey()).getValues())
                    ((EvaluatorDiscrete) entry.getValue()).setEvaluation(vd, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private HashMap<Integer, Integer> userBidChanges(Bid bid1, Bid bid2) {
        HashMap<Integer, Integer> result = new HashMap<>();
        try {
            for (Issue issue : resultUtilitySpace.getDomain().getIssues()) {
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

    private int numberOfChanges(HashMap<Integer, Integer> opponentBidChanges) {
        int numberOfChanges = 0;
        for (Integer issueName : opponentBidChanges.keySet()) {
            if (opponentBidChanges.get(issueName) == 0) {
                numberOfChanges += 1;
            }
        }
        return numberOfChanges;
    }

}
