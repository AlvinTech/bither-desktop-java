package net.bither.qrcode;

import net.bither.Bither;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.froms.WizardPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class SelectQRCodePanel extends WizardPanel implements IReadQRCode {

    public interface IFileChooser {
        void selectFile(File file);
    }

    protected IScanQRCode scanQRCode;
    private JButton btnFromFile;
    private JButton btnFromCamera;
    private JLabel labMsg;


    public SelectQRCodePanel(IScanQRCode scanQRCode) {
        this(scanQRCode, false);

    }

    public SelectQRCodePanel(IScanQRCode scanQRCode, boolean isPopover) {
        super(MessageKey.QR_CODE, AwesomeIcon.QRCODE, isPopover);
        this.scanQRCode = scanQRCode;

    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "[][][][]80[]20[]" // Row constraints
        ));
        btnFromFile = Buttons.newFromFileButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fromFile();
            }
        });
        btnFromCamera = Buttons.newFromCameraButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fromScan();
            }
        });
        labMsg = Labels.newMessage();
        panel.add(btnFromFile, "align center,cell 0 2 ,wrap");
        panel.add(btnFromCamera, "align center,cell 0 3,wrap");
        panel.add(labMsg, "align center,cell 0 5,wrap");
        clearMessage();

    }

    protected void startFileChooser(IFileChooser ifileChooser) {
        // Create a file save dialog.
        JFileChooser.setDefaultLocale(LocaliserUtils.getLocale());
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setLocale(LocaliserUtils.getLocale());
        fileChooser.setDialogTitle(LocaliserUtils.getString("showExportPrivateKeysPanel.filename.text.2"));

        fileChooser.applyComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils.getLocale()));
        fileChooser.addChoosableFileFilter(new FileFilter() {

            private final String[] okFileExtensions =
                    new String[]{"jpg", "png", "gif", "bmp"};

            @Override
            public boolean accept(File f) {
                for (String extension : okFileExtensions) {
                    if (f.getName().toLowerCase().endsWith(extension)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String getDescription() {
                return null;
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        int returnVal = fileChooser.showSaveDialog(Bither.getMainFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                ifileChooser.selectFile(file);
            }
        }
    }

    protected void fromFile() {
        clearMessage();
        startFileChooser(new IFileChooser() {
            @Override
            public void selectFile(final File file) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (file != null) {
                            String str = QRCodeEncoderDecoder.decode(file);
                            if (Utils.isEmpty(str)) {
                                setMsg(LocaliserUtils.getString("no_format_qr_code"));
                            } else {
                                scanQRCode.handleResult(str, SelectQRCodePanel.this);
                            }

                        }
                    }
                });


            }
        });

    }

    protected void clearMessage() {
        labMsg.setText("");

    }

    protected void setMsg(String msg) {
        labMsg.setText(msg);
    }

    protected void fromScan() {
        ScanQRCodeDialog scanQRCodeDialog = new ScanQRCodeDialog(this.scanQRCode);
        scanQRCodeDialog.pack();
        scanQRCodeDialog.setVisible(true);
        onCancel();

    }


    @Override
    public void close() {
        onCancel();
    }

    @Override
    public void reTry(String msg) {
        labMsg.setText(msg);

    }

}
