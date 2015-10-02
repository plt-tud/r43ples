# /usr/bin/Rscript
# Create all necessary tables and graphics for R43ples evaluation
# 
# Author: Markus Graube
###############################################################################

r43ples <- read.csv2(file=file.choose(), encoding="UTF-8", comment.char = "#", dec = ".")


boxplot(Time~Revision,    data=r43ples, main='R43ples Operation Time', log="y", xlab='Revision', ylab='Time (ms)')
boxplot(Time~Mode,    data=r43ples, main='R43ples Operation Time', log="y", xlab='Join Mechanism', ylab='Time (ms)')
boxplot(Time~(Mode*Revision),    data=r43ples, main='R43ples Operation Time', log="y", xlab='Dataset Size (Triples)', ylab='Time (ms)')
# boxplot(Time~Endpoint,    data=r43ples, main='R43ples Operation Time', log="y", xlab='Endpoint', ylab='Time (ms)')
# boxplot(Time~(Mode*Endpoint),    data=r43ples, main='R43ples Operation Time', log="y", xlab='Endpoint', ylab='Time (ms)')



col <- c("darkgray", "lightgray")
col <- c("gray40", "gray90")

# lmts <- c(0, max(r43ples$Time))


# tdb <- subset(r43ples, Endpoint=="TDB")
# stardog <- subset(r43ples, Endpoint=="STARDOG")

par(mfrow=c(1,2))
ds <- subset(r43ples, Revision==12 & Changesize==50)
lmts <- c(0.1, 10)
lmts <- c(0.17, 75)
boxplot(Time~Dataset, data=ds, subset=Mode=="new", ylim=lmts, log="y", col=col[1], outline=FALSE, xlab="Dataset Size", ylab='Time (s)', main="Revision 12", names=c("100", "1k", "10k", "100k", "1M"))
boxplot(Time~Dataset, data=ds, subset=Mode=="off", ylim=lmts, log="y", col=col[2], outline=FALSE, add=TRUE, show.names=FALSE)
legend("topleft", c("Query Rewriting", "Temporary Graph"), fill = col)


rev <- subset(r43ples, Dataset==10000 & Changesize==50)
lmts <- c(0.1, 6.5)
boxplot(Time~Revision, data=rev, subset=Mode=="new", ylim=lmts, log="y", col=col[1], outline=FALSE, xlab="Revision", ylab='Time (s)', main="Dataset Size 10k")
boxplot(Time~Revision, data=rev, subset=Mode=="off", ylim=lmts, log="y", col=col[2], outline=FALSE, add=TRUE, show.names=FALSE)
legend("topright", c("Query Rewriting", "Temporary Graph"), fill = col)



# not interesting
cs <- subset(r43ples, Revision==12 & Dataset==10000)
lmts <- c(0.1, 9)
boxplot(Time~Changesize, data=cs, subset=Mode=="new", ylim=lmts, log="y", col=col[1], outline=FALSE,  xlab="Changeset Size", ylab='Time (s)')
boxplot(Time~Changesize, data=cs, subset=Mode=="off", ylim=lmts, log="y", col=col[2], outline=FALSE, add=TRUE, show.names=FALSE)
legend("topright", c("Query Rewriting", "Temporary graph"), fill = col)



# Modellerstellung
summary(lm(formula=Time ~ Revision + Join, data=data))



