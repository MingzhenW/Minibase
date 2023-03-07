package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.Atom;
import ed.inf.adbs.minibase.base.Constant;
import ed.inf.adbs.minibase.base.Query;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.Head;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.management.relation.Relation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import ed.inf.adbs.minibase.base.Variable;
import ed.inf.adbs.minibase.base.SumAggregate;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.StringConstant;

/**
 *
 * Minimization of conjunctive queries
 *
 */
public class CQMinimizer {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: CQMinimizer input_file output_file");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        minimizeCQ(inputFile, outputFile);
        // parsingExample(inputFile);
    }

    /**
     * CQ minimization procedure
     *
     * Assume the body of the query from inputFile has no comparison atoms
     * but could potentially have constants in its relational atoms.
     *
     */
    public static StringBuilder answer = new StringBuilder();

    public static void minimizeCQ(String inputFile, String outputFile) {
        String headName = "";
        StringBuilder headString = new StringBuilder();
        HashSet<String> headVariables = new HashSet<String>();
        HashMap<String, ArrayList> relationBody = new HashMap<String, ArrayList>();

        try {
            // Query query = QueryParser.parse(Paths.get(filename));

            //Another approach to "deep code": Parse twice to
            // Query query = QueryParser.parse(Paths.get(filename));
            Query queryOriginal = QueryParser.parse("Q(x) :- R(x, z), T(y, 'ADBS'), R(x, 4)");
            Query queryCopy =     QueryParser.parse("Q(x) :- R(x, z), T(y, 'ADBS'), R(x, 4)");

            //Store head name and head variable
            Head head = queryOriginal.getHead();
            headName = head.getName();
            headString.append(headName);
            headString.append("(");
            for (Variable v : head.getVariables()) {
                headVariables.add(v.getName());
                headString.append(v.getName());
                headString.append(", ");
            }
            headString.deleteCharAt(headString.length() - 1);
            headString.deleteCharAt(headString.length() - 1);
            headString.append(") :- ");
            //Store each relation body.
            List<Atom> temporiginalBody = queryOriginal.getBody();
            List<Atom> tempcopiedBody = queryCopy.getBody();
            List<RelationalAtom> originalBody = new ArrayList<RelationalAtom>();
            List<RelationalAtom> copiedBody = new ArrayList<RelationalAtom>();

            for(int i = 0; i < temporiginalBody.size(); i++){
                originalBody.add((RelationalAtom) temporiginalBody.get(i));
                copiedBody.add((RelationalAtom) tempcopiedBody.get(i));
            }

            findHomomorphism(originalBody, copiedBody, headVariables);
            removeDuplicate(copiedBody);            
            answer.insert(0, headString.toString());
            
            System.out.println(answer.toString());
        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param originalAtomsList
     * @param copiedAtomsList
     * @param headVariables
     */
    private static void findHomomorphism(List<RelationalAtom> originalAtomsList, List<RelationalAtom> copiedAtomsList, HashSet<String> headVariables){
        //originalAtomsList is only used to check if copiedAtomsList is the subset of originalAtomsList.
        HashMap<Object, Object> relationMapping = new HashMap<>();

        //loop through each atom
        for (RelationalAtom currentRelationalAtom : copiedAtomsList){
            //The currentRelationalAtom will not be stored into the restRelationalAtomList
            List<RelationalAtom> restRelationalAtomList = new ArrayList<RelationalAtom>(copiedAtomsList);
            restRelationalAtomList.remove(currentRelationalAtom);

            
            HashMap<Object, Object> tempRelationMapping = new HashMap<>();


            //Loop through each element in currentAtomList
            //This step will find all of possible mapping rule to see if the outer currentAtomList can be mapped to inner c.
            for(RelationalAtom restRelationalAtom : restRelationalAtomList){
                //Check if currentRelationalAtom can be mapped into restRelationalAtom
                //First, check if two atoms are in one type.
                if(!currentRelationalAtom.getName().equals(restRelationalAtom.getName()) || currentRelationalAtom.getTerms().size() != restRelationalAtom.getTerms().size()){
                    continue;
                }

                //Now the relation name and the size of terms of currentRelationalAtom and restRelationalAtom are same.
                //[x, y, 2]
                List<Term> currentRelationalAtomTermList = currentRelationalAtom.getTerms();
                List<Term> restRelationalAtomTermList = restRelationalAtom.getTerms();

                //This for loop will loop through the xyz and yxr in R(x,y,x) and R(y,x,r)
                for(int i = 0; i < currentRelationalAtomTermList.size(); i++){
                    //Distinguished variable cannot be mapped to other variable
                    //Check when both are variable
                    if(currentRelationalAtomTermList.get(i) instanceof Variable && restRelationalAtomTermList.get(i) instanceof Variable){
                        Variable tempCurAtomVar = (Variable) currentRelationalAtomTermList.get(i);
                        Variable tempRestAtomVar = (Variable) restRelationalAtomTermList.get(i);
                        //If the value of these variable are same. Continue to the next element in term list.
                        if(tempCurAtomVar.getName().equals(tempRestAtomVar.getName())){
                            continue;
                        }
                        else{
                            //If this current relation term is distinguished variable. It cannot be mapped to other variable
                            if(headVariables.contains(tempCurAtomVar.getName())){
                                break;
                            }
                            else{
                                if(tempRelationMapping.containsKey(relationMapping)){
                                    tempRelationMapping.replace(tempCurAtomVar, tempRestAtomVar);
                                }
                                else{
                                    tempRelationMapping.put(tempCurAtomVar, tempRestAtomVar);
                                }
                            }
                        }
                    }
                    //Check when both are integer
                    else if(currentRelationalAtomTermList.get(i) instanceof IntegerConstant && restRelationalAtomTermList.get(i) instanceof IntegerConstant){
                        IntegerConstant tempCurAtomInt = (IntegerConstant) currentRelationalAtomTermList.get(i);
                        IntegerConstant tempRestAtomInt = (IntegerConstant) restRelationalAtomTermList.get(i);
                        
                        if(tempCurAtomInt.getValue().equals(tempRestAtomInt.getValue())){
                            continue;
                        }
                        else{
                            break;
                        }
                    }
                    //Check when both are string
                    else if(currentRelationalAtomTermList.get(i) instanceof StringConstant && restRelationalAtomTermList.get(i) instanceof StringConstant){
                        StringConstant tempCurAtomStr = (StringConstant) currentRelationalAtomTermList.get(i);
                        StringConstant tempRestAtomStr = (StringConstant) restRelationalAtomTermList.get(i);

                        if(tempCurAtomStr.getValue().equals(tempRestAtomStr.getValue())){
                            continue;
                        }
                        else{
                            break;
                        }
                    }
                    //If the current term is string or int. The current term cannot map to the right side.
                    else if(currentRelationalAtomTermList.get(i) instanceof IntegerConstant || currentRelationalAtomTermList.get(i) instanceof StringConstant){
                        break;
                    }
                    //The last sitution is the left(current term) is a variable. the right(rest term) is a int/string
                    else{
                        Variable tempCurAtomVar = (Variable) currentRelationalAtomTermList.get(i);
                        //If the left is a head variable. Cannot be mapped
                        if(headVariables.contains(tempCurAtomVar.getName())){
                            break;
                        }
                        if(restRelationalAtomTermList.get(i) instanceof StringConstant){
                            StringConstant tempRestAtomStr = (StringConstant) restRelationalAtomTermList.get(i);

                            if(tempRelationMapping.containsKey(relationMapping)){
                                tempRelationMapping.replace(tempCurAtomVar, tempRestAtomStr);
                            }
                            else{
                                tempRelationMapping.put(tempCurAtomVar, tempRestAtomStr);
                            }
                        }
                        else{
                            IntegerConstant tempRestAtomInt = (IntegerConstant) restRelationalAtomTermList.get(i);

                            if(tempRelationMapping.containsKey(relationMapping)){
                                tempRelationMapping.replace(tempCurAtomVar, tempRestAtomInt);
                            }
                            else{
                                tempRelationMapping.put(tempCurAtomVar, tempRestAtomInt);
                            }
                        }
                    }
                }
                //
                // System.out.println("==========================================");
                // System.out.println("Current relation: " + currentRelationalAtom);
                // System.out.println("Target relation: " + restRelationalAtom);
                // System.out.println("Current mapping: " + tempRelationMapping);
                if(applyMap(tempRelationMapping, originalAtomsList, copiedAtomsList)){
                    relationMapping.putAll(tempRelationMapping);
                    //break;
                }
                else{
                    tempRelationMapping.clear();
                }
                //System.out.println("Current: " + currentRelationalAtom);
            }
            
            // System.out.println("==========================================");
            // System.out.println("Current relation: " + currentRelationalAtom);
            // System.out.println("Current mapping: " + tempRelationMapping);
            // if(applyMap(tempRelationMapping, originalAtomsList, copiedAtomsList)){
            //     relationMapping.putAll(tempRelationMapping);
            // }
        }
    }

    private static boolean applyMap(HashMap<Object, Object> tempRelationMapping, List<RelationalAtom> originalAtomsList, List<RelationalAtom> copiedAtomsList){        
        //recorder will record which relation atom has been changed and change from what to what.
        ArrayList<ArrayList<Object>> recoder = new ArrayList<>();
        for(Map.Entry<Object, Object> myEntry : tempRelationMapping.entrySet()){
            //Loop through copiedAtomsList and apply all of the mapping rules
            for(RelationalAtom ra : copiedAtomsList){
                //Get the variable list
                List<Term> copiedAtomsTermList = ra.getTerms();

                for(int i = 0; i < copiedAtomsTermList.size(); i++){
                    //Only the variable canbe mapped to other things.
                    if(copiedAtomsTermList.get(i) instanceof Variable){
                        //The left is variable and the map from
                        Variable tempCurAtomInt = (Variable) copiedAtomsTermList.get(i);
                        Variable mapFrom = (Variable) myEntry.getKey();
                        //The variable name must be the same.
                        if(tempCurAtomInt.getName().equals(mapFrom.getName())){
                            copiedAtomsTermList.set(i, (Term) myEntry.getValue());

                            ArrayList<Object> tempRecorder = new ArrayList<>();
                            tempRecorder.add(ra);
                            tempRecorder.add(i);
                            tempRecorder.add(mapFrom);
                            tempRecorder.add((Term) myEntry.getValue());
                            recoder.add(tempRecorder);
                        }
                        else{
                            continue;
                        }
                    }
                    else{
                        continue;
                    }
                }
            }
        }
        
        if(isSubset(copiedAtomsList, originalAtomsList)){
            return true;
        }
        else{
            //System.out.println("================");
            //System.out.println(recoder);
            //If it is not sub set. Change every thing back depend on the recorder
            for(int i = 0; i < recoder.size(); i++){       
                RelationalAtom tempTargetRelation = (RelationalAtom) recoder.get(i).get(0);

                for(RelationalAtom eachRA : copiedAtomsList){
                    if(eachRA == tempTargetRelation){
                        List<Term> tempTermList = eachRA.getTerms();
                        if(recoder.get(i).get(2) instanceof Variable){
                            tempTermList.set((int) recoder.get(i).get(1), (Variable) recoder.get(i).get(2));
                        }
                        if(recoder.get(i).get(2) instanceof StringConstant){
                            tempTermList.set((int) recoder.get(i).get(1), (StringConstant) recoder.get(i).get(2));
                        }
                        if(recoder.get(i).get(2) instanceof IntegerConstant){
                            tempTermList.set((int) recoder.get(i).get(1), (IntegerConstant) recoder.get(i).get(2));
                        }
                    }
                }
            }
            return false;
        }
    }

    private static void removeDuplicate(List<RelationalAtom> copiedBody){
        HashSet<String> tempSet = new HashSet<>();

        for(RelationalAtom ra : copiedBody){
            StringBuilder tempST = parseToString(ra);
            if(!tempSet.contains(tempST.toString())){
                tempSet.add(tempST.toString());
                answer.append(tempST.toString());
            }
        }

        answer.deleteCharAt(answer.length() - 1);
        answer.deleteCharAt(answer.length() - 1);
    }

    private static StringBuilder parseToString(RelationalAtom target){
        StringBuilder partOfString = new StringBuilder();
        partOfString.append(target.getName());
        partOfString.append("(");

        List<Term> tempTermList = target.getTerms();
        int index = 0;
        for(Term t : tempTermList){
            if(t instanceof Variable || t instanceof IntegerConstant){
                if(index != 0){
                    partOfString.append(" ");
                }
                index++;
                partOfString.append(t.toString());
                partOfString.append(",");
            }
            else{
                if(index != 0){
                    partOfString.append(" ");
                }
                index++;
                //partOfString.append("'");
                partOfString.append(t.toString());
                //partOfString.append("'");
                partOfString.append(",");
            }
        }
        partOfString.deleteCharAt(partOfString.length() - 1);
        partOfString.append("), ");
        return partOfString;
    }
    //Check if one list is the subset of another list.
    private static boolean isSubset(List<RelationalAtom> shortList, List<RelationalAtom> longList){
        //Check if all of the elements in short list appears in long list.
        //shortList is a list: [R(x,y), S(1, x)]
        //SL_Item is a RelationalAtom: R(x, y)
        for(RelationalAtom SLAtom : shortList){
            //SLAtom and LLAtom is something like R[x, y, 1]
            boolean isSame = false;

            for(RelationalAtom LLAtom : longList){
                //If both relation are in the same type.
                //Then check if both relation has the same elements.
                if(SLAtom.isRelationNameEqual(LLAtom.getName())){
                    //Now, check if the relation body are the same.
                    if(SLAtom.isRelationBodyEqual(LLAtom.getTerms())){
                        //The body are the same, break the internal for loop
                        isSame = true;
                        //No need to check the rest body items, because there is not same body items
                        break;
                    }
                }
            }
            //If we didn't find the same element from the long list. Which means that short list is not subset of long list.
            if(!isSame){
                return false;
            }
        }
        return true;
    }
}