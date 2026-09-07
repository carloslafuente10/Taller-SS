import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import javax.swing.*;

public class InventarioEjercicio2 {

    // PARAMETROS DEL PROBLEMA
    static final int DIAS = 260;
    static final int INVENTARIO_INICIAL = 15;

    // Costos
    static final double COSTO_PEDIDO = 100;
    static final double COSTO_MANTENIMIENTO = 26;
    static final double COSTO_ESPERA = 20;
    static final double COSTO_DEMANDA_PERDIDA = 50;

    // DISTRIBUCIONES DE PROBABILIDAD

    // Demanda diaria: 25 a 34
    static final int[] DEMANDAS = {
        25, 26, 27, 28, 29,
        30, 31, 32, 33, 34
    };

    static final double[] PROB_DEMANDA = {
        0.02, 0.04, 0.06, 0.12, 0.20,
        0.24, 0.15, 0.10, 0.05, 0.02
    };

    // Tiempo de entrega: 1 a 4 dias
    static final int[] TIEMPOS_ENTREGA = {1, 2, 3, 4};

    static final double[] PROB_ENTREGA = {
        0.20, 0.30, 0.25, 0.25
    };

    // Tiempo maximo de espera: 0 a 4 dias
    static final int[] TIEMPOS_ESPERA = {0, 1, 2, 3, 4};

    static final double[] PROB_ESPERA = {
        0.40, 0.20, 0.15, 0.15, 0.10
    };


    // CLASE PARA GUARDAR EL RESULTADO DE UNA SIMULACION

    static class Resultado {

        double costoTotal;

        double costoPedido;
        double costoMantenimiento;
        double costoEspera;
        double costoDemandaPerdida;

        int[] demanda = new int[DIAS];
        int[] recepcion = new int[DIAS];
        int[] inventarioInicial = new int[DIAS];
        int[] faltanteAnterior = new int[DIAS];
        int[] inventarioDisponible = new int[DIAS];
        int[] inventarioFinal = new int[DIAS];
        int[] faltante = new int[DIAS];

        int[] tiempoEspera = new int[DIAS];
        int[] tiempoEntrega = new int[DIAS];
        int[] diaLlegada = new int[DIAS];

        int[] clienteEspera = new int[DIAS];
        int[] demandaPerdida = new int[DIAS];

        String[] ordenar = new String[DIAS];
        int[] cantidadPedido = new int[DIAS];

        double[] costoPedidoDia = new double[DIAS];
        double[] costoMantenimientoDia = new double[DIAS];
        double[] costoEsperaDia = new double[DIAS];
        double[] costoDemandaPerdidaDia = new double[DIAS];
        double[] costoTotalDia = new double[DIAS];
    }


    // TRANSFORMADA INVERSA

    static int generarDemanda(double rn) {

        double acumulada = 0;

        for (int i = 0; i < DEMANDAS.length; i++) {

            acumulada += PROB_DEMANDA[i];

            if (rn < acumulada) {
                return DEMANDAS[i];
            }
        }

        return DEMANDAS[DEMANDAS.length - 1];
    }


    static int generarTiempoEntrega(double rn) {

        double acumulada = 0;

        for (int i = 0; i < TIEMPOS_ENTREGA.length; i++) {

            acumulada += PROB_ENTREGA[i];

            if (rn < acumulada) {
                return TIEMPOS_ENTREGA[i];
            }
        }

        return TIEMPOS_ENTREGA[TIEMPOS_ENTREGA.length - 1];
    }


    static int generarTiempoEspera(double rn) {

        double acumulada = 0;

        for (int i = 0; i < TIEMPOS_ESPERA.length; i++) {

            acumulada += PROB_ESPERA[i];

            if (rn < acumulada) {
                return TIEMPOS_ESPERA[i];
            }
        }

        return TIEMPOS_ESPERA[TIEMPOS_ESPERA.length - 1];
    }


    // GENERACION DE NUMEROS ALEATORIOS

