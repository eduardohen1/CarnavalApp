import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class JogoCarnaval extends JFrame {
    // Constantes do jogo
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;
    private static final int VELOCIDADE_FOLIA = 5;
    private static final int RAIO_CONFETE = 10;
    private static final int MAX_CONFETES = 100;
    
    // Componentes do jogo
    private JPanel painelJogo;
    private Timer timer;
    private Foliao foliao;
    private ArrayList<Confete> confetes;
    private ArrayList<Serpentina> serpentinas;
    private Random random;
    private int pontuacao = 0;
    private boolean jogoIniciado = false;
    private Clip clipMusica;
    
    // Construtor
    public JogoCarnaval() {
        super("Folia de Carnaval");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(LARGURA, ALTURA);
        setResizable(false);
        setLocationRelativeTo(null);
        
        // Inicialização
        random = new Random();
        confetes = new ArrayList<>();
        serpentinas = new ArrayList<>();
        
        // Painel principal do jogo
        painelJogo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                desenharJogo(g);
            }
        };
        painelJogo.setBackground(new Color(100, 200, 255)); // Azul claro para o céu
        painelJogo.setFocusable(true);
        add(painelJogo);
        
        // Criar o folião
        foliao = new Foliao(LARGURA / 2, ALTURA - 100);
        
        // Configurar controles
        configurarControles();
        
        // Timer para animação do jogo (60 FPS)
        timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atualizarJogo();
                painelJogo.repaint();
            }
        });
        
        // Tela inicial
        exibirTelaInicial();
        
        // Carregar e tocar música
        carregarMusica();
    }
    
    // Configuração dos controles de teclado
    private void configurarControles() {
        painelJogo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                
                if (!jogoIniciado) {
                    iniciarJogo();
                    return;
                }
                
                if (key == KeyEvent.VK_LEFT) {
                    foliao.moverEsquerda();
                } else if (key == KeyEvent.VK_RIGHT) {
                    foliao.moverDireita();
                } else if (key == KeyEvent.VK_SPACE) {
                    foliao.pular();
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                
                if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
                    foliao.parar();
                }
            }
        });
    }
    
    // Exibir tela inicial
    private void exibirTelaInicial() {
        painelJogo.add(new JLabel(new ImageIcon(getClass().getResource("fundo_carnaval.png"))));
        
        JLabel titulo = new JLabel("FOLIA DE CARNAVAL");
        titulo.setFont(new Font("Arial", Font.BOLD, 36));
        titulo.setForeground(Color.YELLOW);
        titulo.setBounds(LARGURA/2 - 200, 100, 400, 50);
        painelJogo.add(titulo);
        
        JLabel instrucoes = new JLabel("Pressione qualquer tecla para iniciar");
        instrucoes.setFont(new Font("Arial", Font.PLAIN, 18));
        instrucoes.setForeground(Color.WHITE);
        instrucoes.setBounds(LARGURA/2 - 150, 400, 300, 30);
        painelJogo.add(instrucoes);
        
        painelJogo.setLayout(null);
    }
    
    // Iniciar o jogo
    private void iniciarJogo() {
        jogoIniciado = true;
        painelJogo.removeAll();
        painelJogo.revalidate();
        timer.start();
        
        // Iniciar música
        if (clipMusica != null) {
            clipMusica.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }
    
    // Carregar e reproduzir música de marchinha
    private void carregarMusica() {
        try {
            URL url = getClass().getResource("/marchinha.wav");
            if (url != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                clipMusica = AudioSystem.getClip();
                clipMusica.open(audioIn);
                // Volume reduzido
                FloatControl gainControl = (FloatControl) clipMusica.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-10.0f);
            }
        } catch (Exception e) {
            System.out.println("Erro ao carregar música: " + e.getMessage());
        }
    }
    
    // Atualizar a lógica do jogo
    private void atualizarJogo() {
        // Atualizar posição do folião
        foliao.atualizar();
        
        // Limitar folião à tela
        if (foliao.getX() < 0) {
            foliao.setX(0);
        } else if (foliao.getX() > LARGURA - foliao.getLargura()) {
            foliao.setX(LARGURA - foliao.getLargura());
        }
        
        // Criar novos confetes aleatoriamente
        if (random.nextInt(10) < 3) {
            Confete confete = new Confete(
                random.nextInt(LARGURA),
                0,
                new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256))
            );
            confetes.add(confete);
        }
        
        // Criar serpentinas aleatoriamente
        if (random.nextInt(50) < 1) {
            Serpentina serpentina = new Serpentina(
                random.nextInt(LARGURA),
                0,
                random.nextInt(100) + 100,
                new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256))
            );
            serpentinas.add(serpentina);
        }
        
        // Atualizar e verificar colisão com confetes
        for (int i = confetes.size() - 1; i >= 0; i--) {
            Confete confete = confetes.get(i);
            confete.atualizar();
            
            // Verificar colisão com folião
            if (foliao.colideComConfete(confete)) {
                pontuacao += 10;
                confetes.remove(i);
                continue;
            }
            
            // Remover confetes que saíram da tela
            if (confete.getY() > ALTURA) {
                confetes.remove(i);
            }
        }
        
        // Atualizar serpentinas
        for (int i = serpentinas.size() - 1; i >= 0; i--) {
            Serpentina serpentina = serpentinas.get(i);
            serpentina.atualizar();
            
            // Remover serpentinas que saíram da tela
            if (serpentina.getY() > ALTURA) {
                serpentinas.remove(i);
            }
        }
        
        // Limitar número máximo de objetos
        while (confetes.size() > MAX_CONFETES) {
            confetes.remove(0);
        }
    }
    
    // Desenhar todos os elementos do jogo
    private void desenharJogo(Graphics g) {
        // Desenhar fundo com degradê
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gradiente = new GradientPaint(
            0, 0, new Color(100, 200, 255),
            0, ALTURA, new Color(255, 200, 100)
        );
        g2d.setPaint(gradiente);
        g2d.fillRect(0, 0, LARGURA, ALTURA);
        
        // Desenhar serpentinas
        for (Serpentina serpentina : serpentinas) {
            serpentina.desenhar(g);
        }
        
        // Desenhar confetes
        for (Confete confete : confetes) {
            confete.desenhar(g);
        }
        
        // Desenhar folião
        foliao.desenhar(g);
        
        // Desenhar pontuação
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Pontos: " + pontuacao, 20, 30);
    }
    
    // Classe para representar o folião (jogador)
    private class Foliao {
        private int x, y;
        private int velocidadeX = 0;
        private int velocidadeY = 0;
        private int largura = 50;
        private int altura = 80;
        private boolean pulando = false;
        
        public Foliao(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public void moverEsquerda() {
            velocidadeX = -VELOCIDADE_FOLIA;
        }
        
        public void moverDireita() {
            velocidadeX = VELOCIDADE_FOLIA;
        }
        
        public void parar() {
            velocidadeX = 0;
        }
        
        public void pular() {
            if (!pulando) {
                velocidadeY = -15;
                pulando = true;
            }
        }
        
        public void atualizar() {
            x += velocidadeX;
            y += velocidadeY;
            
            // Aplicar gravidade se estiver pulando
            if (pulando) {
                velocidadeY += 1; // Gravidade
                
                // Verificar se voltou ao chão
                if (y >= ALTURA - 100) {
                    y = ALTURA - 100;
                    velocidadeY = 0;
                    pulando = false;
                }
            }
        }
        
        public void desenhar(Graphics g) {
            // Corpo do folião
            g.setColor(Color.RED);
            g.fillRect(x, y, largura, altura);
            
            // Cabeça
            g.setColor(new Color(255, 223, 196));
            g.fillOval(x + 5, y - 25, 40, 40);
            
            // Olhos
            g.setColor(Color.BLACK);
            g.fillOval(x + 15, y - 15, 6, 6);
            g.fillOval(x + 30, y - 15, 6, 6);
            
            // Sorriso
            g.drawArc(x + 15, y - 10, 20, 10, 0, -180);
            
            // Chapéu de carnaval
            g.setColor(Color.YELLOW);
            int[] xPontos = {x + 5, x + 25, x + 45};
            int[] yPontos = {y - 25, y - 45, y - 25};
            g.fillPolygon(xPontos, yPontos, 3);
            
            // Enfeite do chapéu
            g.setColor(Color.MAGENTA);
            g.fillOval(x + 22, y - 45, 6, 6);
        }
        
        public boolean colideComConfete(Confete confete) {
            int confeteX = confete.getX();
            int confeteY = confete.getY();
            
            return (confeteX >= x && confeteX <= x + largura &&
                    confeteY >= y - 25 && confeteY <= y + altura);
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public int getLargura() { return largura; }
        public void setX(int x) { this.x = x; }
    }
    
    // Classe para representar os confetes
    private class Confete {
        private int x, y;
        private Color cor;
        private int velocidade;
        
        public Confete(int x, int y, Color cor) {
            this.x = x;
            this.y = y;
            this.cor = cor;
            this.velocidade = random.nextInt(3) + 2;
        }
        
        public void atualizar() {
            y += velocidade;
            // Movimento oscilante horizontal
            x += Math.sin(y / 30.0) * 2;
        }
        
        public void desenhar(Graphics g) {
            g.setColor(cor);
            g.fillOval(x - RAIO_CONFETE / 2, y - RAIO_CONFETE / 2, RAIO_CONFETE, RAIO_CONFETE);
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
    }
    
    // Classe para representar as serpentinas
    private class Serpentina {
        private int x, y;
        private int comprimento;
        private Color cor;
        private int velocidade;
        private ArrayList<Point> pontos;
        
        public Serpentina(int x, int y, int comprimento, Color cor) {
            this.x = x;
            this.y = y;
            this.comprimento = comprimento;
            this.cor = cor;
            this.velocidade = random.nextInt(2) + 3;
            this.pontos = new ArrayList<>();
            
            // Inicializar pontos da serpentina
            for (int i = 0; i < comprimento / 5; i++) {
                pontos.add(new Point(x, y - i * 5));
            }
        }
        
        public void atualizar() {
            y += velocidade;
            
            // Atualizar todos os pontos da serpentina
            for (int i = 0; i < pontos.size(); i++) {
                Point p = pontos.get(i);
                p.y += velocidade;
                // Movimento ondulante
                p.x = x + (int)(Math.sin((p.y + i * 10) / 30.0) * 20);
            }
        }
        
        public void desenhar(Graphics g) {
            g.setColor(cor);
            
            // Desenhar linha que conecta os pontos
            for (int i = 1; i < pontos.size(); i++) {
                Point p1 = pontos.get(i-1);
                Point p2 = pontos.get(i);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
        
        public int getY() {
            return y;
        }
    }
    
    // Método main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JogoCarnaval().setVisible(true);
            }
        });
    }
}