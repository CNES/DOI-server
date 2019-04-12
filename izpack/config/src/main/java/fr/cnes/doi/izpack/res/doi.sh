#! /bin/sh
case "$1" in
    start)
            echo -n "Starting DOI-server"
            cd ${INSTALL_PATH}
            java -Dlog4j.configurationFile=./log4j2.xml -jar DOI-server.jar -f config.properties --start
            ;;
   stop) 
          echo -n "Stopping DOI-server"
          cd ${INSTALL_PATH}
          java -jar DOI.jar -f config.properties --stop
           ;;
   restart)
          echo -n "Stopping DOI-server"
          cd ${INSTALL_PATH}
          java -jar DOI.jar -f config.properties --stop
          echo -n "Starting SOI-server"
          cd ${INSTALL_PATH}
          java -Dlog4j.configurationFile=./log4j2.xml -jar DOI-server.jar -f config.properties --start
          ;;
   status)
          echo -n "Status DOI-server"
          cd ${INSTALL_PATH}
          java -Dlog4j.configurationFile=./log4j2.xml -jar DOI-server.jar -f config.properties --status
          ;;
   version)
          echo -n "DOI-server version"
          cd ${INSTALL_PATH}
          java -Dlog4j.configurationFile=./log4j2.xml -jar DOI-server.jar -f config.properties --version
          ;;
        *)
          echo "Usage: doi.sh start|stop|restart|status"
          exit 1
          ;;   
    esac
