import java.awt.*;
import java.awt.event.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    private Image birdImg;
    private Image pipeImg;
    private Image bgImg;

    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    private Timer timer;
    private Rectangle bird;
    private ArrayList<Rectangle> pipes;
    private int score;
    private boolean gameOver;
    private double velocity = 0;
    private double gravity = 0.5;

    public FlappyBird() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        loadOnlineAssets();
        initGame();

        timer = new Timer(20, this);
        timer.start();

        requestFocusInWindow(); // FIX: ensures key listener works
    }

    private void loadOnlineAssets() {
        try {
            birdImg = fetchImage("https://raw.githubusercontent.com/AristanSingh/Assets/main/flappy_bird.png");
            pipeImg = fetchImage("https://raw.githubusercontent.com/AristanSingh/Assets/main/pipe.png");
            bgImg = fetchImage("https://raw.githubusercontent.com/AristanSingh/Assets/main/day_sky.jpg");
            System.out.println("Images loaded successfully.");
        } catch (Exception e) {
            System.out.println("Failed to load images. Using fallback graphics.");
        }
    }

    private Image fetchImage(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0"); // FIXED
        return ImageIO.read(connection.getInputStream());
    }

    private void initGame() {
        bird = new Rectangle(100, HEIGHT / 2 - 25, 40, 30);
        pipes = new ArrayList<>();
        score = 0;
        velocity = 0;
        gameOver = false;

        addPipe(true);
        addPipe(true);
    }

    private void addPipe(boolean first) {
        int gap = 180;
        int width = 65;
        int height = 50 + new Random().nextInt(200);

        int x = first
                ? WIDTH + pipes.size() * 280
                : pipes.get(pipes.size() - 1).x + 320;

        pipes.add(new Rectangle(x, HEIGHT - height, width, height)); // bottom
        pipes.add(new Rectangle(x, 0, width, HEIGHT - height - gap)); // top
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g; // FIX

        // Background
        if (bgImg != null) {
            g.drawImage(bgImg, 0, 0, WIDTH, HEIGHT, null);
        } else {
            GradientPaint sky = new GradientPaint(0, 0, new Color(135, 206, 235),
                    0, HEIGHT, new Color(255, 228, 181));
            g2d.setPaint(sky);
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
        }

        // Pipes
        for (Rectangle pipe : pipes) {
            if (pipeImg != null) {
                g.drawImage(pipeImg, pipe.x, pipe.y, pipe.width, pipe.height, null);
            } else {
                g.setColor(new Color(34, 139, 34));
                g.fillRect(pipe.x, pipe.y, pipe.width, pipe.height);

                g.setColor(new Color(0, 100, 0));
                g2d.setStroke(new BasicStroke(3)); // FIX
                g2d.drawRect(pipe.x, pipe.y, pipe.width, pipe.height);
            }
        }

        // Bird
        if (birdImg != null) {
            g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);
        } else {
            g.setColor(Color.YELLOW);
            g.fillOval(bird.x + 5, bird.y + 5, bird.width - 10, bird.height - 10);

            g.setColor(Color.ORANGE);
            g.fillRect(bird.x + bird.width - 8, bird.y + 10, 8, 6);

            g.setColor(Color.BLACK);
            g.fillOval(bird.x + 12, bird.y + 8, 4, 4);
        }

        // Score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 32));

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("GAME OVER", WIDTH / 2 - 150, HEIGHT / 2);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Press SPACE to Restart", WIDTH / 2 - 150, HEIGHT / 2 + 50);

            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("Final Score: " + score, WIDTH / 2 - 120, HEIGHT / 2 + 100);
        } else {
            g.drawString("Score: " + score, 20, 40);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            velocity += gravity;
            bird.y += (int) velocity;

            for (Rectangle p : pipes) {
                p.x -= 5;

                if (p.intersects(bird)) {
                    gameOver = true;
                }
            }

            // Remove pipes safely
            for (int i = 0; i < pipes.size(); i++) {
                Rectangle p = pipes.get(i);

                if (p.x + p.width < 0) {
                    pipes.remove(i);

                    if (p.y == 0) {
                        score++;
                        if (!pipes.isEmpty()) {
                            addPipe(false);
                        }
                    }
                    i--;
                }
            }

            if (bird.y > HEIGHT - 80 || bird.y < -20) {
                gameOver = true;
            }
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) {
                initGame();
            } else {
                velocity = -11;
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird game = new FlappyBird();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        game.requestFocusInWindow(); // FIX
    }
}