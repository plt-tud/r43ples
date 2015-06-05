# /usr/bin/Rscript
# Create all necessary tables and graphics for R43ples evaluation
# 
# Author: Markus Graube
###############################################################################

data <- read.csv2(file=file.choose(), encoding="UTF-8", comment.char = "#", dec = ".")


boxplot(Time~Revision,    data=data, main='R43ples Operation Time', log="y", xlab='Revision', ylab='Time (ms)')
boxplot(Time~Mode,    data=data, main='R43ples Operation Time', log="y", xlab='Join Mechanism', ylab='Time (ms)')
boxplot(Time~(Mode*Revision),    data=data, main='R43ples Operation Time', log="y", xlab='Dataset Size (Triples)', ylab='Time (ms)')
boxplot(Time~Endpoint,    data=data, main='R43ples Operation Time', log="y", xlab='Endpoint', ylab='Time (ms)')
boxplot(Time~(Mode*Endpoint),    data=data, main='R43ples Operation Time', log="y", xlab='Endpoint', ylab='Time (ms)')

require(RColorBrewer)

colors <- brewer.pal(3,"Set3")
lmts <- c(0, max(data$Time))


tdb <- subset(data, Mode=="TDB")
stardog <- subset(data, Mode=="STARDOG")


boxplot(Time~Revision,   data=data, subset= data$Mode=="new", ylim=lmts, boxwex = 0.25, col=colors[1], main="Revision",  outline=FALSE, yaxs = "i")
boxplot(Time~Revision,   data=data, subset= Mode=="off", ylim=lmts, boxwex = 0.25, col=colors[2], main="Revision",  outline=FALSE, add=TRUE, show.names=FALSE)
legend("topleft", c("Join", "Temp graph"), fill = colors)




boxplot(Time~Revision,   data=tdb, subset=(Mode=="new"), ylim=lmts, boxwex = 0.25, col=colors[1], main="Revision",  outline=FALSE, yaxs = "i")
boxplot(Time~Revision,   data=tdb, subset=(Mode=="off"), ylim=lmts, boxwex = 0.25, col=colors[2], main="Revision",  outline=FALSE, add=TRUE, show.names=FALSE)
legend("topleft", c("Join", "Temp graph"), fill = colors)


boxplot(Time~Revision,   data=stardog, subset=(Mode=="new"), ylim=lmts, boxwex = 0.25, col=colors[1], main="Revision",  outline=FALSE, yaxs = "i")
boxplot(Time~Revision,   data=stardog, subset=(Mode=="off"), ylim=lmts, boxwex = 0.25, col=colors[2], main="Revision",  outline=FALSE, add=TRUE, show.names=FALSE)
legend("topleft", c("Join", "Temp graph"), fill = colors)

# Modellerstellung
summary(lm(formula=Time ~ Revision + Join, data=data))



