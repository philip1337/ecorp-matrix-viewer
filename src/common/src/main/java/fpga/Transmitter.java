package fpga;

import com.fazecast.jSerialComm.SerialPort;

import util.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Transmitter {
    /**
     * Serial port
     */
    private List<SerialPort> ports_ = null;

    // Display size
    private int width_;
    private int height_;

    /**
     * Constructor
     */
    public Transmitter(int width, int height) {
        ports_ = new ArrayList<>();
        width_ = width;
        height_ = height;
    }

    /**
     * Get width
     * @return int
     */
    public int GetWidth() {
        return width_;
    }

    /**
     * Get height
     * @return int
     */
    public int GetHeight() {
        return height_;
    }

    /**
     * Get serial ports
     * @return ports
     */
    public SerialPort[] GetModules() {
        return SerialPort.getCommPorts();
    }

    /**
     * Show modules
     */
    public void DumpModules() {
        for (SerialPort p : GetModules()) {
            System.out.printf("System: %s | port: %s | description: %s \n",
                              p.getSystemPortName(),
                              p.getDescriptivePortName(),
                              p.getPortDescription());
        }
    }

    /**
     * Discover first com serial module
     * @return 3 = Types.READY if success.
     */
    public byte FirstModule() {
        SerialPort[] ports = GetModules();

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
                ports_.add(p);
                return Types.READY;
            }
        }

        // We're gucci
        return Types.NOT_READY;
    }

    /**
     * Discover module by name
     * @return 3 = Types.READY if success.
     */
    public byte FindModules(String device) {
        SerialPort[] ports = SerialPort.getCommPorts();

        // Module is not connected
        if (ports.length <= 0) {
            return Types.NOT_CONECTED;
        }

        // Loop trough ports
        for (SerialPort p : ports) {
            if (!p.getSystemPortName().equals("COM9") && !p.getPortDescription().contains(device))
                continue;

            p.setComPortParameters(921600, 8, 1, SerialPort.NO_PARITY);
            p.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

            // If port is valid we stop here
            if (p.openPort() && p.getOutputStream() != null) {
                ports_.add(p);
            }
        }

        // We're gucci
        return ports_.size() > 0 ? Types.READY : Types.NOT_READY;
    }

    /**
     * Discover module by name
     * @return 3 = Types.READY if success.
     */
    public byte FindModule(String device) {
        SerialPort[] ports = SerialPort.getCommPorts();

        // Module is not connected
        if (ports.length <= 0) {
            return Types.NOT_CONECTED;
        }

        // Loop trough ports
        for (SerialPort p : ports) {
            // Device is not matching
            if (!p.getSystemPortName().equals(device))
                continue;

            p.setComPortParameters(921600, 8, 1, SerialPort.NO_PARITY);
            p.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

            // If port is valid we stop here
            if (p.openPort() && p.getOutputStream() != null) {
                ports_.add(p);
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
        assert ports_.size() > 0 : "No port found";

        // Loop trough ports and transmit
        for (SerialPort port : ports_) {
            // Output stream
            OutputStream o = port.getOutputStream();
            if (o == null)
                return;

            // Flush
            o.write(data);
            o.flush();
        }
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
     * Transmit raw image pixels
     * @param img data
     * @param brightness float 1 = max
     * @throws IOException if comport is failing
     */
    public void TransmitImage(BufferedImage img, float brightness) throws IOException {
        assert ports_.size() > 0 : "No port found";
        int temp = 0;

        // Loop trough ports and transmit
        for (SerialPort port : ports_) {
            // Output stream
            OutputStream o = port.getOutputStream();
            if (o == null)
                return;

            // Translate image
            int height = img.getHeight();
            int width = img.getWidth();

            // Limit on matrix size (16x16)
            assert width == width_ : "Invalid image size";
            assert height == height_ : "Invalid image size";

            // Loop trough stuff
            for (int y = 0; y < height; y++) {
                final boolean readFromBeginning = y % 2 == 0;

                // Write first block of 8 pixels a 3 bytes
                o.write(String.format("<%02X", y * 2 + 1).getBytes());
                if (readFromBeginning) {
                    for (int x = 0; x < 8; ++x) {
                        try {
                            temp = img.getRGB(x, y);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            temp = 0;
                        }
                        final Color c = new Color(temp);
                        c.SetBrightness(brightness);
                        o.write(c.ToBytes());
                    }
                } else {
                    for (int x = 16 - 1; x >= 8; --x) {
                        try {
                            temp = img.getRGB(x, y);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            temp = 0;
                        }
                        final Color c = new Color(temp);
                        c.SetBrightness(brightness);
                        o.write(c.ToBytes());
                    }
                }
                o.write(">".getBytes());

                // Write second block of 8 pixels a 3 bytes
                o.write(String.format("<%02X", y * 2 + 2).getBytes());
                if (readFromBeginning) {
                    for (int x = 8; x < width; ++x) {
                        try {
                            temp = img.getRGB(x, y);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            temp = 0;
                        }

                        final Color c = new Color(temp);
                        c.SetBrightness(brightness);
                        o.write(c.ToBytes());
                    }
                } else {
                    for (int x = 8 - 1; x >= 0; --x) {
                        try {
                            temp = img.getRGB(x, y);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            temp = 0;
                        }

                        final Color c = new Color(temp);
                        c.SetBrightness(brightness);
                        o.write(c.ToBytes());
                    }
                }
                o.write(">".getBytes());
            }

            o.flush();
        }
    }

    /**
     * Transmit color pixels
     * @param color color
     * @param brightness float 1 = max
     * @throws IOException if comport is failing
     */
    public void TransmitColor(Color color, float brightness) throws IOException {
        assert ports_.size() > 0 : "No port found";

        // Loop trough ports and transmit
        for (SerialPort port : ports_) {
            // Output stream
            OutputStream o = port.getOutputStream();
            if (o == null)
                return;

            // Translate image
            int height = 16;
            int width = 16;

            // Loop trough stuff
            for (int y = 0; y < height; y++) {
                final boolean readFromBeginning = y % 2 == 0;

                // Write first block of 8 pixels a 3 bytes
                o.write(String.format("<%02X", y * 2 + 1).getBytes());
                if (readFromBeginning) {
                    for (int x = 0; x < 8; ++x) {
                        color.SetBrightness(brightness);
                        o.write(color.ToBytes());
                    }
                } else {
                    for (int x = 16 - 1; x >= 8; --x) {
                        color.SetBrightness(brightness);
                        o.write(color.ToBytes());
                    }
                }
                o.write(">".getBytes());

                // Write second block of 8 pixels a 3 bytes
                o.write(String.format("<%02X", y * 2 + 2).getBytes());
                if (readFromBeginning) {
                    for (int x = 8; x < width; ++x) {
                        color.SetBrightness(brightness);
                        o.write(color.ToBytes());
                    }
                } else {
                    for (int x = 8 - 1; x >= 0; --x) {
                        color.SetBrightness(brightness);
                        o.write(color.ToBytes());
                    }
                }
                o.write(">".getBytes());
            }

            o.flush();
        }
    }
}
