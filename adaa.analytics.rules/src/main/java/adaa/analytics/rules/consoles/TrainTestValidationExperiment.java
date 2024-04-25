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

import adaa.analytics.rules.consoles.config.DatasetConfiguration;
import adaa.analytics.rules.consoles.config.ParamSetConfiguration;
import adaa.analytics.rules.logic.representation.Logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class TrainTestValidationExperiment implements Runnable {

    private TrainProcess trainProcess;

    private TestProcess testProcess;

    private boolean logsVerbose = false;

    public TrainTestValidationExperiment(DatasetConfiguration datasetConfiguration, ParamSetConfiguration paramSet,
                                         String outDirPath, boolean verbose) throws IOException {
        VersionService versionService = new VersionService();

        File f = new File(outDirPath);
        String finalOutDirPath = f.isAbsolute() ? outDirPath : (System.getProperty("user.dir") + "/" + outDirPath);
        this.logsVerbose = verbose;

        SynchronizedReport trainingReport = new SynchronizedReport(outDirPath, datasetConfiguration.trainingReportFilePath, versionService.getHeader());
        this.trainProcess = new TrainProcess(datasetConfiguration, paramSet, trainingReport, finalOutDirPath);

        SynchronizedReport performanceTable = new SynchronizedReport(outDirPath, datasetConfiguration.predictionPerformanceFilePath, versionService.getSimpleHeader());
        SynchronizedReport testingReport = new SynchronizedReport(outDirPath, datasetConfiguration.testingReportFilePath, versionService.getHeader());
        this.testProcess = new TestProcess(datasetConfiguration, paramSet, testingReport, performanceTable, finalOutDirPath);
    }

    @Override
    public void run() {

        try {

            this.trainProcess.executeProcess();
            this.testProcess.executeProcess();

        } catch (Exception e) {
            if (this.logsVerbose) {
                e.printStackTrace();
            } else {
                Logger.log(e.getMessage() + "\n", Level.SEVERE);
            }

        }
    }
}
