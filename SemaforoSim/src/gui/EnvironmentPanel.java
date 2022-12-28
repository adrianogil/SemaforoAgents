package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import objects.CarByStatus;
import objects.CarObject;
import objects.SemaphoreObject;
import objects.CarByStatus;
import objects.CarObject.CarStatus;
import objects.SemaphoreObject.SemaphoreSignal;
import utils.ImageToolKitX;
import agents.EnvironmentAgent;

public class EnvironmentPanel extends JPanel {

	
	private final int totalPhotoImages = 1;
	private ImageIcon backgroundImage;
	private Image gradBox;
	
	private Image photoImages[];
	private int width; // largura da imagem
	private int height; // altura da imagem
	private final int ANIMATION_DELAY = 50; // retardo em milissegundos
	
	// About Car
	CarObject car;
	
	// About Semaphore
	SemaphoreObject semaphore;
	
	private Timer animationTimer; // O timer guia a animacao
	private EnvironmentAgent agentEnv;
	
	public EnvironmentPanel() {
		// TODO Auto-generated constructor stub
		backgroundImage = new ImageIcon(getClass().getResource("../images/street_background.png"));
		width = backgroundImage.getIconWidth();
		height = backgroundImage.getIconHeight();
		
		// Generating a semaphore
		semaphore = new SemaphoreObject(this);
		
		photoImages = new Image[totalPhotoImages];
		
		File gradBoxFile = new File("images/grad_box.png");
		File photoFiles[] = new File[totalPhotoImages];
		photoFiles[0] = new File("images/cop.png");
		try {
			gradBox = ImageIO.read(gradBoxFile);
			gradBox = ImageToolKitX.makeColorTransparent(gradBox, new Color(170,170,170));
			for (int count = 0; count < totalPhotoImages; count++) {
				photoImages[count] = ImageIO.read(photoFiles[count]);
				photoImages[count] = ImageToolKitX.makeColorTransparent(photoImages[count], new Color(170,170,170));
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		backgroundImage.paintIcon(this, g, 0, 0);
		
		if (semaphore != null)
			semaphore.draw(g);
			
		if (car != null && semaphore != null)
			car.draw(g, semaphore.getPos_x(), semaphore.getPos_y());
		
//		g.drawImage(gradBox, 500, 0, 140, 120, this);
//		
//		g.drawImage(photoImages[0], 505, 5, 50, 70, this );
//		
		if (agentEnv != null && car != null && agentEnv.getStatusCar() != null) {
			if (agentEnv.getStatusCar().getCarStatus() == CarStatus.CAR_PASSED_RED_SEMAPHORE)
				agentEnv.getStatusCar().setCarStatus(CarStatus.KNOWN_VIOLATOR);
			else agentEnv.getStatusCar().setCarStatus(car.getCurrentStatus());
		}
		
	}
	
	// retorna tamanho preferido da animacao    
    public Dimension getPreferredSize()      
    {                                        
       return new Dimension( width, height );
    } // fim do metodo getPreferredSize      
   
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

    // para o timer de animacao 
    public void stopAnimation()
    {
       animationTimer.stop();
    } // fim do metodo stopAnimation 

    // retorna o tamanho minimo de animacao
    public Dimension getMinimumSize()  
    {                                  
       return getPreferredSize();      
    } // fim do metodo getMinimumSize     

    // retorna tamanho preferido da animacao       

    // classe interna para tratar eventos de acao do Timer
    private class TimerHandler implements ActionListener 
    {
       // responde ao evento do Timer
       public void actionPerformed( ActionEvent actionEvent )
       {
          repaint(); // pinta o animator novamente
       } // fim do metodo actionPerformed
    } // fim da classe TimerHandler \
    
    /**
     * Retorna valor atual 
     * @return
     */
	public SemaphoreSignal getCurrentSemaphoreSignal() {
		return semaphore.getCurrentSignal();
	}

	public CarObject getCar() {
		return car;
	}

	public void setCar(CarObject car) {
		this.car = car;
	}


	public void setEnvironmentAgent(EnvironmentAgent agentEnv) {
		this.agentEnv = agentEnv;
		
	}
	
	public void removeCar() {
		System.out.println("Tentando remover carro da GUI");
		agentEnv.sendFinishWarn2Car(car.getIdentifier());
		agentEnv.setStatusCar(null);
		car = null;
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
   
}
