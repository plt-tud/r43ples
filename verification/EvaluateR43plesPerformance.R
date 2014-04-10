# /usr/bin/Rscript
# Create All necessary tables and graphics for R43ples evaluation
# 
# Author: MGraube
###############################################################################

data_csv <- read.csv2(file=file.choose(), encoding="UTF-8")

data_csv$revisionPath = ifelse(grepl("MASTER", data_csv$label), "MASTER", ifelse(grepl("Direct", data_csv$label), "DIRECT", 20-data_csv$revision))

#d_direct <- subset(data_csv, data_csv[["revisionPath"]]=="DIRECT")
#sparqlTime <- aggregate(d_direct$elapsed, by=list(dataset=d_direct$dataset,changesize=d_direct$changesize), FUN=median)
#data_merged <- merge(data_csv,sparqlTime,by=c("dataset","changesize"))

#data_merged$r43plesTime = data_merged$elapsed - data_merged$x
data_csv$r43plesTime = data_csv$elapsed

data_filtered <- subset(data_csv, success=="true", select=c(dataset,revision,revisionPath,changesize,elapsed,r43plesTime))



par(mfrow=c(1,2))
boxplot(elapsed~dataset, data=data_filtered, subset= revisionPath=="MASTER", main='Response Time (MASTER)', xlab='Dataset Size', ylab='Time (ms)', outline=FALSE)
boxplot(elapsed~dataset, data=data_filtered, subset= revisionPath=="DIRECT", main='Response Time (DIRECT)', xlab='Dataset Size', ylab='Time (ms)', outline=FALSE)



# ohne master und direct
data <- subset(data_filtered, !(revisionPath %in% c("MASTER","DIRECT")))
data$revisionPath = as.integer(data$revisionPath)
data$changedTriples = data$revisionPath*data$changesize

par(mfrow=c(1,3))
boxplot(r43plesTime~dataset, data=data, main='R43ples Operation Time', log="y", xlab='Dataset Size (Triples)', ylab='Time (ms)', outline=FALSE)
boxplot(r43plesTime~changesize, data=data, main='R43ples Operation Time', log="y", xlab='Change Size (Triples)', ylab='Time (ms)', outline=FALSE)
boxplot(r43plesTime~revisionPath, data=data, main='R43ples Operation Time', log="y", xlab='Revsision Path Length', ylab='Time (ms)', outline=FALSE)

par(mfrow=c(1,1))
boxplot(r43plesTime~revisionPath*dataset, data=data, main="R43ples Operation Time vs. dataset size and path length", outline=FALSE)



# Betrachtung von Standard
# dataset: 1000
# changesize: 50
# revisionpath: 5
par(mfrow=c(1,3))
boxplot(r43plesTime~revisionPath, data=data, subset=(dataset==1000 & changesize==50), main='1000 Triples, Changesize 50', xlab='Revision Path Length', ylab='Time (ms)')
boxplot(r43plesTime~changesize, data=data, subset=(dataset==1000 & revisionPath==5), main='1000 Triples, RevisionPath 5', xlab='Change Size', ylab='Time (ms)')
boxplot(r43plesTime~dataset, data=data, subset=(revisionPath==5 & changesize==50), main='ChangeSize 50, RevisionPath 5', log="y", xlab='Dataset Size', ylab='Time (ms)')


par(mfrow=c(1,3))
boxplot(r43plesTime~revisionPath, data=data, subset=(dataset==1000 & changesize==50 & (revisionPath %in% c(0,1,3,5,7,9)) ), main='1000 Triples, Changesize 50', xlab='Revision Path Length', ylab='Time (ms)')
boxplot(r43plesTime~changesize, data=data, subset=(dataset==1000 & revisionPath==5 & (changesize %in% c(10,30,50,70,90)) ), main='1000 Triples, RevisionPath 5', xlab='Change Size', ylab='Time (ms)')
boxplot(r43plesTime~dataset, data=data, subset=(revisionPath==5 & changesize==50), main='ChangeSize 50, RevisionPath 5', log="y", xlab='Dataset Size', ylab='Time (ms)')



# Abhängigkeit von changedTriples
par(mfrow=c(1,2))
boxplot(r43plesTime~changedTriples, data=data, subset=(dataset==1000 & changesize==50), main='R43ples Operation Time vs. Revision Path Length (1000 Triples, Changesize 50)', xlab='Changed Triples', ylab='Time (ms)')
boxplot(r43plesTime~changedTriples, data=data, subset=(dataset==1000 & revisionPath==5), main='R43ples Operation Time vs. Change Size (1000 Triples, RevisionPath 5)', xlab='Changed Triples', ylab='Time (ms)')


# Modellerstellung
data_lm <- subset(data, revisionPath > 1)

summary(lm(formula=r43plesTime ~ dataset + revisionPath + changesize:revisionPath, data=data_lm))
summary(lm(formula=r43plesTime ~ dataset + changesize + revisionPath, data=data_lm))
summary(lm(formula=r43plesTime ~ dataset + changesize:revisionPath, data=data_lm))



