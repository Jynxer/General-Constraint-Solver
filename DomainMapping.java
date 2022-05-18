import java.util.ArrayList;

public class DomainMapping {

    int var;
    ArrayList<Integer> domain;

    public DomainMapping(int var, ArrayList<Integer> domain) {
        this.var = var;
        this.domain = domain;
    }

    public void addToDomain(int val) {
        if (!this.domain.contains(val)) {
            this.domain.add(val);
        }
    }

    public void pruneFromDomain(int val) {
        if (this.domain.contains(val)) {
            for (int i = 0; i < this.domain.size(); i++) {
                if (this.domain.get(i) == val) {
                    this.domain.remove(i);
                }
            }
        }
    }

    public boolean isDomainEmpty() {
        if (this.domain.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        String mapping = Integer.toString(this.var) + " -> {";
        for (int i = 0; i < this.domain.size(); i++) {
            mapping = mapping + Integer.toString(this.domain.get(i));
            if ((i + 1) < this.domain.size()) {
                mapping = mapping + ", ";
            }
        }
        mapping = mapping + "}";
        return mapping;
    }

}