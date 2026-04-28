import java.awt.geom.Point2D;
import java.util.Locale;

public class main {
    private static final double REFERENCE_HEIGHT_CM = 180.0;

    // Replace these coordinates with values measured from your photo.
    private static final Person REFERENCE = new Person(
            new Point2D.Double(550, 220),
            new Point2D.Double(550, 860));

    private static final Point2D.Double VANISHING_POINT = new Point2D.Double(900, 120);

    private static final Person[] TARGETS = {
            new Person(new Point2D.Double(321.2, 238.6), new Point2D.Double(318.9, 982.4)),
            new Person(new Point2D.Double(701.8, 286.2), new Point2D.Double(698.1, 960.5))
    };

    public static void main(String[] args) {
        System.out.println("=== HeightMeasurement ===");
        System.out.println("Estimate student heights using the Just do it reference student.");
        System.out.println("Reference height = " + REFERENCE_HEIGHT_CM + " cm");
        System.out.println();

        Person reference = REFERENCE;
        Point2D.Double vanishing = VANISHING_POINT;
        Person[] targets = TARGETS;

        if (args.length > 0) {
            if (args.length < 6 || (args.length - 6) % 4 != 0) {
                System.out.println("Usage: java HeightMeasurement refTopX refTopY refBottomX refBottomY vanishX vanishY [targetTopX targetTopY targetBottomX targetBottomY]...");
                return;
            }
            reference = parsePerson(args, 0);
            vanishing = new Point2D.Double(parseDouble(args[4]), parseDouble(args[5]));
            int targetCount = (args.length - 6) / 4;
            targets = new Person[targetCount];
            for (int i = 0; i < targetCount; i++) {
                targets[i] = parsePerson(args, 6 + 4 * i);
            }
        } else {
            System.out.println("Using built-in coordinates. Edit REFERENCE, VANISHING_POINT, and TARGETS in the source for your photo.");
        }

        for (int i = 0; i < targets.length; i++) {
            Person target = targets[i];
            double height = estimateHeight(reference, target, vanishing, REFERENCE_HEIGHT_CM);
            System.out.printf(Locale.US, "Student %d height = %.2f cm (pixel height %.2f, depth ratio %.5f)\n",
                    i + 1, height, target.pixelHeight(), depthRatio(reference.bottom, target.bottom, vanishing));
        }
    }

    private static Person parsePerson(String[] args, int offset) {
        return new Person(new Point2D.Double(parseDouble(args[offset]), parseDouble(args[offset + 1])),
                new Point2D.Double(parseDouble(args[offset + 2]), parseDouble(args[offset + 3])));
    }

    private static double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            System.out.println("Invalid numeric argument: " + text);
            throw e;
        }
    }

    private static double estimateHeight(Person reference, Person target, Point2D.Double vanishing, double referenceHeightCm) {
        double pixelRef = reference.pixelHeight();
        double pixelTarget = target.pixelHeight();
        double depthRatio = depthRatio(reference.bottom, target.bottom, vanishing);
        return referenceHeightCm * (pixelTarget / pixelRef) * depthRatio;
    }

    private static double depthRatio(Point2D.Double refFoot, Point2D.Double targetFoot, Point2D.Double vanishing) {
        double refDistance = refFoot.distance(vanishing);
        double targetDistance = targetFoot.distance(vanishing);
        return targetDistance < 1e-6 ? 1.0 : refDistance / targetDistance;
    }

    private static class Person {
        final Point2D.Double top;
        final Point2D.Double bottom;

        Person(Point2D.Double top, Point2D.Double bottom) {
            this.top = top;
            this.bottom = bottom;
        }

        double pixelHeight() {
            return top.distance(bottom);
        }
    }
}