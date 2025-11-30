#!/bin/bash
cd "$(dirname "$0")"
export JAVA_HOME=/usr/local/openjdk17
export PATH=$JAVA_HOME/bin:$PATH
nohup java -Xmx256m -jar hrms.jar \
  --spring.datasource.url=jdbc:postgresql://pg:5432/studs \
  --spring.datasource.username=s335175 \
  --spring.datasource.password=sFzhSqlWVp2Z0yYF \
  --spring.jpa.properties.hibernate.default_schema=s335175 \
  --server.port=${PORT:-8888} \
  > app.log 2>&1 & echo $! > app.pid
