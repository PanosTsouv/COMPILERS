import java.util.ArrayList;
import java.util.Hashtable;

import minipython.node.ADefFunction;
import minipython.node.AReturnStatement;

public class Function {
    private String name;
    private int args;
    private int defaultArgs;
    private ArrayList<Hashtable<String, Object>> argsInfo;
    private AReturnStatement returnStatement;
    private ADefFunction node;

    public Function()
    {
        this.name = "";
        this.args = 0;
        this.defaultArgs = 0;
        this.argsInfo = new ArrayList<>();
        this.returnStatement = null;
    }

    public String getName()
    {
        return this.name;
    }

    public int getArgs()
    {
        return this.args;
    }

    public int getDefaultArgs()
    {
        return this.defaultArgs;
    }

    public ArrayList<Hashtable<String, Object>> getArgsInfo()
    {
        return this.argsInfo;
    }

    public AReturnStatement getReturnStatement()
    {
        return this.returnStatement;
    }

    public ADefFunction getADefFunction()
    {
        return this.node;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setArgs(int args)
    {
        this.args = args;
    }

    public void setDefaultArgs(int defaultArgs)
    {
        this.defaultArgs = defaultArgs;
    }

    public void setArgsInfo(ArrayList<Hashtable<String, Object>> argsInfo)
    {
        this.argsInfo = argsInfo;
    }

    public void setReturnStatement(AReturnStatement returnStatement)
    {
        this.returnStatement = returnStatement;
    }

    public void setADefFunction(ADefFunction node)
    {
        this.node = node;
    }

    @Override
    public String toString() { 
        return String.format("**name : " + this.name + " args : " + this.args + " defaultArgs : " + this.defaultArgs
        + " argsInfo : " + this.argsInfo + "**"); 
    }
}