    static double[] generarRNDemanda(Random random) {

        double[] rn = new double[DIAS];

        for (int i = 0; i < DIAS; i++) {
            rn[i] = random.nextDouble();
        }

        return rn;
    }


    static double[] generarRNTiempoEntrega(Random random) {

        double[] rn = new double[DIAS];

        for (int i = 0; i < DIAS; i++) {
            rn[i] = random.nextDouble();
        }

        return rn;
    }


    static double[] generarRNTiempoEspera(Random random) {

        double[] rn = new double[DIAS];

        for (int i = 0; i < DIAS; i++) {
            rn[i] = random.nextDouble();
        }

        return rn;
    }


    // SIMULACION DE LOS 260 DIAS

    static Resultado simular(
            int q,
            int R,
            double[] rnDemanda,
            double[] rnEntrega,
            double[] rnEspera) {

        Resultado res = new Resultado();

        int inventario = INVENTARIO_INICIAL;

        for (int dia = 0; dia < DIAS; dia++) {

            // 1. RECEPCION DE PEDIDOS PENDIENTES

            int recepcion = 0;

            for (int d = 0; d < dia; d++) {

                if (res.diaLlegada[d] == dia + 1) {
                    recepcion += res.cantidadPedido[d];
                }
            }

            res.recepcion[dia] = recepcion;


            // 2. INVENTARIO INICIAL DEL DIA

            res.inventarioInicial[dia] = inventario + recepcion;


            // 3. FALTANTE DEL DIA ANTERIOR

            if (dia == 0) {
                res.faltanteAnterior[dia] = 0;
            } else {
                res.faltanteAnterior[dia] = res.faltante[dia - 1];
            }


            // 4. INVENTARIO DISPONIBLE

            res.inventarioDisponible[dia] =
                    Math.max(
                            res.inventarioInicial[dia]
                                    - res.faltanteAnterior[dia],
                            0);


            // 5. GENERAR DEMANDA

            res.demanda[dia] =
                    generarDemanda(rnDemanda[dia]);


            // 6. CALCULAR INVENTARIO FINAL Y FALTANTE

            if (res.demanda[dia] <= res.inventarioDisponible[dia]) {

                res.inventarioFinal[dia] =
                        res.inventarioDisponible[dia]
                                - res.demanda[dia];

                res.faltante[dia] = 0;

            } else {

                res.inventarioFinal[dia] = 0;

                res.faltante[dia] =
                        res.demanda[dia]
                                - res.inventarioDisponible[dia];
            }


            // 7. GENERAR TIEMPO DE ESPERA

            res.tiempoEspera[dia] =
                    generarTiempoEspera(rnEspera[dia]);


            // 8. GENERAR TIEMPO DE ENTREGA

            res.tiempoEntrega[dia] =
                    generarTiempoEntrega(rnEntrega[dia]);


            // 9. DETERMINAR SI EL CLIENTE ESPERA

            res.clienteEspera[dia] =
                    Math.min(
                            res.faltante[dia],
                            res.tiempoEspera[dia]);


            // 10. DEMANDA PERDIDA

            res.demandaPerdida[dia] =
                    Math.max(
                            res.faltante[dia]
                                    - res.clienteEspera[dia],
                            0);


            // 11. REGLA DE REORDEN

            if (res.inventarioFinal[dia] <= R) {

                res.ordenar[dia] = "SI";

                res.cantidadPedido[dia] = q;

                res.diaLlegada[dia] =
                        dia + 1 + res.tiempoEntrega[dia];

            } else {

                res.ordenar[dia] = "NO";

                res.cantidadPedido[dia] = 0;

                res.diaLlegada[dia] = 0;
            }


            // 12. COSTOS

            if (res.ordenar[dia].equals("SI")) {

                res.costoPedidoDia[dia] =
                        COSTO_PEDIDO;

            } else {

                res.costoPedidoDia[dia] = 0;
            }


            res.costoMantenimientoDia[dia] =
                    res.inventarioFinal[dia]
                            * (COSTO_MANTENIMIENTO / DIAS);


            res.costoEsperaDia[dia] =
                    res.clienteEspera[dia]
                            * COSTO_ESPERA;


            res.costoDemandaPerdidaDia[dia] =
                    res.demandaPerdida[dia]
                            * COSTO_DEMANDA_PERDIDA;


            res.costoTotalDia[dia] =
                    res.costoPedidoDia[dia]
                            + res.costoMantenimientoDia[dia]
                            + res.costoEsperaDia[dia]
                            + res.costoDemandaPerdidaDia[dia];


            res.costoPedido += res.costoPedidoDia[dia];

            res.costoMantenimiento +=
                    res.costoMantenimientoDia[dia];

            res.costoEspera +=
                    res.costoEsperaDia[dia];

            res.costoDemandaPerdida +=
                    res.costoDemandaPerdidaDia[dia];

            res.costoTotal +=
                    res.costoTotalDia[dia];


            inventario = res.inventarioFinal[dia];
        }

        return res;
    }


