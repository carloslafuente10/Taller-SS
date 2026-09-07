import java.awt.*;
import java.util.Random;
import java.util.Scanner;
import javax.swing.*;

public class DistribucionTriangular extends JPanel {

    static double a, b, c;

    public static double calcularX(double R1) {

        double p = (b - a) / (c - a);

        if (R1 <= p) {

            return Math.sqrt(
                R1 * (b - a) * (c - a)
            ) + a;

        } else {

            return c - Math.sqrt(
                (1 - R1) * (c - b) * (c - a)
            );
        }
    }

    public static double f(double x) {

        if (x >= a && x <= b) {

            return 2 * (x - a) /
                   ((b - a) * (c - a));

        } else if (x > b && x <= c) {

            return 2 * (x - c) /
                   ((b - c) * (c - a));
        }

        return 0;
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        int ancho = getWidth();
        int alto = getHeight();

        int margenIzq = 60;
        int margenAbajo = 50;
        int margenArriba = 30;

        // Eje X
        g2.drawLine(
            margenIzq,
            alto - margenAbajo,
            ancho - 30,
            alto - margenAbajo
        );

        // Eje Y
        g2.drawLine(
            margenIzq,
            margenArriba,
            margenIzq,
            alto - margenAbajo
        );

        double escalaX =
            (ancho - margenIzq - 30) / (c - a);

        double escalaY =
            (alto - margenArriba - margenAbajo) / 0.4;

        int xAnterior = 0;
        int yAnterior = 0;

        for (int i = 0; i <= 600; i++) {

            double x =
                a + (c - a) * i / 600.0;

            double y = f(x);

            int px =
                margenIzq +
                (int)((x - a) * escalaX);

            int py =
                alto - margenAbajo -
                (int)(y * escalaY);

            if (i > 0) {

                g2.drawLine(
                    xAnterior,
                    yAnterior,
                    px,
                    py
                );
            }

            xAnterior = px;
            yAnterior = py;
        }

        g2.drawString("X",
            ancho - 25,
            alto - margenAbajo + 20);

        g2.drawString("f(X)",
            margenIzq - 35,
            margenArriba);
    }

    public static void main(String[] args) {

        Scanner entrada = new Scanner(System.in);
        Random aleatorio = new Random();

        System.out.println("Ejercicio 2 parte 2 TRIANGULAR");
        System.out.println();

        // ENTRADAS
        System.out.print("Ingrese a: ");
        a = entrada.nextDouble();

        System.out.print("Ingrese b: ");
        b = entrada.nextDouble();

        System.out.print("Ingrese c: ");
        c = entrada.nextDouble();

        System.out.print("Ingrese numero de iteraciones: ");
        int n = entrada.nextInt();

        // Validación básica
        if (a >= b || b >= c) {

            System.out.println(
                "Error: debe cumplirse a < b < c."
            );

            return;
        }

        double p = (b - a) / (c - a);

        System.out.println();
        System.out.println("p = " + p);

        System.out.println();
        System.out.printf(
            "%-5s %-12s %-8s %-12s%n",
            "N", "R1", "Tramo", "X"
        );

        // SIMULACION
        for (int i = 1; i <= n; i++) {

            double R1 = aleatorio.nextDouble();

            double x = calcularX(R1);

            String tramo;

            if (R1 <= p) {
                tramo = "F1";
            } else {
                tramo = "F2";
            }

            System.out.printf(
                "%-5d %-12.6f %-8s %-12.6f%n",
                i,
                R1,
                tramo,
                x
            );
        }

        // GRAFICA
        JFrame ventana =
            new JFrame("Distribución Triangular");

        ventana.add(
            new DistribucionTriangular()
        );

        ventana.setSize(800, 500);

        ventana.setDefaultCloseOperation(
            JFrame.EXIT_ON_CLOSE
        );

        ventana.setLocationRelativeTo(null);

        ventana.setVisible(true);

        entrada.close();
    }
}