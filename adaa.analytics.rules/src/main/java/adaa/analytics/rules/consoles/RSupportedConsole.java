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

import adaa.analytics.rules.experiments.*;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.operator.ExpertRuleGenerator;

import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attributes;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class RSupportedConsole {

    protected class ParamSetWrapper {
        String name;
        Map<String, Object> map = new TreeMap<String, Object>();
    }

    public static void main(String[] args) {
        try {
            if (args.length == 1) {

                RSupportedConsole console = new RSupportedConsole();
                console.execute(args[0]);

            } else {
                throw new IllegalArgumentException("Please specify two arguments");
            }

        } catch (IOException | ParserConfigurationException | SAXException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    protected void execute(String configFile) throws ParserConfigurationException, SAXException, IOException, InterruptedException, ExecutionException {
        RapidMiner.init();
        Logger.getInstance().addStream(System.out, Level.FINE);
        //Logger.getInstance().addStream(new PrintStream("d:/bad.log"), Level.FINEST);
        String lineSeparator = System.getProperty("line.separator");

        int threadCount = 1;//Runtime.getRuntime().availableProcessors();

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        List<Future> futures = new ArrayList<Future>();

        List<ParamSetWrapper> paramSets = new ArrayList<ParamSetWrapper>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = (Document) dBuilder.parse(configFile);

        NodeList paramSetNodes = ((Document) doc).getElementsByTagName("parameter_set");

        for (int setId = 0; setId < paramSetNodes.getLength(); setId++) {
            ParamSetWrapper wrapper = new ParamSetWrapper();
            Element setNode = (Element) paramSetNodes.item(setId);
            wrapper.name = setNode.getAttribute("name");
            Logger.getInstance().log("Reading parameter set " + setNode.getAttribute("name")
                    + lineSeparator, Level.INFO);
            NodeList paramNodes = setNode.getElementsByTagName("param");

            for (int paramId = 0; paramId < paramNodes.getLength(); ++paramId) {
                Element paramNode = (Element) paramNodes.item(paramId);
                String name = paramNode.getAttribute("name");

                String[] expertParamNames = new String[]{
                        ExpertRuleGenerator.PARAMETER_EXPERT_RULES,
                        ExpertRuleGenerator.PARAMETER_EXPERT_PREFERRED_CONDITIONS,
                        ExpertRuleGenerator.PARAMETER_EXPERT_FORBIDDEN_CONDITIONS
                };

                // parse expert rules/conditions
                boolean paramProcessed = false;
                for (String expertParamName : expertParamNames) {
                    if (name.equals(expertParamName)) {
                        List<String[]> expertRules = new ArrayList<String[]>();
                        NodeList ruleNodes = paramNode.getElementsByTagName("entry");

                        for (int ruleId = 0; ruleId < ruleNodes.getLength(); ++ruleId) {
                            Element ruleNode = (Element) ruleNodes.item(ruleId);
                            String ruleName = ruleNode.getAttribute("name");
                            String ruleContent = ruleNode.getTextContent();
                            expertRules.add(new String[]{ruleName, ruleContent});
                        }
                        wrapper.map.put(expertParamName, expertRules);
                        paramProcessed = true;
                    }
                }

                if (!paramProcessed) {
                    String value = paramNode.getTextContent();
                    wrapper.map.put(name, value);
                }
            }

            paramSets.add(wrapper);
        }
        Logger.getInstance().log("Processing datasets" + lineSeparator, Level.INFO);
        NodeList datasetNodes = ((Document) doc).getElementsByTagName("dataset");
        for (int datasetId = 0; datasetId < datasetNodes.getLength(); datasetId++) {
            Logger.getInstance().log("Processing dataset" + datasetId + lineSeparator, Level.INFO);
            Element node = (Element) datasetNodes.item(datasetId);

            String name = node.getAttribute("name");
            String path = node.getElementsByTagName("path").item(0).getTextContent();
            String label = node.getElementsByTagName("label").item(0).getTextContent();
            String reportPath = node.getElementsByTagName("report_path").item(0).getTextContent();

            Map<String, String> options = new HashMap<String, String>();
            if (node.getElementsByTagName(SurvivalRule.SURVIVAL_TIME_ROLE).getLength() > 0) {
                String val = node.getElementsByTagName(SurvivalRule.SURVIVAL_TIME_ROLE).item(0).getTextContent();
                options.put(SurvivalRule.SURVIVAL_TIME_ROLE, val);
            }

            if (node.getElementsByTagName(Attributes.WEIGHT_NAME).getLength() > 0) {
                String val = node.getElementsByTagName(Attributes.WEIGHT_NAME).item(0).getTextContent();
                options.put(Attributes.WEIGHT_NAME, val);
            }

            Logger.log("Name " + name + lineSeparator +
                    "Path " + path + lineSeparator +
                    "Label " + label + lineSeparator +
                    "Report path " + reportPath + lineSeparator, Level.INFO);

            // create experiments for all params sets
            for (ParamSetWrapper wrapper : paramSets) {
                String paramString = "";

                if (wrapper.name.length() > 0) {
                    paramString += ", " + wrapper.name;

                } else {
                    for (String key : wrapper.map.keySet()) {
                        Object o = wrapper.map.get(key);
                        if (o instanceof String) {
                            paramString += ", " + key + "=" + wrapper.map.get(key);

                        }
                    }
                }
                File file = new File(path);

                String qualityReport = reportPath + "/" + name + paramString + ".csv";
                String modelReport = reportPath + "/" + name + paramString + ".res";

                ExperimentBase exp = null;
                exp = new GeneralExperiment(file, new SynchronizedReport(qualityReport), new SynchronizedReport(modelReport), label, options, wrapper.map);
                Future f = pool.submit(exp);
                futures.add(f);
            }
        }
        Logger.getInstance().log("Finished processing datasets" + lineSeparator, Level.INFO);

        for (Future f : futures) {
            f.get();
        }

        Logger.getInstance().log("Experiments finished", Level.INFO);
        RapidMiner.quit(RapidMiner.ExitMode.NORMAL);
    }
}
