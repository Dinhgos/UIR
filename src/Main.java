import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Hlavní třída pro klasifikaci ručně psaných číslic.
 */
public class Main {
    /** název parametrizačního algoritmu - počet bílých pixelů */
    public static final String whitePix = "white_pixels";

    /** název parametrizačního algoritmu - histogram řádků */
    public static final String hisRow = "histogram_row";

    /** název klasifikačního algoritmu - minimální vzdálenost */
    public static final String minDis = "min_distance";

    /** název klasifikačního algoritmu - k-nejbližších sousedů */
    public static final String knn = "k-NN";

    /** cesta k trénovacím množinám */
    private static String trainPath = "mnist_png/training";

    /** cesta k testovacím množinám */
    private static String testPath = "mnist_png/testing";

    /** název modelu */
    private static String modelName;

    /** parametrizačního algoritmu */
    public static String param;

    /** klasifikačního algoritmu */
    public static String clsfr;

    /** cesty k souborům */
    private static ArrayList<String> paths;

    /** data pro knn */
    public static ArrayList<Entity> entities;

    /** data pro minimální vzdálenost */
    public static float[][] clases;

    /**
     * Hlavní algoritmus, který zpracuje vstup a rozhodne jestli se má vztrořit model.
     * @param args parametry vstupu
     */
    public static void main(String[] args) {
        if (args.length == 5) {
            trainPath = args[0];
            testPath = args[1];
            param = args[2];
            clsfr = args[3];
            modelName = args[4];

            paths = getDataPath(trainPath);

            if (param.equals(whitePix)) {
                createEnt();
            } else if(param.equals(hisRow)) {
                createEnt();
            }

            if (clsfr.equals(minDis)) {
                float[][] means = getMean(entities);
                saveMean(means);
            } else if(clsfr.equals(knn)) {
                saveEnt();
            }

            test();
        } else if (args.length == 1) {
            modelName = args[0];
            readFile();

            MyWindow win = new MyWindow();
            win.open();

        } else {
            System.out.println("Wrong input.");
        }
    }

    /**
     * Ohodnotí kvalitu lasifikátoru na vytvořených datech.
     */
    private static void test() {
        paths = getDataPath(testPath);
        readFile();

        int correct = 0;
        int all = paths.size();

        for (String p : paths) {
            int[] v = getVector(p);
            int cor = Integer.parseInt(p.split("/")[2]);

            int result;
            int len = 1;

            if (param.equals(hisRow)) {
                len = 28;
            }

            if (clsfr.equals(minDis)) {
                result = minDis(clases, v, len);
            } else {
                result = knn(entities, v);
            }

            if (result == cor) {
                correct++;
            }
        }

        double acc = ((double) correct/all) * 100;
        System.out.printf("Accuracy: %.2f%s%n", acc, "%");
    }

