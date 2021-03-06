package net.bither.factory;

import net.bither.Bither;
import net.bither.BitherSetting;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.qrcode.QRCodeEnodeUtil;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.qrcode.IReadQRCode;
import net.bither.qrcode.IScanQRCode;
import net.bither.qrcode.SelectTransportQRCodePanel;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.TransactionsUtil;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.dialogs.ProgressDialog;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MonitorAddress {

    private ArrayList<String> addresses = new ArrayList<String>();
    private ProgressDialog progressDialog;

    public void monitorAddress() {
        SelectTransportQRCodePanel selectQRCodeDialog = new SelectTransportQRCodePanel(new IScanQRCode() {
            public void handleResult(final String result, IReadQRCode readQRCode) {
                readQRCode.close();

                if (Utils.isEmpty(result) || !checkQrCodeContent(result)) {
                    new MessageDialog(LocaliserUtils.getString("scan.for.all.addresses.in.bither.cold.failed")).showMsg();

                } else {
                    progressDialog = new ProgressDialog();
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            processQrCodeContent(result);
                        }


                    };
                    thread.start();
                    progressDialog.pack();
                    progressDialog.setVisible(true);
                }
            }
        },false);
        selectQRCodeDialog.showPanel();

    }


    private boolean checkQrCodeContent(String content) {
        String[] strs = QRCodeUtil.splitString(content);
        for (String str : strs) {
            boolean checkCompressed = str.length() == 66 || ((str.length() == 67)
                    && (str.indexOf(QRCodeUtil.XRANDOM_FLAG) == 0));
            boolean checkUnCompressed = str.length() == 130 || ((str.length() == 131)
                    && (str.indexOf(QRCodeUtil.XRANDOM_FLAG) == 0));
            if (!checkCompressed && !checkUnCompressed) {
                return false;
            }
        }
        return true;
    }

    private void processQrCodeContent(String content) {
        try {
            addresses.clear();
            java.util.List<Address> wallets = QRCodeEnodeUtil.formatPublicString(content);
            for (Address address : wallets) {
                if (!AddressManager.getInstance().getAllAddresses().contains(address)) {
                    addresses.add(address.getAddress());
                }
            }
            checkAddress(wallets);
        } catch (Exception e) {
            new MessageDialog(LocaliserUtils.getString("scan.for.all.addresses.in.bither.cold.failed")).showMsg();

        }
    }


    private void checkAddress(
            final List<Address> wallets) {
        try {
            List<String> addressList = new ArrayList<String>();
            for (Address bitherAddress : wallets) {
                addressList.add(bitherAddress.getAddress());
            }
            BitherSetting.AddressType addressType = TransactionsUtil.checkAddress(addressList);
            switch (addressType) {
                case Normal:
                    KeyUtil.addAddressListByDesc(wallets);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dispose();
                            Bither.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            Bither.getCoreController().fireRecreateAllViews(true);
                            Bither.getCoreController().fireDataChangedUpdateNow();
                            if (Bither.getMainFrame() != null) {
                                Bither.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            }
                            Bither.getMainFrame().getMainFrameUi().clearScroll();

                        }
                    });

                    break;
                case SpecialAddress:
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dispose();
                            new MessageDialog(LocaliserUtils.getString("Cannot monitor address with special transactions.")).showMsg();
                        }
                    });
                    break;
                case TxTooMuch:
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dispose();
                            new MessageDialog(LocaliserUtils.getString("Cannot monitor address with large amount of transactions.")).showMsg();
                        }
                    });
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dispose();
                    new MessageDialog(LocaliserUtils.getString("network.or.connection.error")).showMsg();
                }
            });

        }
    }

}
