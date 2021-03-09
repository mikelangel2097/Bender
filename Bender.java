public class Bender {
    private char[][] tablero;
    private final int alturaMapa;
    private final int anchoMapa;
    private final String mapa;
    private int verticalX;
    private int horizontalX;
    private String direccion = "SENW";

    //Constructor Bender
    public Bender(String mapa) {
        this.mapa = mapa;
        //LLamada a funciones para la obtencion de datos
        alturaMapa = CalcularAltoMapa();
        anchoMapa = CalcularAnchoMapa();
        tablero = CrearMapa();
        int[] posicionX = BuscarX();
        //Valores de la localizacion de X
        verticalX = posicionX[0];
        horizontalX = posicionX[1];
    }

    //Metodo para calcular la altura del mapa
    private int CalcularAltoMapa() {
        int contador = 0;
        for (int i = 0; i < mapa.length(); i++) {
            if (mapa.charAt(i) == '\n') {
                contador++;
            }
        }
        //Sumamos un valor para compensar la ultima linea sin el caracter del salto de linea \n
        contador++;
        return contador;
    }

    //Metodo para calcular el ancho del mapa
    private int CalcularAnchoMapa() {
        int linea = 0;
        int lineaGrande = 0;
        for (int j = 0; j < mapa.length() ; j++) {
            if(mapa.charAt(j) != '\n'){
                linea++;
            }else{
                //Siempre hay que reiniciar la variable lina cuando hay un salto de linea
                if(linea > lineaGrande){
                    lineaGrande = linea;
                    linea = 0;
                }else{
                    linea = 0;
                }
            }
        }
        return lineaGrande;
    }

    //Metodo para crear el mapa en forma de array multidimensional
    private char[][] CrearMapa() {
        tablero = new char[alturaMapa][anchoMapa];
        int linea = 0;
        for (int j = 0; j < alturaMapa ; j++) {
            int i = 0;
            while (mapa.charAt(i+linea) != '\n') {
                //Controlamos si estamos en la ultima fila y en el ultimo caracter del mapa.
                if(j == alturaMapa-1 && i+linea == mapa.length()-1){
                    tablero[j][i] = mapa.charAt(i+linea);
                    break;
                }
                tablero[j][i] = mapa.charAt(i+linea);
                i++;
            }
            //Le sumamos a linea el valor de i mas 1 para evitar introducir el caracter \n
            linea += i+1 ;
        }
        return tablero;
    }

    //Navega fins a l'objectiu (<<$>>).
    //El valor retornat pel metode consisteix en una cadena de
    //caracters on cada lletra pot tenir
    //els valors <<S>>, <<N>>, <<W>> o <<E>>,
    //segons la posicio del robot a cada moment.
    public String run(){
        String resultado = "";
        int contador = 0;
        int pasos = 0;

        while (tablero[verticalX][horizontalX] != '$'){
            if (pasos > AreaMapa()){
                return "El robot esta perdut ";
            } else if (EsInversor()){
                contador = 0;
                direccion = InvertirDirecciones();
            } else if (EsTeleporter()){
                HacerTeletransporte();
            }

            if (EsPared(direccion.charAt(contador))){
                contador = 0;
                contador = SolventarPared(contador);
            }
            resultado += direccion.charAt(contador);
            MovemosX(direccion.charAt(contador));
            pasos++;
        }
        return resultado;
    }

    //Buscamos la posicion del robot en el mapa
    private int [] BuscarX(){
        int[] coordenadas = new int[2];
        for (int i = 0; i < alturaMapa; i++) {
            for (int j = 0; j < anchoMapa; j++) {
                if (tablero[i][j] == 'X'){
                    coordenadas[0] = i;
                    coordenadas[1] = j;
                    return coordenadas;
                }
            }
        }
        return null;
    }

    //Movemos al robot en todas direcciones
    private void MovemosX(char direccions){
        switch (direccions){
            case 'S':
                verticalX += 1;
                break;
            case 'E':
                horizontalX += 1;
                break;
            case 'N':
                verticalX -= 1;
                break;
            case 'W':
                horizontalX -= 1;
                break;
        }
    }

    //Comprobamos que la siguiente posicion, si hay una pared o no
    private boolean EsPared(char direccio){
        switch (direccio){
            case 'S':
                return tablero[verticalX + 1][horizontalX] == '#';
            case 'E':
                return tablero[verticalX][horizontalX + 1] == '#';
            case 'N':
                return tablero[verticalX - 1][horizontalX] == '#';
            case 'W':
                return tablero[verticalX][horizontalX - 1] == '#';
        }
        return false;
    }

    //En caso que si haya una pared, movemos al robot una direccion
    private int SolventarPared(int contador){
        boolean obstaculs = EsPared(direccion.charAt(contador));
        while (obstaculs){
            obstaculs = EsPared(direccion.charAt(contador));
            contador = (obstaculs) ? contador + 1 : contador;
        }
        return contador;
    }

    //Calculamos el area del mapa total
    private int AreaMapa() {
        int area = 0;
        for (int i = 0; i < tablero.length; i++) {
            for (int j = 0; j < tablero[i].length; j++) {
                //Si el valor es igual a un espacio, entonces al valo del atributo are se le suma 1
                if (tablero[i][j] == ' ') {
                    area++;
                }
            }
        }
        return area;
    }

    //Comprobamos si en el camino se encuentra un Inversor
    private boolean EsInversor(){
        return tablero[verticalX][horizontalX] == 'I';
    }

    //Si se encuentra un Inversor, invertimos las posiciones de movimiento ya predeterminadas anteriormente
    private String InvertirDirecciones(){
        String cambio;

        cambio = direccion.substring(0, 2);
        direccion = direccion.substring(2, 4);

        direccion += cambio;

        return direccion;
    }

    //Comprobamos si hay teletransportador
    private boolean EsTeleporter() {
        return tablero[verticalX][horizontalX] == 'T';
    }

    private void HacerTeletransporte(){
        for (int i = 0; i < alturaMapa; i++) {
            for (int j = 0; j < anchoMapa; j++) {
                if (tablero[i][j] == 'T' && i != verticalX && j != horizontalX){
                    verticalX = i;
                    horizontalX = j;
                    return;
                }
            }
        }
    }

}
