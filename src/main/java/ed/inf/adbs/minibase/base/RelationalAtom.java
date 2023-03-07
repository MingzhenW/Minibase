package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;

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

    //The parameter is a list of terms.
    //This function will compare if all of the term in this.List is equal to the terms in parameter.
    //List<Term>: [x, y, 1]
    public boolean isRelationBodyEqual(List<Term> longListBody){
        //If the term has different length.
        if(this.terms.size() != longListBody.size()){
            return false;
        }
        //Now, check if the list of term has the same elements
        for(int index = 0; index < this.terms.size(); index++ ){
            //Check if two terms are the same object type (Variable/IntegerConstant/StringConstant)
            if(((this.terms.get(index) instanceof Variable) && (longListBody.get(index) instanceof Variable)) || ((this.terms.get(index) instanceof IntegerConstant) && (longListBody.get(index) instanceof IntegerConstant)) || ((this.terms.get(index) instanceof StringConstant) && (longListBody.get(index) instanceof StringConstant))){
                //Once the object type are the same, check if two values are equal
                if(this.terms.get(index) instanceof Variable){
                    Variable temp_SL = (Variable) this.terms.get(index);
                    Variable temp_LL = (Variable) longListBody.get(index);
                    //If the type are same but the value are different. Return false
                    if(!temp_SL.getName().equals(temp_LL.getName())){
                        return false;
                    }
                }
                else if(this.terms.get(index) instanceof IntegerConstant){
                    IntegerConstant temp_SL = (IntegerConstant) this.terms.get(index);
                    IntegerConstant temp_LL = (IntegerConstant) longListBody.get(index);

                    if(!temp_SL.getValue().equals(temp_LL.getValue())){
                        return false;
                    }
                }
                else{
                    StringConstant temp_SL = (StringConstant) this.terms.get(index);
                    StringConstant temp_LL = (StringConstant) longListBody.get(index);

                    if(!temp_SL.getValue().equals(temp_LL.getValue())){
                        return false;
                    }
                }
            }
            //If two terms are not in the same type. Which means these two lists are different.
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
}
