# RuleKit
[![GitHub downloads](https://img.shields.io/github/downloads/adaa-polsl/RuleKit/total.svg?style=flag&label=GitHub%20downloads)](https://github.com/adaa-polsl/RuleKit/releases)
[![GitHub Actions CI](../../actions/workflows/main.yml/badge.svg)](../../actions/workflows/main.yml)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPLv3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0.en.html)


Rule-based models are often used for data analysis as they combine interpretability with predictive power. We present RuleKit, a versatile tool for rule learning. Based on a sequential covering induction algorithm, it is suitable for classification, regression, and survival problems. The presence of user-guided induction mode facilitates verifying hypotheses concerning data dependencies which are expected or of interest. The powerful and flexible experimental environment allows straightforward investigation of different induction schemes. The analysis can be performed in batch mode, through RapidMiner plugin, as well as R package and [Python](https://github.com/adaa-polsl/RuleKit-python) packages. A documented Java API is also provided for convenience. 

RuleKit provides all the functionalities included in our previous packages:
* [LR-Rules](https://github.com/adaa-polsl/LR-Rules) (Wróbel et al, 2017) for survival rules induction,
* [GuideR](https://github.com/adaa-polsl/GuideR) (Sikora et al, 2019) for user-guided induction.


# Getting started

In the following subsections we provide a brief introduction on how to install and use RuleKit batch interface, RapidMiner plugin, and R package. The software requires Java Development Kit in version 8 (version 1.8.0 tested) to work properly. In Windows one can download the installer from Oracle webpage. In Linux, a system package manager should be used instead. For instance, in Ubuntu 16.04 execute the following command:
```
sudo apt-get install default-jdk
``` 

## Batch interface

In order to use batch mode, please download *rulekit-\<version\>-all.jar* file from the [releases](../../releases) folder. Alternatively, one can build the package from the sources by running the following commands in the *adaa.analytics.rules* directory of this repository. 
Windows:
```
gradlew -b build.gradle rjar
```
Linux:
```
./gradlew -b build.gradle rjar
```
The JAR file will be placed in *adaa.analytics.rules/build/libs* subdirectory. Once the package has been downloaded/built, the analysis can be performed. The example batch experiment concerns the problem of classifying whether a person making a purchase will be a future customer. The corresponding dataset is named *deals* and is split into train and test parts ([download](data/deals)). To run the experiment, copy RuleKit JAR file into *./examples* folder of the repository and execute:
```
java -jar rulekit-<version>-all.jar minimal-deals.xml
```
Ignore the SLF4J warning reported on the console - it does not affect the procedure. The results of the analysis will be located in *./examples/results-minimal/deals/* folder. Note, that the repository already contains reference results - they will be overwritten. See [this Wiki section](../../wiki/1-Batch-interface) for detailed information on how to configure batch analyses in RuleKit. 

## RapidMiner plugin

In order to use RuleKit RapidMiner plugin, download *rulekit-\<version\>-rmbundle.zip* file from the [releases](../../releases) folder. The archive contains RapidMiner 9.3 bundled with the plugin. The bundle can be also built from the sources by running the following commands in the *adaa.analytics.rules* directory.
Windows:
```
gradlew -b build.gradle rmbundle
```
Linux:
```
./gradlew -b build.gradle rmbundle
```
The output archive will be stored in *adaa.analytics.rules/build/distributions*. After unpacking ZIP file, please execute *RapidMiner-Studio.bat* (Windows) or *RapidMiner-Studio.sh* (Linux) script. Note, that the archive built under Windows may not work on Linux due to different new line characters in the shell script. The opposite situation is not the problem, though. In the releases we provide the archive that works under both systems. 

In the following subsection we show an example regression analysis with a use of the plugin. The investigated dataset is named *methane* and concerns the problem of predicting methane concentration in a coal mine. The set is split into separate testing and training parts distributed in ARFF format ([download](data/methane)). For demonstration needs, a smaller version of these datasets suffixed with *-minimal* have been provided. 

To perform the analysis under RapidMiner, import [./examples/preparation.rmp](/examples/preparation.rmp) process (*File &rarr; Import Process...*) and execute it (*Play* button). Its role is to add metadata to the sets and store them in the RM format (RapidMiner does not support metadata for ARFF files). After loading sets with *Read ARFF*, the *Set Role* operator is used for setting *MM116_pred* as the label attribute (in the survival analysis, a *survival_time* role has to be additionally assigned to some other attribute). Then, the sets are saved in RapidMiner repository under locations *Local Repository/methane-train-minimal* and *Local Repository/methane-test-minimal* with *Store* operators. 

As the next step, please import [./examples/regression.rmp](./examples/regression.rmp) process. After executing it, datasets are loaded from the RM repository with *Retrieve* operators. Then, the training set is provided as an input for *RuleKit Generator*. The model generated by *RuleKit Generator* is then applied on unseen data (*Apply Model* operator). The performance of the prediction is assesed using *RuleKit Evaluator* operator. Performance metrices as well as generated model are passed as process outputs.

See [this Wiki section](../../wiki/2-RapidMiner-plugin) for detailed information how to configure RuleKit RapidMiner plugin. 

## R package


RuleKit is compatible with R 3.4.x or later. In Linux, *curl*, *ssl*, and *xml* system packages are additionally required for RuleKit building. For instance, under Ubuntu 18.04, execute in terminal:
```
sudo apt-get install libcurl4-gnutls-dev
sudo apt-get install libssl-dev
sudo apt-get install libxml2-dev
```
In other distributions, package names may differ slightly. To build RuleKit, please download the *rulekit-\<version\>-all.jar* file from the [releases](../../releases) folder and copy it to the *./r-package/inst/java/* directory of the repository. Then, open *./r-package/rulekit.Rproj* project under RStudio environment and install all required dependencies:
```
install.packages(c('RWeka','XML','caret','rprojroot','devtools'))
``` 
Then, build the package with *Install and Restart* button (the appropiate version of RTools will be downloaded automatically, if it is not present at the target platform). RuleKit will be installed under default R package directory.

Below we present a survival analysis of *BMT-Ch* dataset with RuleKit R package. The set concerns the problem of analyzing factors contributing to the patients’ survival following bone marrow transplants. In order to perform the experiment, please run [./examples/survival.R](./examples/survival.R) script in R. As a result, a rule model is trained and survival function estimates for the entire dataset and for the rules are plotted.
 
[This Wiki section](../../wiki/3-R-package) contains detailed information on using RuleKit R package. 

## Python package

Rulekit Python package can be found [here](https://github.com/adaa-polsl/RuleKit-python)

# Documentation

The detailed RuleKit documentation can be found on [Wiki pages](../../wiki) which cover the following topics: 

1. [Batch interface](../../wiki/1-Batch-interface)
    1. [General information](../../wiki/1-Batch-interface#11-general-information)
    2. [Parameter set definition](../../wiki/1-Batch-interface#12-parameter-set-definition)
    3. [Dataset definition](../../wiki/1-Batch-interface#13-dataset-definition)
    4. [Example](../../wiki/1-Batch-interface#14-example)
2. [RapidMiner plugin](../../wiki/2-RapidMiner-plugin)
	1. [Installation](../../wiki/2-RapidMiner-plugin#21-installation)
	2. [Usage](../../wiki/2-RapidMiner-plugin#22-usage)
	3. [Example](../../wiki/2-RapidMiner-plugin#23-example)
3. [R package](../../wiki/3-R-package)
	1. [Installation](../../wiki/3-R-package#31-installation)
	2. [Usage](../../wiki/3-R-package#32-usage)
	3. [Example](../../wiki/3-R-package#33-example)
4. [Quality and evaluation](../../wiki/4-Quality-and-evaluation)
    1. [Rule quality](../../wiki/4-Quality-and-evaluation#41-rule-quality)
	2. [Model characteristics](../../wiki/4-Quality-and-evaluation#42-model-characteristics)
	2. [Performance metrices](../../wiki/4-Quality-and-evaluation#43-performance-metrices)
5. [Output files](../../wiki/5-Output-files)
    1. [Training report](../../wiki/5-Output-files#51-training-report)
    2. [Prediction performance report](../../wiki/5-Output-files#52-prediction-performance-report)    	
6. [User-guided induction](../../wiki/6-User-guided-induction)
	1. [Defining user's knowledge](../../wiki/6-User-guided-induction#61-defining-users-knowledge)
	2. [Examples from GuideR paper](../../wiki/6-User-guided-induction#62-examples-from-guider-paper)
7. [Library API](../../wiki/7-Library-API)
	1.	[Running an experiment](../../wiki/7-Library-API#71-running-an-experiment)
	2.	[Developing a new algorithm](../../wiki/7-Library-API#72-developing-a-new-algorithm)
8. [Empirical results](../../wiki/8-Empirical-results)

JavaDoc for the project is available [here](https://adaa-polsl.github.io/RuleKit/).  

# Datasets 

The repository contains the datasets used in the GuideR study. We also provide the latest UCI revision of the [*Bone marrow transplant: children*](https://github.com/adaa-polsl/RuleKit/blob/master/data/bone-marrow-uci.arff) dataset. We recommend using this dataset at it contains lots of improvements compared to the previous release (e.g., textual encoding of attribute values). 

# Authors and licensing

RuleKit Development Team:
* [Adam Gudyś](https://github.com/agudys)
* [Łukasz Wróbel](https://github.com/l-wrobel)
* Marek Sikora

Contributors:
* Wojciech Górka
* [Joanna Henzel](https://github.com/AsiaHenzel)
* [Paweł Matyszok](https://github.com/pmatyszok)
* [Wojciech Sikora](https://github.com/Denominatee)

The software is publicly available under [GNU AGPL-3.0 license](LICENSE).
 
# Citing

[Gudyś, A, Sikora, M, Wróbel, Ł (2019) RuleKit: A Comprehensive Suite for Rule-Based Learning, Knowledge-Based Systems, https://doi.org/10.1016/j.knosys.2020.105480](https://doi.org/10.1016/j.knosys.2020.105480)

[Sikora, M, Wróbel, Ł, Gudyś, A (2019) GuideR: a guided separate-and-conquer rule learning in classification, regression, and survival settings, Knowledge-Based Systems, 173:1-14.](https://www.sciencedirect.com/science/article/abs/pii/S0950705119300802?dgcid=coauthor)

[Wróbel, Ł, Gudyś, A, Sikora, M (2017) Learning rule sets from survival data, BMC Bioinformatics, 18(1):285.](https://bmcbioinformatics.biomedcentral.com/articles/10.1186/s12859-017-1693-x) 

