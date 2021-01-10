import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.Vector;

import minipython.node.*;
import minipython.analysis.DepthFirstAdapter;

public class MyVisitor2 extends DepthFirstAdapter
{
    private Hashtable symtable;
    private String key = "";
    private String currentFuction = "";
    private Stack<String> operationQueue;
    private Stack<String> functionCallStack = new Stack<>();
    private boolean inAssignStatement = false;
    private boolean inPrintStatement = false;
    private boolean inFunctionCallStatement = false;
    private boolean checkReturnStatementType = false;
    private Hashtable<String, Boolean> allCheckReturnStatements = new Hashtable<>();
    private String returnStatementFunctionName = "";
    private int line, column = 0;



    MyVisitor2(Hashtable symtable)
    {
        this.symtable = symtable;
    }


    public void inAAssignStatement(AAssignStatement node)
	{	
		TId vTid = ((AIdentifierExpression)node.getIdExp()).getId();
		String vName = vTid.toString().trim();
        inAssignStatement = true;
		if (currentFuction == "")
		{
			key = vName + "GVar";
		}
		else
		{
			key = vName + "LVar";
        }
        if (!symtable.containsKey(key))
		{
            symtable.put(key, "Unknown");
		}
        operationQueue = new Stack<>();
    }

    public void outAAssignStatement(AAssignStatement node)
	{
        inAssignStatement = true;
        if (symtable.containsKey(key))
		{
            symtable.put(key, getExpressionType(node.getExp()));
        }
        inAssignStatement = false;
    }

    public void inAPrintStatement(APrintStatement node)
    {
        inPrintStatement = true;
    }

    public void outAPrintStatement(APrintStatement node)
    {
        inPrintStatement = false;
    }

    public void inAIdentifierExpression(AIdentifierExpression node)
    {
        if(!checkReturnStatementType)
        {
            boolean identifierInAFunctionPar = false;
            String vName = node.getId().toString().trim();
            if (currentFuction == "")
            {
                if(node.parent() instanceof AFunctionCallExpression)
                {
                    if(!symtable.containsKey(vName + "GVar") && !(node == ((AFunctionCallExpression)node.parent()).getIdExp()))
                    {
                        print(String.format("[%d,%d] : Name %s is not defined", node.getId().getLine(), node.getId().getPos(), vName));
                    }
                }
                else
                {
                    if(!symtable.containsKey(vName + "GVar"))
                    {
                        print(String.format("[%d,%d] : Name %s is not defined", node.getId().getLine(), node.getId().getPos(), vName));
                    }
                }
            }
            else
            {
                Function function = ((Hashtable<String, Function>)symtable.get(currentFuction)).get(currentFuction + line + column);
                for(Hashtable<String, Object> arg : function.getArgsInfo())
                {
                    if(arg.get("argName").equals(vName))
                    {
                        identifierInAFunctionPar = true;
                    }
                }
                if(!identifierInAFunctionPar)
                {
                    if(node.parent() instanceof AFunctionCallExpression)
                    {
                        if(!symtable.containsKey(vName + "GVar") && !symtable.containsKey(vName + "LVar") && !symtable.containsKey(vName) && !(node == ((AFunctionCallExpression)node.parent()).getIdExp()))
                        {
                            print(String.format("[%d,%d] : Name %s is not defined", node.getId().getLine(), node.getId().getPos(), vName));
                        }
                    }
                    else
                    {
                        if(!symtable.containsKey(vName + "GVar") && !symtable.containsKey(vName + "LVar") && !symtable.containsKey(vName))
                        {
                            print(String.format("[%d,%d] : Name %s is not defined", node.getId().getLine(), node.getId().getPos(), vName));
                        }
                    }
                }
            }
        }
    }

    public String getExpressionType(PExpression node)
    {
        return ckeckInstanceOf(node);
    }

    public void outAAdditionExpression(AAdditionExpression node)
    {
        if(inAssignStatement || inFunctionCallStatement || inPrintStatement)
        {
            PExpression left = node.getLeftExp();
            PExpression right = node.getRightExp();
            getType(left, right, node);
        }
    }

    public void outAMultiplicationExpression(AMultiplicationExpression node)
    {
        if(inAssignStatement || inFunctionCallStatement || inPrintStatement)
        {
            PExpression left = node.getLeftExp();
            PExpression right = node.getRightExp();
            getType(left, right, node);
        }
    }

    public void outADivisionExpression(ADivisionExpression node)
    {
        if(inAssignStatement || inFunctionCallStatement || inPrintStatement)
        {
            PExpression left = node.getLeftExp();
            PExpression right = node.getRightExp();
            getType(left, right, node);
        }
    }

