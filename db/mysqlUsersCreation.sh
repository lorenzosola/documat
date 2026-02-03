#!/usr/bin/bash
creationString='
DROP USER root@localhost;
CREATE USER root@localhost IDENTIFIED BY "mysqladmin";
GRANT ALL PRIVILEGES ON *.* TO root@localhost WITH GRANT OPTION;
FLUSH PRIVILEGES;

DROP USER IF EXISTS "dmserver"@"%";
CREATE USER "dmserver"@"%" IDENTIFIED BY "docuserv";
GRANT ALL PRIVILEGES ON documat.* TO docuserv;

DROP USER IF EXISTS "dmclient"@"%";
CREATE USER "dmclient"@"%" IDENTIFIED BY "dmclient";
GRANT SELECT ON documat.* TO dmclient;
'
sudo mysql -u root <<< "$creationString" 2>/dev/null || sudo mysql -u root --password='mysqladmin' <<< "$creationString"
if [ $? -eq 0 ]; then
	echo -e "\n*** !! ALL RIGTH !! ***"
fi
