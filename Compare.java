import java.util.ArrayList;

public class Compare {

    static ArrayList<SolutionMeasures> measures = new ArrayList<SolutionMeasures>();

    public static void main(String args[]) {
        String[] instances = { "4Queens", "6Queens", "8Queens", "10Queens", "FinnishSudoku", "langfords2_3",
                "langfords2_4", "langfords3_9", "langfords3_10", "SimonisSudoku" };
        String[] orderingStrategies = { "0", "1" }; // 0 is Smallest-Domain, 1 is Ascending
        for (int i = 0; i < instances.length; i++) {
            for (int j = 0; j < orderingStrategies.length; j++) {
                String[] arguments = { "./instances/" + instances[i] + ".csp", orderingStrategies[j] };
                measures.add(MAC.solve(arguments));
                measures.add(FC.solve(arguments));
            }
        }
        PrintResults();
    }

    public static void PrintResults() {
        for (int i = 0; i < measures.size(); i++) {
            System.out.print(measures.get(i).toString());
        }
    }

}