.setup <- function(train_data,test_data,formula) {
  dir.create(main_dir <- file.path(tempdir(), "experiment"))
  dir.create(experiments_dir <- file.path(main_dir, "experiments"))
  dir.create(reports_dir <- file.path(main_dir, "reports"))
  dir.create(datasets_dir <- file.path(main_dir, "datasets"))
  config_file <- tempfile(pattern = "experiment", tmpdir = experiments_dir, fileext = ".xml")
  if (!is.data.frame(train_data)) {
    for (i in seq_along(train_data)) {
      train_file_name <- paste0("train_",i,".arff")
      train_file <- file.path(datasets_dir,train_file_name)
      test_file_name <- paste0("test_",i,".arff")
      test_file <- file.path(datasets_dir,test_file_name)
      train_frame <- model.frame(formula, train_data[[i]])
      RWeka::write.arff(train_frame, train_file)
      test_frame <- model.frame(formula, test_data[[i]])
      RWeka::write.arff(test_frame, test_file)
    }
  } else {
    train_file <- file.path(datasets_dir, "train_1.arff")
    test_file <- file.path(datasets_dir, "test_1.arff")
    RWeka::write.arff(train_data, train_file)
    RWeka::write.arff(test_data, test_file)
  }
  list <- list("reports" = reports_dir, "sets" = datasets_dir, "config" = config_file)
  return(list)
}

.xmlCreateParameterSet <- function(xml,control) {
  xml$addTag("parameter_set", attrs = c(name="experiment1"), close = FALSE)
  names <- names(control)
  for (i in 1: length(control)) {
    list_type <- is.list(control[[i]])
    if (list_type){
      xml$addTag("param" , attrs = c(name=names[i]), close =  FALSE)
      list <- control[[i]]
      names_list <- names(list)
      for (j in 1:length(list)){
        xml$addTag("entry", list[[j]], attrs = c(name=names_list[j]), close =  TRUE)
      }
      xml$closeTag()
    } else {
      xml$addTag("param" , control[[i]] , attrs = c(name=names[i]), close =  TRUE)
    }
  }
  xml$closeTag()
  return(xml)
}

.getType <- function(data, label) {
  if (!is.data.frame(data)){
    temp <- data[[1]]
  } else {
    temp <- data
  }
  type <- ""
  if (length(temp[label]) > 1)
    return("Survival")
  attribute <- temp[label][,1]
  if (is.factor(attribute)){
    if (length(levels(attribute)) > 2) {
      type <- "Classification"
    } else {
      type <- "BinaryClassification"
    }
  } else if (is.numeric(attribute)) {
    type <- "Regression"
  } else {
    stop("Invalid data type for label. Expecting factor,numeric or surv, was", class(attribute))
  }
  return(type)
}

.getResults <- function(reports) {
  dir <- "/experiment1/"
  csv_dir <- file.path(paste0(reports, dir),"performance.csv")
  res_dir <- file.path(paste0(reports, dir),"training-log.txt")
  result_frame <-read.csv(csv_dir, header = TRUE, skip = 1)
  text <- readLines(res_dir)
  return(list(result_frame, text))
}

.checkTypes <- function(formula,train_data, test_data = train_data, control) {
  if (class(formula) != 'formula')
    stop("Invalid data type for formula. Expecting formula, was ",class(formula))
  if (!(class(train_data) == 'data.frame' || class(train_data) == 'list'))
    stop("Invalid data type for train_data. Expecting list or data.frame, was ",class(train_data))
  if (!(class(test_data) == 'data.frame' || class(test_data) == 'list'))
    stop("Invalid data type for train_data. Expecting list or data.frame, was ",class(train_data))
  if (class(control) != 'list')
    stop("Invalid data type for control. Expecting list, was ",class(control))
}

#' Execution of rule induction algorithm.
#'
#' @param formula Formulae specifing label attribute and attributes included in rule induction (e.g. class ~ .)
#' @param control Named list with parameters for algorithm.
#' @param train_data Data frame or list of data frames with training data.
#' @param test_data Data frame or list of data frames with test data. If not specified is equals to train_data.
#' @return List containing data frame with performance statistics for each data set and character vector with generated rules.
#' @examples
#' control <- list(min_rule_covered = 11, induction_measure = 'C2', pruning_measure = 'C2', max_growing = 0)
#' train_data <- iris
#' formula <- Species ~ .
#'
#' results <- learn_rules(formula, control, train_data)
learn_rules <- function(formula, control, train_data, test_data = train_data) {
  if (is.null(control)) {
    control <- list(min_rule_covered = 5, max_uncovered_fraction = 0, max_growing = 0, induction_measure = 'Correlation',
                    pruning_measure = 'Correlation', voting_measure = 'Correlation', ignore_missing = FALSE)
  }
  jar_dir <- system.file('java',package = "adaa.rules")
  jar_file <- list.files(jar_dir,pattern = '.jar')
  if (length(jar_file)>1){
    stop("Expected single .jar file in inst/java fodler but found ", length(jar_file))
  }
  jar <- file.path(jar_dir,jar_file[1])
  .checkTypes(formula,train_data, test_data = train_data, control)
  directories <- .setup(train_data,test_data,formula)
  formula_left <- all.vars(formula[-3])
  formula_right <- all.vars(formula[-2])
  xml <- XML::xmlTree()
  xml$addTag("experiment", close = FALSE)
  xml$addTag("parameter_sets", close = FALSE)
  xml <- .xmlCreateParameterSet(xml, control)
  xml$closeTag()
  xml$closeTag()
  xml$addTag("datasets", close = FALSE)
  xml$addTag("dataset", close = FALSE)
  type <- .getType(train_data,formula_left)
  if (type == "Survival") {
    xml$addTag("label", formula_left[2])
    xml$addTag("out_directory", directories$reports)
    xml$addTag("survival_time", formula_left)
  } else {
    xml$addTag("label", formula_left)
    xml$addTag("out_directory", directories$reports)
  }
  xml$addTag("training", close = FALSE)
  xml$addTag("report_file", 'training-log.txt')
  training_files <- list.files(directories$sets,pattern = 'train_.+.arff')
  for (i in seq_along(training_files))
  {
    xml$addTag("train", close = FALSE)
    xml$addTag("in_file", paste(directories$sets,'/train_',i,'.arff',sep = ''))
    xml$addTag("model_file", 'model_file',i,'.mdl')
    xml$closeTag()
  }
  xml$closeTag()
  xml$addTag("prediction", close = FALSE)
  xml$addTag("performance_file", 'performance.csv')
  for (i in seq_along(training_files))
  {
    xml$addTag("predict", close = FALSE)
    xml$addTag("model_file", 'model_file',i,'.mdl')
    xml$addTag("test_file", paste(directories$sets,'/test_',i,'.arff',sep = ''))
    xml$addTag("predictions_file", 'predictions-file',i,'.arff')
    xml$closeTag()
  }
  xml$closeTag()
  xml$closeTag()
  xml$closeTag()
  xml$closeTag()
  XML::saveXML(xml,directories$config)
  output <- system("java -version")
  if (output != 0L){
    stop("Environment variable 'JAVA_HOME' not found.")
  }
  command <- paste0("java -jar ",jar," ",directories$config)
  system(command)
  return (.getResults(directories$reports))
}
