package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;

import java.util.ArrayList;
import java.util.List;

import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.StringConstant;
import ed.inf.adbs.minibase.base.Variable;

public class RelationalAtom extends Atom {
    private String name;

    private List<Term> terms;

    public RelationalAtom(String name, List<Term> terms) {
        this.name = name;
        this.terms = terms;
    }

    //getName will return something like R, S, T...
    public String getName() {
        return name;
    }

    //getTerm will return something like this [x, y, z]
    //each term in List<Term> is an object. such as y is Variable, 5 is IntegerConstant
    public List<Term> getTerms() {
        return terms;
    }

    //Check if the two relations has the same name.
    public boolean isRelationNameEqual(String inputName){
        if(inputName.equals(this.name)){
            return true;
        }
        else{
            return false;
        }
    }

    //This function will compare if all of the term in this.List is equal to the terms in parameter.
    public boolean isRelationBodyEqual(List<Term> longListBody){
        //If the term has different length.
        if(this.terms.size() != longListBody.size()){
            return false;
        }
        //Now, check if the list of term has the same elements
        for(int index = 0; index < this.terms.size(); index++){
            if(this.terms.get(index).equals(longListBody.get(index))){
                continue;
            }
            else{
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return name + "(" + Utils.join(terms, ", ") + ")";
    }

    //Deep copy this object and return a new RelationAtom object
    public RelationalAtom myDeepCopy(){
        List<Term> myNewDeepListTerm = new ArrayList<>();
        for(Term t : this.terms){
            if(t instanceof Variable){
                myNewDeepListTerm.add(((Variable) t).myDeepCopy());
            }
            if(t instanceof StringConstant){
                myNewDeepListTerm.add(((StringConstant) t).myDeepCopy());
            }
            if(t instanceof IntegerConstant){
                myNewDeepListTerm.add(((IntegerConstant) t).myDeepCopy());
            }
        }
        return new RelationalAtom(this.getName(), myNewDeepListTerm);
    }
}
