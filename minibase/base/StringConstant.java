package ed.inf.adbs.minibase.base;

public class StringConstant extends Constant {
    private String value;

    public StringConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }

    /**
     * Override the equals function.
     * @param targetObj //Compare target object with this object
     * @return //return the compare result
     */
    @Override
    public boolean equals(Object targetObj){
        //If the target object is nulll
        if(targetObj == null){
            return false;
        }
        //If target object is not StringConstant, return false
        if(!(targetObj instanceof StringConstant)){
            return false;
        }
        //If two object are the same, return true
        if(targetObj == this){
            return true;
        }
        //If two objects have same value, return true.
        return this.value.equals(((StringConstant) targetObj).getValue());
        // StringConstant tempObj = (StringConstant) targetObj;
        // return tempObj.getValue().equals(this.value);
    }

    //Deep copy this object and return the new object
    public StringConstant myDeepCopy(){
        return new StringConstant(this.getValue());
    }
}