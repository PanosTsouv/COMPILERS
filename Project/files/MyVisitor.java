import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

import minipython.node.*;

import minipython.analysis.DepthFirstAdapter;

public class MyVisitor extends DepthFirstAdapter
{
	private Hashtable symtable;
	private String currentFuction = "";
	Hashtable<String, Object> fData;

	MyVisitor(Hashtable symtable) 
	{
		this.symtable = symtable;
	}
	
    public void inADefFunction(ADefFunction node)
	{
		/*
			This method create fData Hashtable which contains all info we need about current function

			fname : String
				The name of defined function

			Used Methods:
				getExpression() : PExpression object
					PExpression object can be cast to all alternatives (AIdentifierExpression is PExpression)
				getArgument() : LinkedList with AFirstArgument object (Exist in ADefFunction class)
					Return the first argument of function - Could be empty or size 1
		*/
		String fName = ((AIdentifierExpression)node.getExpression()).getId().toString().trim();
		fData = new Hashtable<>();
		currentFuction = fName;
		int args = node.getArgument().size();
		fData.put("args", args);
		fData.put("defaultArgs", 0);
	}

	public void outADefFunction(ADefFunction node)
	{
		/*
			This method compare the args of current function with the args of fuction with the same name 
			If a fuction with the same name exist and has same args - print an error message else add Data of this fuction

			allFuctionWithSameName : String
				All fuction with same name and different args have already visited
			currentFunctionArgs : int
				The number of args which check fuction contains
			currentFunctionDefaultArgs : int
				The number of default args which check fuction contains
			line, pos : int
				The position where function is defined
			fuctionArgs : int
				The number of args which fuction with same name has
			fuctionDefaultArgs : int
				The number of default args which fuction with same name has

			Used Methods:
				getExpression() : PExpression object
					PExpression object can be cast to all alternatives (AIdentifierExpression is PExpression)
		*/
		ArrayList<Hashtable<String, Object>> allFuctionWithSameName;
		int line = ((TId)((AIdentifierExpression)node.getExpression()).getId()).getLine();
		int pos = ((TId)((AIdentifierExpression)node.getExpression()).getId()).getPos();
		if(!symtable.containsKey(currentFuction))
		{
			allFuctionWithSameName = new ArrayList<>();
			symtable.put(currentFuction, allFuctionWithSameName);
			fData.put("name", currentFuction + allFuctionWithSameName.size());
			allFuctionWithSameName.add(fData);
		}
		else
		{
			allFuctionWithSameName = (ArrayList<Hashtable<String,Object>>)symtable.get(currentFuction);

			for(Hashtable<String,Object> function : allFuctionWithSameName)
			{
				int currentFunctionArgs = (int)fData.get("args");
				int currentFunctionDefaultArgs = (int)fData.get("defaultArgs");
				int fuctionArgs = (int)function.get("args");
				int fuctionDefaultArgs = (int)function.get("defaultArgs");
				if((currentFunctionArgs >= (fuctionArgs - fuctionDefaultArgs) && currentFunctionArgs <= fuctionArgs)
					|| (fuctionArgs >= (currentFunctionArgs - currentFunctionDefaultArgs) && fuctionArgs <= currentFunctionArgs))
				{
					print("[" + line + "," + pos + "]" + ": " +" Function " + currentFuction +" is already defined");
					return;
				}
			}
			fData.put("name", currentFuction + allFuctionWithSameName.size());
			allFuctionWithSameName.add(fData);
		}
		currentFuction = "";
	}

	public void inAFirstArgument(AFirstArgument node)
	{
		/*
			Check if first argument have default value - add +1 or 0 to fData(defaultAgs key)
			Check the number of children(NextArgs) - add +number to fData(args key)

			nextArgsSize : int
				The number of all next arguments(with default and non default values)
			defaultArgs : int
				The number of first argument with default value(0 or 1)
			nextArgs : LinkedList with AAfterFirstArgNextArgs objects
				Contains all next args after first arg

			Used Methods:
				getNextArgs() : LinkedList with AAfterFirstArgNextArgs objects
					Return all next args after first arg - Could be gteater or equal than 0
				getValue() : LinkedList with default values
					Return the value of an arg - Could be empty or size 1
		*/
		int defaultArgs = node.getValue().size();
		defaultArgs = defaultArgs + (int)fData.get("defaultArgs");
		fData.put("defaultArgs", defaultArgs);
		LinkedList nextArgs = node.getNextArgs();
		int nextArgsSize = (int)fData.get("args") + nextArgs.size();
		fData.put("args", nextArgsSize);
	}

	public void inAAfterFirstArgNextArgs(AAfterFirstArgNextArgs node)
	{
		/*
			Check if next argument have default value - add +1 or 0 to fData(defaultAgs key)

			Variables:
				defaultValueOfNextArgs : int
					The number of next argument with default value(0 or 1)

			Used Methods:
				getValue() : LinkedList with default values
					Return the value of an arg - Could be empty or size 1
		*/
		int defaultValueOfNextArgs = node.getValue().size();
		defaultValueOfNextArgs = defaultValueOfNextArgs + (int)fData.get("defaultArgs");
		fData.put("defaultArgs", defaultValueOfNextArgs);
	}

	public void inAFunctionCallExpression(AFunctionCallExpression node)
	{
		boolean findFunction = false;
		LinkedList args = node.getArglistExps();
		TId fCallTId = ((AIdentifierExpression)node.getIdExp()).getId();
		String fCallname = fCallTId.toString().trim();
		if(symtable.containsKey(fCallname))
		{
			for(Hashtable<String,Object> function : (ArrayList<Hashtable<String,Object>>)symtable.get(fCallname))
			{
				if(args.size() <= (int)function.get("args") && args.size() >= ((int)function.get("args") - (int)function.get("defaultArgs")))
				{
					Hashtable<String, Object> fCallData = new Hashtable<>();
					fCallData.put("functionName", function.get("name"));
					fCallData.put("returnType", "");
					symtable.put(fCallname + "Call", fCallData);
					findFunction = true;
				}
			}
			if(!findFunction)
			{
				print("[" + fCallTId.getLine() + "," + fCallTId.getPos() + "]" + ": " +" Function " + fCallname +" is defined with different number of arguments");
			}
		}
		else
		{
			if(currentFuction != "")
			{
				return;
			}
			print("[" + fCallTId.getLine() + "," + fCallTId.getPos() + "]" + ": " +" Function " + fCallname +" is not defined");
		}
	}

	public void print(Object objectToPrint)
	{
		System.out.println(objectToPrint);
	}
}