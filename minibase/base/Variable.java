package ed.inf.adbs.minibase.base;

public class Variable extends Term {
    private String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }

    /** Compare if this object is equal to the target object
     * @targetObj
     */
    @Override
    public boolean equals(Object targetObj){
        //If the target object is nulll
        if(targetObj == null){
            return false;
        }
        //If target object is not StringConstant, return false
        if(!(targetObj instanceof Variable)){
            return false;
        }
        //If two object are the same, return true
        if(targetObj == this){
            return true;
        }
        //If two objects have same value, return true.
        return this.name.equals(((Variable) targetObj).getName());
    }

    //Return a new Variable object which contains the same string value.
    public Variable myDeepCopy(){
        return new Variable(this.getName());
    }
}
