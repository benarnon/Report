import java.util.List;

/**
 * Created by ran on 30/07/15.
 */
public class DB {
    private String id , name , type;
    private Location location;
    private List<GeneUnit> geneUnits;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<GeneUnit> getGeneUnits() {
        return geneUnits;
    }

    public void setGeneUnits(List<GeneUnit> geneUnits) {
        this.geneUnits = geneUnits;
    }
}
