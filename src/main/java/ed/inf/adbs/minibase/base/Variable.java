package ed.inf.adbs.minibase.base;

public class Variable extends Term {
    private String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String changeTo){
        this.name = changeTo;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
