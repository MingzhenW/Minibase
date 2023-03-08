package ed.inf.adbs.minibase.base;

public class IntegerConstant extends Constant {
    private Integer value;

    public IntegerConstant(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer changeTo){
        this.value = changeTo;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    /**
     * Override the equals function.
     * @param targetObj Compare target object with this object
     * @return Return the compare result
     */
    @Override
    public boolean equals(Object targetObj){
        //If the target object is nulll
        if(targetObj == null){
            return false;
        }
        //If target object is not IntegerConstant, return false
        if(!(targetObj instanceof IntegerConstant)){
            return false;
        }
        //If two object are the same, return true
        if(targetObj == this){
            return true;
        }
        //If two objects have same value, return true.
        return this.value.equals(((IntegerConstant) targetObj).getValue());
        // IntegerConstant tempObj = (IntegerConstant) targetObj;
        // return tempObj.getValue().equals(this.value);
    }

    //Deep copy this object and return the new object
    public IntegerConstant myDeepCopy(){
        return new IntegerConstant(this.getValue());
    }
}