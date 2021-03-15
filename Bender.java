import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class Bender {
        //Iniciamos las variables globales
        casilla comenzar; //La posicion inicial
        casilla meta; //La posición objetivo
        casilla actual; //La posición actual
        casilla[][] mapa; //Todas las posiciones posibles

        boolean invertir = false; //La prioridad de dirección actual
        char[] direcciones = new char[]{
                'S',
                'E',
                'N',
                'W'
        }; //La prioridad de dirección predeterminada
        char direccion = direcciones[0]; //La dirección actual, por defecto Sur
        String camino = ""; //Aquí almacenamos la dirección en la que tomamos cada movimiento

        List<casilla> teleList = new ArrayList<casilla>(); //Lista de teletransportadores


        public Bender(String mapa) {
            String[] split = mapa.split("\n"); //Almacenamos el mapa como una matriz de filas, definimos las filas por los saltos de línea('\n')

            //Tenemos las filas, ahora para hacer el mapa necesitamos las columnas
            int maximo = 0;
            for (String s : split) {
                if (s.length() > maximo) {
                    maximo = s.length();//La columna más larga
                }
            }

            this.mapa = new casilla[split.length][maximo];//El mapa es una matriz bidimensional vacía

            //Cree una nueva celda para cada posición en el mapa y guárdela en su posición respectiva
            for (int i = 0; i < split.length; i++) {
                for (int j = 0; j < split[i].length(); j++) {

                    this.mapa[i][j] = new casilla(split[i].charAt(j), i, j);

                    //Almacene las células de inicio, objetivo y teletransporte
                    if (this.mapa[i][j].type.equals("start")) {
                        comenzar = this.mapa[i][j];
                    }
                    if (this.mapa[i][j].type.equals("goal")) {
                        meta = this.mapa[i][j];
                    }
                    if (this.mapa[i][j].type.equals("teleport")) {
                        teleList.add(this.mapa[i][j]);
                    }
                }
            }
        }

        //Aquí comienza el método de fuerza bruta
        public String run() {
            if (!testMap()) {
                return null;
            }
            //Establecemos la celda actual en el punto de partida
            actual = comenzar;
            //Compruebe si estamos en la meta, si no...
            while (actual != meta) {
                //mirar hacia adelante, si no una pared...
                if (checkWall()) {
                    //Avanzar
                    move();
                    //Si nos mudamos a un teletransportador...
                    if (actual.type.equals("teleport")) {
                        //teletransportador
                        teleport();
                    }
                    //Si nos mudamos a una celda invertida
                    if (actual.type.equals("invert")) {
                        //invertida
                        invert();
                    }

                } else {
                    //Si vemos una pared dar la vuelta hasta que no veamos una
                    turn();
                }
                //Compruebe si estamos en un bucle infinito
                if (infinite()) {
                    return null;
                }
            }
            //Devolver el camino a la meta
            return camino;
        }

        public boolean testMap() {
            //Esta función comprueba algunos requisitos básicos del mapa como tener un objetivo
            if (teleList.size() == 1) {
                return false;
            }
            if (comenzar == null) {
                return false;

            }
            return meta != null;
        }

        public void turn() {
            //En cada dirección predefinido...
            for (char c : direcciones) {
                //esperar una pared
                if (checkWall()) {
                    //Si no hay muro hemos terminado
                    break;
                }
                //Si hay una pared intente la siguiente dirección
                direccion = c;
            }
        }

        public void move() {
            //Almacene la instancia de x e y para hacer menos llamadas a ellos
            int x = actual.x;
            int y = actual.y;

            //Mover una celda en la dirección preestable establecida
            switch (this.direccion) {
                case 'S':
                    actual = mapa[x + 1][y];
                    break;
                case 'E':
                    actual = mapa[x][y + 1];
                    break;
                case 'N':
                    actual = mapa[x - 1][y];
                    break;
                case 'W':
                    actual = mapa[x][y - 1];
                    break;
            }
            //Almacene el movimiento en el camino
            camino += this.direccion;
        }

        public boolean checkWall() {
            //Almacene la instancia de x e y para hacer menos llamadas a ellos
            int x = actual.x;
            int y = actual.y;
            casilla target = new casilla();

            //Trate de obtener la siguiente celda en la dirección preestable establecida
            try {
                switch (this.direccion) {
                    case 'S':
                        target = mapa[x + 1][y];
                        break;
                    case 'E':
                        target = mapa[x][y + 1];
                        break;
                    case 'N':
                        target = mapa[x - 1][y];
                        break;
                    case 'W':
                        target = mapa[x][y - 1];
                        break;
                }
            } catch (Exception ignore) {
                //Tenemos que intentarlo en el caso de que no haya ninguna célula en la dirección que estamos mirando
            }

            //Devolver true si la siguiente celda sobre no es un muro, false si es
            return !target.type.equals("wall");


        }

        public casilla teleport() {
            //Retire el teletransporte actual de la lista
            for (int i = 0; i < teleList.size(); i++) {
                if (teleList.get(i) == actual) {
                    teleList.remove(i);
                }
            }

            casilla baseCell = teleList.get(0);
            //Almacene la instancia de x e y para hacer menos llamadas a ellos
            int x = actual.x;
            int y = actual.y;

            //Establezca el teletransporte más cercano predeterminado como el primero de la lista
            int base = (Math.abs(baseCell.x - x)) + (Math.abs(baseCell.y - y));
            int idx = 0;
            //Para cada teletransportador de la lista...
            for (int c = 1; c < teleList.size(); c++) {
                //lo buscamos para un tiempo de ejecución más eficiente
                casilla tmpCell = teleList.get(c);
                //entonces obtenemos la distancia desde el teletransportador actual a la célula actual
                int tmpBase = (Math.abs(tmpCell.x - x)) + (Math.abs(tmpCell.y - y));
                //Si el teletransportador actual está más cerca que el base, establecemos el nuevo como base
                if (tmpBase < base) {
                    idx = c;
                    base = tmpBase;
                    baseCell = teleList.get(0);
                } else if (tmpBase == base) {
                    //Si la distancia es la misma tenemos que obtener la que tiene el ángulo más corto
                    if (preferableTeleport(actual, baseCell, tmpCell)) {
                        //Si el nuevo tiene un ángulo más corto lo establecemos como la nueva base
                        idx = c;
                        baseCell = teleList.get(c);
                    }
                }
            }
            //Cambiamos nuestra posición al teletransporte más cercano
            actual = teleList.get(idx);
            //Añadimos el teletransporte original a la lista para posibles teletransportaciones futuras
            teleList.add(actual);
            return teleList.get(idx);


        }

        public boolean preferableTeleport(casilla reference, casilla cell1, casilla cell2) {
            //Consigue la tangente de dos puntos
            double angle1 = Math.toDegrees(Math.atan2((cell1.y - reference.y), (cell1.x - reference.x)));
            angle1 -= 180;
            double angle2 = Math.toDegrees(Math.atan2((cell2.y - reference.y), (cell2.x - reference.x)));
            angle2 -= 180;

            //Devolver true si el primer ángulo es más grande que el segundo
            return Math.abs(angle1) > Math.abs(angle2);
        }

        public void invert() {
            //Cambiar las prioridades de dirección
            if (!invertir) {
                direcciones = new char[]{
                        'N',
                        'W',
                        'S',
                        'E'
                };
                invertir = true;
            } else {
                direcciones = new char[]{
                        'S',
                        'E',
                        'N',
                        'W'
                };
                invertir = false;
            }
        }

        public boolean infinite() {
            //Como hay 8 posibles tipos de movimientos en nuestra lógica podemos definir que en el caso de que pasemos por el
            //misma célula más de 8 veces hemos entrado en un bucle infinito
            actual.visits++;
            return actual.visits >= 8;
        }

        //Aquí comienza la calculadora de ruta más corta
        public int caminoCorto() {
            //Establecemos la heurística de cada célula
            setHeuristic();
            //Devolvemos la heurística del punto de partida, ya que es la distancia mínima a la meta
            return mapa[comenzar.x][comenzar.y].heuristic;
        }

        public void setHeuristic() {
            //Creamos dos listas temporales para asignar el valor heurístico a cada célula
            List<casilla> openList = new LinkedList<casilla>();
            List<casilla> closedList = new LinkedList<casilla>();
            //Establecemos el valor de la meta como 0
            mapa[meta.x][meta.y].heuristic = 0;
            //Añadimos el objetivo a la lista abierta
            openList.add(mapa[meta.x][meta.y]);
            boolean found = false; //Usamos este booleano para determinar cuándo finalizar el bucle
            //Hasta que encontremos el punto de partida...
            while (!found) {
                //para cada celda de la lista abierta...
                for (casilla cell : openList) {
                    List<casilla> neighbors = assignNeighbors(cell);
                    for (casilla neighbour : neighbors) {
                        //si son el punto de partida terminar el bucle
                        if (neighbour.type.equals("start")) {
                            found = true;
                            break;
                        }
                        //añadirlos a la lista cerrada
                        closedList.add(neighbour);
                    }
                }
                //La lista abierta se convierte en una copia de la lista cerrada
                openList = closedList;
                //Restablecemos la lista cerrada
                closedList = new LinkedList<casilla>();
            }

        }

        public List<casilla> assignNeighbors(casilla cell) {
            //Creamos los vecinos a la celda actual
            List<casilla> neighbors = new LinkedList<>();
            for (int i = 0; i < 4; i++) {
                switch (i) {
                    case 0:
                        actual = mapa[cell.x + 1][cell.y];
                        break;
                    case 1:
                        actual = mapa[cell.x - 1][cell.y];
                        break;
                    case 2:
                        actual = mapa[cell.x][cell.y + 1];
                        break;
                    case 3:
                        actual = mapa[cell.x][cell.y - 1];
                        break;
                }
                //Si el vecino es un teletransportador añadimos su salida en su lugar como el vecino
                if (actual.type.equals("teleport")) {
                    casilla tmp = teleport();
                    mapa[tmp.x][tmp.y].heuristic = cell.heuristic + 1;
                    neighbors.add(tmp);
                    break;
                }
                //Si el vecino es una celda transitable le asignamos la heurística y la añadimos como vecino
                if (actual.heuristic == null && !actual.type.equals("wall")) {
                    actual.heuristic = cell.heuristic + 1;
                    neighbors.add(actual);
                }

            }
            return neighbors;
        }


    }

    class casilla {
        //Estos son los atributos de cada celda, aunque no todos son necesarios
        String type; //Define la función de la celda. Obligatorio
        int x; //Define la columna del mapa en la que se puede encontrar la celda. Obligatorio
        int y; //Define la fila del mapa en el que se puede encontrar la celda. Obligatorio
        Integer heuristic = null; //Define la distancia de la celda al objetivo
        int visits; //Cuenta el número de instancias en las que la ruta de acceso ha pasado por la celda

        public casilla() {

        }

        public casilla(char type, int x, int y) {
            String tmpStr = "";
            switch (type) {
                case '#':
                    tmpStr = "wall";
                    break;
                case 'X':
                    tmpStr = "start";
                    break;
                case '$':
                    tmpStr = "goal";
                    break;
                case 'T':
                    tmpStr = "teleport";
                    break;
                case 'I':
                    tmpStr = "invert";
                    break;
                default:
                    tmpStr = "void";
                    break;
            }
            this.type = tmpStr;

            this.x = x;
            this.y = y;


        }

    }
