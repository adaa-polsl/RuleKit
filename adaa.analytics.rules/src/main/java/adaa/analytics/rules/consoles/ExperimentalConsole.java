/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package adaa.analytics.rules.consoles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import adaa.analytics.rules.consoles.config.*;

import org.apache.commons.cli.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import adaa.analytics.rules.logic.representation.Logger;


/**
 * Console used in batch mode.
 *
 * @author Adam Gudys
 */
public class ExperimentalConsole {

    boolean logsVerbose = false;

    boolean logsVeryVerbose = false;
    protected int experimentalThreads = 1;

    private String configFile;

    private ExecutorService pool;
    public static void main(String[] args) {
        ExperimentalConsole console = new ExperimentalConsole();
        console.printVersionInfo();
        if (console.parseCliArgs(args)) {
            console.initializeAndRun();
        }
    }

    public void printVersionInfo() {
        VersionService versionService = new VersionService();
        System.out.print(versionService.getHeader());
    }

    public void initializeAndRun() {
        try {
            initLogs();

            Document doc = readXmlDocument(this.configFile);
            Logger.log("Loading XML experiment file: " + configFile, Level.INFO);

            //Paramset
            List<ParamSetConfiguration> paramSets = ParamSetConfiguration.readParamSetConfigurations(doc);

            // Dataset
            List<DatasetConfiguration> datasetConfigurationList = DatasetConfiguration.readConfigurations(doc);

            List<Future> futures = executeExperiments(paramSets, datasetConfigurationList);

            waitForResultsAndQuit(futures);
        } catch (IOException | ParserConfigurationException | SAXException | InterruptedException |
                 ExecutionException e) {
            if (logsVerbose) {
                e.printStackTrace();
            } else {
                Logger.log(e.getMessage() + "\n", Level.SEVERE);
            }
        }
    }

    private boolean parseCliArgs(String[] args) {
        Options options = new Options();
        options.addOption("v", "v", false, "verbose mode");
        options.addOption("n", "vv", false, "very verbose mode");
        options.addOption("e", "exp-threads", true, "number of experimental threads (default 1)");
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("v")) {
                this.logsVerbose = true;
            }

            if (cmd.hasOption("vv")) {
                this.logsVerbose = true;
                this.logsVeryVerbose = true;
            }

            this.experimentalThreads = Integer.parseInt(cmd.getOptionValue("exp-threads", "1"));
            List<String> unparsedArgs = cmd.getArgList();
            if (unparsedArgs.size() == 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar RuleKit.jar", options);
                return false;
            }
            configFile = unparsedArgs.get(0);
            return true;
        } catch (Exception exp) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar RuleKit.jar", options);
            return false;
        }
    }

    private void initLogs() {
        Logger.getInstance().addStream(System.out, logsVeryVerbose ? Level.FINEST : (logsVerbose ? Level.FINE : Level.INFO));
    }

    private Document readXmlDocument(String configFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(configFile);
        return doc;
    }

    private List<Future> executeExperiments(List<ParamSetConfiguration> paramSets, List<DatasetConfiguration> datasetConfigurationList) throws ParserConfigurationException, SAXException, IOException, InterruptedException, ExecutionException {
        List<Future> futures = new ArrayList<>();
        String lineSeparator = System.getProperty("line.separator");
        pool = Executors.newFixedThreadPool(experimentalThreads);

        for (DatasetConfiguration dc : datasetConfigurationList) {
            // create experiments for all params sets
            for (ParamSetConfiguration wrapper : paramSets) {

                String outDirPath = dc.outDirectory + "/" + wrapper.getName();

                //noinspection ResultOfMethodCallIgnored
                new File(outDirPath).mkdirs();

                Logger.log("Creating new TrainTestValidationExperiment" + lineSeparator + "outDirPath = " + outDirPath + lineSeparator + "trainingReportPathFile = " + dc.trainingReportFilePath + lineSeparator + "predictionReportPathFile = " + dc.predictionPerformanceFilePath + lineSeparator, Level.FINE);

                TrainTestValidationExperiment ttValidationExp = new TrainTestValidationExperiment(dc, wrapper, outDirPath, logsVerbose);
                Future f = pool.submit(ttValidationExp);
                futures.add(f);
            }
        }
        return futures;
    }

    private void waitForResultsAndQuit(List<Future> futures) throws ExecutionException, InterruptedException {
        for (Future f : futures) {
            f.get();
        }
        pool.shutdown();
        Logger.log("Experiments finished", Level.INFO);
    }


}
