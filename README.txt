Complete configuration was performed only for a Linux machine but with few step also a Windows-developer can begin.
In order to make running the project must be installed MySQL server or Firebird server (as the DBMS), on the developer machine.
After the DBMS is started, it is necessary to add 2 users account. In Linux it is sufficient to run the script into the dir ./scripts.
The users to be added are:
1) Name=dmclient ; Password=dmclient (account for the GUI side that will be uncensored).
2) Name=dmserver ; Password=docuserv (account for the server side that will be secret).
To insert different passwords and/or names must be changed two configuration files: properties.prop and guiprop.prop (not recommended at begin).
Last essential step is to include the library ./lib/firebirdsql-full.jar in the IDE path (or command line path).

At the end remember that both, users must have read-write permission on "documat" database.

Directory "testingdocs" is not part of the project but contains some documents used during tests.
At the first star of che client - that can run on the server machine itself or remotely, as specified by the relative configuration parameter - it should start a login window where the only registered user is the administrator's one. After the login (with administrator credentials) it can be created all other users with their respective credentials.
