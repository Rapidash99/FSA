import java.util.*;


/**our FSA validator class**/
public class FSAValidator {
    private ArrayList<String> states, alpha, fin;  //states, alphabet, final state
    private ArrayList<String[]> trans;  //transitions
    private String init;  //initial state
    private String[] input;  //input strings

    //constructor that creates and assigns input values
    public FSAValidator(String[] inp) {
        this.input = inp.clone();
        this.states = new ArrayList<>(Arrays.asList(inp[0].substring(8, inp[0].length() - 1).split(",")));
        this.alpha = new ArrayList<>(Arrays.asList(inp[1].substring(7, inp[1].length() - 1).split(",")));
        this.init = inp[2].substring(9, inp[2].length() - 1);
        this.fin = new ArrayList<>(Arrays.asList(inp[3].substring(8, inp[3].length() - 1).split(",")));
        this.trans = new ArrayList<>();
        String[] arr = inp[4].substring(7, inp[4].length() - 1).split(",");

        for (String anArr : arr) {
            this.trans.add(anArr.split(">"));  //split transitions into arrays by '>'
        }
    }

    //the main method that validates and returns result string
    String validate() {
        String error = this.errorsCheck();
        if (error != null) return error;  //if we have error, return it
        return Kleene(); //if not, return result of Kleene's algorithm;
    }

    //method that checks for errors and return any if it exists
    private String errorsCheck() {
        if (!E5Check()) return "Error:\nE5: Input file is malformed";
        if (!E6Check()) return "Error:\nE6: FSA is nondeterministic";
        if (!E4Check()) return "Error:\nE4: Initial state is not defined";
        String E3 = E3Check();
        if (E3Check() != null) return "Error:\nE3: A transition '" + E3 + "' is not represented in the alphabet";
        if (!E2Check()) return "Error:\nE2: Some states are disjoint";
        if (!E1Check()) return "Error:\nE1: A state '" + this.init + "' is not in set of states";
        return null;  //return null if we have not any error
    }

    //method that checks for Error 1
    private boolean E1Check() { return this.states.contains(this.init); }

    //method that checks for Error 2
    private boolean E2Check() {
        if (this.states.size() == 1) return true;  //return true if we have only one state

        int[] check = new int[this.states.size()];  //creating array for counting transitions for each state
        for (int i = 0; i < check.length; i++) {
            check[i] = 0;  //array filling by zeros
        }

        for (String[] transition : this.trans) {
            for (int k = 0; k < this.states.size(); k++) {
                if (transition[0].equals(transition[2])) break;  //forget about transition if it leads to itself
                if (this.states.get(k).equals(transition[0])) {
                    check[k] += 1;  //count +1 if it leads from this state
                }
                if (this.states.get(k).equals(transition[2])) {
                    check[k] += 1;  //count +1 if it leads to this state
                }
            }
        }

        for (int state : check) {
            if (state == 0) return false;  //if any state have not transitions, return false
        }
        return true;  //return true otherwise
    }

    //method that checks for Error 3
    private String E3Check() {
        for (String[] transition : this.trans) {
            boolean contains = false;
            for (String anAlpha : this.alpha) {
                if (transition[1].equals(anAlpha)) {  //checking if we have transition in alphabet
                    contains = true;
                    break;
                }
            }
            if (!contains) return transition[1];  //return transition that not represented in alphabet
        }
        return null;  //return null otherwise
    }

    //method that checks for Error 4
    private boolean E4Check() { return this.init != null; }  //just return true if initial state is defined; false otherwise

    //method that checks for Error 5
    private boolean E5Check() {
        boolean check1 = this.input[0].startsWith("states={") && this.input[0].endsWith("}") &&
                this.input[1].startsWith("alpha={") && this.input[1].endsWith("}") &&
                this.input[2].startsWith("init.st={") && this.input[2].endsWith("}") &&
                this.input[3].startsWith("fin.st={") && this.input[3].endsWith("}") &&
                this.input[4].startsWith("trans={") && this.input[4].endsWith("}");  //check if it is ok with input
        if (!check1) return false;  //return false if input is malformed
        for (int i = 0; i < this.states.size(); i++) {
            for (int k = 0; k < this.states.size(); k++) {
                if (i != k && this.states.get(i).equals(this.states.get(k))) {
                    this.states.remove(k);  //we have the same states, drop one of them
                    k--;
                }
            }
        }
        return true;  //return true if input is not malformed
    }

