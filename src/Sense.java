import java.util.ArrayList;

/**
 *
 * @author Alessandro
 */
public class Sense {
    private String name;
    private int depth;
    private ArrayList<String> hypernymNames;

    public Sense(String name, ArrayList<String> hypernymNames) {
        this.name = name;
        this.hypernymNames = hypernymNames;
        this.depth = this.hypernymNames.size();
    }

    public int getDepth() {
        return depth;
    }

    public ArrayList<String> getHypernymNames() {
        return hypernymNames;
    }


    
    
}
