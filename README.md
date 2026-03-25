Dependencias: Docker, gradlew y jdk 
En principio dentro de la carpeta backend para poder iniciar la base de datos mediante docker lanzar:
<bash> docker-compose up -d </bash>
Con "sudo" si se hace en linux 

luego en el mismo lugar hacer:
<bash> gradlew tasks </bash>
para poder arrancar el proyecto con:
<bash> gradlew bootRun </bash>
Se deberia de quedar en 85% para saber si se ha lanzado correctamente 

Para poder lanzar frontend (angular)
Dentro de la carpeta frontend/JuegoRolPagina
Para poder instalar dependencias de node 
<bash> npm install </bash>
Y para lanzar el proyecto 

<bash> ng serve </bash>


