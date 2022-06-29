FROM confluentinc/cp-kafka-connect-base:7.0.0-1-ubi8

#RUN wget

RUN   confluent-hub install --no-prompt --component-dir /usr/share/java confluentinc/kafka-connect-jdbc:10.5.0
#   && confluent-hub install --no-prompt microsoft/kafka-connect-iothub:0.6 \
#   && confluent-hub install --no-prompt wepay/kafka-connect-bigquery:1.1.0