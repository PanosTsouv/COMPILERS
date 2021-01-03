import java.util.Hashtable;
import java.util.LinkedList;

import minipython.node.*;

import minipython.analysis.DepthFirstAdapter;

public class MyVisitor extends DepthFirstAdapter
{
    private Hashtable symtable;	

	MyVisitor(Hashtable symtable) 
	{
		this.symtable = symtable;
    }
    
    public void inADefFunction(ADefFunction node) 
	{
		/*
			This method check if a function with same name and same number of attributes is defined multiple times
			If function is not defined -> add to Hashtable symtable
			-ex -> x() is same with x(a = 0) , y(a, b) is same with y(a, b = 0)

			fname : String
				The name of defined function
			args : int
				The number of all arguments(with default and non default values)
			defaultArgs : int
				The number of arguments with default values
			line, pos : int
				The position where function is defined
			nextArgs : LinkedList with AAfterFirstArgNextArgs objects
				Contains all next args after first arg
			fKey : String
				ex -> x(a, b) has fKey x(2) , x(a, b, c = 0) has fKey x(3)
			fKeyWithoutDefaultArgs : String
				ex -> x(a, b) has fKeyWithoutDefaultArgs x(2) , x(a, b, c = 0) has fKeyWithoutDefaultArgs x(2)

			Used Methods:
				getExpression() : PExpression object
					PExpression object can be cast to all alternatives (AIdentifierExpression is PExpression)
				getArgument() : LinkedList with AFirstArgument object (Exist in ADefFunction class)
					Return the first argument of function - Could be empty or size 1
				getNextArgs() : LinkedList with AAfterFirstArgNextArgs objects (Exist in AFirstArgument class)
					Return all next args after first arg - Could be gteater or equal than 0
				getValue() : LinkedList with default values (Exist in AFirstArgument and AAfterFirstArgNextArgs class)
					Return the value of an arg - Could be empty or size 1
		*/
		String fName = ((AIdentifierExpression)node.getExpression()).getId().toString().trim();
		int args = node.getArgument().size();
		int defaultArgs = 0;
		if(args != 0)
		{
			defaultArgs = ((AFirstArgument)node.getArgument().get(0)).getValue().size();
			LinkedList nextArgs = ((AFirstArgument)node.getArgument().get(0)).getNextArgs();
			args = args + nextArgs.size();
			for (Object iArg : nextArgs)
			{
				defaultArgs = defaultArgs + ((AAfterFirstArgNextArgs)iArg).getValue().size();
			}
		}
		String fKey = fName + "(" + Integer.toString(args) + ")";
		String fKeyWithoutDefaultArgs = fName + "(" + Integer.toString(args - defaultArgs) + ")";

        int line = ((TId)((AIdentifierExpression)node.getExpression()).getId()).getLine();
		int pos = ((TId)((AIdentifierExpression)node.getExpression()).getId()).getPos();

		if (symtable.containsKey(fKey) || symtable.containsKey(fKeyWithoutDefaultArgs))
		{
			System.out.println("[" + line + "," + pos + "]" + ": " +" Function " + fName +" is already defined");
		}
		else
		{
			symtable.put(fKey, node);
		}
	}
}