    public void outAModuloExpression(AModuloExpression node)
    {
        if(inAssignStatement || inFunctionCallStatement || inPrintStatement)
        {
            PExpression left = node.getLeftExp();
            PExpression right = node.getRightExp();
            getType(left, right, node);
        }
    }

    public void outAPowerExpression(APowerExpression node)
    {
        if(inAssignStatement || inFunctionCallStatement || inPrintStatement)
        {
            PExpression left = node.getLeftExp();
            PExpression right = node.getRightExp();
            getType(left, right, node);
        }
    }

    public void getType(PExpression left, PExpression right,PExpression node)
    {
        if(operationQueue == null){operationQueue = new Stack<>();}
        String rightType = ckeckInstanceOf(right);
        String leftType = ckeckInstanceOf(left);

        if(leftType != "Unknown" && rightType != "Unknown")
        {
            if (!(node instanceof AMultiplicationExpression) || leftType == "None" || rightType == "None")
            {
                if(leftType != rightType)
                {
                    print(String.format("In line %d you can't add type %s with type %s",line, leftType, rightType));
                }
            }
        }
        if(leftType != "Unknown" && rightType != "Unknown")
        {
            operationQueue.add(leftType);
        }
        else
        {
            operationQueue.add("Unknown");
        }
        print(operationQueue);
    }

    public String ckeckInstanceOf(PExpression x)
    {
        String xType = "";
        if(x instanceof AIdentifierExpression)
        {
            boolean findInPar = false;
            String currentFunctionCallName = "";
            if(currentFuction != "")
            {
                currentFunctionCallName = currentFuction;
            }
            if((allCheckReturnStatements.get(returnStatementFunctionName) != null) && allCheckReturnStatements.get(returnStatementFunctionName))
            {
                currentFunctionCallName = returnStatementFunctionName;
            }
            if(symtable.containsKey(currentFunctionCallName))
            {
                String functionName = (String)((Hashtable<String, Object>)symtable.get(currentFunctionCallName + "Call")).get("functionName");
                Function function = ((Hashtable<String, Function>)symtable.get(currentFunctionCallName)).get(functionName);
                for(int i = 0; i < function.getArgsInfo().size(); i++)
                {
                    if(function.getArgsInfo().get(i).get("argName").equals(((AIdentifierExpression)x).getId().toString().trim()))
                    {
                        xType = (String)function.getArgsInfo().get(i).get("type");
                        findInPar = true;
                    }
                }
            }
            if(symtable.containsKey(x.toString().trim() + "GVar") && !findInPar)
            {
                xType = (String)symtable.get(x.toString().trim() + "GVar");
                line = ((AIdentifierExpression)x).getId().getLine();
            }
            else if(!symtable.containsKey(x.toString().trim() + "GVar") && !findInPar)
            {
                xType = "Error";
            }
        }
        else if(x instanceof AMaxsExpression)
        {
            xType = getExpressionType(((AMaxsExpression)x).getValue());
        }
        else if(x instanceof AMinsExpression)
        {
            xType = getExpressionType(((AMinsExpression)x).getValue());
        }
        else if(x instanceof AArrayExpression)
        {
            xType = getExpressionType((PExpression)(((AArrayExpression)x).getExpression().get(0)));
        }
        else if(x instanceof ANumExpression)
        {
            xType = "INTEGER_LITERAL";
            line = ((ANumExpression)x).getNumber().getLine();
        }
        else if(x instanceof ANoneExpression)
        {
            xType = "None";
            line = ((ANoneExpression)x).getNone().getLine();
        }
        else if(x instanceof AStringExpression)
        {
            xType = "STRING_LITERAL";
            line = ((AStringExpression)x).getString().getLine();
        }
        else if(x instanceof AFunctionCallExpression)
        {
            if(symtable.containsKey(((AIdentifierExpression)((AFunctionCallExpression)x).getIdExp()).getId().toString().trim() + "Call"))
            {
                xType = (String)((Hashtable<String, Object>)symtable.get(((AIdentifierExpression)((AFunctionCallExpression)x).getIdExp()).getId().toString().trim() + "Call")).get("returnType");
            }
            else
            {
                xType = "Unknown";
            }
            line = ((AIdentifierExpression)((AFunctionCallExpression)x).getIdExp()).getId().getLine();
        }
        else
        {
            if(operationQueue.size() > 0)
            {
                xType = operationQueue.pop();
            }
            else
            {
                xType = "";
            }
        }
        return xType;
    }

