# /usr/bin/Rscript
# Create all necessary tables and graphics for R43ples evaluation
# 
# Author: Markus Graube
###############################################################################

csv <- file.choose()
csv <- "logs/time.2015-12-18_07:42:45.log"

r43ples <- read.csv2(file=csv, encoding="UTF-8", comment.char = "#", dec = ".")

r43ples$rev_length <- max(r43ples$Revision)-r43ples$Revision

boxplot(Time~Revision,    data=r43ples, main='R43ples Operation Time', log="y", xlab='Revision', ylab='Time (s)')


boxplot(Time~rev_length,    data=r43ples, main='R43ples Operation Time', log="y", xlab='Revision', ylab='Time (s)')


# Modellerstellung
linear_model <- lm(formula=Time ~ Revision, data=r43ples)
summary(linear_model)

linear_model <- lm(formula=Time ~ rev_length, data=r43ples)
summary(linear_model)


