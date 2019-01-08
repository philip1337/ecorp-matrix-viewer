package fpga;

import com.fazecast.jSerialComm.SerialPort;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Transmitter {
    /**
     * Serial port
     */
    private SerialPort port_ = null;

    /**
     * Constructor
     */
    public Transmitter() {

    }

    /**
     * Discover module
     * @return 3 = Types.READY if success.
     */
    public byte FindModule() {
        SerialPort[] ports = SerialPort.getCommPorts();

        // Module is not connected
        if (ports.length <= 0) {
            return Types.NOT_CONECTED;
        }

        // Loop trough ports
        for (SerialPort p : ports) {
            p.setComPortParameters(921600, 8, 1, SerialPort.NO_PARITY);
            p.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

            // If port is valid we stop here
            if (p.openPort() && p.getOutputStream() != null) {
                port_ = p;
                return Types.READY;
            }
        }

        // We're gucci
        return Types.NOT_READY;
    }

    /**
     * Transmit data
     * @param data buffer
     * @throws IOException if comport is failing
     */
    public void Transmit(byte[] data) throws IOException {
        assert port_ != null : "No port found";

        // Output stream
        OutputStream o = port_.getOutputStream();
        if (o == null)
            return;

        // Flush
        o.write(data);
        o.flush();
    }

    /**
     * Transmit data
     * @param data buffer
     * @throws IOException if comport is failing
     */
    public void TransmitString(String data) throws IOException {
        Transmit(data.getBytes());
    }

    /**
     * Transmit data
     * @param buffer buffer
     * @throws IOException if comport is failing
     */
    public void TransmitUartBuffer(ByteBuffer buffer) throws IOException {
        Transmit(buffer.array());
    }

    /**
     * Get image buffer
     * @param img Buffer
     * @return ByteBuffer with translated image for the rgb matrix
     */
    public ByteBuffer Image(BufferedImage img) {
        // Translate image
        int height = img.getHeight();
        int width = img.getWidth();
        int size = height * width * (Integer.SIZE / 8);

        assert width == 16;
        assert height == 16;



        // Buffer
        ByteBuffer buffer = ByteBuffer.allocate(size);

        return buffer;
    }

    /**
     * Transmit raw image pixels
     * @param img data
     * @throws IOException if comport is failing
     */
    public void TransmitImage(BufferedImage img) throws IOException {
        assert port_ != null : "No port found";

        // Output stream
        OutputStream o = port_.getOutputStream();
        if (o == null)
            return;

        // Translate image
        int height = img.getHeight();
        int width = img.getWidth();

        assert width == 16;
        assert height == 16;

        // Loop trough stuff
        for (int y = 0; y < height; y++) {
            final boolean readFromBeginning = y % 2 == 0;

            // Write first block of 8 pixels a 3 bytes
            o.write(String.format("<%02X", y * 2 + 1).getBytes());
            if (readFromBeginning) {
                for (int x = 0; x < 8; ++x) {
                    final Color c = new Color(img.getRGB(x, y));
                    o.write(String.format("%02X", c.getRed()).getBytes());
                    o.write(String.format("%02X", c.getGreen()).getBytes());
                    o.write(String.format("%02X", c.getBlue()).getBytes());
                }
            } else {
                for (int x = 16 - 1; x >= 8; --x) {
                    final Color c = new Color(img.getRGB(x, y));
                    o.write(String.format("%02X", c.getRed()).getBytes());
                    o.write(String.format("%02X", c.getGreen()).getBytes());
                    o.write(String.format("%02X", c.getBlue()).getBytes());
                }
            }
            o.write(">".getBytes());

            // Write second block of 8 pixels a 3 bytes
            o.write(String.format("<%02X", y * 2 + 2).getBytes());
            if (readFromBeginning) {
                for (int x = 8; x < 16; ++x) {
                    final Color c = new Color(img.getRGB(x, y));
                    o.write(String.format("%02X", c.getRed()).getBytes());
                    o.write(String.format("%02X", c.getGreen()).getBytes());
                    o.write(String.format("%02X", c.getBlue()).getBytes());
                }
            } else {
                for (int x = 8 - 1; x >= 0; --x) {
                    final Color c = new Color(img.getRGB(x, y));
                    o.write(String.format("%02X", c.getRed()).getBytes());
                    o.write(String.format("%02X", c.getGreen()).getBytes());
                    o.write(String.format("%02X", c.getBlue()).getBytes());
                }
            }
            o.write(">".getBytes());
        }

        o.flush();
    }
}
