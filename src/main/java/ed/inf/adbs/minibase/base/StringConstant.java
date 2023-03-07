package ed.inf.adbs.minibase.base;

public class StringConstant extends Constant {
    private String value;

    public StringConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String changeTo){
        this.value = changeTo;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }
}