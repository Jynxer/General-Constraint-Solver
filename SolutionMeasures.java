import java.util.ArrayList;

public class SolutionMeasures {
    String solver;
    String instance;
    String orderingStrategy;
    ArrayList<AssignmentMapping> solution;
    long timeTaken;
    int searchTreeNodes;
    int arcRevisions;

    public SolutionMeasures(String solver, String instance, String orderingStrategy,
            ArrayList<AssignmentMapping> solution, long timeTaken,
            int searchTreeNodes, int arcRevisions) {
        this.solver = solver;
        this.instance = instance;
        if (orderingStrategy == "1") {
            this.orderingStrategy = "smallest-domain first";
        } else {
            this.orderingStrategy = "ascending";
        }
        this.solution = solution;
        this.timeTaken = timeTaken;
        this.searchTreeNodes = searchTreeNodes;
        this.arcRevisions = arcRevisions;
    }

    public String toString() {
        return ("Using " + this.solver + " with the " + this.orderingStrategy
                + " ordering strategy, the " + this.instance + " problem was solved in "
                + Long.toString(this.timeTaken) + " milliseconds using " + Integer.toString(this.searchTreeNodes)
                + " search tree nodes and making " + Integer.toString(this.arcRevisions) + " arc revisions.");
    }
}