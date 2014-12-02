# /usr/bin/Rscript
# Create All necessary tables and graphics for R43ples evaluation
# 
# Author: MGraube
###############################################################################

data_csv <- read.csv(file=file.choose(), encoding="UTF-8")

data_csv$revision = ifelse(grepl("MASTER", data_csv$label), "MASTER", ifelse(grepl("Direct", data_csv$label), "DIRECT", data_csv$revision))
data_csv$join = ifelse(grepl("JOIN", data_csv$label), TRUE, FALSE)

#d_direct <- subset(data_csv, data_csv[["revision"]]=="DIRECT")
#sparqlTime <- aggregate(d_direct$elapsed, by=list(dataset=d_direct$dataset,changesize=d_direct$changesize), FUN=median)
#data_merged <- merge(data_csv,sparqlTime,by=c("dataset","changesize"))

#data_merged$r43plesTime = data_merged$elapsed - data_merged$x
data_csv$r43plesTime = data_csv$elapsed

data_filtered <- subset(data_csv, success=="true", select=c(dataset,revision,join,revision,changesize,elapsed,r43plesTime))



par(mfrow=c(1,2))
data_master_direct <- subset(data_filtered, subset=(revision=="MASTER" | revision=="DIRECT"))
lmts <- range(data_master_direct$elapsed)
boxplot(elapsed~dataset, data=data_filtered, subset= revision=="MASTER", main='Response Time (MASTER)', xlab='Dataset Size', ylab='Time (ms)', ylim=lmts)
boxplot(elapsed~dataset, data=data_filtered, subset= revision=="DIRECT", main='Response Time (DIRECT)', xlab='Dataset Size', ylab='Time (ms)', ylim=lmts)



# ohne master und direct
data <- subset(data_filtered, !(revision %in% c("MASTER","DIRECT")))
data$revision = 21 - as.integer(data$revision)
order
data$changedTriples = data$revision*data$changesize


lmts <- range(data$r43plesTime)
lmts <- c(25,25000)

par(mfrow=c(1,3))
boxplot(r43plesTime~dataset,    data=data, main='R43ples Operation Time', log="y", xlab='Dataset Size (Triples)', ylab='Time (ms)', outline=FALSE, ylim=lmts)
boxplot(r43plesTime~changesize, data=data, main='R43ples Operation Time', log="y", xlab='Change Size (Triples)', ylab='Time (ms)', outline=FALSE, ylim=lmts)
boxplot(r43plesTime~revision,   data=data, main='R43ples Operation Time', log="y", xlab='Revsision', ylab='Time (ms)', outline=FALSE, ylim=lmts)

par(mfrow=c(1,1))
boxplot(r43plesTime~revision*dataset, data=data, main="R43ples Operation Time vs. Dataset Size and Revision", outline=FALSE)


col=c("gold","darkgreen")

par(mfrow=c(1,3))
boxplot(r43plesTime~join*revision,   data=data, ylim=lmts, col=col, main="R43ples Operation Time vs. Revision",  log="y", outline=FALSE)
boxplot(r43plesTime~join*dataset,    data=data, ylim=lmts, col=col, main="R43ples Operation Time vs. Dataset Size", log="y", outline=FALSE)
boxplot(r43plesTime~join*changesize, data=data, ylim=lmts, col=col, main="R43ples Operation Time vs. Change Size",  log="y",outline=FALSE)