    public void inADefFunction(ADefFunction node)
	{
        String fName = ((AIdentifierExpression)node.getExpression()).getId().toString().trim();
        currentFuction = fName;
        line = ((AIdentifierExpression)node.getExpression()).getId().getLine();
        column = ((AIdentifierExpression)node.getExpression()).getId().getPos();
    }

    public void outADefFunction(ADefFunction node)
	{
        currentFuction = "";
    }
    
    public void inAFunctionCallExpression(AFunctionCallExpression node)
	{
        boolean findFunction = false;
        inFunctionCallStatement = true;
		LinkedList args = node.getArglistExps();
		TId fCallTId = ((AIdentifierExpression)node.getIdExp()).getId();
        String fCallname = fCallTId.toString().trim();
        allCheckReturnStatements.put(fCallname, false);
        functionCallStack.add(fCallname);
		if(symtable.containsKey(fCallname))
		{
			for(Function function : ((Hashtable<String, Function>)symtable.get(fCallname)).values())
			{
				if(args.size() <= function.getArgs() && args.size() >= (function.getArgs() - function.getDefaultArgs()))
				{
					Hashtable<String, Object> fCallData = new Hashtable<>();
					fCallData.put("functionName", function.getName());
					fCallData.put("returnType", "Unknown");
					symtable.put(fCallname + "Call", fCallData);
					findFunction = true;
				}
			}
			if(!findFunction)
			{
                if(!checkReturnStatementType)
				    print("[" + fCallTId.getLine() + "," + fCallTId.getPos() + "]" + ": " +" Function " + fCallname +" is defined with different number of arguments");
			}
		}
		else
		{
            if(!checkReturnStatementType)
			    print("[" + fCallTId.getLine() + "," + fCallTId.getPos() + "]" + ": " +" Function " + fCallname +" is not defined");
        }
    }
    
    public void outAFunctionCallExpression(AFunctionCallExpression node)
    {
        int count = 0;
        TId fCallTId = ((AIdentifierExpression)node.getIdExp()).getId();
        String callName = fCallTId.toString().trim();
        Function function = null;
        if(symtable.containsKey(callName) && symtable.containsKey(functionCallStack.get(functionCallStack.size()-1) + "Call"))
        {
            function = ((Hashtable<String, Function>)symtable.get(callName)).get(((Hashtable<String, Object>)symtable.get(functionCallStack.get(functionCallStack.size()-1) + "Call")).get("functionName"));
        }
        if(function != null)
        {
            for(PExpression arg : ((LinkedList<PExpression>)node.getArglistExps()))
            {
                function.getArgsInfo().get(count).put("type", getExpressionType(arg));
                count++;
            }
            if(function.getReturnStatement() != null)
            {
                if(function.getReturnStatement().getExpression() instanceof AAdditionExpression
                    || function.getReturnStatement().getExpression() instanceof AMultiplicationExpression
                    || function.getReturnStatement().getExpression() instanceof ADivisionExpression
                    || function.getReturnStatement().getExpression() instanceof AModuloExpression
                    || function.getReturnStatement().getExpression() instanceof APowerExpression)
                {
                    checkReturnStatementType = true;
                    allCheckReturnStatements.put(callName, true);
                    returnStatementFunctionName = callName;
                    caseAAdditionExpression((AAdditionExpression)function.getReturnStatement().getExpression());
                    checkReturnStatementType = false;
                    allCheckReturnStatements.put(callName, false);
                    ((Hashtable<String, Object>)symtable.get(functionCallStack.get(functionCallStack.size()-1) + "Call")).put("returnType", getExpressionType(function.getReturnStatement().getExpression()));
                    
                }
                else
                {
                    checkReturnStatementType = true;
                    allCheckReturnStatements.put(callName, true);
                    returnStatementFunctionName = callName;
                    ((Hashtable<String, Object>)symtable.get(functionCallStack.get(functionCallStack.size()-1) + "Call")).put("returnType", getExpressionType(function.getReturnStatement().getExpression()));
                    checkReturnStatementType = false;
                    allCheckReturnStatements.put(callName, false);
                }
            }
            else
            {
                ((Hashtable<String, Object>)symtable.get(functionCallStack.get(functionCallStack.size()-1) + "Call")).put("returnType", "None");
            }
        }
        inFunctionCallStatement =false;
        functionCallStack.pop();
        if(functionCallStack.size() > 0)
            returnStatementFunctionName = functionCallStack.get(functionCallStack.size()-1);
    }
    
    public void print(Object objectToPrint)
	{
		System.out.println(objectToPrint);
	}
}