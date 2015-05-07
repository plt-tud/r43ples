# /usr/bin/Rscript
# Create all necessary tables and graphics for R43ples evaluation
# 
# Author: Markus Graube
###############################################################################

data <- read.csv2(file=file.choose(), encoding="UTF-8", comment.char = "#", dec = ".")

 library(RColorBrewer)

boxplot(Time~Revision,    data=data, main='R43ples Operation Time', log="y", xlab='Revision', ylab='Time (ms)')
boxplot(Time~Join,    data=data, main='R43ples Operation Time', log="y", xlab='Join Mechanism', ylab='Time (ms)')
boxplot(Time~(Join*Revision),    data=data, main='R43ples Operation Time', log="y", xlab='Dataset Size (Triples)', ylab='Time (ms)')


require(RColorBrewer)

colors <- brewer.pal(3,"Set3")
lmts <- c(0, max(data$Time))
boxplot(Time~Revision,   data=data, subset=(Join=="new"), ylim=lmts, boxwex = 0.25, col=colors[1], main="Revision",  outline=FALSE, yaxs = "i")
boxplot(Time~Revision,   data=data, subset=(Join=="off"), ylim=lmts, boxwex = 0.25, col=colors[2], main="Revision",  outline=FALSE, add=TRUE, show.names=FALSE)
legend("topleft", c("Join", "Temp graph"), fill = colors)





# Modellerstellung
summary(lm(formula=Time ~ Revision + Join, data=data))



