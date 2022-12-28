package objects;

import gui.EnvironmentPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Timer;

import utils.ImageToolKitX;

public class SemaphoreObject {

	private final int ANIMATION_DELAY = 20000; // retardo em milissegundos
	private Timer animationTimer; // O timer guia a animacao
	
	public enum SemaphoreSignal {RED, GREEN};
	private Image semaphore_image;
	private int pos_x = 230;
	private int pos_y = 85;
	private File semaphore_green;
	private File semaphore_red;
	private int width;
	private int height;
	private EnvironmentPanel p;
	
	private SemaphoreSignal currentSignal;
	
	public SemaphoreObject(EnvironmentPanel p) {
		this.p = p;
		
		pos_x = 230;
		pos_y = 85;
		width = 60;
		height = 80;
		
		semaphore_green = new File("images/semaforo_verde.png");
		semaphore_red = new File("images/semaforo_vermelho.png");
		
		setSemaphoreSignal(SemaphoreSignal.GREEN);
		startAnimation();
	}
	
	public void draw(Graphics g) {
		g.drawImage(semaphore_image, pos_x, pos_y, width, height, p);
	}
	
	private void setSemaphoreSignal(SemaphoreSignal current) {
    	try {
    		if (current == SemaphoreSignal.GREEN) {
				semaphore_image = ImageToolKitX.makeColorTransparent(ImageIO.read(semaphore_green), new Color(170,170,170));
				this.currentSignal = current;
    		} else if (current == SemaphoreSignal.RED) {
    			semaphore_image = ImageToolKitX.makeColorTransparent(ImageIO.read(semaphore_red), new Color(170,170,170));
				this.currentSignal = current;
    		}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
	
	 // classe interna para tratar eventos de acao do Timer
    private class TimerHandler implements ActionListener 
    {
       // responde ao evento do Timer
       public void actionPerformed( ActionEvent actionEvent )
       {
          changeColor(); // pinta o animator novamente
       } // fim do metodo actionPerformed
       
    } // fim da classe TimerHandler \

    public void changeColor() {
		if (currentSignal == SemaphoreSignal.RED)
			setSemaphoreSignal(SemaphoreSignal.GREEN);
		else setSemaphoreSignal(SemaphoreSignal.RED);
	}
    
 // inicia a animacao ou reinicia se a janela for reexibida
    public void startAnimation()
    {
       if ( animationTimer == null ) 
       {
          // cria o timer                                     
          animationTimer =                                    
             new Timer( ANIMATION_DELAY, new TimerHandler() );

          animationTimer.start(); // inicia o timer
       } // fim do if
       else // animationTimer já existe, reinicia animacao
       {
          if ( ! animationTimer.isRunning())
             animationTimer.restart();
       } // fim de else
    } // fim do metodo startAnimation 
    
	public int getPos_x() {
		return pos_x;
	}

	public void setPos_x(int pos_x) {
		this.pos_x = pos_x;
	}

	public int getPos_y() {
		return pos_y;
	}

	public void setPos_y(int pos_y) {
		this.pos_y = pos_y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public SemaphoreSignal getCurrentSignal() {
		return currentSignal;
	}

	public void setCurrentSignal(SemaphoreSignal currentSignal) {
		this.currentSignal = currentSignal;
	}

	
	
}