    /**
     * Přečte vytvořený model a nastaví jednotlivá data.
     */
    private static void readFile() {
        float[][] mean;

        try {
            File myObj = new File(modelName + ".txt");
            Scanner myReader = new Scanner(myObj);

            String method = myReader.nextLine();
            String[] strings = method.split(";");

            int len = -1;

            if (strings[1].equals(whitePix)) {
                param = whitePix;
                len = 1;
            } else if (strings[1].equals(hisRow)) {
                param = hisRow;
                len = 28;
            }

            if (strings[0].equals(minDis)) {
                clsfr = minDis;
                mean = createMean(myReader, len);
                clases = mean;
            } else if (strings[0].equals(knn)) {
                clsfr = knn;
                createKnn(myReader, len);
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Načte data pro k-nn ze souboru.
     * @param myReader data ze souboru.
     * @param len délka vektoru.
     */
    private static void createKnn(Scanner myReader, int len) {
        entities = new ArrayList<>();

        while (myReader.hasNextLine()) {
            String label = myReader.nextLine();
            String vector = myReader.nextLine();
            String[] v = vector.split(";");
            int[] iv = new int[len];

            for (int i = 0; i < len; i++) {
                iv[i] = Integer.parseInt(v[i]);
            }

            Entity e = new Entity(label, iv);
            entities.add(e);
        }
    }

    /**
     * Načte data pro minimální vzdálenost ze souboru.
     * @param myReader data ze souboru.
     * @param len délka vektoru.
     * @return středy jednotlivých tříd.
     */
    private static float[][] createMean(Scanner myReader, int len) {
        float[][] mean = new float[10][len];
        int counter = 0;

        while (myReader.hasNextLine()) {
            myReader.nextLine();
            String vector = myReader.nextLine();
            String[] v = vector.split(";");

            for (int i = 0; i < len; i++) {
                mean[counter][i] = Float.parseFloat(v[i]);
            }

            counter++;
        }

        return mean;
    }

    /**
     * Uloží knn data do souboru.
     */
    private static void saveEnt() {
        String fileName = modelName + ".txt";
        createFile(fileName);

        try {
            FileWriter myWriter = new FileWriter(fileName);

            myWriter.write(clsfr + ";" + param + "\n");

            for (Entity e : entities) {
                myWriter.write(e.getLabel() + "\n");
                int[] v = e.getVector();

                for (int i : v) {
                    myWriter.write(i + ";");
                }

                myWriter.write("\n");
            }

            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Uloží středy jednotlivých tříd do souboru.
     * @param means středy jednotlivých tříd.
     */
    private static void saveMean(float[][] means) {
        String fileName = modelName + ".txt";
        createFile(fileName);
        try {
            FileWriter myWriter = new FileWriter(fileName);

            myWriter.write(clsfr + ";" + param + "\n");

            for (int i = 0; i < 10; i++) {
                myWriter.write(i + "\n");

                for (int j = 0; j < means[0].length; j++) {
                    myWriter.write(means[i][j] +";");
                }

                myWriter.write("\n");
            }

            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Vytvoří txt soubor.
     * @param name název souboru.
     */
    private static void createFile(String name) {
        try {
            File myObj = new File(name);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Vytvoři entitu z dat pro zpracování.
     */
    private static void createEnt() {
        entities = new ArrayList<>();

        for (String path : paths) {
            String[] label = path.split("/");
            int[] v = getVector(path);

            Entity e = new Entity(label[2],v);
            entities.add(e);
        }
    }

    /**
     * Vytvoří vektor z obrázku.
     * @param path cesta k obrázku.
     * @return vektor.
     */
    public static int[] getVector(String path) {

        int[] u = new int[28];
        BufferedImage img = null;

        try {
            img = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println("failed to load data");
        }

        for (int y = 0; y < img.getHeight(); y++) {
            int[] v = new int[28];

            for (int x = 0; x < img.getWidth(); x++) {
                int color = img.getRGB(x, y);

                int blue = color & 0xff;
                int green = (color & 0xff00) >> 8;
                int red = (color & 0xff0000) >> 16;

                if (blue != 0 && green != 0 && red !=0) {
                    v[x] = 1;
                }
            }

            u[y] = IntStream.of(v).sum();
        }

        if (param.equals(whitePix)) {
            return new int[] {IntStream.of(u).sum()}; // white
        } else if (param.equals(hisRow)) {
            return u; // his
        }

        return null;
    }

    /**
     * Algoritmus k-nejbližších sousedů.
     * @param entities natrénovaná data.
     * @param t vstupní vektor.
     * @return výsledek kvalifikace.
     */
    public static int knn(ArrayList<Entity> entities, int[] t) {
        double[] dis = new double[] {Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE};
        String[] labels = new String[3];
        int len = 1;

        if (param.equals(hisRow)) {
            len = 28;
        }

        for (Entity e : entities) {
            double ldis = getDistance(t, e.getVector(), len);

            for (int i = 0; i < 3; i++) {
                if (ldis < dis[i]) {
                    dis[i] = ldis;
                    labels[i] = e.getLabel();
                }
            }
        }

        int[] res = new int[10];

        for (String s : labels) {
            int i = Integer.parseInt(s);
            res[i]++;
        }

        int result = -1;
        int max = -1;

        for (int i = 0; i < res.length; i++) {
            if (res[i] > max) {
                max = res[i];
                result = i;
            }
        }

        return result;
    }

    /**
     * Minimální vzdálenost mezi vstupním prvkem a jednotlivých tŕíd.
     * @param means těžiště tříd.
     * @param t vstupní vektor.
     * @param l délka vektoru.
     * @return třida s nejmenší vzdáleností.
     */
    public static int minDis(float[][] means, int[] t, int l) {
        int index = -1;
        double min = Integer.MAX_VALUE;

        for (int i = 0; i < 10; i++) {
            double dis = getDistance(means[i],t,l);

            if (dis < min) {
                index = i;
                min = dis;
            }
        }

        return index;
    }

    /**
     * Euclidovská vzdálenost mezi vektory.
     * @param mean vektor1.
     * @param t vektor2.
     * @param l délka obou vektorů.
     * @return vzdálenost.
     */
    private static double getDistance(float[] mean, int[] t,int l) {
        double sqrt = 0;

        for (int i = 0; i < l; i++) {
            sqrt += (mean[i] - t[i]) * (mean[i] - t[i]);
        }

        return Math.sqrt(sqrt);
    }

    /**
     * Euclidovská vzdálenost mezi vektory.
     * @param u vektor1.
     * @param t vektor2.
     * @param l délka obou vektorů.
     * @return vzdálenost.
     */
    private static double getDistance(int[] u, int[] t, int l) {
        double sqrt = 0;

        for (int i = 0; i < l; i++) {
            sqrt += (u[i] - t[i]) * (u[i] - t[i]);
        }

        return Math.sqrt(sqrt);
    }

    /**
     * Vytvoří těžiště jednotlivých tříd.
     * @param entities data.
     * @return těžiště jednotlivých tříd.
     */
    private static float[][] getMean(ArrayList<Entity> entities) {
        int len = entities.get(0).getVector().length;
        float[][] sum = new float[10][len];
        float[][] m = new float[10][len];

        for (Entity e : entities) {
            int index = Integer.parseInt(e.getLabel());
            int[] v = e.getVector();

            for (int i = 0; i < len; i++) {
                sum[index][i] += v[i];
                m[index][i]++;
            }
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < len; j++) {
                sum[i][j] /= m[i][j];
            }
        }

        return sum;
    }

    /**
     * Najde cesty k jednotlivým obrázkům.
     * @param path cesta k trénovacím/testovacím množinám.
     * @return cesty k obrázkům.
     */
    private static ArrayList<String> getDataPath(String path) {
        ArrayList<String> pathList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String[] pathnames;

            // Creates a new File instance by converting the given pathname string
            // into an abstract pathname
            File f = new File(path + "/" + i);

            // Populates the array with names of files and directories
            pathnames = f.list();

            // For each pathname in the pathnames array
            for (String pathname : pathnames) {

                // Print the names of files and directories
                pathList.add(path + "/" + i + "/" + pathname);
            }
        }

        return pathList;
    }
}
