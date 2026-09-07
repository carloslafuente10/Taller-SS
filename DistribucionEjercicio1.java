import java.awt.*;
import java.util.Random;
import java.util.Scanner;
import javax.swing.*;

public class DistribucionEjercicio1 extends JPanel {

    // Funcion de densidad
    public static double f(double x) {
        if (x >= 0 && x <= 6) {
            return Math.pow(x - 3, 2) / 18.0;
        }

        return 0;
    }

    // Funcion acumulada
    public static double F(double x) {
        if (x >= 0 && x <= 6) {
            return (Math.pow(x - 3, 3) + 27) / 54.0;
        }

        return 0;
    }

    // Transformada inversa
    public static double calcularX(double R) {

        return 3 + Math.cbrt(54 * R - 27);
    }

    // Dibujar la grafica
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

        // Escalas
        double escalaX =
            (ancho - margenIzq - 30) / 6.0;

        double escalaY =
            (alto - margenArriba - margenAbajo) / 0.55;

        int xAnterior = 0;
        int yAnterior = 0;

        // Dibujar la funcion
        for (int i = 0; i <= 600; i++) {

            double x = 6.0 * i / 600.0;

            double y = f(x);

            int px =
                margenIzq +
                (int)(x * escalaX);

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

        // Etiquetas
        g2.drawString(
            "X",
            ancho - 25,
            alto - margenAbajo + 20
        );

        g2.drawString(
            "f(X)",
            margenIzq - 35,
            margenArriba
        );

        g2.drawString(
            "0",
            margenIzq - 5,
            alto - margenAbajo + 15
        );

        g2.drawString(
            "3",
            margenIzq + (int)(3 * escalaX) - 5,
            alto - margenAbajo + 15
        );

        g2.drawString(
            "6",
            margenIzq + (int)(6 * escalaX) - 5,
            alto - margenAbajo + 15
        );
    }

    public static void main(String[] args) {

        Scanner entrada = new Scanner(System.in);
        Random aleatorio = new Random();

        System.out.println(
            " EJERCICIO 1 Parte 1 "
        );

        System.out.println(
            "Metodo de Transformada Inversa"
        );

        System.out.println();

        // Entrada
        System.out.print(
            "Ingrese numero de iteraciones: "
        );

        int n = entrada.nextInt();

        System.out.println();

        System.out.printf(
            "%-5s %-12s %-12s %-12s %-12s%n",
            "N", "R", "X", "F(X)", "f(X)"
        );

        // Simulación
        for (int i = 1; i <= n; i++) {

            // Generar número aleatorio
            double R = aleatorio.nextDouble();

            // Transformada inversa
            double x = calcularX(R);

            // Valores de comprobacion
            double Fx = F(x);
            double fx = f(x);

            System.out.printf(
                "%-5d %-12.6f %-12.6f %-12.6f %-12.6f%n",
                i,
                R,
                x,
                Fx,
                fx
            );
        }

        // Crear ventana de la grafica
        JFrame ventana =
            new JFrame(
                "Ejercicio 1 Parte 1 "
            );

        ventana.add(
            new DistribucionEjercicio1()
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