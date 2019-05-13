library(ggplot2)
library(reshape2)
library(adaa.rules)

formula <- survival::Surv(survival_time,survival_status) ~ .
control <- list(min_rule_covered = 5)
results <- learn_rules(formula,control,bone_marrow)

performance <- results[[1]] # data frame with performance metrics
report <- results[[2]]      # text report

# get separating empty lines in the report
seps = which(report == "")

# extract rules from the report
start = which(report == "Rules:") + 1
rules = report[start : (seps[which(seps > start)[1]] - 1)] # first separator after start

# extract survival function estimates from the report
start = which(report == "Estimator:") + 1
estimates = report[start : (seps[which(seps > start)[1]] - 1)] # first separator after start

# convert estimates into data frame with following columns:
# - time - survival time,
# - entire-set - values of survival function of entire dataset,
# - r1, r2, ... - values of survival function for rules r1, r2, etc.
names = strsplit(estimates[1],',')[[1]]
data = lapply(estimates[2:length(estimates)], function(row) {
  return(as.numeric(strsplit(row,',')[[1]]))
})
surv <- data.frame(matrix(unlist(data), nrow=length(data), byrow=T))
colnames(surv) <- names

# melt dataset for automatic plotting of multiple series
meltedSurv = melt(surv, id.var="time")

# plot survival functions estimates
ggplot(meltedSurv, aes(x=time, y=value, color=variable)) +
  geom_line(size=1.0) +
  xlab("time") + ylab("survival probability") +
  theme_bw() + theme(text = element_text(size=8),legend.title=element_blank())


ggsave("survival.pdf", plot = last_plot(), device = NULL, path = NULL,
       scale = 1, width = 8, height = 7, units = "cm",
       dpi = 600, limitsize = TRUE)
