import java.util.*;

public class MAC {

    static ArrayList<DomainMapping> domains = new ArrayList<DomainMapping>();
    static ArrayList<AssignmentMapping> assignments = new ArrayList<AssignmentMapping>();
    static ArrayList<BinaryConstraint> constraints = new ArrayList<BinaryConstraint>();
    static DomainMapping latestPrune = new DomainMapping(0, new ArrayList<>());
    static ArrayList<DomainMapping> prunesFromAC = new ArrayList<DomainMapping>();
    static ArrayList<ArrayList<DomainMapping>> prunesToUndo = new ArrayList<ArrayList<DomainMapping>>();
    static DomainMapping domainMinusXi = new DomainMapping(0, new ArrayList<>());
    static int lastAssigned;
    static int orderingStrategy;
    static long startTime = 0;
    static long endTime = 0;
    static int totalNodes;
    static int totalArcRevisions;

    public static void main(String args[]) {
        SolutionMeasures result = solve(args);
        System.out.println(result.toString());
    }

    public static SolutionMeasures solve(String arguments[]) {
        BinaryCSPReader reader = new BinaryCSPReader();
        startTime = System.currentTimeMillis();
        BinaryCSP csp = reader.readBinaryCSP(arguments[0]);
        orderingStrategy = Integer.parseInt(arguments[1]);
        initConstraints(csp.getConstraints());
        initDomains(csp);
        initAssignments(csp);
        ArrayList<Integer> varList = initVarList();
        totalNodes = 0;
        totalArcRevisions = 0;
        AC3();
        MAC3(varList);
        // print("Failed");
        // PrintDomains();
        return new SolutionMeasures("MAC", arguments[0], arguments[1], assignments, endTime - startTime, totalNodes,
                totalArcRevisions);
    }

    public static void MAC3(ArrayList<Integer> varList) {
        totalNodes++;
        ////////////////////////////////////////////////// LEFT BRANCH
        int var;
        if (orderingStrategy == 0) {
            var = SelectVar(varList);
        } else {
            var = SelectVarAscending(varList);
        }
        int val = SelectVal(var);
        Assign(var, val, varList);
        if (CompleteAssignment()) {
            PrintAssignments();
            endTime = System.currentTimeMillis();
            return;
        } else if (AC3()) {
            MAC3(varList);
        }
        UndoPruning();
        ////////////////////////////////////////////////// RIGHT BRANCH
        Unassign(var, varList);
        boolean isEmpty = PruneFromDomain(val, var);
        if (!isEmpty) {
            if (AC3()) {
                MAC3(varList);
            }
            UndoPruning();
        }
        AddToDomain(var, val);
    }

    public static boolean AC3() {
        Queue<BinaryConstraint> queue = new LinkedList<BinaryConstraint>();
        for (int i = 0; i < constraints.size(); i++) {
            if (constraints.get(i).getSecond() == lastAssigned) {
                queue.add(constraints.get(i));
            }
        }
        while (!queue.isEmpty()) {
            BinaryConstraint constraint = queue.poll();
            int revise = Revise(constraint);
            if (revise == 1) {
                prunesFromAC.add(latestPrune);
                for (int j = 0; j < constraints.size(); j++) {
                    if ((constraints.get(j).getSecond() == constraint.getFirst())
                            && (constraints.get(j).getFirst() != constraint.getSecond())
                            && (!queue.contains(constraints.get(j)))) {
                        queue.add(constraints.get(j));
                    }
                }
            } else if (revise == Integer.MAX_VALUE) {
                return false;
            }
        }
        prunesToUndo.add(prunesFromAC);
        return true;
    }

