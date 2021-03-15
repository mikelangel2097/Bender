import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class Bender {
        //We initiate the global variables
        casilla comenzar; //The initial position
        casilla meta; //The target position
        casilla actual; //The current position
        casilla[][] mapa; //All the possible positions

        boolean invertir = false; //The current direction priority
        char[] direcciones = new char[]{
                'S',
                'E',
                'N',
                'W'
        }; //The default direction priority
        char direccion = direcciones[0]; //The current direction, by default South
        String camino = ""; //Here we store the direction in which we take every movement

        List<casilla> teleList = new ArrayList<casilla>(); //List of the teleporters


        public Bender(String mapa) {
            String[] split = mapa.split("\n"); //We store the map as a array of rows, we define the rows by the line jumps('\n')

            //We have the rows, now to make the map we need the columns
            int maximo = 0;
            for (String s : split) {
                if (s.length() > maximo) {
                    maximo = s.length();//The longest column
                }
            }

            this.mapa = new casilla[split.length][maximo];//The map is a empty bi-dimensional array

            //Create a new cell for each position on the map and store it in its respective position
            for (int i = 0; i < split.length; i++) {
                for (int j = 0; j < split[i].length(); j++) {

                    this.mapa[i][j] = new casilla(split[i].charAt(j), i, j);

                    //Store the start, goal and teleport cells
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

        //Here starts the brute force method
        public String run() {
            if (!testMap()) {
                return null;
            }
            //Establish the current cell at the starting point
            actual = comenzar;
            //Check if we are at the goal, if not...
            while (actual != meta) {
                //look forward, if not a wall...
                if (checkWall()) {
                    //move forward
                    move();
                    //If we moved into a teleporter...
                    if (actual.type.equals("teleport")) {
                        //teleport
                        teleport();
                    }
                    //If we moved into a invert cell
                    if (actual.type.equals("invert")) {
                        //invert
                        invert();
                    }

                } else {
                    //If we see a wall turn around until we don't see one
                    turn();
                }
                //Check if we are in a infinite loop
                if (infinite()) {
                    return null;
                }
            }
            //Return the path to the goal
            return camino;
        }

        public boolean testMap() {
            //This function checks some basic map requirements like having a goal
            if (teleList.size() == 1) {
                return false;
            }
            if (comenzar == null) {
                return false;

            }
            return meta != null;
        }

        public void turn() {
            //In each pre-defined direction...
            for (char c : direcciones) {
                //look forward for a wall
                if (checkWall()) {
                    //If there is no wall we're done
                    break;
                }
                //If there is a wall try the next direction
                direccion = c;
            }
        }

        public void move() {
            //Store the instance of x and y in order to make less calls to them
            int x = actual.x;
            int y = actual.y;

            //Move one cell in the pre established direction
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
            //Store the movement in the path
            camino += this.direccion;
        }

        public boolean checkWall() {
            //Store the instance of x and y in order to make less calls to them
            int x = actual.x;
            int y = actual.y;
            casilla target = new casilla();

            //Try to get the next cell in the pre established direction
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
                //We have to try it in the case that there is no cell in the direction we are looking at
            }

            //Return true if the next cell over is not a wall, false if it is
            return !target.type.equals("wall");


        }

        public casilla teleport() {
            //Remove the current teleport from the list
            for (int i = 0; i < teleList.size(); i++) {
                if (teleList.get(i) == actual) {
                    teleList.remove(i);
                }
            }

            casilla baseCell = teleList.get(0);
            //Store the instance of x and y in order to make less calls to them
            int x = actual.x;
            int y = actual.y;

            //Set the default closest teleport as the first of the list
            int base = (Math.abs(baseCell.x - x)) + (Math.abs(baseCell.y - y));
            int idx = 0;
            //For each teleporter in the list...
            for (int c = 1; c < teleList.size(); c++) {
                //we instance it for a more efficient run-time
                casilla tmpCell = teleList.get(c);
                //we then get the distance from the current teleporter to the current cell
                int tmpBase = (Math.abs(tmpCell.x - x)) + (Math.abs(tmpCell.y - y));
                //If the current teleporter is closer than the base one we set the new one as the base one
                if (tmpBase < base) {
                    idx = c;
                    base = tmpBase;
                    baseCell = teleList.get(0);
                } else if (tmpBase == base) { //If the distance is the same we have to obtain the one with the shortest angle
                    if (preferableTeleport(actual, baseCell, tmpCell)) {
                        //If the new one has a shorter angle we set it as the new base
                        idx = c;
                        baseCell = teleList.get(c);
                    }
                }
            }
            //We change our position to the closest teleport
            actual = teleList.get(idx);
            //We add the original teleport back to the list for potential future teleportations
            teleList.add(actual);
            return teleList.get(idx);


        }

        public boolean preferableTeleport(casilla reference, casilla cell1, casilla cell2) {
            //Get the tangent of two points
            double angle1 = Math.toDegrees(Math.atan2((cell1.y - reference.y), (cell1.x - reference.x)));
            angle1 -= 180;
            double angle2 = Math.toDegrees(Math.atan2((cell2.y - reference.y), (cell2.x - reference.x)));
            angle2 -= 180;

            //Return true if the first angle is bigger than the second one
            return Math.abs(angle1) > Math.abs(angle2);
        }

        public void invert() {
            //Switch the direction priorities
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
            //As there are 8 possible types of movements in our logic we can define that in the case that we pass by the
            // same cell more than 8 times we have entered a infinite loop
            actual.visits++;
            return actual.visits >= 8;
        }

        //Here starts the shortest path calculator
        public int caminoCorto() {
            //We set the heuristic of each cell
            setHeuristic();
            //We return the heuristic of the starting point as it is the minimum distance to the goal
            return mapa[comenzar.x][comenzar.y].heuristic;
        }

        public void setHeuristic() {
            //We create two temporary lists in order to assign the heuristic value to each cell
            List<casilla> openList = new LinkedList<casilla>();
            List<casilla> closedList = new LinkedList<casilla>();
            //We set the value of the goal as 0
            mapa[meta.x][meta.y].heuristic = 0;
            //We add the goal to the open list
            openList.add(mapa[meta.x][meta.y]);
            boolean found = false; //We use this boolean to determine when to end the loop
            //Until we find the starting point...
            while (!found) {
                //for each cell in the open list...
                for (casilla cell : openList
                ) {
                    //get it's neighbors...
                    List<casilla> neighbors = assignNeighbors(cell);
                    //for each neighbour...
                    for (casilla neighbour : neighbors) {
                        //if they are the starting point end the loop
                        if (neighbour.type.equals("start")) {
                            found = true;
                            break;
                        }
                        //add them to the closed list
                        closedList.add(neighbour);
                    }
                }
                //The open list becomes a copy of the closed list
                openList = closedList;
                //We reset the closed list
                closedList = new LinkedList<casilla>();
            }

        }

        public List<casilla> assignNeighbors(casilla cell) {
            //We create the neighbors to the current cell
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
                //If the neighbour is a teleporter we add it's exit instead as the neighbour
                if (actual.type.equals("teleport")) {
                    casilla tmp = teleport();
                    mapa[tmp.x][tmp.y].heuristic = cell.heuristic + 1;
                    neighbors.add(tmp);
                    break;
                }
                //If the neighbour is a walkable cell we assign it the heuristic and add it as a neighbour
                if (actual.heuristic == null && !actual.type.equals("wall")) {
                    actual.heuristic = cell.heuristic + 1;
                    neighbors.add(actual);
                }

            }
            return neighbors;
        }


    }

    class casilla {
        //This are the attributes of each cell, although not all of them are necessary
        String type; //Defines the function of the cell. Required
        int x; //Defines the column of the map in which the cell can be found. Required
        int y; //Defines the row of the map in which the cell can be found. Required
        Integer heuristic = null; //Defines the distance from the cell to the goal
        int visits; //Counts the number of instances in which the path has gone through the cell

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
