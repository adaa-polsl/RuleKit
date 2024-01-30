package adaa.analytics.rules.experiments;

import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;

import java.io.*;


public class ModelFileInOut {


    public static Model read(String filePath) throws OperatorException, IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filePath);
        ObjectInputStream objectIn = new ObjectInputStream(fis);
        Model model = (Model) objectIn.readObject();
        objectIn.close();
        return model;
    }


    public static void write(Model model, String filePath) throws OperatorException, IOException {
        File modelFile = new File(filePath);
        ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(modelFile));
        objectOut.writeObject(model);
        objectOut.close();
    }

}
