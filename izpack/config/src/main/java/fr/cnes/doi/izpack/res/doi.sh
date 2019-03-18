#! /bin/sh
case “$1” in
    start)
            echo –n “Starting DOI-server”
            java -Dlog4j.configurationFile=./log4j2.xml -jar DOI.jar -f config.properties --start
            echo “.”
            ;;
   stop) 
          echo –n “Stopping DOI-server”
          java -jar DOI.jar -f config.properties --stop
          echo “.”
           ;;
   restart)
          echo –n “Stopping DOI-server”
          java -jar DOI.jar -f config.properties --stop
          echo “.”
          echo –n “Starting SOI-server”
            java -Dlog4j.configurationFile=./log4j2.xml -jar DOI.jar -f config.properties --start
            echo “.”
            ;;
        *)
          echo “Usage: doi.sh start|stop|restart”
          exit 1
          ;;
    esac
