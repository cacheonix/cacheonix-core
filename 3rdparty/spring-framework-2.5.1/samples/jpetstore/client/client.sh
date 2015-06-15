#!/bin/sh
#SET CLIENT_HOME directory

CLIENT_HOME=`dirname $0`

CPATH=$CLIENT_HOME/../../../lib/axis/axis.jar:$CPATH
CPATH=$CLIENT_HOME/../../../lib/axis/wsdl4j.jar:$CPATH
CPATH=$CLIENT_HOME/../../../lib/caucho/hessian-3.1.3.jar:$CPATH
CPATH=$CLIENT_HOME/../../../lib/j2ee/jaxrpc.jar:$CPATH
CPATH=$CLIENT_HOME/../../../lib/jakarta-commons/commons-collections.jar:$CPATH
CPATH=$CLIENT_HOME/../../../lib/jakarta-commons/commons-discovery.jar:$CPATH
CPATH=$CLIENT_HOME/../../../lib/jakarta-commons/commons-logging.jar:$CPATH
CPATH=$CLIENT_HOME/../../../lib/jaxws/saaj-api.jar:$CPATH
CPATH=$CLIENT_HOME/../../../dist/spring.jar:$CPATH
CPATH=$CLIENT_HOME/jpetstore.jar:$CPATH

java -cp $CPATH org.springframework.samples.jpetstore.service.client.OrderServiceClient $1 $2
