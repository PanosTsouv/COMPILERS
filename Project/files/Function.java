import java.util.ArrayList;
import java.util.Hashtable;

public class Function {
    private String name;
    private int args;
    private int defaultArgs;
    private ArrayList<Hashtable<String, Object>> argsInfo;

    public Function()
    {
        this.name = "";
        this.args = 0;
        this.defaultArgs = 0;
        this.argsInfo = new ArrayList<>();
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

    @Override
    public String toString() { 
        return String.format("**name : " + this.name + " args : " + this.args + " defaultArgs : " + this.defaultArgs
        + " argsInfo : " + this.argsInfo + "**"); 
    }
}