par(mfrow=c(1,3))
boxplot(r43plesTime~revision,   data=data, subset=(join==FALSE), ylim=lmts,  boxwex = 0.25, at = 1:6 - 0.2, col="yellow", main="Revision",  outline=FALSE, yaxs = "i")
boxplot(r43plesTime~revision,   data=data, subset=(join==TRUE),  ylim=lmts,  boxwex = 0.25, at = 1:6 + 0.2, col="orange", main="Revision",  outline=FALSE, add=TRUE, show.names=FALSE)
legend("topleft", c("Temp graph", "SPARQL JOIN"),       fill = c("yellow", "orange"))
boxplot(r43plesTime~dataset,   data=data, subset=(join==FALSE), ylim=lmts,  boxwex = 0.25, at = 1:5 - 0.2, col="yellow", main="Dataset Size",  log="y", outline=FALSE, yaxs = "i")
boxplot(r43plesTime~dataset,   data=data, subset=(join==TRUE),  ylim=lmts,  boxwex = 0.25, at = 1:5 + 0.2, col="orange", main="Dataset Size",  log="y", outline=FALSE, add=TRUE, show.names=FALSE)
legend("topleft", c("Temp graph", "SPARQL JOIN"),       fill = c("yellow", "orange"))
boxplot(r43plesTime~changesize,   data=data, subset=(join==FALSE), ylim=lmts,  boxwex = 0.25, at = 1:5 - 0.2, col="yellow", main="Change Size", outline=FALSE, yaxs = "i" )
boxplot(r43plesTime~changesize,   data=data, subset=(join==TRUE),  ylim=lmts,  boxwex = 0.25, at = 1:5 + 0.2, col="orange", main="Change Size", outline=FALSE, add=TRUE, show.names=FALSE)
legend("topleft", c("Temp graph", "SPARQL JOIN"),       fill = c("yellow", "orange"))


lmts1 <- c(0,13000)
lmts2 <- c(25,130000)
par(mfrow=c(1,3))
boxplot(r43plesTime~revision,   data=data, subset=(join==FALSE), ylim=lmts1,  boxwex = 0.25, at = 1:6 - 0.2, col="yellow", main="Revision",  outline=FALSE, yaxs = "i")
boxplot(r43plesTime~revision,   data=data, subset=(join==TRUE),  ylim=lmts1,  boxwex = 0.25, at = 1:6 + 0.2, col="orange", main="Revision",  outline=FALSE, add=TRUE, show.names=FALSE)
legend("topright", c("Temp graph", "SPARQL JOIN"),       fill = c("yellow", "orange"))
boxplot(r43plesTime~dataset,   data=data, subset=(join==FALSE), ylim=lmts2,  boxwex = 0.25, at = 1:5 - 0.2, col="yellow", main="Dataset Size",  log="y", outline=FALSE, yaxs = "i")
boxplot(r43plesTime~dataset,   data=data, subset=(join==TRUE),  ylim=lmts2,  boxwex = 0.25, at = 1:5 + 0.2, col="orange", main="Dataset Size",  log="y", outline=FALSE, add=TRUE, show.names=FALSE)
legend("topright", c("Temp graph", "SPARQL JOIN"),       fill = c("yellow", "orange"))
boxplot(r43plesTime~changesize,   data=data, subset=(join==FALSE), ylim=lmts1,  boxwex = 0.25, at = 1:5 - 0.2, col="yellow", main="Change Size", outline=FALSE, yaxs = "i" )
boxplot(r43plesTime~changesize,   data=data, subset=(join==TRUE),  ylim=lmts1,  boxwex = 0.25, at = 1:5 + 0.2, col="orange", main="Change Size", outline=FALSE, add=TRUE, show.names=FALSE)
legend("topright", c("Temp graph", "SPARQL JOIN"),       fill = c("yellow", "orange"))





# Betrachtung von Standard
# dataset: 1000
# changesize: 50
# revisionpath: 9
par(mfrow=c(1,3))
boxplot(r43plesTime~revision, data=data, subset=(dataset==1000 & changesize==50), main='1000 Triples, Changesize 50', xlab='Revision Path Length', ylab='Time (ms)')
boxplot(r43plesTime~changesize, data=data, subset=(dataset==1000 & revision==9), main='1000 Triples, RevisionPath 9', xlab='Change Size', ylab='Time (ms)')
boxplot(r43plesTime~dataset, data=data, subset=(revision==9 & changesize==50), main='ChangeSize 50, RevisionPath 9', log="y", xlab='Dataset Size', ylab='Time (ms)')