    // HOOKE-JEEVES

    static int[] hookeJeeves(
            int qInicial,
            int rInicial,
            int numeroIteraciones,
            double[] rnDemanda,
            double[] rnEntrega,
            double[] rnEspera,
            ArrayList<Integer> historialIteracion,
            ArrayList<Double> historialCosto) {

        int qActual = qInicial;
        int rActual = rInicial;

        int pasoQ = 1;
        int pasoR = 1;

        double mejorCosto =
                simular(
                        qActual,
                        rActual,
                        rnDemanda,
                        rnEntrega,
                        rnEspera).costoTotal;


        System.out.println();
        System.out.println("");
        System.out.println("           ALGORITMO DE HOOKE-JEEVES");
        System.out.println("");

        System.out.println(
                "Punto inicial: q = "
                        + qActual
                        + ", R = "
                        + rActual
                        + ", Costo = "
                        + mejorCosto);

        System.out.println();

        System.out.println(
                String.format(
                        "%-10s %-10s %-10s %-15s %-15s",
                        "Iteracion",
                        "q",
                        "R",
                        "Costo",
                        "Resultado"));

        System.out.println(
                "");


        for (int iteracion = 1;
             iteracion <= numeroIteraciones;
             iteracion++) {

            int qBase = qActual;
            int rBase = rActual;

            int mejorQ = qBase;
            int mejorR = rBase;


            // q

            int qMas = qBase + pasoQ;

            double costoQMas =
                    simular(
                            qMas,
                            rBase,
                            rnDemanda,
                            rnEntrega,
                            rnEspera).costoTotal;

            historialIteracion.add(iteracion);
            historialCosto.add(costoQMas);

            System.out.println(
                    String.format(
                            "%-10d %-10d %-10d %-15.2f %-15s",
                            iteracion,
                            qMas,
                            rBase,
                            costoQMas,
                            "q + paso"));


            if (costoQMas < mejorCosto) {

                mejorCosto = costoQMas;
                mejorQ = qMas;
                mejorR = rBase;
            }


            // q - paso

            int qMenos =
                    Math.max(qBase - pasoQ, 1);

            double costoQMenos =
                    simular(
                            qMenos,
                            rBase,
                            rnDemanda,
                            rnEntrega,
                            rnEspera).costoTotal;

            historialIteracion.add(iteracion);
            historialCosto.add(costoQMenos);

            System.out.println(
                    String.format(
                            "%-10d %-10d %-10d %-15.2f %-15s",
                            iteracion,
                            qMenos,
                            rBase,
                            costoQMenos,
                            "q - paso"));


            if (costoQMenos < mejorCosto) {

                mejorCosto = costoQMenos;
                mejorQ = qMenos;
                mejorR = rBase;
            }


            // R

            int rMas = rBase + pasoR;

            double costoRMas =
                    simular(
                            qBase,
                            rMas,
                            rnDemanda,
                            rnEntrega,
                            rnEspera).costoTotal;

            historialIteracion.add(iteracion);
            historialCosto.add(costoRMas);

            System.out.println(
                    String.format(
                            "%-10d %-10d %-10d %-15.2f %-15s",
                            iteracion,
                            qBase,
                            rMas,
                            costoRMas,
                            "R + paso"));


            if (costoRMas < mejorCosto) {

                mejorCosto = costoRMas;
                mejorQ = qBase;
                mejorR = rMas;
            }


            // R - paso

            int rMenos =
                    Math.max(rBase - pasoR, 0);

            double costoRMenos =
                    simular(
                            qBase,
                            rMenos,
                            rnDemanda,
                            rnEntrega,
                            rnEspera).costoTotal;

            historialIteracion.add(iteracion);
            historialCosto.add(costoRMenos);

            System.out.println(
                    String.format(
                            "%-10d %-10d %-10d %-15.2f %-15s",
                            iteracion,
                            qBase,
                            rMenos,
                            costoRMenos,
                            "R - paso"));


            if (costoRMenos < mejorCosto) {

                mejorCosto = costoRMenos;
                mejorQ = qBase;
                mejorR = rMenos;
            }


            // MOVIMIENTO DE PATRON

            if (mejorQ != qBase || mejorR != rBase) {

                int direccionQ =
                        mejorQ - qBase;

                int direccionR =
                        mejorR - rBase;

                int qPatron =
                        Math.max(
                                mejorQ + direccionQ,
                                1);

                int rPatron =
                        Math.max(
                                mejorR + direccionR,
                                0);


                double costoPatron =
                        simular(
                                qPatron,
                                rPatron,
                                rnDemanda,
                                rnEntrega,
                                rnEspera).costoTotal;

                historialIteracion.add(iteracion);
                historialCosto.add(costoPatron);


                System.out.println(
                        String.format(
                                "%-10d %-10d %-10d %-15.2f %-15s",
                                iteracion,
                                qPatron,
                                rPatron,
                                costoPatron,
                                "Movimiento patron"));


                if (costoPatron < mejorCosto) {

                    qActual = qPatron;
                    rActual = rPatron;
                    mejorCosto = costoPatron;

                } else {

                    qActual = mejorQ;
                    rActual = mejorR;
                }

            } else {

                qActual = mejorQ;
                rActual = mejorR;
            }


            System.out.println(
                    "  -> Mejor punto de iteracion "
                            + iteracion
                            + ": q = "
                            + qActual
                            + ", R = "
                            + rActual
                            + ", Costo = "
                            + mejorCosto);

            System.out.println();
        }


        System.out.println("");
        System.out.println("RESULTADO FINAL DE HOOKE-JEEVES");
        System.out.println("");

        System.out.println(
                "q optimo encontrado = "
                        + qActual);

        System.out.println(
                "R optimo encontrado = "
                        + rActual);

        System.out.println(
                String.format(
                        "Costo minimo encontrado = %.2f",
                        mejorCosto));

        System.out.println("");


        return new int[]{qActual, rActual};
    }


