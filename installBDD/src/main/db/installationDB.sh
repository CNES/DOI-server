#!/bin/bash
echo "Arret postgresql"
systemctl stop postgresql-9.6.service

echo "Desinstallation de l'ancienne base et postgresql"
rpm -e `rpm -qa | grep "^postgresql96-server"`
rpm -e `rpm -qa | grep "^postgresql96-9.6"`
rpm -e `rpm -qa | grep "^postgresql96-libs"`
rm -rf /var/lib/pgsql/9.6/data

echo "Installation de postgresql "
rpm -i postgresql96-libs-9.6.10-1PGDG.rhel7.x86_64.rpm 
rpm -i postgresql96-9.6.10-1PGDG.rhel7.x86_64.rpm 
rpm -i postgresql96-server-9.6.10-1PGDG.rhel7.x86_64.rpm 

echo "Initialisation de postgresql "

/usr/pgsql-9.6/bin/postgresql96-setup initdb
sed -i  '/\(^host.*127.0.0.1\/32\)/s/ident/md5/' /var/lib/pgsql/9.6/data/pg_hba.conf
echo "Démarrage du service postgresql"
systemctl start postgresql-9.6.service

echo "Creation de la base de données"
more doidb.sql | (cd / && runuser postgres -c "psql -U postgres -q")

echo "Creation de la base de données de test"
more doidbtest.sql | (cd / && runuser postgres -c "psql -U postgres -q")

echo "Fin de l'installation."