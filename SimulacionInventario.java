import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SimulacionInventario {

    static final int INVENTARIO_INICIAL = 15;
    static final int DIAS = 260;

    static final double COSTO_PEDIDO = 50.0;
    static final double COSTO_MANTENIMIENTO_ANUAL = 26.0;
    static final double COSTO_FALTANTE = 25.0;
    static final double COSTO_MANTENIMIENTO_DIARIO =
            COSTO_MANTENIMIENTO_ANUAL / DIAS;

    static class Pedido {
        int diaLlegada;
        int cantidad;

        Pedido(int diaLlegada, int cantidad) {
            this.diaLlegada = diaLlegada;
            this.cantidad = cantidad;
        }
    }

    static class ResultadoCorrida {
        double costoPedidos;
        double costoMantenimiento;
        double costoFaltantes;
        double costoTotal;

        int numeroPedidos;

        int[] inventario;
        int[] demanda;
        int[] recepcion;
        int[] inventarioInicial;
        int[] faltanteAnterior;
        int[] faltante;
        int[] orden;
        int[] tiempoEntrega;
        int[] diaLlegada;

        double[] rnDemanda;
        double[] rnTiempo;

        double[] costoPedidoDia;
        double[] costoMantenimientoDia;
        double[] costoFaltanteDia;
        double[] costoTotalDia;

        ResultadoCorrida() {
            inventario = new int[DIAS];
            demanda = new int[DIAS];
            recepcion = new int[DIAS];
            inventarioInicial = new int[DIAS];
            faltanteAnterior = new int[DIAS];
            faltante = new int[DIAS];
            orden = new int[DIAS];
            tiempoEntrega = new int[DIAS];
            diaLlegada = new int[DIAS];

            rnDemanda = new double[DIAS];
            rnTiempo = new double[DIAS];

            costoPedidoDia = new double[DIAS];
            costoMantenimientoDia = new double[DIAS];
            costoFaltanteDia = new double[DIAS];
            costoTotalDia = new double[DIAS];
        }
    }

    static class Punto {
        int q;
        int r;
        double costo;

        Punto(int q, int r, double costo) {
            this.q = q;
            this.r = r;
            this.costo = costo;
        }
    }

    static class IteracionHJ {
        int iteracion;
        int q;
        int r;
        double costo;

        IteracionHJ(int iteracion, int q, int r, double costo) {
            this.iteracion = iteracion;
            this.q = q;
            this.r = r;
            this.costo = costo;
        }
    }

    static int generarDemanda(double rn) {

        if (rn < 0.04)
            return 0;
        else if (rn < 0.10)
            return 1;
        else if (rn < 0.20)
            return 2;
        else if (rn < 0.40)
            return 3;
        else if (rn < 0.70)
            return 4;
        else if (rn < 0.88)
            return 5;
        else if (rn < 0.96)
            return 6;
        else if (rn < 0.99)
            return 7;
        else
            return 8;
    }

    static int generarTiempoEntrega(double rn) {

        if (rn < 0.25)
            return 1;
        else if (rn < 0.75)
            return 2;
        else if (rn < 0.95)
            return 3;
        else
            return 4;
    }

    static double[] generarRNDemanda(Random random) {

        double[] rn = new double[DIAS];

        for (int i = 0; i < DIAS; i++)
            rn[i] = random.nextDouble();

        return rn;
    }

    static double[] generarRNTiempo(Random random) {

        double[] rn = new double[DIAS];

        for (int i = 0; i < DIAS; i++)
            rn[i] = random.nextDouble();

        return rn;
    }

    static ResultadoCorrida simular(
            int q,
            int r,
            double[] rnDemanda,
            double[] rnTiempo) {

        ResultadoCorrida resultado =
                new ResultadoCorrida();

        int inventario = INVENTARIO_INICIAL;
        int faltanteAcumulado = 0;

        List<Pedido> pedidosPendientes =
                new ArrayList<>();

        for (int dia = 1; dia <= DIAS; dia++) {

            int indice = dia - 1;

            int recepcion = 0;

            for (int i = pedidosPendientes.size() - 1;
                 i >= 0;
                 i--) {

                Pedido pedido =
                        pedidosPendientes.get(i);

                if (pedido.diaLlegada == dia) {

                    recepcion += pedido.cantidad;

                    pedidosPendientes.remove(i);
                }
            }

            inventario += recepcion;

            resultado.recepcion[indice] =
                    recepcion;

            resultado.inventarioInicial[indice] =
                    inventario;

            resultado.faltanteAnterior[indice] =
                    faltanteAcumulado;

            resultado.rnDemanda[indice] =
                    rnDemanda[indice];

            int demanda =
                    generarDemanda(
                            rnDemanda[indice]
                    );

            resultado.demanda[indice] =
                    demanda;

            if (faltanteAcumulado > 0 &&
                inventario > 0) {

                int atendido =
                        Math.min(
                                inventario,
                                faltanteAcumulado
                        );

                inventario -= atendido;

                faltanteAcumulado -= atendido;
            }

            if (inventario >= demanda) {

                inventario -= demanda;

            } else {

                int faltante =
                        demanda - inventario;

                inventario = 0;

                faltanteAcumulado += faltante;
            }

            resultado.inventario[indice] =
                    inventario;

            resultado.faltante[indice] =
                    faltanteAcumulado;

            int orden = 0;
            int tiempo = 0;
            int llegada = 0;

            double rnT = -1;

            double costoPedido = 0;

            if (inventario <= r) {

                orden = q;

                rnT =
                        rnTiempo[indice];

                tiempo =
                        generarTiempoEntrega(
                                rnT
                        );

                llegada =
                        dia + tiempo;

                pedidosPendientes.add(
                        new Pedido(
                                llegada,
                                q
                        )
                );

                resultado.numeroPedidos++;

                costoPedido =
                        COSTO_PEDIDO;
            }

            double costoMantenimiento =
                    inventario *
                    COSTO_MANTENIMIENTO_DIARIO;

            double costoFaltante =
                    faltanteAcumulado *
                    COSTO_FALTANTE;

            double costoTotalDia =
                    costoPedido +
                    costoMantenimiento +
                    costoFaltante;

            resultado.orden[indice] =
                    orden;

            resultado.tiempoEntrega[indice] =
                    tiempo;

            resultado.diaLlegada[indice] =
                    llegada;

            resultado.rnTiempo[indice] =
                    rnT;

            resultado.costoPedidoDia[indice] =
                    costoPedido;

            resultado.costoMantenimientoDia[indice] =
                    costoMantenimiento;

            resultado.costoFaltanteDia[indice] =
                    costoFaltante;

            resultado.costoTotalDia[indice] =
                    costoTotalDia;

            resultado.costoPedidos +=
                    costoPedido;

            resultado.costoMantenimiento +=
                    costoMantenimiento;

            resultado.costoFaltantes +=
                    costoFaltante;
        }

        resultado.costoTotal =
                resultado.costoPedidos +
                resultado.costoMantenimiento +
                resultado.costoFaltantes;

        return resultado;
    }

    static double evaluarCosto(
            int q,
            int r,
            double[] rnDemanda,
            double[] rnTiempo) {

        if (q < 1 || r < 0)
            return Double.MAX_VALUE;

        ResultadoCorrida resultado =
                simular(
                        q,
                        r,
                        rnDemanda,
                        rnTiempo
                );

        return resultado.costoTotal;
    }

    static Punto exploracion(
        Punto base,
        int pasoQ,
        int pasoR,
        double[] rnDemanda,
        double[] rnTiempo) {

    Punto mejor = base;

    double costoQMas =
            evaluarCosto(
                    base.q + pasoQ,
                    base.r,
                    rnDemanda,
                    rnTiempo
            );

    if (costoQMas < mejor.costo) {
        mejor =
                new Punto(
                        base.q + pasoQ,
                        base.r,
                        costoQMas
                );
    }

    if (base.q - pasoQ >= 1) {

        double costoQMenos =
                evaluarCosto(
                        base.q - pasoQ,
                        base.r,
                        rnDemanda,
                        rnTiempo
                );

        if (costoQMenos < mejor.costo) {
            mejor =
                    new Punto(
                            base.q - pasoQ,
                            base.r,
                            costoQMenos
                    );
        }
    }

    double costoRMas =
            evaluarCosto(
                    base.q,
                    base.r + pasoR,
                    rnDemanda,
                    rnTiempo
            );

    if (costoRMas < mejor.costo) {
        mejor =
                new Punto(
                        base.q,
                        base.r + pasoR,
                        costoRMas
                );
    }

    if (base.r - pasoR >= 0) {

        double costoRMenos =
                evaluarCosto(
                        base.q,
                        base.r - pasoR,
                        rnDemanda,
                        rnTiempo
                );

        if (costoRMenos < mejor.costo) {
            mejor =
                    new Punto(
                            base.q,
                            base.r - pasoR,
                            costoRMenos
                    );
        }
    }

    return mejor;
}
    static Punto hookeJeeves(
            int qInicial,
            int rInicial,
            int pasoInicialQ,
            int pasoInicialR,
            double[] rnDemanda,
            double[] rnTiempo,
            List<IteracionHJ> historial) {
        int pasoQ = pasoInicialQ;
        int pasoR = pasoInicialR;
        int contador = 0;
        Punto base =
                new Punto(
                        qInicial,
                        rInicial,
                        evaluarCosto(
                                qInicial,
                                rInicial,
                                rnDemanda,
                                rnTiempo
                        )
                );
        historial.add(
                new IteracionHJ(
                        contador,
                        base.q,
                        base.r,
                        base.costo
                )
        );

        while (pasoQ >= 1 || pasoR >= 1) {

            Punto explorado =
                    exploracion(
                            base,
                            pasoQ,
                            pasoR,
                            rnDemanda,
                            rnTiempo
                    );

            contador++;

            historial.add(
                    new IteracionHJ(
                            contador,
                            explorado.q,
                            explorado.r,
                            explorado.costo
                    )
            );

            if (explorado.costo < base.costo) {

                int nuevoQ =
                        explorado.q +
                        (explorado.q - base.q);

                int nuevoR =
                        explorado.r +
                        (explorado.r - base.r);

                if (nuevoQ < 1)
                    nuevoQ = 1;

                if (nuevoR < 0)
                    nuevoR = 0;

                double costoPatron =
                        evaluarCosto(
                                nuevoQ,
                                nuevoR,
                                rnDemanda,
                                rnTiempo
                        );

                contador++;

                historial.add(
                        new IteracionHJ(
                                contador,
                                nuevoQ,
                                nuevoR,
                                costoPatron
                        )
                );

                if (costoPatron < explorado.costo) {

                    base =
                            new Punto(
                                    nuevoQ,
                                    nuevoR,
                                    costoPatron
                            );

                } else {

                    base =
                            new Punto(
                                    explorado.q,
                                    explorado.r,
                                    explorado.costo
                            );
                }

            } else {

                pasoQ =
                        pasoQ > 1
                                ? pasoQ / 2
                                : 0;

                pasoR =
                        pasoR > 1
                                ? pasoR / 2
                                : 0;

                contador++;

                historial.add(
                        new IteracionHJ(
                                contador,
                                base.q,
                                base.r,
                                base.costo
                        )
                );
            }
        }

        return base;
    }

    static void mostrarHookeJeeves(
            List<IteracionHJ> historial) {

        System.out.println();
        System.out.println(
                ""
        );

        System.out.println(
                "                 HOOKE-JEEVES"
        );

        System.out.println(
                ""
        );

        System.out.printf(
                "%-10s %-10s %-10s %-15s%n",
                "Iteracion",
                "q",
                "R",
                "Costo total"
        );

        System.out.println(
                ""
        );

        for (IteracionHJ h : historial) {

            System.out.printf(
                    "%-10d %-10d %-10d $%-14.2f%n",
                    h.iteracion,
                    h.q,
                    h.r,
                    h.costo
            );
        }

        System.out.println(
                ""
        );
    }

    static void guardarHookeJeeves(
            List<IteracionHJ> historial) {

        try {

            PrintWriter salida =
                    new PrintWriter(
                            new FileWriter(
                                    "hooke_jeeves.csv"
                            )
                    );

            salida.println(
                    "Iteracion,q,R,CostoTotal"
            );

            for (IteracionHJ h : historial) {

                salida.printf(
                        "%d,%d,%d,%.2f%n",
                        h.iteracion,
                        h.q,
                        h.r,
                        h.costo
                );
            }

            salida.close();

            System.out.println(
                    "Archivo generado: hooke_jeeves.csv"
            );

        } catch (IOException e) {

            System.out.println(
                    "No se pudo guardar hooke_jeeves.csv"
            );
        }
    }

    static void mostrarTabla(
            ResultadoCorrida resultado,
            int q) {

        System.out.println();

        System.out.println(
                "DIA | RN_D | DEM | REC | INV.INI | " +
                "FALT.ANT | INV.FIN | FALT | ORDEN | " +
                "RN_T | L | LLEG | C.PED | C.MANT | " +
                "C.FALT | C.TOTAL"
        );

        System.out.println(
                ""
        );

        for (int i = 0; i < DIAS; i++) {

            System.out.printf(
                    "%3d  %.4f %3d %3d  %7d  " +
                    "%8d  %7d  %4d  %5d  " +
                    "%6s  %1d  %4d  %5.2f  " +
                    "%6.2f  %6.2f  %7.2f%n",

                    i + 1,

                    resultado.rnDemanda[i],

                    resultado.demanda[i],

                    resultado.recepcion[i],

                    resultado.inventarioInicial[i],

                    resultado.faltanteAnterior[i],

                    resultado.inventario[i],

                    resultado.faltante[i],

                    resultado.orden[i],

                    resultado.orden[i] > 0
                            ? String.format(
                                    "%.4f",
                                    resultado.rnTiempo[i])
                            : "-",

                    resultado.tiempoEntrega[i],

                    resultado.diaLlegada[i],

                    resultado.costoPedidoDia[i],

                    resultado.costoMantenimientoDia[i],

                    resultado.costoFaltanteDia[i],

                    resultado.costoTotalDia[i]
            );
        }
    }

    static void guardarCSV(
            ResultadoCorrida resultado,
            int corrida,
            int q,
            int r) {

        String nombre =
                "corrida_" + corrida + ".csv";

        try {

            PrintWriter salida =
                    new PrintWriter(
                            new FileWriter(nombre)
                    );

            salida.println(
                    "Dia,RN_Demanda,Demanda,Recepcion," +
                    "InventarioInicial,FaltanteAnterior," +
                    "InventarioFinal,Faltante,Orden,q," +
                    "RN_Tiempo,TiempoEntrega,DiaLlegada," +
                    "CostoPedido,CostoMantenimiento," +
                    "CostoFaltante,CostoTotal"
            );

            for (int i = 0; i < DIAS; i++) {

                salida.printf(
                        "%d,%.6f,%d,%d,%d,%d,%d,%d,%d,%d," +
                        "%.6f,%d,%d,%.2f,%.2f,%.2f,%.2f%n",

                        i + 1,

                        resultado.rnDemanda[i],

                        resultado.demanda[i],

                        resultado.recepcion[i],

                        resultado.inventarioInicial[i],

                        resultado.faltanteAnterior[i],

                        resultado.inventario[i],

                        resultado.faltante[i],

                        resultado.orden[i],

                        q,

                        resultado.rnTiempo[i],

                        resultado.tiempoEntrega[i],

                        resultado.diaLlegada[i],

                        resultado.costoPedidoDia[i],

                        resultado.costoMantenimientoDia[i],

                        resultado.costoFaltanteDia[i],

                        resultado.costoTotalDia[i]
                );
            }

            salida.close();

            System.out.println(
                    "Archivo generado: " + nombre
            );

        } catch (IOException e) {

            System.out.println(
                    "Error al guardar " + nombre
            );
        }
    }

    static class GraficaInventario
            extends JPanel {

        int[] inventario;
        int r;

        GraficaInventario(
                int[] inventario,
                int r) {

            this.inventario = inventario;
            this.r = r;
        }

        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g;

            int ancho = getWidth();
            int alto = getHeight();

            int izq = 60;
            int der = 30;
            int sup = 40;
            int inf = 50;

            int anchoGrafica =
                    ancho - izq - der;

            int altoGrafica =
                    alto - sup - inf;

            int max = r;

            for (int valor : inventario) {

                if (valor > max)
                    max = valor;
            }

            if (max <= 0)
                max = 1;

            g2.drawLine(
                    izq,
                    sup,
                    izq,
                    alto - inf
            );

            g2.drawLine(
                    izq,
                    alto - inf,
                    ancho - der,
                    alto - inf
            );

            g2.drawString(
                    "Inventario",
                    10,
                    sup
            );

            g2.drawString(
                    "Día",
                    ancho / 2,
                    alto - 15
            );

            g2.setStroke(
                    new BasicStroke(2)
            );

            for (int i = 1;
                 i < inventario.length;
                 i++) {

                int x1 =
                        izq +
                        (i - 1) *
                        anchoGrafica /
                        (DIAS - 1);

                int x2 =
                        izq +
                        i *
                        anchoGrafica /
                        (DIAS - 1);

                int y1 =
                        alto - inf -
                        inventario[i - 1] *
                        altoGrafica / max;

                int y2 =
                        alto - inf -
                        inventario[i] *
                        altoGrafica / max;

                g2.drawLine(
                        x1,
                        y1,
                        x2,
                        y2
                );
            }

            int yR =
                    alto - inf -
                    r * altoGrafica / max;

            g2.drawLine(
                    izq,
                    yR,
                    ancho - der,
                    yR
            );

            g2.drawString(
                    "R = " + r,
                    izq + 5,
                    yR - 5
            );
        }
    }

    static class GraficaCostoDiario
            extends JPanel {

        double[] costos;

        GraficaCostoDiario(
                double[] costos) {

            this.costos = costos;
        }

        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g;

            int ancho = getWidth();
            int alto = getHeight();

            int izq = 60;
            int der = 30;
            int sup = 40;
            int inf = 50;

            int anchoGrafica =
                    ancho - izq - der;

            int altoGrafica =
                    alto - sup - inf;

            double max = 1;

            for (double valor : costos) {

                if (valor > max)
                    max = valor;
            }

            g2.drawLine(
                    izq,
                    sup,
                    izq,
                    alto - inf
            );

            g2.drawLine(
                    izq,
                    alto - inf,
                    ancho - der,
                    alto - inf
            );

            g2.drawString(
                    "Costo diario",
                    10,
                    sup
            );

            g2.drawString(
                    "Día",
                    ancho / 2,
                    alto - 15
            );

            g2.setStroke(
                    new BasicStroke(2)
            );

            for (int i = 1;
                 i < costos.length;
                 i++) {

                int x1 =
                        izq +
                        (i - 1) *
                        anchoGrafica /
                        (DIAS - 1);

                int x2 =
                        izq +
                        i *
                        anchoGrafica /
                        (DIAS - 1);

                int y1 =
                        alto - inf -
                        (int)(
                                costos[i - 1] *
                                altoGrafica /
                                max
                        );

                int y2 =
                        alto - inf -
                        (int)(
                                costos[i] *
                                altoGrafica /
                                max
                        );

                g2.drawLine(
                        x1,
                        y1,
                        x2,
                        y2
                );
            }
        }
    }

    static class GraficaHookeJeeves
            extends JPanel {

        List<IteracionHJ> datos;

        GraficaHookeJeeves(
                List<IteracionHJ> datos) {

            this.datos = datos;
        }

        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g;

            int ancho = getWidth();
            int alto = getHeight();

            int izq = 70;
            int der = 30;
            int sup = 40;
            int inf = 50;

            int anchoGrafica =
                    ancho - izq - der;

            int altoGrafica =
                    alto - sup - inf;

            double max = 1;
            double min = Double.MAX_VALUE;

            for (IteracionHJ dato : datos) {

                if (dato.costo > max)
                    max = dato.costo;

                if (dato.costo < min)
                    min = dato.costo;
            }

            if (max == min)
                max = min + 1;

            g2.drawLine(
                    izq,
                    sup,
                    izq,
                    alto - inf
            );

            g2.drawLine(
                    izq,
                    alto - inf,
                    ancho - der,
                    alto - inf
            );

            g2.drawString(
                    "Costo total",
                    10,
                    sup
            );

            g2.drawString(
                    "Iteración",
                    ancho / 2,
                    alto - 15
            );

            g2.setStroke(
                    new BasicStroke(2)
            );

            for (int i = 1;
                 i < datos.size();
                 i++) {

                int x1 =
                        izq +
                        (i - 1) *
                        anchoGrafica /
                        Math.max(
                                datos.size() - 1,
                                1
                        );

                int x2 =
                        izq +
                        i *
                        anchoGrafica /
                        Math.max(
                                datos.size() - 1,
                                1
                        );

                int y1 =
                        alto - inf -
                        (int)(
                                (datos.get(i - 1).costo - min) *
                                altoGrafica /
                                (max - min)
                        );

                int y2 =
                        alto - inf -
                        (int)(
                                (datos.get(i).costo - min) *
                                altoGrafica /
                                (max - min)
                        );

                g2.drawLine(
                        x1,
                        y1,
                        x2,
                        y2
                );
            }
        }
    }

    static void mostrarResumen(
            ResultadoCorrida resultado,
            int q,
            int r,
            int corrida) {

        System.out.println();

        System.out.println(
                ""
        );

        System.out.println(
                "CORRIDA " + corrida
        );

        System.out.println(
                "q = " + q
        );

        System.out.println(
                "R = " + r
        );

        System.out.printf(
                "Costo de pedidos:       $%.2f%n",
                resultado.costoPedidos
        );

        System.out.printf(
                "Costo mantenimiento:    $%.2f%n",
                resultado.costoMantenimiento
        );

        System.out.printf(
                "Costo faltantes:        $%.2f%n",
                resultado.costoFaltantes
        );

        System.out.printf(
                "Costo total:            $%.2f%n",
                resultado.costoTotal
        );

        System.out.println(
                "Numero de pedidos: " +
                resultado.numeroPedidos
        );

        System.out.println(
                ""
        );
    }

    public static void main(String[] args) {

        Scanner entrada =
                new Scanner(System.in);

        Random random =
                new Random();

        System.out.println(
                ""
        );

        System.out.println(
                "       SISTEMA DE INVENTARIO - SIMULACION"
        );

        System.out.println(
                ""
        );

        System.out.print(
        "q inicial para Hooke-Jeeves: "
);

int qInicial =
        entrada.nextInt();

System.out.print(
        "R inicial para Hooke-Jeeves: "
);

int rInicial =
        entrada.nextInt();

int pasoQ = 1;
int pasoR = 1;

System.out.print(
        "Numero de corridas finales: "
);

int numeroCorridas =
        entrada.nextInt();

if (qInicial <= 0 ||
    rInicial < 0 ||
    numeroCorridas <= 0) {

            System.out.println(
                    "Los valores ingresados no son validos."
            );

            entrada.close();
            return;
        }

        System.out.println();
        System.out.println(
                "Generando numeros aleatorios..."
        );

        double[] rnDemandaHJ =
                generarRNDemanda(random);

        double[] rnTiempoHJ =
                generarRNTiempo(random);

        List<IteracionHJ> historial =
                new ArrayList<>();

        Punto mejor =
                hookeJeeves(
                        qInicial,
                        rInicial,
                        pasoQ,
                        pasoR,
                        rnDemandaHJ,
                        rnTiempoHJ,
                        historial
                );

        mostrarHookeJeeves(
                historial
        );

        guardarHookeJeeves(
                historial
        );

        System.out.println();
        System.out.println(
                ""
        );

        System.out.println(
                "                 SOLUCION ENCONTRADA"
        );

        System.out.println(
                ""
        );

        System.out.println(
                "Mejor q = " + mejor.q
        );

        System.out.println(
                "Mejor R = " + mejor.r
        );

        System.out.printf(
                "Menor costo encontrado = $%.2f%n",
                mejor.costo
        );

        System.out.println(
                ""
        );

        System.out.println();
        System.out.println(
                "Ahora se realizan las corridas finales..."
        );

        double sumaPedidos = 0;
        double sumaMantenimiento = 0;
        double sumaFaltantes = 0;
        double sumaTotal = 0;

        double[] costosCorridas =
                new double[numeroCorridas];

        ResultadoCorrida ultimaCorrida =
                null;

        for (int corrida = 1;
             corrida <= numeroCorridas;
             corrida++) {

            double[] rnDemanda =
                    generarRNDemanda(random);

            double[] rnTiempo =
                    generarRNTiempo(random);

            ResultadoCorrida resultado =
                    simular(
                            mejor.q,
                            mejor.r,
                            rnDemanda,
                            rnTiempo
                    );

            ultimaCorrida =
                    resultado;

            costosCorridas[corrida - 1] =
                    resultado.costoTotal;

            sumaPedidos +=
                    resultado.costoPedidos;

            sumaMantenimiento +=
                    resultado.costoMantenimiento;

            sumaFaltantes +=
                    resultado.costoFaltantes;

            sumaTotal +=
                    resultado.costoTotal;

            mostrarResumen(
                    resultado,
                    mejor.q,
                    mejor.r,
                    corrida
            );

            mostrarTabla(
                    resultado,
                    mejor.q
            );

            guardarCSV(
                    resultado,
                    corrida,
                    mejor.q,
                    mejor.r
            );
        }

        double promedioPedidos =
                sumaPedidos /
                numeroCorridas;

        double promedioMantenimiento =
                sumaMantenimiento /
                numeroCorridas;

        double promedioFaltantes =
                sumaFaltantes /
                numeroCorridas;

        double promedioTotal =
                sumaTotal /
                numeroCorridas;

        System.out.println();
        System.out.println(
                ""
        );

        System.out.println(
                "                 RESULTADO FINAL"
        );

        System.out.println(
                ""
        );

        System.out.println(
                "q optimo encontrado = " +
                mejor.q
        );

        System.out.println(
                "R optimo encontrado = " +
                mejor.r
        );

        System.out.printf(
                "Costo minimo de Hooke-Jeeves = $%.2f%n",
                mejor.costo
        );

        System.out.println();

        System.out.printf(
                "Costo promedio pedidos:       $%.2f%n",
                promedioPedidos
        );

        System.out.printf(
                "Costo promedio mantenimiento: $%.2f%n",
                promedioMantenimiento
        );

        System.out.printf(
                "Costo promedio faltantes:     $%.2f%n",
                promedioFaltantes
        );

        System.out.printf(
                "COSTO PROMEDIO TOTAL:          $%.2f%n",
                promedioTotal
        );

        System.out.println();

        System.out.println(
                "Corridas finales: " +
                numeroCorridas
        );

        System.out.println(
                "Dias por corrida: " +
                DIAS
        );

        System.out.println(
                "Dias simulados: " +
                numeroCorridas * DIAS
        );

        System.out.println(
                ""
        );

        JFrame ventanaInventario =
                new JFrame(
                        "Inventario - q=" +
                        mejor.q +
                        " R=" +
                        mejor.r
                );

        ventanaInventario.setSize(
                900,
                500
        );

        ventanaInventario.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        ventanaInventario.add(
                new GraficaInventario(
                        ultimaCorrida.inventario,
                        mejor.r
                )
        );

        ventanaInventario.setLocation(
                50,
                50
        );

        ventanaInventario.setVisible(
                true
        );

        JFrame ventanaCosto =
                new JFrame(
                        "Costo diario"
                );

        ventanaCosto.setSize(
                900,
                500
        );

        ventanaCosto.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        ventanaCosto.add(
                new GraficaCostoDiario(
                        ultimaCorrida.costoTotalDia
                )
        );

        ventanaCosto.setLocation(
                100,
                100
        );

        ventanaCosto.setVisible(
                true
        );

        JFrame ventanaHJ =
                new JFrame(
                        "Hooke-Jeeves"
                );

        ventanaHJ.setSize(
                900,
                500
        );

        ventanaHJ.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        ventanaHJ.add(
                new GraficaHookeJeeves(
                        historial
                )
        );

        ventanaHJ.setLocation(
                150,
                150
        );

        ventanaHJ.setVisible(
                true
        );

        entrada.close();
    }
}