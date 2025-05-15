import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MusicPlayer extends JFrame implements ActionListener {

    Clip clip;
    JButton playButton, stopButton;
    File audioFile;

    public MusicPlayer(String filePath) {
        setTitle("Music Player");
        setSize(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        audioFile = new File(filePath);

        playButton = new JButton("Play");
        stopButton = new JButton("Stop");

        playButton.addActionListener(this);
        stopButton.addActionListener(this);

        add(playButton);
        add(stopButton);
        setVisible(true);

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == playButton) {
            if (clip != null) {
                clip.setFramePosition(0); 
                clip.start();
            }
        } else if (e.getSource() == stopButton) {
            if (clip != null && clip.isRunning()) {
                clip.stop();
                clip.setFramePosition(0); 
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new MusicPlayer("C:/Users/home/Downloads/bury the light.wav")
        );
    }
}
