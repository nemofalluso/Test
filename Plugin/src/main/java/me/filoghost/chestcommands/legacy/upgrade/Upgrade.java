/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.filoghost.chestcommands.legacy.upgrade;

import me.filoghost.chestcommands.config.framework.exception.ConfigLoadException;
import me.filoghost.chestcommands.config.framework.exception.ConfigSaveException;
import me.filoghost.chestcommands.logging.ErrorMessages;
import me.filoghost.chestcommands.util.Preconditions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Upgrade {

	private boolean modified;
	private boolean hasRun;

	protected void setModified() {
		this.modified = true;
	}


	public boolean backupAndUpgradeIfNecessary() throws UpgradeException {
		Preconditions.checkState(!hasRun, "Upgrade can only be run once");
		hasRun = true;

		try {
			computeChanges();
		} catch (ConfigLoadException e) {
			throw new UpgradeException(ErrorMessages.Upgrade.loadError(getOriginalFile()), e);
		}

		if (modified) {
			try {
				createBackupFile(getOriginalFile());
			} catch (IOException e) {
				throw new UpgradeException(ErrorMessages.Upgrade.backupError(getOriginalFile()), e);
			}

			try {
				saveChanges();
			} catch (ConfigSaveException e) {
				throw new UpgradeException(ErrorMessages.Upgrade.saveError(getUpgradedFile()), e);
			}
		}

		return modified;
	}

	private void createBackupFile(Path path) throws IOException {
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd-HH.mm"));
		String backupName = path.getFileName() + "_" + date + ".backup";

		Files.copy(path, path.resolveSibling(backupName), StandardCopyOption.REPLACE_EXISTING);
	}

	public abstract Path getOriginalFile();

	public abstract Path getUpgradedFile();

	protected abstract void computeChanges() throws ConfigLoadException;

	protected abstract void saveChanges() throws ConfigSaveException;

}
