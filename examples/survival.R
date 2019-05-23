library(ggplot2)
library(reshape2)
library(adaa.rules)

formula <- survival::Surv(survival_time,survival_status) ~ .
control <- list(min_rule_covered = 5)
results <- learn_rules(formula,control,bone_marrow)

# extract outputs:
rules = results[["rules"]]        # list of rules
cov = results[["train-coverage"]] # coverage of training examples by rules
surv = results[["estimator"]]      # data frame with survival function estimates
perf = results[["test-performance"]]   # data frame with performance metrices

# melt dataset for automatic plotting of multiple series
melted_surv = melt(surv, id.var="time")

# plot survival functions estimates
ggplot(melted_surv, aes(x=time, y=value, color=variable)) +
  geom_line(size=1.0) +
  xlab("time") + ylab("survival probability") +
  theme_bw() + theme(text = element_text(size=8),legend.title=element_blank())


ggsave("survival.pdf", plot = last_plot(), device = NULL, path = NULL,
       scale = 1, width = 8, height = 7, units = "cm",
       dpi = 600, limitsize = TRUE)