par(mfrow=c(1,3))
boxplot(r43plesTime~revision, data=data, subset=(dataset==1000 & changesize==50 & (revision %in% c(1,5,9,13,17,21)) ), main='1000 Triples, Changesize 50', xlab='Revision Path Length', ylab='Time (ms)')
boxplot(r43plesTime~changesize, data=data, subset=(dataset==1000 & revision==9 & (changesize %in% c(10,30,50,70,90)) ), main='1000 Triples, RevisionPath 9', xlab='Change Size', ylab='Time (ms)')
boxplot(r43plesTime~dataset, data=data, subset=(revision==9 & changesize==50), main='ChangeSize 50, RevisionPath 9', log="y", xlab='Dataset Size', ylab='Time (ms)')


lmts1 <- c(0,4100)
lmts2 <- c(400,100000)
par(mfrow=c(1,3))
boxplot(r43plesTime~revision, data=data, subset=(dataset==1000 & changesize==50 & (revision %in% c(0,4,8,12,16,20)) & (join==FALSE) ), main='1000 Triples, Changesize 50', ylim=lmts1, xlab='Revision Path Length', ylab='Time (ms)', boxwex = 0.25, at = 1:6 - 0.2, col="yellow")
boxplot(r43plesTime~revision, data=data, subset=(dataset==1000 & changesize==50 & (revision %in% c(0,4,8,12,16,20)) & (join==TRUE) ), main='1000 Triples, Changesize 50', ylim=lmts1, xlab='Revision Path Length', ylab='Time (ms)', add=TRUE, show.names=FALSE, boxwex = 0.25, at = 1:6 + 0.2, col="orange")
legend("topleft", c("Temp graph", "SPARQL JOIN"),       fill = c("yellow", "orange"))
boxplot(r43plesTime~changesize, data=data, subset=(dataset==1000 & revision==12 & (changesize %in% c(10,30,50,70,90))  & (join==FALSE) ), main='1000 Triples, RevisionPath 9', ylim=lmts1, xlab='Change Size', ylab='Time (ms)',  boxwex = 0.25, at = 1:5 - 0.2, col="yellow")
boxplot(r43plesTime~changesize, data=data, subset=(dataset==1000 & revision==12 & (changesize %in% c(10,30,50,70,90))  & (join==TRUE) ), main='1000 Triples, RevisionPath 9', ylim=lmts1, xlab='Change Size', ylab='Time (ms)',  boxwex = 0.25, at = 1:5 + 0.2, col="orange", add=TRUE, show.names=FALSE)
legend("topleft", c("Temp graph", "SPARQL JOIN"),       fill = c("yellow", "orange"))
boxplot(r43plesTime~dataset, data=data, subset=(revision==12 & changesize==50  & join==FALSE), main='ChangeSize 50, RevisionPath 9', log="y", ylim=lmts2, xlab='Dataset Size', ylab='Time (ms)', boxwex = 0.25, at = 1:5 - 0.2, col="yellow")
boxplot(r43plesTime~dataset, data=data, subset=(revision==12 & changesize==50  & join==TRUE), main='ChangeSize 50, RevisionPath 9', log="y", ylim=lmts2, xlab='Dataset Size', ylab='Time (ms)', boxwex = 0.25, at = 1:5 + 0.2, col="orange", add=TRUE, show.names=FALSE)
legend("topleft", c("Temp graph", "SPARQL JOIN"),       fill = c("yellow", "orange"))

# Abhängigkeit von changedTriples
par(mfrow=c(1,2))
boxplot(r43plesTime~changedTriples, data=data, subset=(dataset==1000 & changesize==50), main='R43ples Operation Time vs. Revision Path Length (1000 Triples, Changesize 50)', xlab='Changed Triples', ylab='Time (ms)')
boxplot(r43plesTime~changedTriples, data=data, subset=(dataset==1000 & revision==9), main='R43ples Operation Time vs. Change Size (1000 Triples, RevisionPath 9)', xlab='Changed Triples', ylab='Time (ms)')


# Modellerstellung
data_lm <- subset(data, revision > 1)

summary(lm(formula=r43plesTime ~ dataset + revision + changesize:revision, data=data_lm))
summary(lm(formula=r43plesTime ~ dataset + changesize + revision+join, data=data_lm))
summary(lm(formula=r43plesTime ~ dataset + changesize:revision:join, data=data_lm))



