package cop5556sp17;
import java.util.HashMap;
import java.util.Stack;
import cop5556sp17.AST.Dec;

public class SymbolTable {

	//TODO  add fields
   int current_scope = 0, next_scope=0;
   Stack<Integer> st = null;

   HashMap<String, HashMap<Integer,Dec>> outerMap = new HashMap<String, HashMap<Integer,Dec>>();
   HashMap<Integer, Dec> innerMap = new HashMap<Integer, Dec>();

	/**
	 * to be called when block entered
	 */
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		next_scope++;
		current_scope = next_scope;
		st.push(current_scope);

	}
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		st.pop();
		current_scope = st.peek();

	}

	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
		if(!outerMap.containsKey(ident)){
			innerMap = new HashMap<Integer, Dec>();
			innerMap.put(current_scope, dec);
			outerMap.put(ident, innerMap);
		}
		else{
			innerMap = outerMap.get(ident);
			if(innerMap.containsKey(current_scope)){
				return false;
			}else innerMap.put(current_scope, dec);
		}
		return true;
	}

	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		if(outerMap.containsKey(ident)){
			innerMap = outerMap.get(ident);

			for (int i = st.size()-1; i >=0 ; i--) {
				int sc_num = st.get(i);
				if (innerMap.containsKey(sc_num)) {
					return innerMap.get(sc_num);
				}
			}
		}
		return null;
	}

	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		st  = new Stack<Integer>();
		st.push(0);
	}

	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return "";
	}
}
