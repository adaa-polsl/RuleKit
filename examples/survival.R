library(ggplot2); library(reshape2); library(rulekit)

formula <- survival::Surv(survival_time,survival_status) ~ .
control <- list(min_rule_covered = 5)
results <- rulekit::learn_rules(formula,control,bone_marrow)

# extract outputs:
rules <- results[["rules"]] # list of rules
cov <- results[["train-coverage"]] # coverage information
surv <- results[["estimator"]] # survival function estimates
perf <- results[["test-performance"]]  # testing performance

# melt dataset for automatic plotting of multiple series
melted_surv <- reshape2::melt(surv, id.var="time")

# plot survival functions estimates
ggplot(melted_surv, aes(x=time, y=value, color=variable)) +
  geom_line(size=1.0) +
  xlab("time") + ylab("survival probability") +
  theme_bw() + theme(text = element_text(size=8),legend.title=element_blank())


ggsave("survival.png", plot = last_plot(), device = "png", path = NULL,
       scale = 1, width = 8, height = 4, units = "cm",
       dpi = 600, limitsize = TRUE)
