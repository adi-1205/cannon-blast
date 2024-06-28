import java.util.ArrayList;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class Game extends Application {

    final int CANVAS_WIDTH = 400;
    final int CANVAS_HEIGHT = 600;
    final int MAX_ALLOWED_TARGETS = 3;
    Canvas canvas;
    Image landscape = new Image("file:src/resources/landscape.jpg");
    Cannon cannon = new Cannon();
    ArrayList<GlowBall> glowBalls = new ArrayList<GlowBall>();
    ArrayList<Target> targets = new ArrayList<Target>();
    long lastShootTime = 0;
    long currentShootTime = 0;
    long lastTargetCreated = 0;
    long currentTargetCreated = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);

        scene.setOnMouseDragged(e -> {
            cannon.moveMouse(e.getSceneX(), CANVAS_WIDTH);
        });

        primaryStage.setScene(scene);
        primaryStage.show();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                currentShootTime = now;
                currentTargetCreated = now;
                drawFrame();
            }
        }.start();
    }

    private void drawFrame() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        shootGlowBall();
        handleTargets();
        drawBackground(gc);

        for (int i = 0; i < glowBalls.size(); i++) {
            GlowBall glowBall = glowBalls.get(i);
            glowBall.update(gc);

            if (glowBall.y < -50) {
                glowBalls.remove(i);
                continue;
            }
            for (int j = 0; j < targets.size(); j++) {
                Target target = targets.get(j);
                double distance = computeDistance(glowBall.x, glowBall.y, target.x, target.y);

                if (distance <= glowBall.radius + target.radius) {
                    glowBalls.remove(i);

                    if (target.alpha < 0.09) {
                        targets.remove(j);
                        continue;
                    }
                    if (target.hits > 1)
                        target.hits--;
                    else
                        target.alpha -= 0.1;
                }
            }
        }

        for (int j = 0; j < targets.size(); j++) {
            Target target = targets.get(j);
            target.update(gc);
        }
        cannon.update(gc);
    }

    void drawBackground(GraphicsContext gc) {
        gc.drawImage(landscape, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    void shootGlowBall() {
        if (currentShootTime - lastShootTime > 1e9 / 3) {
            glowBalls.add(new GlowBall(cannon.bodyMidX, cannon.bodyMidY - cannon.bodyHeight / 4));
            lastShootTime = currentShootTime;
        }
    }

    void handleTargets() {
        if (!(currentTargetCreated - lastTargetCreated > 1e9 / 3))
            return;

        System.out.println("here");
        if (targets.size() < MAX_ALLOWED_TARGETS) {
            targets.add(
                    new Target(
                            generateRandomNumber(100, 300),
                            generateRandomNumber(50, 150),
                            generateRandomNumber(20, 40),
                            generateRandomNumber(1, 10) > 5 ? 2 : -2,
                            0.2,
                            ColorUtils.getRandomColor(),
                            ColorUtils.getRandomColor(),
                            2,
                            generateRandomNumber(2, 6)));

            lastTargetCreated = currentTargetCreated;
        }
    }

    int generateRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    double computeDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance;
    }

    class Cannon {
        double cannonScale = 2.3;
        Image cannon = new Image("file:src/resources/cannon.png"); // 0, 0 | 213, 196
        Image wheelOne = new Image("file:src/resources/wheel.png"); // -36, 104 91 321
        Image wheelTwo = new Image("file:src/resources/wheel.png"); // 84, 104

        double cannonWidth = cannon.getWidth() / cannonScale;
        double cannonHeight = cannon.getHeight() / cannonScale;
        double wheelWidth = wheelOne.getWidth() / cannonScale;
        double wheelHeight = wheelOne.getHeight() / cannonScale;
        double wheelXDiff = 109 / cannonScale;
        double wheelYDiff = 21 / cannonScale;

        double bodyWidth = 213 / cannonScale;
        double bodyHeight = 196 / cannonScale;
        double bodyMidX = 200;
        double bodyMidY = 475;

        double rotationRatio = CANVAS_WIDTH / (wheelWidth * Math.PI);

        void update(GraphicsContext gc) {
            this.draw(gc);
        }

        void draw(GraphicsContext gc) {
            gc.drawImage(cannon, bodyMidX - (cannonWidth / 2), bodyMidY - (cannonHeight /
                    2), cannonWidth, cannonHeight);

            gc.save();
            double wheelOneX = bodyMidX - wheelXDiff;
            double wheelOneY = bodyMidY + wheelYDiff;
            gc.translate(wheelOneX + wheelWidth / 2, wheelOneY + wheelHeight / 2);
            gc.rotate(bodyMidX * rotationRatio);
            gc.drawImage(wheelOne, -wheelWidth / 2, -wheelHeight / 2, wheelWidth, wheelHeight);
            gc.restore();

            gc.save();
            double wheelTwoX = bodyMidX + wheelXDiff - wheelWidth - 2;
            double wheelTwoY = bodyMidY + wheelYDiff;
            gc.translate(wheelTwoX + wheelWidth / 2, wheelTwoY + wheelHeight / 2);
            gc.rotate(bodyMidX * rotationRatio);
            gc.drawImage(wheelTwo, -wheelWidth / 2, -wheelHeight / 2, wheelWidth, wheelHeight);
            gc.restore();
        }

        void moveMouse(double x, double canvasWidth) {
            if (x - wheelXDiff > -1 &&
                    x + wheelXDiff - 3 < canvasWidth) {
                this.bodyMidX = x;
            }
        }
    }

    class GlowBall {
        double x;
        double y;
        double radius = 8;
        double accelerationY = 0.1;
        double velocityY = 13;
        Color glowBallOuterColor = Color.rgb(255, 99, 9);
        Color glowBallCenterColor = Color.rgb(255, 99, 9);

        GlowBall(double x, double y) {
            this.x = x;
            this.y = y;
        }

        void update(GraphicsContext gc) {
            velocityY -= accelerationY;
            y -= velocityY;
            this.draw(gc);
        }

        void draw(GraphicsContext gc) {
            gc.setFill(glowBallOuterColor.deriveColor(0, 1, 1, 0.6));
            gc.setEffect(new GaussianBlur(15));
            gc.fillOval(x - radius - 5, y - radius - 5, 2 * (radius + 5),
                    2 * (radius + 5));
            gc.setEffect(null);
            gc.setFill(glowBallCenterColor);
            gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        }
    }

    class Target {
        double x;
        double y;
        double radius;
        Color fillColor;
        Color strokeColor;
        double strokeWidth;
        double dx; // velocity
        double dy;
        double acceleration = 0.12;
        double rotate = 0;
        int hits;
        double alpha = 1;

        Target(double x, double y, double radius, double dx, double dy, Color fillColor, Color strokeColor,
                double strokeWidth, int hits) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.fillColor = fillColor;
            this.strokeColor = strokeColor;
            this.strokeWidth = strokeWidth;
            this.dx = dx;
            this.dy = dy;
            this.hits = hits;
        }

        void update(GraphicsContext gc) {
            this.draw(gc);
            if (this.y + this.radius + this.strokeWidth > 525) {
                this.dy *= -1;
            } else {
                this.dy += this.acceleration;
            }
            if (this.x + this.radius + this.strokeWidth > CANVAS_WIDTH ||
                    this.x - this.radius - this.strokeWidth < 0) {
                this.dx *= -1;
            }
            this.y += this.dy;
            this.x += this.dx;
            rotate += 0.8;

            if (this.alpha != 1) {
                this.alpha -= 0.07;
            }
        }

        public void draw(GraphicsContext gc) {
            gc.save();
            gc.setGlobalAlpha(this.alpha);
            gc.setFill(fillColor);
            gc.setStroke(strokeColor);
            gc.setLineWidth(strokeWidth);

            double ovalX = x - radius;
            double ovalY = y - radius;
            double ovalWidth = 2 * radius;
            double ovalHeight = 2 * radius;

            gc.fillOval(ovalX, ovalY, ovalWidth, ovalHeight);
            gc.strokeOval(ovalX, ovalY, ovalWidth, ovalHeight);

            double centerX = x;
            double centerY = y;

            gc.translate(x, y);
            gc.rotate(rotate);
            gc.translate(-x, -y);

            gc.setFont(new Font(this.radius - this.radius / 10));
            gc.setFill(Color.WHITE);

            String number = "" + hits;
            double textWidth = gc.getFont().getSize() * number.length() / 2;
            double textHeight = gc.getFont().getSize() / 2;
            gc.fillText(number, centerX - textWidth / 2, centerY + textHeight / 2);
            gc.restore();
        }
    }

    class ColorUtils {
        private static final Color[] beautifulColors = {
                Color.web("#E63946"), // Red
                // Color.web("#F1FAEE"), // White
                Color.web("#A8DADC"), // Blue
                Color.web("#457B9D"), // Navy
                Color.web("#1D3557"), // Dark Blue
                Color.web("#FF9F1C"), // Orange
                Color.web("#06D6A0"), // Green
                Color.web("#118AB2"), // Dark Cyan
                Color.web("#073B4C"), // Dark Teal
                Color.web("#B5838D") // Light Pink
        };

        public static Color getRandomColor() {
            int randomIndex = (int) (Math.random() * beautifulColors.length);
            return beautifulColors[randomIndex];
        }
    }
}