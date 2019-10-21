package group11;

import genius.core.issue.Objective;
import genius.core.utility.Evaluator;
import java.util.Comparator;
import java.util.Map;

public class EvaluatorWeightComparator implements Comparator<Map.Entry<Objective, Evaluator>> {

    public int compare(Map.Entry<Objective, Evaluator> issue1, Map.Entry<Objective, Evaluator> issue2) {
       return Double.compare(issue1.getValue().getWeight(), issue2.getValue().getWeight());
    }
}
