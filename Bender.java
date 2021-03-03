public class Bender {
    char[][] map;
    int posRobotX;
    int posRobotY;

    //Constructor: ens passen el mapa en forma d'String
    public Bender (String mapa){
        String[] s = mapa.split("\n");
        char[][] mp = new char[s.length][];
        for (int i = 0; i < s.length; i++) {
            mp[i] = s[i].toCharArray();
        }
        this.map = mp;
    }
    //Navega fins a l'objectiu (<<$>>).
    //El valor retornat pel metode consisteix en una cadena de
    //caracters on cada lletra pot tenir
    //els valors <<S>>, <<N>>, <<W>> o <<E>>,
    //segons la posicio del robot a cada moment.
    public String run(){



        return "";
    }

    private int [] LocalitzarX(){
        int[] coordenadas = new int[2];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 'X'){
                    coordenadas[0] = i;
                    coordenadas[1] = j;
                    return coordenadas;
                }
            }
        }
        return null;
    }

    private void MoureX(char direccions){
        switch (direccions){
            case 'S':
                posRobotX += 1;
                break;
            case 'E':
                posRobotY += 1;
                break;
            case 'N':
                posRobotX -= 1;
                break;
            case 'W':
                posRobotY -= 1;
                break;
        }
    }

    private boolean ComprobarObstacul(char direccio){
        switch (direccio){
            case 'S':
                return map[posRobotX + 1][posRobotY] == '#';
            case 'E':
                return map[posRobotX][posRobotY + 1] == '#';
            case 'N':
                return map[posRobotX - 1][posRobotY] == '#';
            case 'W':
                return map[posRobotX][posRobotY - 1] == '#';
        }
        return true;
    }
}
