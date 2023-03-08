package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.Atom;
import ed.inf.adbs.minibase.base.Constant;
import ed.inf.adbs.minibase.base.Query;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.Head;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    }

    /**
     * CQ minimization procedure
     *
     * Assume the body of the query from inputFile has no comparison atoms
     * but could potentially have constants in its relational atoms.
     *
     */

    //This StringBuilder answer will record the final result of the CQ Minimization
    public static StringBuilder answer = new StringBuilder();

    public static void minimizeCQ(String inputFile, String outputFile) {
        CQMinimizer myCQ = new CQMinimizer();
        HashSet<String> headVariables = new HashSet<String>();
        HashMap<String, ArrayList> relationBody = new HashMap<String, ArrayList>();

        try {
            //Another approach to "deep code": Parse twice to
            Query queryOriginal = QueryParser.parse(Paths.get(inputFile));
            Query queryCopy = QueryParser.parse(Paths.get(inputFile));

            Head head = queryOriginal.getHead();
            myCQ.buildAnswerHead(head, headVariables);

            //Store each relation body.
            List<Atom> tempOriginalBody = queryOriginal.getBody();
            List<Atom> tempCopiedBody = queryCopy.getBody();
            List<RelationalAtom> originalBody = new ArrayList<RelationalAtom>();
            List<RelationalAtom> copiedBody = new ArrayList<RelationalAtom>();

            for(int i = 0; i < tempOriginalBody.size(); i++){
                originalBody.add((RelationalAtom) tempOriginalBody.get(i));
                copiedBody.add((RelationalAtom) tempCopiedBody.get(i));
            }

            myCQ.findHomomorphism(originalBody, copiedBody, headVariables);
            myCQ.removeDuplicateAndWriteIntoAnswer(copiedBody);            
            myCQ.writeIntoOutputFile(answer.toString(), outputFile);
        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

    /**
     * write the answer into the output file. Create the file if the file doesn't exist
     * @param myAnswer Write it in
     * @param outputFile file path
     */
    private void writeIntoOutputFile(String myAnswer, String outputFile){
        try{
            File file = new File(outputFile);
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(myAnswer.getBytes());
            fos.flush();
            fos.close();

        } catch(IOException e) {
            System.out.println("Fail write in :" + myAnswer + " to " + outputFile);
        }
    }

    /**
     * findHomomorphism will find all of the possible mappings between each pair of relation atoms in the query.
     * Apply all of the correct mapping to the copiedAtomsList.
     * @param originalAtomsList
     * @param copiedAtomsList Compare the copiedAtomsList and originalAtomsList to see if copiedAtomsList is the subset of originalAtomsList
     * @param headVariables All of the head variables
     */
    private void findHomomorphism(List<RelationalAtom> originalAtomsList, List<RelationalAtom> copiedAtomsList, HashSet<String> headVariables){
        HashMap<Object, Object> relationMapping = new HashMap<>();

        //Compare the currentRelationalAtom and all of the rest relational atoms to see if there is any possible mapping
        for (RelationalAtom currentRelationalAtom : copiedAtomsList){
            List<RelationalAtom> restRelationalAtomList = new ArrayList<RelationalAtom>(copiedAtomsList);
            restRelationalAtomList.remove(currentRelationalAtom);

            //Record the possible mapping between the current relational atom and another relational atom in rest list.
            HashMap<Object, Object> tempRelationMapping = new HashMap<>();

            for(RelationalAtom restRelationalAtom : restRelationalAtomList){
                //Check if currentRelationalAtom can be mapped into restRelationalAtom
                //First, check if two atoms are in one type.
                if(!currentRelationalAtom.getName().equals(restRelationalAtom.getName()) || currentRelationalAtom.getTerms().size() != restRelationalAtom.getTerms().size()){
                    continue;
                }

                //Now the relation name and the size of terms of currentRelationalAtom and restRelationalAtom are same.
                List<Term> currentRelationalAtomTermList = currentRelationalAtom.getTerms();
                List<Term> restRelationalAtomTermList = restRelationalAtom.getTerms();

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
                    //The last sitution is the left(currentRelationalAtom) is a variable. the right(restRelationalAtom) is a int/string
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

                //After find the mapping rule between current atom and rest atom. Check if this mapping rule works
                if(checkMapping(tempRelationMapping, originalAtomsList, copiedAtomsList)){
                    relationMapping.putAll(tempRelationMapping);
                }
                else{
                    tempRelationMapping.clear();
                }
            }
        }
    }

    /**
     * checkMapping will check if the mapping rule works. If it works, apply it to the copiedAtomsList
     * @param tempRelationMapping Mapping rule
     * @param originalAtomsList Compare the copiedAtomsList and originalAtomsList to see if copiedAtomsList is the subset of originalAtomsList
     * @param copiedAtomsList
     * @return  Return true if the mapping rule works
     */
    private boolean checkMapping(HashMap<Object, Object> tempRelationMapping, List<RelationalAtom> originalAtomsList, List<RelationalAtom> copiedAtomsList){
        //Deep copy copiedAtomsList. Then apply and modify the theOriginalCopiedAtomsList.
        List<RelationalAtom> theOriginalCopiedAtomsList = new ArrayList<>(); 
        for(RelationalAtom ra : copiedAtomsList){
            theOriginalCopiedAtomsList.add(ra.myDeepCopy());
        }
        applyMap(theOriginalCopiedAtomsList, tempRelationMapping);
        
        //Check if the copy of copiedAtomsList is the subset of the original atoms list.
        if(isSubset(theOriginalCopiedAtomsList, originalAtomsList)){
            //If the mapping rule it's valid, apply it to the copiedAtomsList
            applyMap(copiedAtomsList, tempRelationMapping);
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Modify each element in relationatom list based on the mapping rule. After modify, the same relation won't be removed.
     * @param applyTo relationatom list
     * @param tempRelationMapping mapping rule such as(x -> y) or (z -> "abc")
     */
    private void applyMap(List<RelationalAtom> applyTo, HashMap<Object, Object> tempRelationMapping){
        for(Map.Entry<Object, Object> myEntry : tempRelationMapping.entrySet()){
            for(RelationalAtom ra : applyTo){
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
    }

    /**
     * Remove the same relationatom from the list then write it into the public answer.
     * @param copiedBody
     */
    private void removeDuplicateAndWriteIntoAnswer(List<RelationalAtom> copiedBody){
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

    /**
     * Parse the relationalatom to a string and write the string into the public answer.
     * @param target
     * @return
     */
    private StringBuilder parseToString(RelationalAtom target){
        StringBuilder partOfString = new StringBuilder();
        partOfString.append(target.getName());
        partOfString.append("(");

        List<Term> tempTermList = target.getTerms();
        int index = 0;
        for(Term t : tempTermList){
            if(index != 0){
                partOfString.append(" ");
            }
            index++;
            partOfString.append(t.toString());
            partOfString.append(",");
        }
        partOfString.deleteCharAt(partOfString.length() - 1);
        partOfString.append("), ");
        return partOfString;
    }
    
    /**
     * Check if all of the elements in short list appears in long list.
     * @param leftList
     * @param rightList
     * @return return the result of if leftlist is the subset of rightlist.
     */
    private boolean isSubset(List<RelationalAtom> leftList, List<RelationalAtom> rightList){
        for(RelationalAtom SLAtom : leftList){
            boolean isSame = false;

            for(RelationalAtom LLAtom : rightList){
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
            //If we didn't find the same element from the right list. Which means that left list is not subset of right list.
            if(!isSame){
                return false;
            }
        }
        return true;
    }

    /**
     * Write the head information into the public answer.
     * @param head
     * @param headVariables
     */
    private void buildAnswerHead(Head head, HashSet<String> headVariables){
        String headName = head.getName();
        answer.append(headName);
        answer.append("(");
        for (Variable v : head.getVariables()) {
            headVariables.add(v.getName());
            answer.append(v.getName());
            answer.append(", ");
        }
        if(headVariables.size() != 0){
            answer.deleteCharAt(answer.length() - 1);
            answer.deleteCharAt(answer.length() - 1);
        }
        answer.append(") :- ");
    }
}