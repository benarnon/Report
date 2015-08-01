import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYImageAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CreateReport {
    int iExitValue;
    String sCommandString;

    public static final String CSV_FILES_PATH = "/home/ran/WWH-results/app/resources/outputs";
    public static final String DB_PROPERTIES_PATH = "/home/ran/WWH-results/app/resources/dbproperties.json";
    //public static final String GLOBAL_REUDCER_RESULTS = "~/WWH-results/app/resources/part-r-00000 ";

    public static final String SERVER_PATH = "/home/ran/WWH-results/app";


    private void drawOutput(String csvsPath) throws FileNotFoundException {
        File[] sourceFolderList = new File(csvsPath).listFiles();
        for (File dbDir : sourceFolderList) {
            if (dbDir.isDirectory()){
                String Dbname = dbDir.getName();
                System.out.println("Working on cluster " + Dbname );
                String outDbPath = SERVER_PATH +"/" + Dbname;
                new File(outDbPath).mkdirs();
                File[] csvFiles = dbDir.listFiles();
                for (File csvFile : csvFiles) {
                    String filename = csvFile.getName().replace(".csv" , "");
                    System.out.println("Working on gene-unit " + filename);
                    String outGenePath = outDbPath + "/" +filename;
                    File outGeneDir = new File(outGenePath);
                    outGeneDir.mkdir();

                    BufferedReader br = null;
                    String line = "";
                    String cvsSplitBy = ",";
                    try {
                        br = new BufferedReader(new FileReader(csvFile));
                        br.readLine();
                        //parameters[0]-Depth , parameters[1]-coverage parameters[2]-integral parameters[3]-numofreads
                        String[] parameters = br.readLine().split(cvsSplitBy);
                        DefaultTableXYDataset dataSet = prepareDataFromBr(br);

                        String newFileOutPath = outGenePath+"/"+filename;
                        //create and print the chart to jpeg
                        JFreeChart chart = ChartFactory.createStackedXYAreaChart("Coverage Plot" , "Position" , "Base Count" , dataSet);
                        chart.addSubtitle(new TextTitle("Depth- " + parameters[0].substring(0,parameters[0].length()-9) + ". Coverage- " + parameters[1].substring(0,parameters[1].length()-9) + "%. num of reads- "+ parameters[3] + ". Integral- "+ parameters[2]));
                        try {
                            ChartUtilities.saveChartAsJPEG(new File(newFileOutPath+"_hist.jpeg"), chart, 500, 300);
                        } catch (IOException e) {
                            System.err.println("Problem occurred creating chart.");
                        }

                        int depth = (int) Math.round(Double.parseDouble(parameters[0])*100);
                        //Draw the depth circle
                        // Open a JPEG file, open a new buffered image
                        File imgF = new File(newFileOutPath+"_circ.jpeg");
                        BufferedImage img = new BufferedImage(depth+10 , depth+10 , BufferedImage.TYPE_INT_RGB);

                        // Obtain the Graphics2D context associated with the BufferedImage.
                        Graphics2D g = img.createGraphics();
                        g.setColor(new Color(77,153,255));
                        g.setBackground(new Color(255, 255, 204));
                        g.fillOval(5, 5, depth, depth);
                        ImageIO.write(img, "jpeg", imgF);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

        }
        System.out.println("Finisn create jpeg");
    }

    private List<DB> createDBobj(String csvsPath) throws IOException {
        //initialize available databases
        Gson gson = new Gson();
        List<DB> dbs = new ArrayList<DB>();
        Type type = new TypeToken<List<DB>>(){}.getType();
        dbs = gson.fromJson(readFile(DB_PROPERTIES_PATH) , type);

        //add geneUnits to json
        File[] sourceFolderList = new File(csvsPath).listFiles();
        for (File dbDirectory : sourceFolderList) {
            if (dbDirectory.isDirectory()) {
                String dbFolderName = dbDirectory.getName();
                boolean checkFolder = !dbFolderName.startsWith("images") &&  !dbFolderName.startsWith("resources") && !dbFolderName.startsWith("scripts") &&  !dbFolderName.startsWith("styles") && !dbFolderName.startsWith("views");
                if (checkFolder) {
                    String dbId = dbFolderName;
                    DB tempDb = getDbWithId(dbId, dbs);
                    List<GeneUnit> tempGeneUnits = getGeneUnitsFromFolder(dbDirectory);
                    if (tempDb!=null)
                        tempDb.setGeneUnits(tempGeneUnits);
                }
            }
        }
        System.out.println("Finish create Json file");
        return dbs;
    }

    private List<GeneUnit> getGeneUnitsFromFolder(File dbDirectory) {
        List<GeneUnit> geneUnits = new ArrayList<GeneUnit>();
        File[] geneDirs = dbDirectory.listFiles();
        for (File geneDir : geneDirs) {
            String filename = geneDir.getName().replace(".csv","");
            GeneUnit tempGeneUnit = new GeneUnit(filename);
            tempGeneUnit.setDescription(filename+"_desc");
            geneUnits.add(tempGeneUnit);
        }
        return geneUnits;
    }

    private DB getDbWithId(String dbId , List<DB> dbs) {
        for (DB db : dbs) {
            if (db.getId().equals(dbId))
                return db;
        }
        return null;
    }

    private String readFile(String path) throws IOException{
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    private DefaultTableXYDataset prepareDataFromBr(BufferedReader br) {

        XYSeries plusSet = new XYSeries("+",false , false);
        XYSeries minusSet = new XYSeries("-",false , false);
        DefaultTableXYDataset dataSet = new DefaultTableXYDataset();
        String line = "";
        String cvsSplitBy = ",";
        try {
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(cvsSplitBy);
                Double position = Double.parseDouble(values[0]);
                Double basecount = Double.valueOf(values[1]);
                if (values[2].equals("+"))
                    plusSet.add(position , basecount);
                else if (values[2].equals("-"))
                    minusSet.add(position,basecount);
            }
            dataSet.addSeries(plusSet);
            dataSet.addSeries(minusSet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataSet;
    }

    public static void main(String[] args) throws IOException {
        //HistogramParser.parse(GLOBAL_REUDCER_RESULTS, CSV_FILES_PATH);
        CreateReport createReport = new CreateReport();
        createReport.drawOutput(CSV_FILES_PATH);
        List<DB> dbs = createReport.createDBobj(CSV_FILES_PATH);
        WWHClusters clusters = new WWHClusters(dbs);
        Gson gson = new Gson();
        String json = gson.toJson(clusters);
        PrintWriter p = new PrintWriter(new File(SERVER_PATH+"/testJson.json"));
        p.print(json);
        p.close();
    }




}
