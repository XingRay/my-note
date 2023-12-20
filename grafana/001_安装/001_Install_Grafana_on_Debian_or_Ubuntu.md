# Install Grafana on Debian or Ubuntu

This topic explains how to install Grafana dependencies, install Grafana on Linux Debian or Ubuntu, and start the Grafana server on your Debian or Ubuntu system.

There are multiple ways to install Grafana: using the Grafana Labs APT repository, by downloading a `.deb` package, or by downloading a binary `.tar.gz` file. Choose only one of the methods below that best suits your needs.

> **Note:** If you install via the `.deb` package or `.tar.gz` file, then you must manually update Grafana for each new version.

## Install from APT repository

If you install from the APT repository, Grafana automatically updates when you run `apt-get update`.

| Grafana Version           | Package            | Repository                            |
| :------------------------ | :----------------- | :------------------------------------ |
| Grafana Enterprise        | grafana-enterprise | `https://apt.grafana.com stable main` |
| Grafana Enterprise (Beta) | grafana-enterprise | `https://apt.grafana.com beta main`   |
| Grafana OSS               | grafana            | `https://apt.grafana.com stable main` |
| Grafana OSS (Beta)        | grafana            | `https://apt.grafana.com beta main`   |

> **Note:** Grafana Enterprise is the recommended and default edition. It is available for free and includes all the features of the OSS edition. You can also upgrade to the [full Enterprise feature set](https://grafana.com/products/enterprise/?utm_source=grafana-install-page), which has support for [Enterprise plugins](https://grafana.com/grafana/plugins/?enterprise=1&utcm_source=grafana-install-page).

Complete the following steps to install Grafana from the APT repository:

1. To install required packages and download the Grafana repository signing key, run the following commands:

   ```bash
   sudo apt-get install -y apt-transport-https
   sudo apt-get install -y software-properties-common wget
   sudo wget -q -O /usr/share/keyrings/grafana.key https://apt.grafana.com/gpg.key
   ```

   Bash

   

2. To add a repository for stable releases, run the following command:

   ```bash
   echo "deb [signed-by=/usr/share/keyrings/grafana.key] https://apt.grafana.com stable main" | sudo tee -a /etc/apt/sources.list.d/grafana.list
   ```

   Bash

   

3. To add a repository for beta releases, run the following command:

   ```bash
   echo "deb [signed-by=/usr/share/keyrings/grafana.key] https://apt.grafana.com beta main" | sudo tee -a /etc/apt/sources.list.d/grafana.list
   ```

   Bash

   

4. Run the following command to update the list of available packages:

   ```bash
   # Updates the list of available packages
   sudo apt-get update
   ```

   Bash

   

5. To install Grafana OSS, run the following command:

   ```bash
   # Installs the latest OSS release:
   sudo apt-get install grafana
   ```

   Bash

   

6. To install Grafana Enterprise, run the following command:

   ```bash
   # Installs the latest Enterprise release:
   sudo apt-get install grafana-enterprise
   ```

   Bash

   

## Install Grafana using a deb package or as a standalone binary

If you choose not to install Grafana using APT, you can download and install Grafana using the deb package or as a standalone binary.

Complete the following steps to install Grafana using DEB or the standalone binaries:

1. Navigate to the [Grafana download page](https://grafana.com/grafana/download).

2. Select the Grafana version you want to install.

   - The most recent Grafana version is selected by default.
   - The **Version** field displays only tagged releases. If you want to install a nightly build, click **Nightly Builds** and then select a version.

3. Select an

    

   Edition

   .

   - **Enterprise:** This is the recommended version. It is functionally identical to the open source version, but includes features you can unlock with a license, if you so choose.
   - **Open Source:** This version is functionally identical to the Enterprise version, but you will need to download the Enterprise version if you want Enterprise features.

4. Depending on which system you are running, click the **Linux** or **ARM** tab on the [download page](https://grafana.com/grafana/download).

5. Copy and paste the code from the [download page](https://grafana.com/grafana/download) into your command line and run.

## Uninstall on Debian or Ubuntu

Complete any of the following steps to uninstall Grafana.

To uninstall Grafana, run the following commands in a terminal window:

1. If you configured Grafana to run with systemd, stop the systemd servivce for Grafana server:

   ```shell
   sudo systemctl stop grafana-server
   ```

   Shell

   

2. If you configured Grafana to run with init.d, stop the init.d service for Grafana server:

   ```shell
   sudo service grafana-server stop
   ```

   Shell

   

3. To uninstall Grafana OSS:

   ```shell
   sudo apt-get remove grafana
   ```

   Shell

   

4. To uninstall Grafana Enterprise:

   ```shell
   sudo apt-get remove grafana-enterprise
   ```

   Shell

   

5. Optional: To remove the Grafana repository:

   ```bash
   sudo rm -i /etc/apt/sources.list.d/grafana.list
   ```

   Bash

   

## Next steps

- [Start the Grafana server](https://grafana.com/docs/grafana/latest/setup-grafana/start-restart-grafana/)