    public static int Revise(BinaryConstraint constraint) {
        boolean changed = false;
        int xi = constraint.getFirst();
        int xj = constraint.getSecond();
        DomainMapping Di = new DomainMapping(0, new ArrayList<>());
        DomainMapping Dj = new DomainMapping(0, new ArrayList<>());
        ArrayList<Integer> prunedValues = new ArrayList<Integer>();
        for (int i = 0; i < domains.size(); i++) {
            if (domains.get(i).var == xi) {
                Di = domains.get(i);
            } else if (domains.get(i).var == xj) {
                Dj = domains.get(i);
            }
        }
        int DiDomainSize = Di.domain.size();
        int backStep = 0;
        for (int j = 0; j < DiDomainSize; j++) {
            boolean supported = false;
            for (int k = 0; k < Dj.domain.size(); k++) {
                if (constraint.isValidTuple(Di.domain.get(j - backStep), Dj.domain.get(k))) {
                    supported = true;
                }
            }
            if (!supported) {
                prunedValues.add(Di.domain.get(j - backStep));

                Di.pruneFromDomain(Di.domain.get(j - backStep));
                backStep++;
                changed = true;
                totalArcRevisions++;
            }
        }
        if (changed) {
            latestPrune = new DomainMapping(xi, prunedValues);
        }
        if (Di.isDomainEmpty()) {
            return Integer.MAX_VALUE;
        } else {
            if (changed) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public static void Assign(int var, int val, ArrayList<Integer> varList) {
        lastAssigned = var;
        for (int i = 0; i < assignments.size(); i++) {
            if (assignments.get(i).var == var) {
                assignments.get(i).assign(val);
            }
        }
        for (int j = 0; j < varList.size(); j++) {
            if (varList.get(j) == var) {
                varList.remove(j);
            }
        }
        for (int k = 0; k < domains.size(); k++) {
            if (domains.get(k).var == var) {
                ArrayList<Integer> discardedVals = new ArrayList<Integer>();
                for (int l = 0; l < domains.get(k).domain.size(); l++) {
                    if (domains.get(k).domain.get(l) != val) {
                        discardedVals.add(domains.get(k).domain.get(l));
                    }
                }
                for (int m = 0; m < discardedVals.size(); m++) {
                    domains.get(k).pruneFromDomain(discardedVals.get(m));
                }
                domainMinusXi = new DomainMapping(var, discardedVals);
            }
        }
    }

    public static void Unassign(int var, ArrayList<Integer> varList) {
        for (int i = 0; i < assignments.size(); i++) {
            if (assignments.get(i).var == var) {
                assignments.get(i).assign(Integer.MAX_VALUE);
            }
        }
        varList.add(var);
        for (int j = 0; j < domains.size(); j++) {
            if (domains.get(j).var == var) {
                for (int k = 0; k < domainMinusXi.domain.size(); k++) {
                    domains.get(j).addToDomain(domainMinusXi.domain.get(k));
                }
            }
        }
    }

    public static void AddToDomain(int var, int val) {
        for (int i = 0; i < domains.size(); i++) {
            if (domains.get(i).var == var) {
                domains.get(i).addToDomain(val);
            }
        }
    }

    public static boolean PruneFromDomain(int var, int val) {
        for (int i = 0; i < domains.size(); i++) {
            if (domains.get(i).var == var) {
                domains.get(i).pruneFromDomain(val);
                if (domains.size() == 0) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public static void UndoPruning() {
        ArrayList<DomainMapping> undoPrunes = new ArrayList<DomainMapping>();
        if (prunesToUndo.size() != 0) {
            undoPrunes = prunesToUndo.get(prunesToUndo.size() - 1);
        }
        for (int i = 0; i < undoPrunes.size(); i++) {
            DomainMapping prune = undoPrunes.get(i);
            for (int j = 0; j < domains.size(); j++) {
                if (prune.var == domains.get(j).var) {
                    for (int k = 0; k < prune.domain.size(); k++) {
                        domains.get(j).addToDomain(prune.domain.get(k));
                    }
                }
            }
        }
        if (prunesToUndo.size() != 0) {
            prunesToUndo.remove(prunesToUndo.size() - 1);
        }
    }

    public static int SelectVar(ArrayList<Integer> varList) {
        int smallestDomain = Integer.MAX_VALUE;
        int smallestDomainVar = Integer.MAX_VALUE;
        for (int i = 0; i < domains.size(); i++) {
            if (varList.contains(domains.get(i).var)) {
                int domainSize = domains.get(i).domain.size();
                if ((domainSize <= smallestDomain) && (domainSize != 0)) {
                    smallestDomain = domainSize;
                    smallestDomainVar = domains.get(i).var;
                }
            }
        }
        return smallestDomainVar;
    }

    public static int SelectVarAscending(ArrayList<Integer> varList) {
        int min = Integer.MAX_VALUE;
        int minVar = 0;
        for (int i = 0; i < varList.size(); i++) {
            if (varList.get(i) < min) {
                min = varList.get(i);
                minVar = i;
            }
        }
        return minVar;
    }

    public static int SelectVal(int var) {
        for (int i = 0; i < domains.size(); i++) {
            if (domains.get(i).var == var) {
                return domains.get(i).domain.get(0);
            }
        }
        return Integer.MAX_VALUE;
    }

    public static void PrintAssignments() {
        print("\nAssignments:");
        for (int i = 0; i < assignments.size(); i++) {
            print(assignments.get(i).toString());
        }
    }

    public static void PrintDomains() {
        print("\nDomains:");
        for (int i = 0; i < domains.size(); i++) {
            System.out.print("\n" + Integer.toString(i) + ":");
            for (int j = 0; j < domains.get(i).domain.size(); j++) {
                System.out.print(Integer.toString(domains.get(i).domain.get(j)) + " ");
            }
        }
    }

    public static void PrintConstraints() {
        print("\nConstraints:");
        for (int i = 0; i < constraints.size(); i++) {
            System.out.println("c(" + constraints.get(i).getFirst() + ", " + constraints.get(i).getSecond() + ")");
        }
    }

    public static boolean CompleteAssignment() {
        boolean isComplete = true;
        for (int i = 0; i < assignments.size(); i++) {
            if (assignments.get(i).val == Integer.MAX_VALUE) {
                isComplete = false;
            }
        }
        return isComplete;
    }

    public static void initDomains(BinaryCSP csp) {
        int[][] bounds = csp.getDomainBounds();
        for (int i = 0; i < bounds.length; i++) {
            ArrayList<Integer> domain = new ArrayList<Integer>();
            for (int j = bounds[i][0]; j <= bounds[i][1]; j++) {
                domain.add(j);
            }
            DomainMapping mapping = new DomainMapping(i, domain);
            domains.add(mapping);
        }
    }

    public static void initAssignments(BinaryCSP csp) {
        for (int i = 0; i < csp.getDomainBounds().length; i++) {
            AssignmentMapping mapping = new AssignmentMapping(i);
            assignments.add(mapping);
        }
    }

    public static void initConstraints(ArrayList<BinaryConstraint> initialConstraints) {
        for (int i = 0; i < initialConstraints.size(); i++) {
            constraints.add(initialConstraints.get(i));
            int fv = initialConstraints.get(i).getFirst();
            int sv = initialConstraints.get(i).getSecond();
            ArrayList<BinaryTuple> t = new ArrayList<BinaryTuple>();
            for (int j = 0; j < initialConstraints.get(i).getTuples().size(); j++) {
                int v1 = initialConstraints.get(i).getTuples().get(j).getV1();
                int v2 = initialConstraints.get(i).getTuples().get(j).getV2();
                BinaryTuple newTuple = new BinaryTuple(v2, v1);
                t.add(newTuple);
            }
            BinaryConstraint flippedArc = new BinaryConstraint(sv, fv, t);
            constraints.add(flippedArc);
        }
    }

    public static ArrayList<Integer> initVarList() {
        ArrayList<Integer> varList = new ArrayList<Integer>();
        for (int i = 0; i < assignments.size(); i++) {
            varList.add(assignments.get(i).var);
        }
        return varList;
    }

    public static void print(String in) {
        System.out.println(in);
    }

}