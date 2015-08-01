import java.util.List;

/**
 * Created by ran on 30/07/15.
 */
public class WWHClusters {
    private List<DB> Clusters;

    public List<DB> getClusters() {
        return Clusters;
    }

    public void setClusters(List<DB> clusters) {
        Clusters = clusters;
    }

    public WWHClusters(List<DB> clusters) {
        Clusters = clusters;
    }
}
