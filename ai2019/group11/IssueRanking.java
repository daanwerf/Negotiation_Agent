package group11;

import genius.core.issue.Objective;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.Evaluator;

import java.util.*;

public class IssueRanking {
    private AdditiveUtilitySpace utilitySpace;
    private PriorityQueue<Map.Entry<Objective, Evaluator>> weightRankedIssues;

    public IssueRanking(AdditiveUtilitySpace utilitySpace) {
        this.utilitySpace = utilitySpace;

        this.weightRankedIssues = new PriorityQueue<>(this.utilitySpace.getEvaluators().size() - 1,
                new EvaluatorWeightComparator());

        initializeWeightRankedIssues();
    }

    private void initializeWeightRankedIssues() {
        weightRankedIssues.clear();
        weightRankedIssues.addAll(utilitySpace.getEvaluators());
    }

    public Objective getIssueWithNthHighestWeight(int n) {
        PriorityQueue<Map.Entry<Objective, Evaluator>> weightRankedIssuesCopy = weightRankedIssues;
        assert (n <= weightRankedIssuesCopy.size() - 1);

        int count = 0;
        while(count != n) {
            weightRankedIssuesCopy.remove();
            count++;
        }
        return weightRankedIssuesCopy.peek().getKey();
    }

    /**
     * Returns the importance of the issue, dependant on the weight. The higher the returned value the higher the importance.
     * @param issueNumber
     * @return
     */
    public int getIssueImportance(int issueNumber) {
        int count = 0;
        while(weightRankedIssues.peek().getKey().getNumber() != issueNumber) {
            weightRankedIssues.remove();
            count++;
        }
        initializeWeightRankedIssues();
        return count;
    }
}
