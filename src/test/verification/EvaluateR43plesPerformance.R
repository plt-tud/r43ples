# /usr/bin/Rscript
# Create all necessary tables and graphics for R43ples evaluation
# 
# Author: Markus Graube
###############################################################################

data <- read.csv2(file=file.choose(), encoding="UTF-8", comment.char = "#", dec = ".")



boxplot(Time~Revision,    data=data, main='R43ples Operation Time', log="y", xlab='Revision', ylab='Time (ms)')
boxplot(Time~Join,    data=data, main='R43ples Operation Time', log="y", xlab='Join Mechanism', ylab='Time (ms)')
boxplot(Time~(Join*Revision),    data=data, main='R43ples Operation Time', log="y", xlab='Dataset Size (Triples)', ylab='Time (ms)')




col=c("gold","darkgreen")
lmts <- c(0, max(data$Time))
boxplot(Time~Revision,   data=data, subset=(Join=="new"), ylim=lmts,  boxwex = 0.25, col="yellow", main="Revision",  outline=FALSE, yaxs = "i")
boxplot(Time~Revision,   data=data, subset=(Join=="off"),  ylim=lmts,  boxwex = 0.25, col="orange", main="Revision",  outline=FALSE, add=TRUE, show.names=FALSE)
legend("topleft", c("new Join", "Temp graph"),       fill = c("yellow", "orange"))





# Modellerstellung
summary(lm(formula=Time ~ Revision + Join, data=data))



