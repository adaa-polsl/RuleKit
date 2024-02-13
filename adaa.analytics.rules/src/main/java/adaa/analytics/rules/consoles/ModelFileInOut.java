package adaa.analytics.rules.consoles;

import adaa.analytics.rules.logic.representation.model.RuleSetBase;

import java.io.*;


public class ModelFileInOut {


    public static RuleSetBase read(String filePath) throws  IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filePath);
        ObjectInputStream objectIn = new ObjectInputStream(fis);
        RuleSetBase model = (RuleSetBase) objectIn.readObject();
        objectIn.close();
        return model;
    }


    public static void write(RuleSetBase model, String filePath) throws  IOException {
        File modelFile = new File(filePath);
        ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(modelFile));
        objectOut.writeObject(model);
        objectOut.close();
    }

}
