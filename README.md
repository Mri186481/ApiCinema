# ApiCinema
Movie API for a Data Access learning activity.

Es una Api de un sistema de venta de entradas de unos multicines. Esta basada en JAVA 21 con el framework springboot 3.5.6 . Dicha Api está pensada para cubrir una primera etapa todas las necesidades de información que puedan tener las aplicaciones que la utilicen para la gestión de la venta de entradas, pudiéndose conectar fácilmente una App de Android, una aplicación Web o una aplicación de escritorio.

Con unas tablas mas se puede expandir fácilmente en una cadena de multicines. Además de que se pueden sumar en futuras etapa enlaces con facturación para enlaces con programas de contabilidad,
Para arrancar la aplicación hace falta tener en el equipo una aplicación para gestionar y arrancar contenedores. En caso de Windows utilizamos Docker Desktop.

Es necesario descargar una imagen de mariadb para poder generar un contenedor con una BD de mariaDB(hee usado la versión 11.3.2.). Antes de ejecutar la aplicación habrá que iniciar la base de datos, para ello he dejado preparado dentro del proyecto, en la raíz del mismo el archivo <u>*docker-compose.dev.yaml*</u>.
Ahora solo hay que ejecutarlo con las siguientes instrucciones en la consola de comandos:

**docker compose -f docker-compose.dev.yaml up -d**

Con esto se crea la base datos cinemas-db en el contenedor
Si se quiere permanencia en el archivo docker-compose.dev.yaml están comentados un ejemplo de permanencia en un directorio local de un equipo Windows.
A continuación, se ejecuta la aplicación:

**mvn spring-boot:run**

Para poder probar los endpoints realizados es necesario contar con Postman. He exportado una colección Postman que permita probar todas las operaciones desarrolladas, esta en el archivo del repositorio llamado *<u>cinema.postman_collection.json</u>* .

Es recomendable ampliar el espaciovisual para el terminal dentro de Intelij o en el visor de consola que se utilize, puesto está implementado un sistema de logs que permite ver la traza por donde pasa el programa y si hay algún error se puede averiguar relativamente facil donde ha fallado.

Una vez probado pulsando  CTLR + C y despues aceptando terminar el trabajo por lotes se cierra la aplicacion. Es recomendable tambien destruir el contenedor con la siguiente instruccion en consola:

**docker compose -f docker-compose.dev.yaml down**

Saludos¡¡¡
