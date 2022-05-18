public class AssignmentMapping {

    int var;
    int val;

    // Integer.MAX_VALUE will never be the actual assignment
    // It is therefore used to represent an unassigned variable
    public AssignmentMapping(int var) {
        this.var = var;
        this.val = Integer.MAX_VALUE;
    }

    public void assign(int val) {
        this.val = val;
    }

    public void unassign() {
        this.val = Integer.MAX_VALUE;
    }

    public String toString() {
        String mapping = Integer.toString(var) + " -> ";
        if (val == Integer.MAX_VALUE) {
            mapping = mapping + "UNASSIGNED";
        } else {
            mapping = mapping + Integer.toString(val);
        }
        return mapping;
    }

}