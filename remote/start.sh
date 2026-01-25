#!/usr/bin/bash
cd "$(dirname "$0")"
export JAVA_HOME=/usr/local/openjdk17
export PATH=$JAVA_HOME/bin:$PATH

# Generate keystore if not exists
if [ ! -f keystore.p12 ]; then
  keytool -genkeypair -alias hrms -keyalg RSA -keysize 2048 -storetype PKCS12 \
    -keystore keystore.p12 -validity 3650 -storepass hrmspass \
    -dname "CN=helios.cs.ifmo.ru, OU=HRMS, O=ITMO, L=SPb, ST=SPb, C=RU" \
    -ext "SAN=dns:helios.cs.ifmo.ru,dns:localhost,ip:127.0.0.1" 2>/dev/null
fi

nohup java -Xmx256m -jar hrms.jar \
  --spring.datasource.url=jdbc:postgresql://pg:5432/studs \
  --spring.datasource.username=s335175 \
  --spring.datasource.password=sFzhSqlWVp2Z0yYF \
  --spring.jpa.properties.hibernate.default_schema=s335175 \
  --server.port=${PORT:-8888} \
  --server.ssl.enabled=true \
  --server.ssl.key-store=file:./keystore.p12 \
  --server.ssl.key-store-password=hrmspass \
  --server.ssl.key-store-type=PKCS12 \
  --server.ssl.key-alias=hrms \
  > app.log 2>&1 & echo $! > app.pid