    //mostramos

    static void mostrarTabla(Resultado r) {

        System.out.println();
        System.out.println("");

        System.out.println(
                "                                            SIMULACION FINAL - 260 DIAS");

        System.out.println("");


        System.out.println(
                String.format(
                        "%-5s %-8s %-8s %-9s %-9s %-10s %-10s %-9s %-9s %-8s %-8s %-8s %-9s %-9s %-9s",
                        "Dia",
                        "Dem.",
                        "Recep.",
                        "Inv.Ini",
                        "Falt.Ant",
                        "Inv.Disp.",
                        "Inv.Final",
                        "Falt.",
                        "Espera",
                        "Ent.",
                        "Lleg.",
                        "Cliente",
                        "Perdida",
                        "Orden",
                        "Cant."));


        System.out.println("");


        for (int i = 0; i < DIAS; i++) {

            System.out.println(
                    String.format(
                            "%-5d %-8d %-8d %-9d %-9d %-10d %-10d %-9d %-9d %-8d %-8d %-9d %-9d %-9s %-9d",
                            i + 1,
                            r.demanda[i],
                            r.recepcion[i],
                            r.inventarioInicial[i],
                            r.faltanteAnterior[i],
                            r.inventarioDisponible[i],
                            r.inventarioFinal[i],
                            r.faltante[i],
                            r.tiempoEspera[i],
                            r.tiempoEntrega[i],
                            r.diaLlegada[i],
                            r.clienteEspera[i],
                            r.demandaPerdida[i],
                            r.ordenar[i],
                            r.cantidadPedido[i]));
        }


        System.out.println();
        System.out.println("");
        System.out.println("RESUMEN DE COSTOS");
        System.out.println("");

        System.out.printf(
                "Costo total de pedidos:       $%.2f%n",
                r.costoPedido);

        System.out.printf(
                "Costo total mantenimiento:    $%.2f%n",
                r.costoMantenimiento);

        System.out.printf(
                "Costo total por espera:       $%.2f%n",
                r.costoEspera);

        System.out.printf(
                "Costo total demanda perdida:  $%.2f%n",
                r.costoDemandaPerdida);

        System.out.printf(
                "COSTO TOTAL:                  $%.2f%n",
                r.costoTotal);

        System.out.println("");
    }


