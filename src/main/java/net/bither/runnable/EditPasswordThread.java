/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bither.runnable;

import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.BitherjSettings;
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.preference.UserPreference;
import net.bither.utils.BackupUtil;


public class EditPasswordThread extends Thread {
    private SecureCharSequence oldPassword;
    private SecureCharSequence newPassword;
    private EditPasswordListener listener;

    public EditPasswordThread(SecureCharSequence oldPassword, SecureCharSequence newPassword,
                              EditPasswordListener listener) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.listener = listener;
    }

    @Override
    public void run() {
        final boolean result = editPassword(oldPassword, newPassword);
        oldPassword.wipe();
        newPassword.wipe();
        if (listener != null) {

            if (result) {
                listener.onSuccess();
            } else {
                listener.onFailed();
            }


        }
    }

    public static interface EditPasswordListener {
        public void onSuccess();

        public void onFailed();
    }

    public boolean editPassword(SecureCharSequence oldPassword, SecureCharSequence newPassword) {
        try {
            AddressManager.getInstance().changePassword(oldPassword, newPassword);
            if (AddressManager.getInstance().getPrivKeyAddresses().size() > 0) {
                UserPreference.getInstance().setPasswordSeed(
                        new PasswordSeed(AddressManager.getInstance().getPrivKeyAddresses().get(0)));
            } else if (AddressManager.getInstance().getTrashAddresses().size() > 0) {
                UserPreference.getInstance().setPasswordSeed(
                        new PasswordSeed(AddressManager.getInstance().getTrashAddresses().get(0)));
            }


            if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.COLD) {
                BackupUtil.backupColdKey(false);
            } else {
                BackupUtil.backupHotKey();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
