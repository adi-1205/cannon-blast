package application;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class Game extends Application {
    private static final int CANVAS_WIDTH = 400;
    private static final int CANVAS_HEIGHT = 600;
    private Canvas canvas;
    Image landscape = new Image("file:src/resorces/landscape.jpg");
    
    Cannon c = new Cannon();

    public static void main(String[] args) {
        launch(args);
    }
    
    

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);

        initGameObjects();
        
      
        scene.setOnKeyPressed(e -> {
            KeyCode keyCode = e.getCode();
            switch (keyCode) {
                case LEFT:
                    c.moveLeft();
                    break;
                case RIGHT:
                    c.moveRight(CANVAS_WIDTH);
                    break;
                default:
                    break;
            }
        });

        primaryStage.setTitle("Archer");
        primaryStage.setScene(scene);
        primaryStage.show();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                drawFrame();
            }
        }.start();
    }

    private void initGameObjects() {
    	
    }

    private void drawFrame() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
//        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        drawBackground(gc);
        c.update(gc);
    }

    private void drawBackground(GraphicsContext gc) {
//        gc.setFill(Color.web("#222"));
//        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    	gc.drawImage(landscape, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }
}

class Cannon{
	
	int speed = 10;
	double cannonScale = 2.5;
	double x=200, y=435;
	
	Image cannon = new Image("file:src/resorces/cannon.png"); // 0, 0 | 213, 196
	Image wheelOne = new Image("file:src/resorces/wheel.png"); //-36, 104
	Image wheelTwo = new Image("file:src/resorces/wheel.png"); // 84, 104
	
	double cannonWidth = cannon.getWidth();
	double cannonHeight = cannon.getHeight();
	
	double wheelHeight = wheelOne.getHeight();
	double wheelWidth = wheelOne.getWidth();

	double bodyWidth = 213/cannonScale;
	double bodyHeight = 196/cannonScale;
	double bodyMidX = (x + (cannonWidth/(2*cannonScale)));
	
	double wheelOneX = x-(36/cannonScale), wheelOneY = y + (104/cannonScale);
	double wheelTwoX =  x+(84/cannonScale), wheelTwoY = y + (104/cannonScale);
	
	double rotationAngle = 0.0; 
	double rotationIncrement = 15.0; 
	
	 
	void update(GraphicsContext gc) {
		this.updateWheel();
		bodyMidX = (x + (cannonWidth/(2*cannonScale)));
		this.draw(gc);
	}
	
	void draw(GraphicsContext gc) {
		gc.drawImage(cannon, x, y, cannonWidth/cannonScale, cannonHeight/cannonScale);
		
//		gc.save();
//		gc.setFill(Color.RED);
//		gc.fillOval((x + (cannonWidth/(2*cannonScale))) - 2, y - 2, 4, 4);
//		gc.restore();
		
		gc.save();
        gc.drawImage(cannon, x, y, cannonWidth / cannonScale, cannonHeight / cannonScale);
        gc.translate(wheelOneX + wheelWidth / (2 * cannonScale), wheelOneY + wheelHeight / (2 * cannonScale));
        gc.rotate(rotationAngle);
        gc.drawImage(wheelOne, -wheelWidth / (2 * cannonScale), -wheelHeight / (2 * cannonScale),
                     wheelWidth / cannonScale, wheelHeight / cannonScale);
        gc.restore();

        gc.save();
        gc.translate(wheelTwoX + wheelWidth / (2 * cannonScale), wheelTwoY + wheelHeight / (2 * cannonScale));
        gc.rotate(rotationAngle);
        gc.drawImage(wheelTwo, -wheelWidth / (2 * cannonScale), -wheelHeight / (2 * cannonScale),
                     wheelWidth / cannonScale, wheelHeight / cannonScale);
        gc.restore();
	}
	
	void updateWheel() {
		wheelOneX = x - (36/cannonScale); 
		wheelOneY = y + (104/cannonScale);
		wheelTwoX = x + (84/cannonScale);
		wheelTwoY = y + (104/cannonScale);
	}
	
	void moveRight(double canvasWidth) {
		if((bodyMidX + (bodyWidth/2)) < canvasWidth) {
			x+=speed;
            rotationAngle += rotationIncrement;
		}
	}
	
	void moveLeft() {
		if((bodyMidX - (bodyWidth/2)) > 5) {
			x-=speed;
            rotationAngle -= rotationIncrement;
		}
	}
}