    // GRAFICOS

    static class GraficoPanel extends JPanel {

        String titulo;
        String etiquetaX;
        String etiquetaY;
        double[] valores;
        boolean costos;

        GraficoPanel(
                String titulo,
                String etiquetaX,
                String etiquetaY,
                double[] valores,
                boolean costos) {

            this.titulo = titulo;
            this.etiquetaX = etiquetaX;
            this.etiquetaY = etiquetaY;
            this.valores = valores;
            this.costos = costos;

            setBackground(Color.WHITE);
        }


        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g;

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);


            int ancho = getWidth();
            int alto = getHeight();

            int margenIzq = 70;
            int margenDer = 30;
            int margenSup = 55;
            int margenInf = 65;


            int anchoGrafico =
                    ancho - margenIzq - margenDer;

            int altoGrafico =
                    alto - margenSup - margenInf;


            double max = 0;

            for (double valor : valores) {

                if (valor > max) {
                    max = valor;
                }
            }


            if (max == 0) {
                max = 1;
            }


            g2.setColor(Color.BLACK);

            g2.drawString(
                    titulo,
                    ancho / 2 - 100,
                    25);


            g2.drawLine(
                    margenIzq,
                    margenSup,
                    margenIzq,
                    alto - margenInf);

            g2.drawLine(
                    margenIzq,
                    alto - margenInf,
                    ancho - margenDer,
                    alto - margenInf);


            int cantidadPuntos = valores.length;


            int anteriorX = 0;
            int anteriorY = 0;


            for (int i = 0; i < cantidadPuntos; i++) {

                int x =
                        margenIzq
                                + (i * anchoGrafico)
                                / (cantidadPuntos - 1);


                int y =
                        alto - margenInf
                                - (int)
                                ((valores[i] / max)
                                        * altoGrafico);


                if (i > 0) {

                    g2.drawLine(
                            anteriorX,
                            anteriorY,
                            x,
                            y);
                }


                anteriorX = x;
                anteriorY = y;
            }


            g2.drawString(
                    etiquetaX,
                    ancho / 2 - 20,
                    alto - 20);


            g2.drawString(
                    etiquetaY,
                    10,
                    margenSup - 10);


            g2.drawString(
                    "0",
                    margenIzq - 25,
                    alto - margenInf + 5);


            String maxTexto;

            if (costos) {
                maxTexto = String.format("$%.2f", max);
            } else {
                maxTexto = String.format("%.0f", max);
            }


