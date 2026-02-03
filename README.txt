Complete configuration was performed only for a Linux machine but with few step also a Windows-developer can begin.
In order to make running the project must be installed firebird-server on the developer machine.
After the firebird-server is launched  it is necessary to add 2 users with respective passwords. In Linux it is sufficient to run the script into the dir ./scripts.
The users to be added are:
1) Name=dmclient ; Password=dmclient (account for the GUI side that will be uncensored).
2) Name=dmserver ; Password=docuserv (account for the server side that will be secret).
To insert different passwords and/or names must be changed two configuration files: properties.prop and guiprop.prop (not recommended at begin).
Last essential step is to include the library ./lib/firebirdsql-full.jar in the IDE path (or command line path).

At the end remember that both the user and the group for the database file must be "firebird" with read-write permission.

Directory "testingdocs" is not part of the project but contains some documents used in testing phase.
Actually it is not implemented a complete GUI but only a Login mask that allow, logging in as ADMIN, passwords and users management for Documat subsystem.
