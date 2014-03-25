# /usr/bin/Rscript
# Create All necessary tables and graphics for R43ples evaluation
# 
# Author: MGraube
###############################################################################

data_csv <- read.csv(file=file.choose(), encoding="UTF-8")

data_csv$revisionPath = ifelse(grepl("MASTER", data_csv$label), "MASTER", ifelse(grepl("Direct", data_csv$label), "DIRECT",  ifelse(grepl("Revision 10", data_csv$label),0, 10-data_csv$revision)))

d_direct <- subset(data_csv, data_csv[["revisionPath"]]=="DIRECT")
sparqlTime <- aggregate(d_direct$elapsed, by=list(dataset=d_direct$dataset,changesize=d_direct$changesize), FUN=median)
data_merged <- merge(data_csv,sparqlTime,by=c("dataset","changesize"))

data_merged$r43plesTime = data_merged$elapsed - data_merged$x

data_filtered <- subset(data_merged, success=="true", select=c(dataset,revision,revisionPath,changesize,elapsed,r43plesTime))

data <- subset(data_filtered, !(revisionPath %in% c("MASTER","DIRECT")))
data$revisionPath = as.integer(data$revisionPath)

data$changedTriples = data$revisionPath*data$changesize


par(mfrow=c(1,3))
boxplot(elapsed~dataset, data=data_filtered, main='Response Time', log="y", xlab='Dataset Size', ylab='Time in ms', outline=FALSE)
boxplot(elapsed~dataset, data=data_filtered, subset= revisionPath=="MASTER", main='Response Time (MASTER)', xlab='Dataset Size', ylab='Zeit in ms', outline=FALSE)
boxplot(elapsed~dataset, data=data_filtered, subset= revisionPath=="DIRECT", main='Response Time (DIRECT)', xlab='Dataset Size', ylab='Zeit in ms', outline=FALSE)





# ohne master und direct
par(mfrow=c(1,3))
boxplot(r43plesTime~dataset, data=data, main='R43ples Operation Time', log="y", xlab='Dataset Size (Triples)', ylab='Zeit in ms', outline=FALSE)
boxplot(r43plesTime~changesize, data=data, main='R43ples Operation Time', log="y", xlab='Change Size (Triples)', ylab='Zeit in ms', outline=FALSE)
boxplot(r43plesTime~revisionPath, data=data, main='R43ples Operation Time', log="y", xlab='Path Length', ylab='Zeit in ms', outline=FALSE)

par(mfrow=c(1,1))
boxplot(r43plesTime~revisionPath*dataset, data=data, main="R43ples Operation Time vs. dataset size and path length", col=c("blue","red","orange","green"), outline=FALSE)

boxplot(r43plesTime~changesize*revisionPath, data=data, log="y", main="R43ples Operation Time vs. Change size and path length",col=c("blue","red","orange","green"), outline=FALSE)






# Betrachtung von Standard
# dataset: 1000
# changesize: 50
# revisionpath: 5
par(mfrow=c(1,3))
boxplot(r43plesTime~revisionPath, data=data, subset=(dataset==1000 & changesize==50), main='1000 Triples, Changesize 50', xlab='Revision', ylab='Zeit in ms')
boxplot(r43plesTime~changesize, data=data, subset=(dataset==1000 & revisionPath==5), main='1000 Triples, RevisionPath 5', xlab='Change size', ylab='Zeit in ms')
boxplot(r43plesTime~dataset, data=data, subset=(revisionPath==5 & changesize==50), main='ChangeSize 50, RevisionPath 5', log="y", xlab='Dataset', ylab='Zeit in ms')


# Abhängigkeit von changedTriples
par(mfrow=c(1,3))
boxplot(r43plesTime~changedTriples, data=data, subset=(dataset==1000 & changesize==50), main='1000 Triples, Changesize 50', xlab='Change size', ylab='Zeit in ms')
boxplot(r43plesTime~changedTriples, data=data, subset=(dataset==1000 & revisionPath==5), main='1000 Triples, RevisionPath 5', xlab='Change size', ylab='Zeit in ms')
boxplot(r43plesTime~changedTriples, data=data, subset=(dataset==1000), main='1000 Triples', xlab='Change size', ylab='Zeit in ms')


# Modellerstellung
data_lm <- subset(data, revisionPath > 1)

complex <- lm(formula=r43plesTime ~ dataset + changesize + revisionPath + changesize:revisionPath, data=data_lm)
simple <- lm(formula=r43plesTime ~ dataset + changesize + revisionPath, data=data_lm)

summary(complex)
summary(simple)