            g2.drawString(
                    maxTexto,
                    margenIzq - 55,
                    margenSup + 5);


            g2.drawString(
                    "1",
                    margenIzq,
                    alto - margenInf + 20);

            g2.drawString(
                    String.valueOf(cantidadPuntos),
                    ancho - margenDer - 20,
                    alto - margenInf + 20);
        }
    }


    static void mostrarGrafico(
            String titulo,
            String etiquetaX,
            String etiquetaY,
            double[] valores,
            boolean costos) {

        JFrame ventana =
                new JFrame(titulo);

        ventana.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE);

        ventana.setSize(900, 550);

        ventana.setLocationByPlatform(true);

        ventana.add(
                new GraficoPanel(
                        titulo,
                        etiquetaX,
                        etiquetaY,
                        valores,
                        costos));

        ventana.setVisible(true);
    }


    static void mostrarGraficos(
            Resultado r,
            ArrayList<Integer> historialIteracion,
            ArrayList<Double> historialCosto) {

        double[] inventario =
                new double[DIAS];

        double[] demanda =
                new double[DIAS];

        double[] costos =
                new double[DIAS];


        for (int i = 0; i < DIAS; i++) {

            inventario[i] =
                    r.inventarioFinal[i];

            demanda[i] =
                    r.demanda[i];

            costos[i] =
                    r.costoTotalDia[i];
        }


        mostrarGrafico(
                "Inventario final por dia",
                "Dia",
                "Inventario final",
                inventario,
                false);


        mostrarGrafico(
                "Demanda por dia",
                "Dia",
                "Demanda",
                demanda,
                false);


        mostrarGrafico(
                "Costo total por dia",
                "Dia",
                "Costo total",
                costos,
                true);


        double[] costosHJ =
                new double[historialCosto.size()];


        for (int i = 0;
             i < historialCosto.size();
             i++) {

            costosHJ[i] =
                    historialCosto.get(i);
        }


        mostrarGrafico(
                "Evolucion del costo mediante Hooke-Jeeves",
                "Evaluacion",
                "Costo total",
                costosHJ,
                true);
    }


    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("");
        System.out.println("             SISTEMA DE INVENTARIOS - EJERCICIO 2");
        System.out.println("");

        //datos iniciales

        System.out.print(
                "Ingrese la cantidad inicial q: ");

        int qInicial =
                scanner.nextInt();


        System.out.print(
                "Ingrese el punto de reorden inicial R: ");

        int rInicial =
                scanner.nextInt();


        System.out.print(
                "Ingrese el numero de iteraciones de Hooke-Jeeves: ");

        int numeroIteraciones =
                scanner.nextInt();


        if (qInicial < 1) {
            qInicial = 1;
        }

        if (rInicial < 0) {
            rInicial = 0;
        }

        if (numeroIteraciones < 1) {
            numeroIteraciones = 1;
        }


        //numeros aleatorios

        Random random = new Random();

        double[] rnDemanda =
                generarRNDemanda(random);

        double[] rnEntrega =
                generarRNTiempoEntrega(random);

        double[] rnEspera =
                generarRNTiempoEspera(random);


        //aplicar jeeves

        ArrayList<Integer> historialIteracion =
                new ArrayList<>();

        ArrayList<Double> historialCosto =
                new ArrayList<>();


        int[] solucion =
                hookeJeeves(
                        qInicial,
                        rInicial,
                        numeroIteraciones,
                        rnDemanda,
                        rnEntrega,
                        rnEspera,
                        historialIteracion,
                        historialCosto);


        int qOptimo = solucion[0];
        int rOptimo = solucion[1];


        Resultado resultadoFinal =
                simular(
                        qOptimo,
                        rOptimo,
                        rnDemanda,
                        rnEntrega,
                        rnEspera);


        mostrarTabla(resultadoFinal);


        mostrarGraficos(
                resultadoFinal,
                historialIteracion,
                historialCosto);


        scanner.close();
    }
}