    //method that checks for Error 6
    private boolean E6Check() {
        ArrayList<ArrayList<String>> check = new ArrayList<>(this.states.size());  //creating 2-dim arrayList of transitions for each state

        for (int i = 0; i < this.states.size(); i++) {
            check.add(new ArrayList<>());  //arrayList filling by arrayLists
            check.get(i).add(this.states.get(i));  //setting states to the first elements of arrayLists in arrayList
        }

        for (String[] transition : this.trans) {
            int index = -1;
            for (int k = 0; k < check.size(); k++) {
                if (check.get(k).get(0).equals(transition[0])) {  //finding index of i-th state in arrayList
                    index = k;  //index assignment
                    break;  //break if index is found
                }
            }

            for (int k = 1; k < check.get(index).size(); k++) {
                if (check.get(index).get(k).equals(transition[1])) return false;  //checking every previously added in this arrayList transition on duplicating with i-th transition
            }
            check.get(index).add(transition[1]);  //adding i-th transition in this arrayList if everything is fine
        }
        return true;  //return true if we did not find duplicating transitions
    }

    private String Kleene() {
        if (this.fin.size() == 1 && this.fin.get(0).equals("")) return "{}";  //if we have not any final state, just return "{}"

        String[][][] R = new String[this.states.size()][this.states.size()][this.states.size()];  //create array of Rs
        String[][] RminusOne = new String[this.states.size()][this.states.size()];  //create array R^(-1)

        Map<String, Integer> map = new HashMap<>();  //create hashmap of states and their numbers
        for (int i = 0; i < this.states.size(); i++) {
            map.put(this.states.get(i), i);  //filling this hashmap
        }

        for (String[] tr : this.trans) {  //for each transition s1>a>s2
            String s1 = tr[0];
            String a = tr[1];
            String s2 = tr[2];
            if (RminusOne[map.get(s1)][map.get(s2)] == null) RminusOne[map.get(s1)][map.get(s2)] = "(" + a;  //add "(" + a to R^(-1) if this slot is empty
            else RminusOne[map.get(s1)][map.get(s2)] += "|" + a;  //add "|" + a to R^(-1) if this slot is not empty
        }
        for (int i = 0; i < this.states.size(); i++) {  //add epsilons to R^(-1)
            if (RminusOne[i][i] == null) RminusOne[i][i] = "(eps";  //add "(eps" if it is empty
            else RminusOne[i][i] += "|eps";  //add "|eps" if it is not empty
        }
        for (int i = 0; i < this.states.size(); i++) {
            for (int k = 0; k < this.states.size(); k++) {
                if (RminusOne[i][k] == null) RminusOne[i][k] = "({}";  //add "{}" to each empty slot in R^(-1)
            }
        }
        for (int i = 0; i < this.states.size(); i++) {
            for (int k = 0; k < this.states.size(); k++) {
                RminusOne[i][k] += ")";  //add ")" to each slot
            }
        }

        R[0] = Kleene(RminusOne, 0);  //calculate R^0 by Kleene's algorithm
        for (int i = 1; i < R.length; i++) {  //calculate other Rs
            R[i] = Kleene(R[i - 1], i);
        }

        String result = R[R.length - 1][map.get(this.init)][map.get(this.fin.get(0))];  //result is R^(k)ij, where k is the last R; i is the number of initil state; j is number of first final state
        for (int i = 1; i < this.fin.size(); i++) {  //if we have more than 1 final state,
            result += "|" + R[R.length - 1][map.get(this.init)][map.get(this.fin.get(i))];  //add them all to result by "|"
        }
        return result;  //return result
    }

    //method that calculates Rs by Kleene's algorithm
    private String[][] Kleene(String[][] R, int j) {
        String[][] R_new = new String[this.states.size()][this.states.size()];  //create R_new
        for (int i = 0; i < this.states.size(); i++) {
            for (int k = 0; k < this.states.size(); k++) {
                if (j != R.length - 1) R_new[i][k] = "(" + R[i][j] + R[j][j] + "*" + R[j][k] + "|" + R[i][k] + ")";  //calculate R_new by R^(j)
                else R_new[i][k] = R[i][j] + R[j][j] + "*" + R[j][k] + "|" + R[i][k];  //if this R is last, do not add "(" and ")"
            }
        }
        return R_new;  //return R_new
    }
}
