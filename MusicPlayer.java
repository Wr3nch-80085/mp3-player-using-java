import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MusicPlayer extends JFrame
        implements ActionListener, ChangeListener {

    /* ------------ audio ------------ */
    private Clip clip;
    private File audioFile;

    /* ------------ UI --------------- */
    private final JButton playButton  = new JButton("Play");
    private final JButton pauseButton = new JButton("Pause");
    private final JButton stopButton  = new JButton("Stop");
    private final JSlider seekBar     = new JSlider();
    private final JLabel  trackLabel  = new JLabel();

    /* ------------ misc ------------- */
    private Timer   sliderTimer;
    private boolean sliderIsBeingDragged = false;

    public MusicPlayer(String filePath) {
        super("Music Player");
        audioFile = new File(filePath);

        /* ---------- load & fix format ---------- */
        try {
            AudioInputStream original = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat srcFormat     = original.getFormat();

            // Convert to 16‑bit little‑endian PCM (Clip‑friendly)
            AudioFormat dstFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    srcFormat.getSampleRate(),      // keep original sample rate
                    16,                             // 16‑bit
                    srcFormat.getChannels(),
                    srcFormat.getChannels() * 2,    // frame size
                    srcFormat.getSampleRate(),
                    false);                         // little‑endian

            AudioInputStream decoded =
                    AudioSystem.getAudioInputStream(dstFormat, original);

            clip = AudioSystem.getClip();
            clip.open(decoded);

            original.close();          // tidy up; decoded owns the stream now
        } catch (UnsupportedAudioFileException |
                 IOException |
                 LineUnavailableException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load audio: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        /* ---------- build UI ---------- */
        playButton.addActionListener(this);
        pauseButton.addActionListener(this);
        stopButton.addActionListener(this);

        setLayout(new BorderLayout(8, 8));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        controls.add(playButton);
        controls.add(pauseButton);
        controls.add(stopButton);

        trackLabel.setText("Loaded: " + audioFile.getName());
        trackLabel.setHorizontalAlignment(SwingConstants.CENTER);

        seekBar.setMinimum(0);
        seekBar.setMaximum(1000);
        seekBar.addChangeListener(this);

        add(trackLabel, BorderLayout.NORTH);
        add(seekBar, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);

        setSize(400, 150);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        /* ---------- keep seek bar in sync ---------- */
        sliderTimer = new Timer(200, e -> {
            if (clip.isRunning() && !sliderIsBeingDragged) {
                long pos  = clip.getMicrosecondPosition();
                long len  = clip.getMicrosecondLength();
                int val   = (int) (pos * 1000.0 / len);
                seekBar.setValue(val);
            }
        });
        sliderTimer.start();
    }

    /* ---------- button clicks ---------- */
    @Override public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == playButton) {
            if (!clip.isRunning()) clip.start();
        } else if (src == pauseButton) {
            if (clip.isRunning()) clip.stop();
        } else if (src == stopButton) {
            if (clip.isOpen()) {
                clip.stop();
                clip.setMicrosecondPosition(0);
                seekBar.setValue(0);
            }
        }
    }

    /* ---------- seek bar ---------- */
    @Override public void stateChanged(ChangeEvent e) {
        if (e.getSource() != seekBar) return;
        sliderIsBeingDragged = seekBar.getValueIsAdjusting();
        if (!sliderIsBeingDragged && clip.isOpen()) {
            long len    = clip.getMicrosecondLength();
            long newPos = (long) (seekBar.getValue() / 1000.0 * len);
            clip.setMicrosecondPosition(newPos);
        }
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new MusicPlayer("C:/Users/home/Downloads/bury the light.wav"));
    }
}
